package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.ai.McaSpecialMeleeAttackGoal;
import com.guilh.mca_vampirism_compat.attribute.McaSpecialVillagerAttributeService;
import com.guilh.mca_vampirism_compat.ai.McaHunterAi;
import com.guilh.mca_vampirism_compat.ai.McaVampireAi;
import com.guilh.mca_vampirism_compat.ai.McaWerewolfAi;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.monster.McaGuardMonsterDefenseService;
import com.guilh.mca_vampirism_compat.monster.McaSpecialVillagerMonsterIgnoreService;
import com.guilh.mca_vampirism_compat.service.faction.McaFactionCombatRules;
import com.guilh.mca_vampirism_compat.service.faction.hunter.McaHunterCombatRules;
import com.guilh.mca_vampirism_compat.service.faction.vampire.McaVampireCombatRules;
import com.guilh.mca_vampirism_compat.service.faction.werewolf.McaWerewolfCombatRules;
import com.guilh.mca_vampirism_compat.util.McaHunterTargetUtil;
import com.guilh.mca_vampirism_compat.util.McaVampireTargetUtil;
import com.guilh.mca_vampirism_compat.util.McaWerewolfTargetUtil;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;

public final class McaSpecialVillagerCombatService {
    private static final McaFactionCombatRules VAMPIRE_RULES = new McaVampireCombatRules();
    private static final McaFactionCombatRules HUNTER_RULES = new McaHunterCombatRules();
    private static final McaFactionCombatRules WEREWOLF_RULES = new McaWerewolfCombatRules();

    private McaSpecialVillagerCombatService() {
    }

    public static boolean registerGoalsIfNeeded(LivingEntity entity) {
        if (!(entity instanceof VillagerEntityMCA villager)
                || villager.level().isClientSide
                || !McaSpecialVillagerClassifier.isCombatCapableSpecial(villager)) {
            return false;
        }
        McaSpecialVillagerType type = McaSpecialVillagerClassifier.classify(villager);
        boolean added = switch (type) {
            case VAMPIRE -> McaVampireAi.registerGoalsIfNeeded(villager);
            case HUNTER -> McaHunterAi.registerGoalsIfNeeded(villager);
            case WEREWOLF -> McaWerewolfAi.registerGoalsIfNeeded(villager);
            case DEFENSIVE -> registerDefensiveGoalsIfNeeded(villager);
            case HUMAN_NORMAL, HUMAN_INFECTED, CURING_VAMPIRE, CURING_WEREWOLF, CHILD, UNKNOWN -> false;
        };
        if (!added) {
            return false;
        }
        McaVampirismCompat.debugLog(
                "[SPECIAL-AI] goals registered villager={} villagerType={}",
                villager.getUUID(),
                type
        );
        return true;
    }

