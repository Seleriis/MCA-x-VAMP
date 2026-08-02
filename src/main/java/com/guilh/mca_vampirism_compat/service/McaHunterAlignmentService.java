package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.faction.hunter.McaHunterAggressionPolicy;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class McaHunterAlignmentService {
    private McaHunterAlignmentService() {
    }

    public static boolean isMcaHunterVillager(Entity entity) {
        return isHunterAligned(entity);
    }

    public static boolean isHunterAligned(Entity entity) {
        if (!(entity instanceof VillagerEntityMCA villager)
                || McaVampireStateService.isVampire(villager)
                || (McaWerewolfStateService.isWerewolf(villager)
                && McaVampirismCompatConfig.werewolfVillagersImmuneToHunterConversion())) {
            return false;
        }
        return hasHunterTrait(villager) || isVillageHunter(villager) || isDefensiveHunter(villager);
    }

    public static boolean isMcaHunterAligned(Entity entity) {
        return isHunterAligned(entity);
    }

    public static boolean hasHunterTrait(VillagerEntityMCA villager) {
        return villager != null && McaTraitBridge.hasHunterTrait(villager);
    }

    public static boolean isDefensiveHunter(VillagerEntityMCA villager) {
        if (!McaVampirismCompatConfig.defensiveModeGrantsHunterTrait()
                || !McaVampirismCompatConfig.defensiveVillagersAreHunterAligned()
                || villager == null
                || !McaHunterAggressionPolicy.canEnterDefensiveAggressiveMode(
                villager,
                McaHunterAggressionPolicy.DefensiveReason.TEMPORARY_DEFENSE)) {
            return false;
        }
        long gameTime = villager.level().getGameTime();
        return ModCapabilities.get(villager)
                .map(state -> (state.isDefensiveMode() && gameTime <= state.getDefensiveModeUntilTick())
                        || (state.isHunterAligned() && gameTime <= state.getHunterAlignmentUntilTick()))
                .orElse(false);
    }

    public static boolean isVillageHunter(VillagerEntityMCA villager) {
        if (villager == null) {
            return false;
        }
        return ModCapabilities.get(villager)
                .map(state -> state.isHunterVillageApplied())
                .orElse(false);
    }

    public static boolean isHunterCombatVillager(Entity entity) {
        if (!(entity instanceof VillagerEntityMCA villager)
                || McaVampireStateService.isVampire(villager)
                || (McaWerewolfStateService.isWerewolf(villager)
                && McaVampirismCompatConfig.werewolfVillagersImmuneToHunterConversion())) {
            return false;
        }
        if (isHunterAligned(villager)) {
            return true;
        }
        return ModCapabilities.get(villager)
                .map(state -> state.isDefensiveMode()
                        && villager.level().getGameTime() <= state.getDefensiveModeUntilTick()
                        && McaHunterAggressionPolicy.canEnterDefensiveAggressiveMode(
                        villager,
                        McaHunterAggressionPolicy.DefensiveReason.TEMPORARY_DEFENSE))
                .orElse(false);
    }

    public static boolean isHunterLikeEntity(Entity entity) {
        if (isMcaHunterAligned(entity)) {
            return true;
        }
        return entity instanceof LivingEntity && McaVampireFactionService.isHunter(entity);
    }

    public static boolean shouldTreatAsHunterEnemyForVampires(Entity entity) {
        return McaVampirismCompatConfig.defensiveVillagersCountAsHuntersForVampireAi()
                && (McaVampirismCompatConfig.hunterVillagersAttackMcaVampireVillagers()
                || McaVampirismCompatConfig.defensiveVillagersCanAttackMcaVampireVillagers())
                && isMcaHunterAligned(entity);
    }
}
