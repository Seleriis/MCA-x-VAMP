package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.attribute.McaSpecialVillagerAttributeService;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.conczin.mca.entity.VillagerEntityMCA;

/** Server-side cure ticking; mutation remains delegated to the stable cure facade. */
final class McaCureLifecycleService {
    private McaCureLifecycleService() {
    }

    static void processCureTick(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        long gameTime = villager.level().getGameTime();
        ModCapabilities.get(villager).ifPresent(state -> {
            state.clearExpiredCureIngredientProgress(gameTime);
            if (state.isCuringVampire()) {
                processVampireCureTick(villager, gameTime);
            }
            if (state.isCuringWerewolf()) {
                processWerewolfCureTick(villager, gameTime);
            }
        });
    }

    private static void processVampireCureTick(VillagerEntityMCA villager, long gameTime) {
        if (!McaVampirismCompatConfig.enableMcaVampireVillagerCure()) {
            McaVampireCureService.cancelCure(villager, "CONFIG_DISABLED");
            return;
        }
        if (!McaVampireStateService.isVampire(villager)) {
            McaVampireCureService.cancelCure(villager, "NO_LONGER_VAMPIRE");
            return;
        }
        McaVampireCureService.neutralizeCombat(villager);
        McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
        if (McaVampirismCompatConfig.vampireCureAppliesWeaknessDuringProcess()
                && !villager.hasEffect(MobEffects.WEAKNESS)) {
            villager.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, false), villager);
        }
        if (McaVampirismCompatConfig.vampireCureShowsParticles()) {
            sendCureParticles(villager, gameTime);
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            if (gameTime >= state.getCureFinishTick()) {
                McaVampireCureService.completeCure(villager, McaVampireCureService.CureType.VAMPIRE);
            }
        });
    }

    private static void processWerewolfCureTick(VillagerEntityMCA villager, long gameTime) {
        if (!McaVampirismCompatConfig.enableMcaWerewolfVillagerCure()) {
            McaVampireCureService.cancelWerewolfCure(villager, "CONFIG_DISABLED");
            return;
        }
        if (!McaWerewolfStateService.isWerewolf(villager)) {
            McaVampireCureService.cancelWerewolfCure(villager, "NO_LONGER_WEREWOLF");
            return;
        }
        McaVampireCureService.neutralizeCombat(villager);
        McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
        if (McaVampirismCompatConfig.werewolfCureAppliesWeaknessDuringProcess()
                && !villager.hasEffect(MobEffects.WEAKNESS)) {
            villager.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, false), villager);
        }
        if (McaVampirismCompatConfig.werewolfCureShowsParticles()) {
            sendCureParticles(villager, gameTime);
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            if (gameTime >= state.getWerewolfCureFinishTick()) {
                McaVampireCureService.completeCure(villager, McaVampireCureService.CureType.WEREWOLF);
            }
        });
    }

    private static void sendCureParticles(VillagerEntityMCA villager, long gameTime) {
        if (gameTime % 40L != 0L || !(villager.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, villager.getX(), villager.getY() + 1.0D,
                villager.getZ(), 6, 0.35D, 0.45D, 0.35D, 0.02D);
    }
}
