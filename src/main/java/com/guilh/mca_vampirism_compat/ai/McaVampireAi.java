package com.guilh.mca_vampirism_compat.ai;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.AgeState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public final class McaVampireAi {
    private McaVampireAi() {
    }

    public static boolean registerGoalsIfNeeded(LivingEntity entity) {
        if (!McaVillagerUtil.isMcaVampire(entity) || !isAdultMcaVillager(entity) || !(entity instanceof PathfinderMob mob)) {
            return false;
        }

        return entity.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).map(state -> {
            if (state.areAiGoalsAdded()) {
                return false;
            }

            double speed = Math.min(
                    McaVampirismCompatConfig.specialVillagerChaseGoalSpeed(),
                    Math.min(McaVampirismCompatConfig.specialVillagerMaxNavigationSpeed(), 1.0D)
            );
            mob.goalSelector.addGoal(3, new McaVampireSeekShelterGoal(mob, 1.0D));
            mob.goalSelector.addGoal(4, new McaVampireInfectBiteGoal(mob, speed, true));
            mob.goalSelector.addGoal(5, new McaSpecialMeleeAttackGoal(mob));
            state.markAiGoalsAdded();
            McaVampirismCompat.debugLog(
                    "[VAMPIRE-AI] goals added villager={}",
                    entity.getUUID()
            );
            return true;
        }).orElse(false);
    }

    private static boolean isAdultMcaVillager(LivingEntity entity) {
        return entity instanceof VillagerEntityMCA villager && villager.getAgeState() == AgeState.ADULT;
    }
}
