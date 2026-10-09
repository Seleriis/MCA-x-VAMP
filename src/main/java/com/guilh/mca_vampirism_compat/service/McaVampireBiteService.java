package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.util.McaVampireTargetUtil;
import de.teamlapen.vampirism.core.ModEffects;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import forge.net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class McaVampireBiteService {
    private static final double MAX_BITE_DISTANCE_SQR = 9.0D;

    private McaVampireBiteService() {
    }

    public static boolean canBite(PathfinderMob vampire, LivingEntity target) {
        if (vampire.level().isClientSide || !McaVampireStateService.isVampire(vampire)) {
            return false;
        }
        if (McaVampireCureService.isCuring(vampire)) {
            return false;
        }
        if (!McaVampireTargetUtil.isInfectionBiteTarget(vampire, target)) {
            return false;
        }
        if (vampire.distanceToSqr(target) > MAX_BITE_DISTANCE_SQR) {
            return false;
        }
        return ModCapabilities.get(vampire)
                .map(state -> state.canBite(vampire.level().getGameTime(), McaVampirismCompatConfig.biteCooldownTicks()))
                .orElse(false);
    }

    public static boolean isNonLethalInfectionBite(PathfinderMob vampire, LivingEntity target) {
        return McaVampirismCompatConfig.vampireVillagersUseNonLethalBiteOnHumans()
                && McaVampirismCompatConfig.nonLethalBiteAgainstVillagers()
                && McaVampireTargetUtil.isInfectionBiteTarget(vampire, target);
    }

    public static boolean applyInfectionBite(PathfinderMob vampire, LivingEntity target) {
        if (!canBite(vampire, target)) {
            return false;
        }
        return ModCapabilities.get(vampire).map(state -> {
            long gameTime = vampire.level().getGameTime();
            state.markBite(gameTime);
            if (McaVampireStateService.isMcaVillager(target)) {
                ModCapabilities.get(target)
                        .ifPresent(targetState -> targetState.markBittenByMcaVampire(gameTime));
            }
            applySanguinareInfection(target, vampire);
            McaVampirismCompat.debugLog("[BITE] MCA vampire bite attacker={} target={}", vampire.getUUID(), target.getUUID());
            return true;
        }).orElse(false);
    }

    public static boolean applyHunterBloodBite(PathfinderMob vampire, LivingEntity target) {
        if (McaRelationshipCombatPolicy.blocksAutomaticAttack(vampire,target)) return false;
        if (vampire.level().isClientSide
                || target == null
                || !McaVampireStateService.isVampire(vampire)
                || McaVampireCureService.isCuring(vampire)
                || !McaHunterBloodService.isPoisonousBloodTarget(target)
                || vampire.distanceToSqr(target) > MAX_BITE_DISTANCE_SQR) {
            return false;
        }
        return ModCapabilities.get(vampire).map(state -> {
            long gameTime = vampire.level().getGameTime();
            if (!state.canBite(gameTime, McaVampirismCompatConfig.biteCooldownTicks())) {
                return false;
            }
            state.markBite(gameTime);
            McaHunterBloodService.blockHunterVampireInfection(vampire, target);
            McaVampirismCompat.debugLog(
                    "[HUNTER-BLOOD] MCA vampire poisonous bite attacker={} target={}",
                    vampire.getUUID(),
                    target.getUUID()
            );
            return true;
        }).orElse(false);
    }

    public static boolean applySanguinareInfection(LivingEntity target, LivingEntity source) {
        return McaVampireInfectionService.tryApplyConfiguredSanguinare(
                source,
                target,
                McaVampireInfectionService.sourceTypeFor(source)
        );
    }

    public static boolean wasRecentlyBitten(LivingEntity target) {
        if (!McaVampireStateService.isMcaVillager(target)) {
            return false;
        }
        long gameTime = target.level().getGameTime();
        return ModCapabilities.get(target)
                .map(state -> state.wasRecentlyBitten(gameTime, McaVampirismCompatConfig.recentlyBittenTargetCooldownTicks()))
                .orElse(false);
    }

    public static void recordPlayerConversionBite(Player player, VillagerEntityMCA villager) {
        ModCapabilities.get(villager)
                .ifPresent(state -> state.markVampirePlayerConversionBite(player.getUUID(), villager.level().getGameTime()));
        McaVampirismCompat.debugLog("[BITE] conversion bite recorded villager={} player={}", villager.getUUID(), player.getUUID());
    }

    public static boolean canMcaVillagerReceiveInfection(LivingEntity target) {
        if (!McaVampireStateService.isMcaVillager(target) || !McaVampirismCompatConfig.allowMcaVillagerInfection()) {
            return false;
        }
        if (McaHunterBloodService.isHunterVampirismImmune(target)) {
            return false;
        }
        if (McaVampirismCompatConfig.werewolfVillagersImmuneToVampirism()
                && McaWerewolfStateService.isWerewolf(target)) {
            return false;
        }
        return McaVampirismCompatConfig.allowChildInfection()
                || !(target instanceof VillagerEntityMCA villager)
                || villager.getAgeState() == AgeState.ADULT;
    }
}
