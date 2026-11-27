package com.guilh.mca_vampirism_compat.ai;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaHunterAlignmentService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public final class McaHunterAi {
    private McaHunterAi() {
    }

    public static boolean registerGoalsIfNeeded(LivingEntity entity) {
        if (!(entity instanceof VillagerEntityMCA villager)
                || villager.level().isClientSide
                || villager.getAgeState() != AgeState.ADULT
                || McaVampireStateService.isVampire(villager)
                || !McaHunterAlignmentService.isHunterCombatVillager(villager)
                || !(entity instanceof PathfinderMob mob)) {
            return false;
        }

        return ModCapabilities.get(villager).map(state -> {
            if (state.areHunterAiGoalsAdded()) {
                return false;
            }
            mob.goalSelector.addGoal(5, new McaSpecialMeleeAttackGoal(mob));
            state.markHunterAiGoalsAdded();
            McaVampirismCompat.debugLog(
                    "[HUNTER-AI] goals added villager={} followRange={}",
                    villager.getUUID(),
                    McaVampirismCompatConfig.hunterVillagerFollowRange()
            );
            return true;
        }).orElse(false);
    }
}
