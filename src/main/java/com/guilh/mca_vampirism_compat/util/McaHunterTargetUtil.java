package com.guilh.mca_vampirism_compat.util;

import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.faction.PlayerFactionContext;
import com.guilh.mca_vampirism_compat.service.McaHunterAlignmentService;
import com.guilh.mca_vampirism_compat.service.McaCommandRespectService;
import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.service.McaVampireFactionService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaVillageFactionControlService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class McaHunterTargetUtil {
    private McaHunterTargetUtil() {
    }

    public static boolean isValidHunterTarget(VillagerEntityMCA hunter, LivingEntity target) {
        return targetReason(hunter, target) != TargetReason.NONE;
    }

    public static TargetReason targetReason(VillagerEntityMCA hunter, LivingEntity target) {
        if (com.guilh.mca_vampirism_compat.service.McaRelationshipCombatPolicy.blocksAutomaticAttack(hunter,target)) return TargetReason.NONE;
        if (!canHunterFight(hunter) || target == null || target == hunter || !target.isAlive()) {
            return TargetReason.NONE;
        }
        if (com.guilh.mca_vampirism_compat.service.McaRelationshipCombatPolicy.protectedRetaliation(hunter,target)) return TargetReason.PROTECTED_AGGRESSOR;
        if (target instanceof VillagerEntityMCA targetVillager) {
            if (McaVampireCureService.isCuring(targetVillager)) {
                return TargetReason.NONE;
            }
            if (targetVillager.getAgeState() != AgeState.ADULT) {
                return TargetReason.NONE;
            }
            if (McaVampireStateService.isVampire(targetVillager)) {
                return canHunterAttackMcaVampireVillager(hunter, targetVillager)
                        ? TargetReason.MCA_VAMPIRE_VILLAGER
                        : TargetReason.NONE;
            }
            if (McaWerewolfStateService.isWerewolf(targetVillager)) {
                return TargetReason.MCA_WEREWOLF_VILLAGER;
            }
            if (McaHunterAlignmentService.isMcaHunterVillager(targetVillager)) {
                return TargetReason.NONE;
            }
        }
        if (McaVampireFactionService.isHunter(target)) {
            return TargetReason.NONE;
        }

        boolean retaliation = isRetaliatingAgainst(hunter, target);
        if (McaVampirismCompatConfig.hunterVillagersAttackOnlyWhileDefensive()
                && !McaHunterAlignmentService.isDefensiveHunter(hunter)
                && !McaHunterAlignmentService.isVillageHunter(hunter)
                && !retaliation) {
            return TargetReason.NONE;
        }
        if (retaliation
                && McaVampirismCompatConfig.hunterVillagerPreferRecentVampireAggressors()
                && isRecentAggressorTarget(target)) {
            return TargetReason.RECENT_VAMPIRE_AGGRESSOR;
        }

        if (target instanceof Player player) {
            if (isVampirePlayerTarget(hunter, player, retaliation)) {
                return TargetReason.VAMPIRE_PLAYER;
            }
            return isTransformedWerewolfTarget(player)
                    ? TargetReason.TRANSFORMED_WEREWOLF
                    : TargetReason.NONE;
        }
        if (isTransformedWerewolfTarget(target)) {
            return TargetReason.TRANSFORMED_WEREWOLF;
        }
        if (isVampireTarget(target)) {
            return TargetReason.VAMPIRE_MOB;
        }
        return TargetReason.NONE;
    }

    public static boolean isVampireTarget(LivingEntity target) {
        if (!McaVampirismCompatConfig.hunterVillagersAttackVampireMobs() || target instanceof Player) {
            return false;
        }
        if (target instanceof VillagerEntityMCA villager) {
            return McaVampirismCompatConfig.hunterVillagersAttackMcaVampireVillagers()
                    && McaVampireStateService.isVampire(villager);
        }
        return McaVampireFactionService.isVampire(target);
    }

    public static boolean isTransformedWerewolfTarget(LivingEntity target) {
        return McaVampirismCompatConfig.hunterVillagersAttackTransformedWerewolves()
                && McaVampireFactionService.isWerewolf(target)
                && McaVampireFactionService.isTransformedWerewolf(target);
    }

    public static boolean canHunterAttackMcaVampireVillager(VillagerEntityMCA hunter, VillagerEntityMCA target) {
        return hunter != target
                && target.getAgeState() == AgeState.ADULT
                && McaVampireStateService.isVampire(target);
    }

    public static boolean canHunterAttackVampirePlayer(ServerPlayer player) {
        return canHunterAttackVampirePlayer((Player) player);
    }

    public static boolean canHunterAttackVampirePlayer(Player player) {
        return player != null
                && McaVampirismCompatConfig.hunterVillagersAttackVampirePlayers()
                && McaVampireFactionService.classifyPlayer(player) == PlayerFaction.VAMPIRE
                && (!McaVampireFactionService.classifyPlayerContext(player).vampireDisguised()
                || McaVampirismCompatConfig.hunterVillagersIgnoreVampireDisguise());
    }

    public static void markRetaliation(VillagerEntityMCA hunter, LivingEntity attacker) {
        if (hunter == null || attacker == null || hunter.level().isClientSide) {
            return;
        }
        if (!McaCommandRespectService.canRetaliateWhileCommanded(hunter, attacker)) {
            McaCommandRespectService.logRejectedTarget(hunter, attacker);
            return;
        }
        ModCapabilities.get(hunter)
                .ifPresent(state -> state.markRetaliation(
                        attacker.getUUID(),
                        hunter.level().getGameTime(),
                        McaCommandRespectService.retaliationMemoryTicksFor(
                                hunter,
                                McaVampirismCompatConfig.hunterVillagerRetaliationMemoryTicks()
                        )
                ));
        if (isValidHunterTarget(hunter, attacker)) {
            hunter.setTarget(attacker);
        }
    }

    public static DropReason targetDropReason(VillagerEntityMCA hunter, LivingEntity target) {
        if (hunter == null || target == null) {
            return DropReason.INVALID;
        }
        if (isTooFarFromHunterVillage(hunter, target)) {
            return DropReason.TOO_FAR_FROM_VILLAGE;
        }
        if (isTooFarFromHome(hunter, target)) {
            return DropReason.TOO_FAR_FROM_HOME;
        }
        if (target instanceof Player player
                && McaVampireFactionService.classifyPlayer(player) == PlayerFaction.VAMPIRE
                && McaVampireFactionService.classifyPlayerContext(player).vampireDisguised()
                && !McaVampirismCompatConfig.hunterVillagersIgnoreVampireDisguise()
                && !isRetaliatingAgainst(hunter, target)) {
            return DropReason.DISGUISE_NOT_DETECTED;
        }
        return DropReason.NONE;
    }

    private static boolean canHunterFight(VillagerEntityMCA hunter) {
        return hunter != null
                && !hunter.level().isClientSide
                && hunter.isAlive()
                && hunter.getAgeState() == AgeState.ADULT
                && McaHunterAlignmentService.isHunterCombatVillager(hunter);
    }

    private static boolean isVampirePlayerTarget(VillagerEntityMCA hunter, Player player, boolean retaliation) {
        if (!McaVampirismCompatConfig.hunterVillagersAttackVampirePlayers()
                || McaVampireFactionService.classifyPlayer(player) != PlayerFaction.VAMPIRE) {
            return false;
        }
        PlayerFactionContext context = McaVampireFactionService.classifyPlayerContext(player);
        return !context.vampireDisguised()
                || McaVampirismCompatConfig.hunterVillagersIgnoreVampireDisguise()
                || retaliation;
    }

    private static boolean isRetaliatingAgainst(VillagerEntityMCA hunter, LivingEntity target) {
        long gameTime = hunter.level().getGameTime();
        return ModCapabilities.get(hunter)
                .map(state -> state.isRetaliatingAgainst(target.getUUID(), gameTime))
                .orElse(false);
    }

    private static boolean isRecentAggressorTarget(LivingEntity target) {
        if (target instanceof Player player) {
            PlayerFaction realFaction = McaVampireFactionService.classifyPlayer(player);
            return realFaction == PlayerFaction.VAMPIRE
                    || realFaction == PlayerFaction.TRANSFORMED_WEREWOLF;
        }
        return McaVampireFactionService.isVampire(target) || McaVampireFactionService.isTransformedWerewolf(target);
    }

    private static boolean isTooFarFromHunterVillage(VillagerEntityMCA hunter, LivingEntity target) {
        int maxDistance = McaVampirismCompatConfig.hunterVillagerMaxChaseDistanceFromVillage();
        return McaVillageFactionControlService.getHunterVillageTotemPos(hunter)
                .map(totemPos -> target.distanceToSqr(Vec3.atCenterOf(totemPos)) > (double) maxDistance * maxDistance)
                .orElse(false);
    }

    private static boolean isTooFarFromHome(VillagerEntityMCA hunter, LivingEntity target) {
        PathfinderMob mob = (PathfinderMob) hunter;
        if (!mob.hasRestriction()) {
            return false;
        }
        int maxDistance = McaVampirismCompatConfig.hunterVillagerMaxChaseDistanceFromHome();
        BlockPos home = mob.getRestrictCenter();
        return target.distanceToSqr(Vec3.atCenterOf(home)) > (double) maxDistance * maxDistance;
    }

    public enum TargetReason {
        NONE,
        PROTECTED_AGGRESSOR,
        RECENT_VAMPIRE_AGGRESSOR,
        VAMPIRE_PLAYER,
        VAMPIRE_MOB,
        MCA_VAMPIRE_VILLAGER,
        MCA_WEREWOLF_VILLAGER,
        TRANSFORMED_WEREWOLF
    }

    public enum DropReason {
        NONE,
        INVALID,
        TOO_FAR_FROM_VILLAGE,
        TOO_FAR_FROM_HOME,
        DISGUISE_NOT_DETECTED
    }
}
