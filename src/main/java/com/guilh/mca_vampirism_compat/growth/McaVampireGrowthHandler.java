package com.guilh.mca_vampirism_compat.growth;

import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.AgeState;
import net.minecraft.world.entity.LivingEntity;

public final class McaVampireGrowthHandler {
    private McaVampireGrowthHandler() {
    }

    public static void processGrowth(LivingEntity entity) {
        if (entity.level().isClientSide || !McaVampirismCompatConfig.allowVampireChildrenGrowthBoost()
                || !McaTraitBridge.isVampireByTraitOrCapability(entity) || !(entity instanceof VillagerEntityMCA villager)) {
            return;
        }
        if (villager.getAgeState() == AgeState.ADULT || villager.getAge() >= 0) {
            return;
        }

        entity.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
            long gameTime = entity.level().getGameTime();
            long interval = McaVampirismCompatConfig.growthProcessIntervalTicks();
            if (!state.canProcessGrowth(gameTime, interval)) {
                return;
            }

            int extraGrowth = (int) Math.round(interval * (McaVampirismCompatConfig.vampireBabyGrowthMultiplier() - 1.0D));
            villager.setAge(Math.min(0, villager.getAge() + extraGrowth));
            state.markGrowthTick(gameTime);
        });
    }
}