    public static void clearInvalidTarget(Mob mob) {
        if (!(mob instanceof VillagerEntityMCA villager)) {
            return;
        }
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return;
        }
        McaSpecialVillagerType type = McaSpecialVillagerClassifier.classify(villager);
        String dropReason = targetDropReason(villager, target);
        if (dropReason == null && isValidTargetFor(type, villager, target)) {
            return;
        }
        dropTarget(villager, dropReason == null ? "INVALID_OR_CURING" : dropReason);
    }

    public static void processCombatTick(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        McaSpecialVillagerType type = McaSpecialVillagerClassifier.classify(villager);
        if (McaVampireCureService.isCuring(villager)) {
            dropTarget(villager, "CURING");
            return;
        }
        if (!McaSpecialVillagerClassifier.isCombatCapableSpecial(villager)) {
            if (type == McaSpecialVillagerType.CURING_VAMPIRE || type == McaSpecialVillagerType.CURING_WEREWOLF) {
                dropTarget(villager, "CURING");
            }
            return;
        }
        McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
        registerGoalsIfNeeded(villager);
        clearInvalidTarget(villager);
        if (McaCommandRespectService.shouldSuppressAutonomousTargeting(villager)) {
            LivingEntity commandedTarget = villager.getTarget();
            if (commandedTarget != null) {
                String dropReason = targetDropReason(villager, commandedTarget);
                if (dropReason != null) {
                    dropTarget(villager, dropReason);
                }
            }
            McaCommandRespectService.logSuppressedAi(villager, "target scan");
            return;
        }
        LivingEntity currentTarget = villager.getTarget();
        if (currentTarget != null) {
            String dropReason = targetDropReason(villager, currentTarget);
            if (dropReason != null) {
                dropTarget(villager, dropReason);
                return;
            }
            if (isTargetScanTick(villager)) {
                LivingEntity priorityTarget = findBestTarget(type, villager, followRangeFor(type));
                if (priorityTarget != null
                        && priorityTarget != currentTarget
                        && shouldReplaceCurrentTarget(type, villager, currentTarget, priorityTarget)) {
                    logLowerPrioritySkipped(type, villager, currentTarget, priorityTarget);
                    selectTarget(villager, type, priorityTarget);
                }
            }
            return;
        }
        if (!isTargetScanTick(villager)) {
            return;
        }
        double range = followRangeFor(type);
        LivingEntity target = findBestTarget(type, villager, range);
        if (target == null) {
            return;
        }
        selectTarget(villager, type, target);
    }

    public static void markRetaliation(VillagerEntityMCA villager, LivingEntity attacker, String reason) {
        if (villager == null
                || attacker == null
                || villager.level().isClientSide
                || McaVampireCureService.isCuring(villager)) {
            return;
        }
        boolean guardMonsterAttacker = McaGuardMonsterDefenseService.canSpecialGuardTarget(villager, attacker);
        if (McaSpecialVillagerMonsterIgnoreService.isHostileMonster(attacker) && !guardMonsterAttacker) {
            return;
        }
        if (!McaCommandRespectService.canRetaliateWhileCommanded(villager, attacker)) {
            McaCommandRespectService.logRejectedTarget(villager, attacker);
            return;
        }
        McaSpecialVillagerType type = McaSpecialVillagerClassifier.classify(villager);
        if (guardMonsterAttacker) {
            markGuardMonsterRetaliation(villager, attacker);
            McaVampirismCompat.debugLog(
                    "[SPECIAL-AI] retaliation marked villager={} villagerType={} attacker={} reason={}",
                    villager.getUUID(),
                    type,
                    attacker.getUUID(),
                    reason
            );
            return;
        }
        switch (type) {
            case VAMPIRE -> markVampireRetaliation(villager, attacker);
            case HUNTER -> McaHunterTargetUtil.markRetaliation(villager, attacker);
            case WEREWOLF -> markWerewolfRetaliation(villager, attacker);
            case DEFENSIVE -> {
                if (McaDefensiveVillagerService.isValidDefensiveTarget(villager, attacker)) {
                    villager.setTarget(attacker);
                }
            }
            default -> {
                return;
            }
        }
        McaVampirismCompat.debugLog(
                "[SPECIAL-AI] retaliation marked villager={} villagerType={} attacker={} reason={}",
                villager.getUUID(),
                type,
                attacker.getUUID(),
                reason
        );
    }

    private static void markVampireRetaliation(VillagerEntityMCA villager, LivingEntity attacker) {
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .ifPresent(state -> state.markRetaliation(
                        attacker.getUUID(),
                        villager.level().getGameTime(),
                        McaCommandRespectService.retaliationMemoryTicksFor(
                                villager,
                                McaVampirismCompatConfig.retaliationMemoryTicks()
                        )
                ));
        if (McaVampireTargetUtil.isValidTarget(villager, attacker)) {
            villager.setTarget(attacker);
        }
    }

    private static void markGuardMonsterRetaliation(VillagerEntityMCA villager, LivingEntity attacker) {
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .ifPresent(state -> state.markRetaliation(
                        attacker.getUUID(),
                        villager.level().getGameTime(),
                        McaCommandRespectService.retaliationMemoryTicksFor(
                                villager,
                                McaVampirismCompatConfig.specialVillagerRetaliationMemoryTicks()
                        )
                ));
        villager.setTarget(attacker);
        McaGuardMonsterDefenseService.logAllowed(villager, attacker, "retaliation monster target");
    }

    private static void markWerewolfRetaliation(VillagerEntityMCA villager, LivingEntity attacker) {
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .ifPresent(state -> state.markRetaliation(
                        attacker.getUUID(),
                        villager.level().getGameTime(),
                        McaCommandRespectService.retaliationMemoryTicksFor(
                                villager,
                                McaVampirismCompatConfig.werewolfVillagerTargetMemoryTicks()
                        )
                ));
        if (McaWerewolfTargetUtil.isValidWerewolfTarget(villager, attacker)) {
            villager.setTarget(attacker);
        }
    }

    public static boolean isSpecialVillager(Entity entity) {
        return entity instanceof VillagerEntityMCA villager
                && McaSpecialVillagerClassifier.isSpecialNonHuman(villager);
    }

    public static boolean isValidMeleeTarget(VillagerEntityMCA villager, LivingEntity target) {
        McaSpecialVillagerType type = McaSpecialVillagerClassifier.classify(villager);
        if (type == McaSpecialVillagerType.VAMPIRE && target != null
                && McaVampireTargetUtil.isInfectionBiteTarget(villager, target)) {
            return false;
        }
        return targetDropReason(villager, target) == null && isValidTargetFor(type, villager, target);
    }

    public static String targetDropReason(VillagerEntityMCA villager, LivingEntity target) {
        if (villager == null || target == null || target == villager || !target.isAlive()) {
            return "INVALID";
        }
        McaSpecialVillagerType type = McaSpecialVillagerClassifier.classify(villager);
        if (type == McaSpecialVillagerType.CURING_VAMPIRE || type == McaSpecialVillagerType.CURING_WEREWOLF
                || McaVampireCureService.isCuring(villager)) {
            return "CURING";
        }
        if (type == McaSpecialVillagerType.CHILD || type == McaSpecialVillagerType.HUMAN_NORMAL
                || type == McaSpecialVillagerType.HUMAN_INFECTED || type == McaSpecialVillagerType.UNKNOWN) {
            return "NOT_COMBAT_CAPABLE";
        }
        boolean guardMonsterTarget = McaGuardMonsterDefenseService.canSpecialGuardTarget(villager, target);
        if (McaSpecialVillagerMonsterIgnoreService.isHostileMonster(target) && !guardMonsterTarget) {
            return "COMMON_MONSTER";
        }
        if (isFactionNpcTargetingDisabled(villager, target)) {
            return "NPC_TARGETING_DISABLED";
        }
        if (McaCommandRespectService.shouldDropTargetDueToCommand(villager, target)) {
            return "MCA_COMMAND";
        }
        if (guardMonsterTarget) {
            return isTooFarFromVillager(villager, target) ? "TOO_FAR" : null;
        }
        if (!isValidTargetFor(type, villager, target)) {
            return "FACTION_INVALID";
        }
        McaFactionCombatRules rules = rulesFor(type);
        if (rules != null) {
            String rulesDropReason = rules.targetDropReason(villager, target);
            if (rulesDropReason != null) {
                return rulesDropReason;
            }
        }
        if (isTooFarFromVillager(villager, target)) {
            return "TOO_FAR";
        }
        return null;
    }

    public static void dropTarget(VillagerEntityMCA villager, String reason) {
        if (villager == null || villager.getTarget() == null) {
            return;
        }
        LivingEntity target = villager.getTarget();
        villager.setTarget(null);
        boolean stopNavigation = !"MCA_COMMAND".equals(reason)
                || McaCommandRespectService.shouldStopNavigationWhenClearingTarget(villager);
        if (stopNavigation) {
            villager.getNavigation().stop();
        }
        if ("MCA_COMMAND".equals(reason)) {
            McaCommandRespectService.logClearedTarget(villager, target, stopNavigation);
        }
        McaVampirismCompat.debugLog(
                "[SPECIAL-AI] target dropped villager={} villagerType={} target={} reason={}",
                villager.getUUID(),
                McaSpecialVillagerClassifier.classify(villager),
                target == null ? null : target.getUUID(),
                reason
        );
    }

    public static int attackCooldownFor(McaSpecialVillagerType type) {
        int configured = switch (type) {
            case HUNTER, DEFENSIVE -> McaVampirismCompatConfig.hunterVillagerAttackCooldownTicks();
            case WEREWOLF -> McaVampirismCompatConfig.werewolfVillagerAttackCooldownTicks();
            case VAMPIRE -> McaVampirismCompatConfig.specialVillagerAttackCooldownTicks();
            case HUMAN_NORMAL, HUMAN_INFECTED, CURING_VAMPIRE, CURING_WEREWOLF, CHILD, UNKNOWN -> 20;
        };
        return Math.max(configured, McaVampirismCompatConfig.specialVillagerAttackCooldownTicks());
    }

    public static float attackDamageFor(McaSpecialVillagerType type) {
        McaFactionCombatRules rules = rulesFor(type);
        return rules == null ? 0.0F : rules.attackDamage(null);
    }

    public static boolean performMeleeAttack(PathfinderMob attacker, LivingEntity target, McaSpecialVillagerType type) {
        if (attacker == null || target == null || target.level().isClientSide) {
            return false;
        }
        if (type == McaSpecialVillagerType.VAMPIRE) {
            return attacker.doHurtTarget(target);
        }
        float damage = attackDamageFor(type);
        return damage > 0.0F && target.hurt(attacker.damageSources().mobAttack(attacker), damage);
    }

    private static boolean registerDefensiveGoalsIfNeeded(VillagerEntityMCA villager) {
        PathfinderMob mob = (PathfinderMob) villager;
        return villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).map(state -> {
            if (state.areDefensiveGoalsAdded()) {
                return false;
            }
            mob.goalSelector.addGoal(5, new McaSpecialMeleeAttackGoal(mob));
            state.markDefensiveGoalsAdded();
            McaVampirismCompat.debugLog(
                    "[SPECIAL-AI] defensive goals added villager={}",
                    villager.getUUID()
            );
            return true;
        }).orElse(false);
    }

    private static boolean isValidTargetFor(McaSpecialVillagerType type, VillagerEntityMCA villager, LivingEntity target) {
        if (target == null || target == villager || !target.isAlive()) {
            return false;
        }
        if (McaSpecialVillagerMonsterIgnoreService.isHostileMonster(target)) {
            return McaGuardMonsterDefenseService.canSpecialGuardTarget(villager, target);
        }
        McaFactionCombatRules rules = rulesFor(type);
        return rules != null && rules.canTarget(villager, target);
    }

    private static LivingEntity findBestTarget(McaSpecialVillagerType type, VillagerEntityMCA villager, double range) {
        return villager.level()
                .getEntitiesOfClass(
                        LivingEntity.class,
                        villager.getBoundingBox().inflate(range, 4.0D, range),
                        candidate -> isAutomaticTargetCandidate(type, villager, candidate))
                .stream()
                .min(Comparator
                        .<LivingEntity>comparingInt(candidate -> targetPriorityScore(type, villager, candidate).categoryPriority())
                        .thenComparingInt(candidate -> targetPriorityScore(type, villager, candidate).factionPriority())
                        .thenComparingDouble(candidate -> villager.distanceToSqr(candidate)))
                .orElse(null);
    }

    private static boolean shouldReplaceCurrentTarget(McaSpecialVillagerType type, VillagerEntityMCA villager,
                                                      LivingEntity currentTarget, LivingEntity candidateTarget) {
        TargetPriorityScore current = targetPriorityScore(type, villager, currentTarget);
        TargetPriorityScore candidate = targetPriorityScore(type, villager, candidateTarget);
        return candidate.outranksIgnoringDistance(current);
    }

    private static boolean isAutomaticTargetCandidate(McaSpecialVillagerType type, VillagerEntityMCA villager,
                                                      LivingEntity target) {
        if (!isValidTargetFor(type, villager, target)) {
            return false;
        }
        if (!isFactionNpcTargetingDisabled(villager, target)) {
            return true;
        }
        if (villager.tickCount % 40 == 0) {
            McaVampirismCompat.verboseAiLog(
                    "[SPECIAL-AI] target rejected villager={} target={} targetType={} reason=NPC_TARGETING_DISABLED",
                    villager.getUUID(),
                    target.getUUID(),
                    targetTypeFor(target)
            );
        }
        return false;
    }

    private static boolean isMcaSpecialPriorityTarget(McaSpecialVillagerType type, VillagerEntityMCA villager, LivingEntity target) {
        if (!(target instanceof VillagerEntityMCA targetVillager) || !isValidTargetFor(type, villager, target)) {
            return false;
        }
        McaSpecialVillagerType targetType = McaSpecialVillagerClassifier.classify(targetVillager);
        return switch (type) {
            case VAMPIRE -> targetType == McaSpecialVillagerType.HUNTER || targetType == McaSpecialVillagerType.WEREWOLF;
            case HUNTER, DEFENSIVE -> targetType == McaSpecialVillagerType.VAMPIRE || targetType == McaSpecialVillagerType.WEREWOLF;
            case WEREWOLF -> targetType == McaSpecialVillagerType.VAMPIRE || targetType == McaSpecialVillagerType.HUNTER;
            case HUMAN_NORMAL, HUMAN_INFECTED, CURING_VAMPIRE, CURING_WEREWOLF, CHILD, UNKNOWN -> false;
        };
    }

    private static boolean isTargetScanTick(VillagerEntityMCA villager) {
        long gameTime = villager.level().getGameTime();
        int interval = Math.max(1, McaVampirismCompatConfig.specialVillagerTargetScanIntervalTicks());
        return gameTime % interval == Math.floorMod(villager.getId(), interval);
    }

    private static int priorityFor(McaSpecialVillagerType type, VillagerEntityMCA villager, LivingEntity target) {
        if (McaGuardMonsterDefenseService.canSpecialGuardTarget(villager, target)) {
            return McaGuardMonsterDefenseService.targetPriority();
        }
        McaFactionCombatRules rules = rulesFor(type);
        return rules == null ? 100 : rules.targetPriority(villager, target);
    }

    private static Object targetReasonFor(McaSpecialVillagerType type, VillagerEntityMCA villager, LivingEntity target) {
        if (McaGuardMonsterDefenseService.canSpecialGuardTarget(villager, target)) {
            return McaGuardMonsterDefenseService.targetReason();
        }
        McaFactionCombatRules rules = rulesFor(type);
        return rules == null ? "NONE" : rules.targetReason(villager, target);
    }

    private static void selectTarget(VillagerEntityMCA villager, McaSpecialVillagerType type, LivingEntity target) {
        villager.setTarget(target);
        TargetPriorityScore score = targetPriorityScore(type, villager, target);
        McaVampirismCompat.debugLog(
                "[SPECIAL-AI] target selected villager={} villagerType={} target={} targetType={} reason={} priority={} score={}",
                villager.getUUID(),
                type,
                target.getUUID(),
                score.targetType(),
                score.reason(),
                priorityLabelFor(score),
                score.logScore()
        );
    }

    private static void logLowerPrioritySkipped(McaSpecialVillagerType type, VillagerEntityMCA villager,
                                                LivingEntity skippedTarget, LivingEntity selectedTarget) {
        TargetPriorityScore skipped = targetPriorityScore(type, villager, skippedTarget);
        TargetPriorityScore selected = targetPriorityScore(type, villager, selectedTarget);
        McaVampirismCompat.debugLog(
                "[SPECIAL-AI] lower priority target skipped villager={} skippedTarget={} skippedType={} selectedTarget={} selectedType={} reason={}",
                villager.getUUID(),
                skippedTarget == null ? null : skippedTarget.getUUID(),
                skipped.targetType(),
                selectedTarget == null ? null : selectedTarget.getUUID(),
                selected.targetType(),
                selected.categoryPriority() == 0 && skipped.categoryPriority() > 0
                        ? "MCA_TARGET_AVAILABLE"
                        : "HIGHER_PRIORITY_TARGET_AVAILABLE"
        );
    }

    private static TargetPriorityScore targetPriorityScore(McaSpecialVillagerType type, VillagerEntityMCA villager, LivingEntity target) {
        Object reason = targetReasonLabel(type, villager, target);
        String targetType = targetTypeFor(target);
        return new TargetPriorityScore(
                targetCategoryPriority(type, villager, target),
                priorityFor(type, villager, target),
                villager.distanceToSqr(target),
                reason,
                targetType
        );
    }

    private static Object targetReasonLabel(McaSpecialVillagerType type, VillagerEntityMCA villager, LivingEntity target) {
        return isMcaSpecialPriorityTarget(type, villager, target) ? targetTypeFor(target) : targetReasonFor(type, villager, target);
    }

    private static int targetCategoryPriority(McaSpecialVillagerType type, VillagerEntityMCA villager, LivingEntity target) {
        if (isMcaSpecialPriorityTarget(type, villager, target)) {
            return 0;
        }
        if (target instanceof Player) {
            return 1;
        }
        if (McaGuardMonsterDefenseService.canSpecialGuardTarget(villager, target)) {
            return 2;
        }
        if (target instanceof VillagerEntityMCA) {
            return 2;
        }
        return target == null ? 3 : 2;
    }

    private static String priorityLabelFor(TargetPriorityScore score) {
        return switch (score.categoryPriority()) {
            case 0 -> "MCA_SPECIAL";
            case 1 -> "PLAYER";
            case 2 -> "NPC";
            default -> "OTHER";
        };
    }

    private static String targetTypeFor(LivingEntity target) {
        if (target instanceof VillagerEntityMCA villager) {
            return switch (McaSpecialVillagerClassifier.classify(villager)) {
                case VAMPIRE -> "MCA_VAMPIRE";
                case HUNTER, DEFENSIVE -> "MCA_HUNTER";
                case WEREWOLF -> "MCA_WEREWOLF";
                case CURING_VAMPIRE -> "MCA_CURING_VAMPIRE";
                case CURING_WEREWOLF -> "MCA_CURING_WEREWOLF";
                case HUMAN_NORMAL, HUMAN_INFECTED, CHILD, UNKNOWN -> "MCA_HUMAN";
            };
        }
        if (target instanceof Player player) {
            return "PLAYER_" + McaVampireFactionService.classifyPlayerContext(player).effectiveFaction().name();
        }
        if (McaSpecialVillagerMonsterIgnoreService.isHostileMonster(target)) {
            return "COMMON_HOSTILE_MONSTER";
        }
        if (McaVampireFactionService.isVampire(target)) {
            return "NPC_VAMPIRE";
        }
        if (McaVampireFactionService.isHunter(target)) {
            return "NPC_HUNTER";
        }
        if (McaVampireFactionService.isWerewolf(target)) {
            return "NPC_WEREWOLF";
        }
        return target == null ? "UNKNOWN" : target.getType().toString();
    }

    private static double followRangeFor(McaSpecialVillagerType type) {
        McaFactionCombatRules rules = rulesFor(type);
        return rules == null ? 0.0D : rules.followRange(null);
    }

    private static boolean isTooFarFromVillager(VillagerEntityMCA villager, LivingEntity target) {
        int maxDistance = McaVampirismCompatConfig.specialVillagerMaxChaseDistance();
        return maxDistance > 0 && villager.distanceToSqr(target) > (double) maxDistance * maxDistance;
    }

    private static boolean isFactionNpcTargetingDisabled(VillagerEntityMCA villager, LivingEntity target) {
        return !McaVampirismCompatConfig.specialVillagersTargetFactionNpcs()
                && isFactionNpcOrMob(target)
                && !isRetaliationTarget(villager, target);
    }

    private static boolean isFactionNpcOrMob(LivingEntity target) {
        return target != null
                && !(target instanceof Player)
                && !(target instanceof VillagerEntityMCA)
                && (McaVampireFactionService.isVampire(target)
                || McaVampireFactionService.isHunter(target)
                || McaVampireFactionService.isWerewolf(target));
    }

    private static boolean isRetaliationTarget(VillagerEntityMCA villager, LivingEntity target) {
        if (villager == null || target == null) {
            return false;
        }
        long gameTime = villager.level().getGameTime();
        return villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .map(state -> state.isRetaliatingAgainst(target.getUUID(), gameTime))
                .orElse(false);
    }

    private static McaFactionCombatRules rulesFor(McaSpecialVillagerType type) {
        return switch (type) {
            case VAMPIRE -> VAMPIRE_RULES;
            case HUNTER -> HUNTER_RULES;
            case WEREWOLF -> WEREWOLF_RULES;
            case DEFENSIVE -> HUNTER_RULES;
            case HUMAN_NORMAL, HUMAN_INFECTED, CURING_VAMPIRE, CURING_WEREWOLF, CHILD, UNKNOWN -> null;
        };
    }

    private record TargetPriorityScore(
            int categoryPriority,
            int factionPriority,
            double distanceSqr,
            Object reason,
            String targetType
    ) {
        private boolean outranksIgnoringDistance(TargetPriorityScore other) {
            return categoryPriority < other.categoryPriority
                    || (categoryPriority == other.categoryPriority && factionPriority < other.factionPriority);
        }

        private int logScore() {
            int distanceScore = (int) Math.min(99, Math.floor(Math.sqrt(Math.max(0.0D, distanceSqr))));
            return categoryPriority * 1000 + factionPriority * 100 + distanceScore;
        }
    }
}
