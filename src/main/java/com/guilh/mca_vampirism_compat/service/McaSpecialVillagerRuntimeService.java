package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import com.guilh.mca_vampirism_compat.attribute.McaSpecialVillagerAttributeService;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.growth.McaVampireGrowthHandler;
import com.guilh.mca_vampirism_compat.monster.McaSpecialVillagerMonsterIgnoreService;
import de.teamlapen.vampirism.core.ModEffects;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.effect.MobEffectInstance;

public final class McaSpecialVillagerRuntimeService {
    private McaSpecialVillagerRuntimeService() {
    }

    public static void onJoin(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        synchronizeState(villager);
        McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
        McaSpecialVillagerCombatService.registerGoalsIfNeeded(villager);
    }

    public static void onTick(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        long gameTime = villager.level().getGameTime();
        boolean syncTick = gameTime % 100L == Math.floorMod(villager.getId(), 100);

        McaSpecialVillagerType type = McaSpecialVillagerClassifier.classify(villager);
        boolean hasSanguinare = villager.hasEffect(ModEffects.SANGUINARE.get());
        boolean hasLupusSanguinem = McaWerewolfConversionService.isLupusSanguinemActive(villager);
        if (!requiresRuntimeWork(villager, type, hasSanguinare, hasLupusSanguinem)) {
            if (syncTick) {
                synchronizeState(villager);
                McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
            }
            return;
        }

        processVampireInfectionConsistency(villager);
        McaVampireCureService.processCureTick(villager);
        McaWerewolfVillagerService.processWerewolfTick(villager);
        McaVampireGrowthHandler.processGrowth(villager);
        McaVampireSunlightHandler.processSunlightDamage(villager);

        if (syncTick) {
            synchronizeState(villager);
            McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
        }

        McaDefensiveVillagerService.processDefenseTick(villager);
        McaSpecialVillagerMonsterIgnoreService.suppressSpecialVillagerPanicIfNeeded(villager);
        McaSpecialVillagerCombatService.processCombatTick(villager);
    }

    private static void synchronizeState(VillagerEntityMCA villager) {
        McaSpecialStateNormalizer.normalize(villager);
        McaVampireStateService.ensureVampireState(villager);
        McaWerewolfVillagerService.ensureWerewolfState(villager);
        McaVillageFactionControlService.syncHunterTraitFromVillageControl(villager);
        McaSpecialStateNormalizer.normalize(villager);
    }

    private static boolean requiresRuntimeWork(VillagerEntityMCA villager, McaSpecialVillagerType type,
                                               boolean hasSanguinare, boolean hasLupusSanguinem) {
        if (hasSanguinare || hasLupusSanguinem) {
            return true;
        }
        if (type != McaSpecialVillagerType.HUMAN_NORMAL
                && type != McaSpecialVillagerType.CHILD
                && type != McaSpecialVillagerType.UNKNOWN) {
            return true;
        }
        return villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .map(McaSpecialVillagerRuntimeService::hasRelevantState)
                .orElse(false);
    }

    private static boolean hasRelevantState(VampiricVillagerState state) {
        return state.isInfected()
                || state.isConverted()
                || state.wasSanguinareActive()
                || state.isDefensiveMode()
                || state.isHunterAligned()
                || state.isHunterVillageApplied()
                || state.isHunterTraitApplied()
                || state.isWerewolfConverted()
                || state.isWerewolfInfected()
                || state.isWerewolfAppearanceApplied()
                || state.isWerewolfTraitApplied()
                || state.isWerewolfPartialForm()
                || state.isOriginalMainHandItemSaved()
                || state.isOriginalAppearanceSaved()
                || state.isVampireAppearanceApplied()
                || state.isCuringVampire()
                || state.isCuringWerewolf();
    }

    private static void processVampireInfectionConsistency(VillagerEntityMCA villager) {
        villager.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
            MobEffectInstance sanguinare = villager.getEffect(ModEffects.SANGUINARE.get());
            if (sanguinare != null && !McaVampireStateService.isVampire(villager)) {
                if (McaHunterBloodService.isHunterVampirismImmune(villager)) {
                    McaHunterBloodService.clearHunterInfectionState(villager);
                    McaVampirismCompat.debugLog(
                            "[HUNTER-BLOOD] vampire infection blocked target={} reason=MCA_HUNTER_IMMUNE",
                            villager.getUUID()
                    );
                    return;
                }
                if (!McaVampireBiteService.canMcaVillagerReceiveInfection(villager)) {
                    state.clearInfection();
                    return;
                }
                if (!state.isInfected()) {
                    state.startInfection(villager.level().getGameTime(), state.getSource().orElse(null));
                }
                state.setSanguinareActive(true);
                return;
            }

            state.keepConsistent(sanguinare != null);
            state.clearRetaliationIfExpired(villager.level().getGameTime());
        });
    }
}
