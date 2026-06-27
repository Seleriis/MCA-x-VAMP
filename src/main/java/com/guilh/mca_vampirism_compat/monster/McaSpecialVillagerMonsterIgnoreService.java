package com.guilh.mca_vampirism_compat.monster;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerClassifier;
import com.guilh.mca_vampirism_compat.service.McaVampireFactionService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import de.teamlapen.vampirism.core.ModEffects;
import forge.net.mca.util.network.datasync.CDataParameter;
import forge.net.mca.util.network.datasync.CTrackedEntity;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class McaSpecialVillagerMonsterIgnoreService {
    private static final double FEAR_REACTION_MONSTER_RANGE = 12.0D;
    private static final Set<String> LOGGED_UNREGISTERED_MEMORIES = new HashSet<>();
    private static final Set<String> LOGGED_CLEARED_MEMORIES = new HashSet<>();
    private static final Map<String, Long> MONSTER_IGNORE_LOG_TIMES = new HashMap<>();
    private static Field villagerBrainEntityField;
    private static boolean villagerBrainEntityFieldResolved;
    private static Field villagerBrainPanickingField;
    private static boolean villagerBrainPanickingFieldResolved;

    private McaSpecialVillagerMonsterIgnoreService() {
    }

    public static boolean isMcaVampireVillager(Entity entity) {
        return entity instanceof VillagerEntityMCA villager && McaVampireStateService.isVampire(villager);
    }

    public static boolean isSpecialNonPanickingVillager(Entity entity) {
        return specialVillagerType(entity) != SpecialVillagerType.NONE;
    }

    public static String specialVillagerTypeName(Entity entity) {
        return specialVillagerType(entity).name();
    }

    public static boolean shouldMonsterIgnore(Entity monster, Entity target) {
        if (!isHostileMonster(monster)) {
            return false;
        }
        if (McaGuardMonsterDefenseService.allowsMonsterTargetingSpecialGuard(monster, target)) {
            if (target instanceof VillagerEntityMCA villager) {
                McaGuardMonsterDefenseService.logAllowed(villager, monster, "allow monster target");
            }
            return false;
        }
        if (McaVampirismCompatConfig.monstersIgnoreSpecialMcaVillagers()
                && isSpecialNonPanickingVillager(target)) {
            return true;
        }
        return McaVampirismCompatConfig.monstersIgnoreMcaVampireVillagers()
                && isMcaVampireVillager(target);
    }

    public static boolean shouldSpecialVillagerIgnoreMonster(Entity specialVillager, Entity target) {
        if (specialVillager instanceof VillagerEntityMCA villager
                && McaGuardMonsterDefenseService.canSpecialGuardTarget(villager, target)) {
            McaGuardMonsterDefenseService.logAllowed(villager, target, "allow guard target");
            return false;
        }
        return McaVampirismCompatConfig.specialMcaVillagersIgnoreCommonMonsters()
                && isSpecialNonPanickingVillager(specialVillager)
                && isHostileMonster(target);
    }

    public static boolean isHostileMonster(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        if (McaVampireFactionService.isHunter(entity)
                || McaVampireFactionService.isTransformedWerewolf(entity)
                || McaVampireFactionService.isVampire(entity)
                || isSpecialNonPanickingVillager(entity)) {
            return false;
        }
        return entity instanceof Monster || entity instanceof Enemy;
    }

    public static boolean clearMonsterTargetIfNeeded(Mob mob) {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return false;
        }
        if (shouldMonsterIgnore(mob, target)) {
            mob.setTarget(null);
            mob.getNavigation().stop();
            if (shouldLogMonsterIgnore(mob, target, "target_cleared")) {
                McaVampirismCompat.debugLog(
                        "[MONSTER-IGNORE] target cleared mob={} target={} villagerType={}",
                        mob.getType(),
                        target == null ? null : target.getUUID(),
                        specialVillagerTypeName(target)
                );
            }
            return true;
        }
        if (!shouldSpecialVillagerIgnoreMonster(mob, target)) {
            return false;
        }
        mob.setTarget(null);
        if (shouldLogMonsterIgnore(mob, target, "special_target_cleared")) {
            McaVampirismCompat.debugLog(
                    "[MONSTER-IGNORE] special villager target cleared villager={} target={} villagerType={}",
                    mob.getUUID(),
                    target == null ? null : target.getType(),
                    specialVillagerTypeName(mob)
            );
        }
        return true;
    }

    public static boolean shouldCancelMonsterAttack(Entity attacker, Entity target) {
        if (!shouldMonsterIgnore(attacker, target)) {
            return false;
        }
        return (McaVampirismCompatConfig.cancelMonsterDamageToSpecialMcaVillagers()
                && isSpecialNonPanickingVillager(target))
                || (McaVampirismCompatConfig.cancelMonsterDamageToMcaVampireVillagers()
                && isMcaVampireVillager(target));
    }

    public static boolean suppressSpecialVillagerPanicIfNeeded(VillagerEntityMCA villager) {
        if (!shouldSuppressPanicFor(villager)) {
            return false;
        }
        if (McaGuardMonsterDefenseService.shouldSuppressPanicWithoutClearingDefenseTargets(villager)) {
            return suppressSpecialGuardPanicWithoutClearingDefenseTargets(villager);
        }
        Brain<?> brain = villager.getBrain();
        boolean cleared = false;
        cleared |= eraseLivingMemoryIfCommonMonster(brain, MemoryModuleType.NEAREST_HOSTILE);
        cleared |= eraseLivingMemoryIfCommonMonster(brain, MemoryModuleType.AVOID_TARGET);
        cleared |= eraseLivingMemoryIfCommonMonster(brain, MemoryModuleType.HURT_BY_ENTITY);
        cleared |= eraseLivingMemoryIfCommonMonster(brain, MemoryModuleType.ATTACK_TARGET);
        cleared |= eraseLivingMemoryIfCommonMonster(brain, MemoryModuleType.NEAREST_ATTACKABLE);
        cleared |= eraseDamageSourceMemoryIfCommonMonster(brain);
        if (McaSpecialVillagerClassifier.shouldSuppressMonsterPanic(villager)) {
            cleared |= eraseMemoryIfPresent(brain, MemoryModuleType.AVOID_TARGET);
            cleared |= eraseMemoryIfPresent(brain, MemoryModuleType.NEAREST_HOSTILE);
        }

        if (cleared) {
            if (villager.tickCount % 20 == 0) {
                McaVampirismCompat.verboseAiLog(
                        "[PANIC] panic suppressed villager={} villagerType={} reason=SPECIAL_VILLAGER",
                        villager.getUUID(),
                        specialVillagerTypeName(villager)
                );
            }
        }
        return cleared;
    }

    public static boolean shouldSuppressPanicFlag(Entity entity) {
        return entity instanceof VillagerEntityMCA villager && shouldSuppressPanicFor(villager);
    }

    public static boolean shouldLogMonsterIgnore(Entity actor, Entity target, String action) {
        if (actor == null) {
            return false;
        }
        long gameTime = actor.level().getGameTime();
        String targetId = target == null ? "null" : target.getUUID().toString();
        String key = action + ":" + actor.getUUID() + ":" + targetId;
        Long previous = MONSTER_IGNORE_LOG_TIMES.get(key);
        if (previous != null && gameTime - previous < 100L) {
            return false;
        }
        MONSTER_IGNORE_LOG_TIMES.put(key, gameTime);
        if (MONSTER_IGNORE_LOG_TIMES.size() > 512) {
            MONSTER_IGNORE_LOG_TIMES.clear();
        }
        return true;
    }

    public static boolean shouldSuppressPanicFlagFromBrain(Object brain) {
        Entity entity = getVillagerBrainEntity(brain);
        return shouldSuppressPanicFlag(entity);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void forceTrackedPanicFalseFromBrain(Object brain) {
        Entity entity = getVillagerBrainEntity(brain);
        if (!shouldSuppressPanicFlag(entity) || !(entity instanceof CTrackedEntity tracked)) {
            return;
        }
        CDataParameter panicking = getVillagerBrainPanickingParameter(brain);
        if (panicking != null) {
            tracked.setTrackedValue(panicking, Boolean.FALSE);
        }
    }

    public static boolean shouldSuppressFearDialogue(VillagerEntityMCA villager) {
        return (McaVampirismCompatConfig.suppressSpecialMcaVillagerFearDialogue()
                && isSpecialNonPanickingVillager(villager))
                || (McaVampirismCompatConfig.suppressMcaVampireVillagerFearDialogue()
                && isMcaVampireVillager(villager)
                && (hasSuppressibleMonsterMemory(villager) || hasNearbyCommonMonster(villager)));
    }

    public static boolean hasSuppressibleMonsterMemory(VillagerEntityMCA villager) {
        if (!shouldSuppressPanicFor(villager)) {
            return false;
        }
        Brain<?> brain = villager.getBrain();
        return livingMemoryIsCommonMonster(brain, MemoryModuleType.NEAREST_HOSTILE)
                || livingMemoryIsCommonMonster(brain, MemoryModuleType.AVOID_TARGET)
                || livingMemoryIsCommonMonster(brain, MemoryModuleType.HURT_BY_ENTITY)
                || livingMemoryIsCommonMonster(brain, MemoryModuleType.ATTACK_TARGET)
                || livingMemoryIsCommonMonster(brain, MemoryModuleType.NEAREST_ATTACKABLE)
                || damageSourceMemoryIsCommonMonster(brain);
    }

    private static boolean shouldSuppressPanicFor(VillagerEntityMCA villager) {
        if (McaGuardMonsterDefenseService.canSpecialGuardDefend(villager)) {
            return McaVampirismCompatConfig.specialMcaGuardsStillSuppressPanic();
        }
        return (McaVampirismCompatConfig.suppressSpecialMcaVillagerMonsterPanic()
                && isSpecialNonPanickingVillager(villager))
                || (McaVampirismCompatConfig.suppressMcaVampireVillagerZombiePanic()
                && isMcaVampireVillager(villager));
    }

    private static boolean suppressSpecialGuardPanicWithoutClearingDefenseTargets(VillagerEntityMCA villager) {
        Brain<?> brain = villager.getBrain();
        boolean cleared = eraseMemoryIfPresent(brain, MemoryModuleType.AVOID_TARGET);
        if (cleared && villager.tickCount % 20 == 0) {
            McaVampirismCompat.verboseAiLog(
                    "[GUARD-DEFENSE] blocked panic but allowed defense villager={}",
                    villager.getUUID()
            );
        }
        return cleared;
    }

    private static boolean hasNearbyCommonMonster(VillagerEntityMCA villager) {
        return !villager.level()
                .getEntitiesOfClass(
                        Mob.class,
                        villager.getBoundingBox().inflate(FEAR_REACTION_MONSTER_RANGE),
                        McaSpecialVillagerMonsterIgnoreService::isHostileMonster
                )
                .isEmpty();
    }

    private static boolean eraseLivingMemoryIfCommonMonster(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memoryType) {
        if (!livingMemoryIsCommonMonster(brain, memoryType)) {
            return false;
        }
        eraseMemorySafe(brain, memoryType);
        logClearedMemory(memoryType);
        return true;
    }

    private static boolean livingMemoryIsCommonMonster(Brain<?> brain, MemoryModuleType<? extends LivingEntity> memoryType) {
        return getMemorySafe(brain, memoryType)
                .map(McaSpecialVillagerMonsterIgnoreService::isHostileMonster)
                .orElse(false);
    }

    private static boolean eraseDamageSourceMemoryIfCommonMonster(Brain<?> brain) {
        if (!damageSourceMemoryIsCommonMonster(brain)) {
            return false;
        }
        eraseMemorySafe(brain, MemoryModuleType.HURT_BY);
        logClearedMemory(MemoryModuleType.HURT_BY);
        return true;
    }

    private static <T> boolean eraseMemoryIfPresent(Brain<?> brain, MemoryModuleType<T> type) {
        if (!hasRegisteredMemory(brain, type)) {
            return false;
        }
        Optional<T> memory = getMemorySafe(brain, type);
        if (memory.isEmpty()) {
            return false;
        }
        eraseMemorySafe(brain, type);
        logClearedMemory(type);
        return true;
    }

    private static boolean damageSourceMemoryIsCommonMonster(Brain<?> brain) {
        return getMemorySafe(brain, MemoryModuleType.HURT_BY)
                .map(DamageSource::getEntity)
                .map(McaSpecialVillagerMonsterIgnoreService::isHostileMonster)
                .orElse(false);
    }

    private static boolean hasRegisteredMemory(Brain<?> brain, MemoryModuleType<?> type) {
        try {
            brain.getMemory(type);
            return true;
        } catch (IllegalStateException exception) {
            logUnregisteredMemory(type);
            return false;
        }
    }

    private static <T> Optional<T> getMemorySafe(Brain<?> brain, MemoryModuleType<T> type) {
        try {
            return brain.getMemory(type);
        } catch (IllegalStateException exception) {
            logUnregisteredMemory(type);
            return Optional.empty();
        }
    }

    private static void eraseMemorySafe(Brain<?> brain, MemoryModuleType<?> type) {
        if (!hasRegisteredMemory(brain, type)) {
            return;
        }
        try {
            brain.eraseMemory(type);
        } catch (IllegalStateException exception) {
            logUnregisteredMemory(type);
        }
    }

    private static void logUnregisteredMemory(MemoryModuleType<?> type) {
        String key = memoryId(type);
        if (LOGGED_UNREGISTERED_MEMORIES.add(key)) {
            McaVampirismCompat.debugLog(
                    "[MONSTER-IGNORE] memory skipped unregistered type={}",
                    key
            );
        }
    }

    private static void logClearedMemory(MemoryModuleType<?> type) {
        String key = memoryId(type);
        if (LOGGED_CLEARED_MEMORIES.add(key)) {
            McaVampirismCompat.debugLog(
                    "[MONSTER-IGNORE] memory cleared type={} reason=COMMON_MONSTER",
                    key
            );
        }
    }

    private static String memoryId(MemoryModuleType<?> type) {
        ResourceLocation key = BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(type);
        return key == null ? String.valueOf(type) : key.toString();
    }

    private static SpecialVillagerType specialVillagerType(Entity entity) {
        if (!(entity instanceof VillagerEntityMCA villager)) {
            return SpecialVillagerType.NONE;
        }
        return switch (McaSpecialVillagerClassifier.classify(villager)) {
            case VAMPIRE -> SpecialVillagerType.MCA_VAMPIRE;
            case HUNTER -> SpecialVillagerType.MCA_HUNTER;
            case WEREWOLF -> SpecialVillagerType.MCA_WEREWOLF;
            case DEFENSIVE -> SpecialVillagerType.MCA_DEFENSIVE;
            case CURING_VAMPIRE -> SpecialVillagerType.CURING_VAMPIRE;
            case CURING_WEREWOLF -> SpecialVillagerType.CURING_WEREWOLF;
            case HUMAN_INFECTED -> McaVampirismCompatConfig.infectedMcaVillagersCountAsSpecialForMonsterIgnore()
                    ? SpecialVillagerType.MCA_INFECTED
                    : SpecialVillagerType.NONE;
            case HUMAN_NORMAL, CHILD, UNKNOWN -> SpecialVillagerType.NONE;
        };
    }

    private static Entity getVillagerBrainEntity(Object brain) {
        if (brain == null) {
            return null;
        }
        Field field = getVillagerBrainEntityField(brain.getClass());
        if (field == null) {
            return null;
        }
        try {
            Object value = field.get(brain);
            return value instanceof Entity entity ? entity : null;
        } catch (IllegalAccessException exception) {
            McaVampirismCompat.debugLog(
                    "[MONSTER-IGNORE] could not read MCA brain entity error={}",
                    exception.toString()
            );
            return null;
        }
    }

    private static Field getVillagerBrainEntityField(Class<?> type) {
        if (villagerBrainEntityFieldResolved) {
            return villagerBrainEntityField;
        }
        villagerBrainEntityFieldResolved = true;
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField("entity");
                field.setAccessible(true);
                villagerBrainEntityField = field;
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        McaVampirismCompat.debugLog("[MONSTER-IGNORE] MCA VillagerBrain entity field not found");
        return null;
    }

    private static CDataParameter<Boolean> getVillagerBrainPanickingParameter(Object brain) {
        if (brain == null) {
            return null;
        }
        Field field = getVillagerBrainPanickingField(brain.getClass());
        if (field == null) {
            return null;
        }
        try {
            Object value = field.get(null);
            return value instanceof CDataParameter<?> parameter ? (CDataParameter<Boolean>) parameter : null;
        } catch (IllegalAccessException exception) {
            McaVampirismCompat.debugLog(
                    "[MONSTER-IGNORE] could not read MCA panic parameter error={}",
                    exception.toString()
            );
            return null;
        }
    }

    private static Field getVillagerBrainPanickingField(Class<?> type) {
        if (villagerBrainPanickingFieldResolved) {
            return villagerBrainPanickingField;
        }
        villagerBrainPanickingFieldResolved = true;
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField("PANICKING");
                field.setAccessible(true);
                villagerBrainPanickingField = field;
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        McaVampirismCompat.debugLog("[MONSTER-IGNORE] MCA VillagerBrain PANICKING field not found");
        return null;
    }

    private enum SpecialVillagerType {
        NONE,
        MCA_VAMPIRE,
        MCA_HUNTER,
        MCA_WEREWOLF,
        MCA_DEFENSIVE,
        CURING_VAMPIRE,
        CURING_WEREWOLF,
        MCA_INFECTED
    }
}
