package com.guilh.mca_vampirism_compat.monster;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaGuardDetectionService;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerClassifier;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;

import java.util.HashMap;
import java.util.Map;

public final class McaGuardMonsterDefenseService {
    private static final Map<String, Long> GUARD_DEFENSE_LOG_TIMES = new HashMap<>();

    private McaGuardMonsterDefenseService() {
    }

    public static boolean canSpecialGuardDefend(VillagerEntityMCA villager) {
        return McaVampirismCompatConfig.specialMcaGuardsDefendAgainstHostileMobs()
                && McaGuardDetectionService.isMcaGuard(villager)
                && McaSpecialVillagerClassifier.isCombatCapableSpecial(villager);
    }

    public static boolean canSpecialGuardTarget(VillagerEntityMCA villager, Entity target) {
        return canSpecialGuardDefend(villager) && isValidGuardHostileTarget(target);
    }

    public static boolean allowsMonsterTargetingSpecialGuard(Entity monster, Entity target) {
        return target instanceof VillagerEntityMCA villager
                && canSpecialGuardDefend(villager)
                && isValidGuardHostileTarget(monster);
    }

    public static boolean shouldSuppressPanicWithoutClearingDefenseTargets(VillagerEntityMCA villager) {
        return canSpecialGuardDefend(villager)
                && McaVampirismCompatConfig.specialMcaGuardsStillSuppressPanic();
    }

    public static boolean isValidGuardHostileTarget(Entity target) {
        if (!(target instanceof LivingEntity livingTarget) || !livingTarget.isAlive()) {
            return false;
        }
        if (target instanceof Creeper && !McaVampirismCompatConfig.specialMcaGuardsAttackCreepers()) {
            return false;
        }
        return McaSpecialVillagerMonsterIgnoreService.isHostileMonster(target);
    }

    public static int targetPriority() {
        return 50;
    }

    public static String targetReason() {
        return "GUARD_HOSTILE_MONSTER";
    }

    public static void logAllowed(VillagerEntityMCA villager, Entity target, String action) {
        if (villager == null || target == null || !McaVampirismCompatConfig.enableDebugLogs()) {
            return;
        }
        long gameTime = villager.level().getGameTime();
        String key = action + ":" + villager.getUUID() + ":" + target.getUUID();
        Long previous = GUARD_DEFENSE_LOG_TIMES.get(key);
        if (previous != null && gameTime - previous < 100L) {
            return;
        }
        GUARD_DEFENSE_LOG_TIMES.put(key, gameTime);
        if (GUARD_DEFENSE_LOG_TIMES.size() > 256) {
            GUARD_DEFENSE_LOG_TIMES.clear();
        }
        McaVampirismCompat.debugLog(
                "[GUARD-DEFENSE] {} villager={} target={}",
                action,
                villager.getUUID(),
                target.getUUID()
        );
    }
}
