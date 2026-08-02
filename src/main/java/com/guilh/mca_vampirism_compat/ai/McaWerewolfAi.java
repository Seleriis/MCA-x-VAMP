package com.guilh.mca_vampirism_compat.ai;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public final class McaWerewolfAi {
    private McaWerewolfAi() {
    }

    public static boolean registerGoalsIfNeeded(LivingEntity entity) {
        if (!McaVampirismCompatConfig.enableMcaWerewolfVillagerAi()
                || !(entity instanceof VillagerEntityMCA villager)
                || villager.level().isClientSide
                || villager.getAgeState() != AgeState.ADULT
                || McaVampireStateService.isVampire(villager)
                || !McaWerewolfStateService.isWerewolf(villager)
                || !(entity instanceof PathfinderMob mob)) {
            return false;
        }
        return ModCapabilities.get(villager).map(state -> {
            if (state.areWerewolfAiGoalsAdded()) {
                return false;
            }
            mob.goalSelector.addGoal(5, new McaSpecialMeleeAttackGoal(mob));
            state.markWerewolfAiGoalsAdded();
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-AI] goals added villager={} followRange={} maxChase={}",
                    villager.getUUID(),
                    McaVampirismCompatConfig.werewolfVillagerFollowRange(),
                    McaVampirismCompatConfig.werewolfVillagerMaxChaseDistanceFromHome()
            );
            return true;
        }).orElse(false);
    }
}
