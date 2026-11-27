package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import com.guilh.mca_vampirism_compat.attribute.McaSpecialVillagerAttributeService;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.growth.McaVampireGrowthHandler;
import com.guilh.mca_vampirism_compat.monster.McaSpecialVillagerMonsterIgnoreService;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import de.teamlapen.vampirism.core.ModEffects;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.effect.MobEffectInstance;

public final class McaSpecialVillagerRuntimeService {
    private McaSpecialVillagerRuntimeService() {
    }

    public static void onJoin(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        synchronizeNow(villager);
    }

    /**
     * Reuses the safe lifecycle reconciliation after an administrator asks for
     * a repair. It deliberately derives all decisions from existing state,
     * traits and village control instead of granting a faction itself.
     */
    public static void synchronizeNow(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        McaTraitBridge.migrateLegacyTraitIds(villager);
        synchronizeState(villager);
        McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
        McaSpecialVillagerCombatService.registerGoalsIfNeeded(villager);
        McaDefensiveVillagerService.reconcileHunterWeapon(villager);
        McaVampireNightScheduleService.process(villager);
    }

    public static void onTick(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        long gameTime = villager.level().getGameTime();
        boolean syncTick = McaRuntimeTickScheduler.isScheduled(gameTime, villager.getId(), 100);
        boolean dormantInspectionTick = McaRuntimeTickScheduler.isScheduled(
                gameTime,
                villager.getId(),
                McaRuntimeTickScheduler.DORMANT_STATE_INSPECTION_INTERVAL_TICKS
        );
        VampiricVillagerState state = ModCapabilities.get(villager).orElse(null);
        boolean hasRelevantState = state != null && hasRelevantState(state);

        // Most MCA villagers are ordinary humans. Their expensive faction and
        // effect inspection is staggered, while any saved special state stays
        // on the existing per-tick path for combat, sunlight and cures.
        if (!hasRelevantState && !dormantInspectionTick) {
            return;
        }

        McaSpecialVillagerType type = McaSpecialVillagerClassifier.classify(villager);
        boolean hasSanguinare = villager.hasEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare());
        boolean hasLupusSanguinem = McaWerewolfConversionService.isLupusSanguinemActive(villager);
        if (!requiresRuntimeWork(type, hasSanguinare, hasLupusSanguinem, hasRelevantState)) {
            if (syncTick) {
                synchronizeState(villager);
                McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
            }
            return;
        }

        boolean vampireRuntime = hasSanguinare
                || type == McaSpecialVillagerType.VAMPIRE
                || hasVampireRuntimeState(state);
        boolean werewolfRuntime = hasLupusSanguinem
                || type == McaSpecialVillagerType.WEREWOLF
                || hasWerewolfRuntimeState(state);

        // Hunters do not need infection, cure, sunlight or night-schedule
        // work. Keeping those flows faction-scoped removes several full state
        // inspections from every Hunter tick while preserving immediate
        // handling for a newly applied conversion effect.
        if (vampireRuntime) {
            processVampireInfectionConsistency(villager);
            McaVampireGrowthHandler.processGrowth(villager);
            McaVampireNightScheduleService.process(villager);
            McaVampireShelterService.process(villager);
            McaVampireSunlightHandler.processSunlightDamage(villager);
        }
        if (vampireRuntime || werewolfRuntime) {
            McaVampireCureService.processCureTick(villager);
        }
        if (werewolfRuntime) {
            McaWerewolfVillagerService.processWerewolfTick(villager);
        }
        if (vampireRuntime || werewolfRuntime) {
            McaFactionDietService.tickRecovery(villager);
        }

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
        McaDefensiveVillagerService.reconcileHunterWeapon(villager);
    }

    private static boolean requiresRuntimeWork(McaSpecialVillagerType type, boolean hasSanguinare,
                                               boolean hasLupusSanguinem, boolean hasRelevantState) {
        if (hasSanguinare || hasLupusSanguinem) {
            return true;
        }
        if (type != McaSpecialVillagerType.HUMAN_NORMAL
                && type != McaSpecialVillagerType.CHILD
                && type != McaSpecialVillagerType.UNKNOWN) {
            return true;
        }
        return hasRelevantState;
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
                || state.isVampireNightScheduleApplied()
                || state.isCuringVampire()
                || state.isCuringWerewolf();
    }

    private static boolean hasVampireRuntimeState(VampiricVillagerState state) {
        return state != null && (state.isInfected()
                || state.isConverted()
                || state.wasSanguinareActive()
                || state.isVampireAppearanceApplied()
                || state.isVampireNightScheduleApplied()
                || state.isCuringVampire());
    }

    private static boolean hasWerewolfRuntimeState(VampiricVillagerState state) {
        return state != null && (state.isWerewolfConverted()
                || state.isWerewolfInfected()
                || state.isWerewolfAppearanceApplied()
                || state.isWerewolfTraitApplied()
                || state.isWerewolfPartialForm()
                || state.isCuringWerewolf());
    }

    private static void processVampireInfectionConsistency(VillagerEntityMCA villager) {
        ModCapabilities.get(villager).ifPresent(state -> {
            MobEffectInstance sanguinare = villager.getEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare());
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
