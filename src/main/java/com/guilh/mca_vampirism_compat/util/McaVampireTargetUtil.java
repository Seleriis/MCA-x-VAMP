package com.guilh.mca_vampirism_compat.util;

import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaVampireBiteService;
import com.guilh.mca_vampirism_compat.service.McaCommandRespectService;
import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.service.McaVampireFactionService;
import com.guilh.mca_vampirism_compat.service.McaHunterAlignmentService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import de.teamlapen.vampirism.core.ModEffects;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public final class McaVampireTargetUtil {
    private McaVampireTargetUtil() {
    }

    public static boolean isValidTarget(Mob vampire, LivingEntity target) {
        if (!McaVillagerUtil.isMcaVampire(vampire) || McaVampireCureService.isCuring(vampire)
                || target == vampire || !target.isAlive()) {
            return false;
        }
        if (target instanceof VillagerEntityMCA villager && McaVampireCureService.isCuring(villager)) {
            return false;
        }
        if (target instanceof VillagerEntityMCA targetVillager) {
            if (McaVampireStateService.isVampire(targetVillager)) {
                return isDisguisedRetaliationTarget(vampire, target);
            }
            if (McaWerewolfStateService.isWerewolf(targetVillager)) {
                return true;
            }
            if (McaHunterAlignmentService.isMcaHunterVillager(targetVillager)) {
                return true;
            }
        }
        if (isVampireTarget(target)) {
            return isDisguisedRetaliationTarget(vampire, target);
        }
        if (isHunterTarget(target)) {
            return true;
        }
        if (isTransformedWerewolfTarget(target)) {
            return true;
        }
        if (isRetaliationTarget(vampire, target) && isHumanLikeTarget(target)) {
            return true;
        }
        return isInfectionBiteTarget(vampire, target);
    }

    public static boolean isInfectionBiteTarget(Mob vampire, LivingEntity target) {
        if (vampire instanceof VillagerEntityMCA villager
                && McaCommandRespectService.shouldSuppressAutonomousBiteOrHunt(villager)) {
            return false;
        }
        if (!McaVampirismCompatConfig.vampireVillagersInfectHumansAtNight() || vampire.level().isDay()
                || !McaVillagerUtil.isMcaVampire(vampire) || McaVampireCureService.isCuring(vampire)
                || !isHumanLikeTarget(target)
                || isRetaliationTarget(vampire, target) || !canAttackerBite(vampire)) {
            return false;
        }
        return canTargetHumanForInfection(target);
    }

    public static boolean isRetaliationTarget(Mob vampire, LivingEntity target) {
        long gameTime = vampire.level().getGameTime();
        return ModCapabilities.get(vampire)
                .map(state -> state.isRetaliatingAgainst(target.getUUID(), gameTime))
                .orElse(false);
    }

    public static boolean isHumanLikeTarget(LivingEntity target) {
        if (target == null || isVampireTarget(target) || isHunterTarget(target) || isTransformedWerewolfTarget(target)) {
            return false;
        }
        if (target instanceof VillagerEntityMCA villager
                && (McaVampireStateService.isVampire(villager)
                || McaHunterAlignmentService.isMcaHunterVillager(villager)
                || McaWerewolfStateService.isWerewolf(villager))) {
            return false;
        }
        if (target instanceof Player player) {
            PlayerFaction faction = McaVampireFactionService.classifyPlayer(player);
            return faction == PlayerFaction.HUMAN || faction == PlayerFaction.UNKNOWN;
        }
        return McaVillagerUtil.isMcaVillager(target) || target instanceof Villager;
    }

    public static boolean isHunterTarget(LivingEntity target) {
        if (!McaVampirismCompatConfig.vampireVillagersTreatHuntersAsEnemies()) {
            return false;
        }
        if (McaHunterAlignmentService.isMcaHunterAligned(target)) {
            return McaHunterAlignmentService.shouldTreatAsHunterEnemyForVampires(target);
        }
        return McaVampireFactionService.isHunter(target);
    }

    public static boolean isTransformedWerewolfTarget(LivingEntity target) {
        if (!McaVampirismCompatConfig.vampireVillagersTreatTransformedWerewolvesAsEnemies()) {
            return false;
        }
        if (target instanceof Player player) {
            return McaVampireFactionService.classifyPlayerContext(player).transformedWerewolf();
        }
        if (!McaVampireFactionService.isWerewolf(target)) {
            return false;
        }
        return !McaVampirismCompatConfig.attackOnlyTransformedWerewolves()
                || McaVampireFactionService.isTransformedWerewolf(target);
    }

    public static boolean isVampireTarget(LivingEntity target) {
        return McaVampireFactionService.isVampire(target);
    }

    private static boolean canTargetHumanForInfection(LivingEntity target) {
        if (target.hasEffect(VampirismEffectCompat.sanguinare()) || McaVampireBiteService.wasRecentlyBitten(target)) {
            return false;
        }
        if (!McaVampirismCompatConfig.allowChildInfection() && target instanceof VillagerEntityMCA villager
                && villager.getAgeState() != AgeState.ADULT) {
            return false;
        }
        return ModCapabilities.get(target)
                .map(state -> !state.isInfected() && !state.isConverted())
                .orElse(true);
    }

    private static boolean canAttackerBite(Mob vampire) {
        long gameTime = vampire.level().getGameTime();
        return ModCapabilities.get(vampire)
                .map(state -> state.canBite(gameTime, McaVampirismCompatConfig.biteCooldownTicks()))
                .orElse(false);
    }

    private static boolean isDisguisedRetaliationTarget(Mob vampire, LivingEntity target) {
        return target instanceof Player player
                && isRetaliationTarget(vampire, target)
                && McaVampireFactionService.classifyPlayerContext(player).vampireDisguised();
    }
}
