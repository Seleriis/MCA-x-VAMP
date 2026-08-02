package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import de.teamlapen.vampirism.core.ModEffects;
import net.conczin.mca.entity.VillagerEntityMCA;

public final class McaSpecialStateNormalizer {
    private McaSpecialStateNormalizer() {
    }

    public static boolean normalize(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return false;
        }
        return ModCapabilities.get(villager).map(state -> {
            boolean changed = false;
            changed |= normalizePostCureWindow(villager, state);
            changed |= normalizeCuringState(villager, state);
            changed |= normalizeConvertedState(villager, state);
            changed |= normalizeDefensiveState(villager, state);
            changed |= normalizeHunterState(villager, state);
            if (changed) {
                logStart(villager, state);
                logEnd(villager, state);
            }
            return changed;
        }).orElse(false);
    }

    private static boolean normalizePostCureWindow(VillagerEntityMCA villager, VampiricVillagerState state) {
        long gameTime = villager.level().getGameTime();
        int windowTicks = McaVampirismCompatConfig.postCureResyncBlockTicks();
        boolean changed = false;

        if (state.wasCureRecentlyCompleted(gameTime, McaVampireCureService.CureType.VAMPIRE.name(), windowTicks)) {
            if (state.isConverted()) {
                state.setConverted(false);
                changed = true;
            }
            if (state.isInfected() || state.wasSanguinareActive() || villager.hasEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare())) {
                state.clearPendingInfection();
                villager.removeEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare());
                changed = true;
            }
            if (McaTraitBridge.hasVampireTrait(villager)) {
                McaTraitBridge.removeVampireTrait(villager);
                changed = true;
            }
            if (changed) {
                log(villager, "skippedDuePostCureWindow");
            }
        }

        if (state.wasCureRecentlyCompleted(gameTime, McaVampireCureService.CureType.WEREWOLF.name(), windowTicks)) {
            if (state.isWerewolfConverted()) {
                state.setWerewolfConverted(false);
                changed = true;
            }
            if (state.isWerewolfInfected()) {
                state.clearWerewolfInfection();
                changed = true;
            }
            if (state.isWerewolfTraitApplied()) {
                state.setWerewolfTraitApplied(false);
                changed = true;
            }
            if (McaTraitBridge.hasWerewolfTrait(villager)) {
                McaTraitBridge.removeWerewolfTrait(villager);
                changed = true;
            }
            if (McaWerewolfConversionService.removeLupusSanguinem(villager)) {
                changed = true;
            }
            if (changed) {
                log(villager, "skippedDuePostCureWindow");
            }
        }

        return changed;
    }

    private static boolean normalizeCuringState(VillagerEntityMCA villager, VampiricVillagerState state) {
        boolean changed = false;

        if (state.isCuringVampire() && state.isCuringWerewolf()) {
            state.cancelWerewolfCure();
            changed = true;
            log(villager, "clearedWerewolfCureOnDualCureConflict");
        }

        if (state.isCuringVampire()) {
            changed |= clearTarget(villager);
            changed |= clearInvalidDefensiveMode(villager, state);
            changed |= clearWerewolfMarkers(villager, state, "clearedWerewolfOnVampireCureConflict");
            changed |= clearAutomaticHunter(villager, state, "curingVampire");
        }

        if (state.isCuringWerewolf()) {
            changed |= clearTarget(villager);
            changed |= clearInvalidDefensiveMode(villager, state);
            changed |= clearVampireMarkers(villager, state, "clearedVampireOnWerewolfCureConflict");
            changed |= clearAutomaticHunter(villager, state, "curingWerewolf");
        }

        return changed;
    }

    private static boolean normalizeConvertedState(VillagerEntityMCA villager, VampiricVillagerState state) {
        boolean changed = false;
        boolean vampireConverted = state.isConverted();
        boolean werewolfConverted = state.isWerewolfConverted();

        if (vampireConverted && werewolfConverted) {
            log(villager, "vampire/werewolf conflict detected");
            changed |= clearWerewolfMarkers(villager, state, "clearedWerewolfOnVampireConflict");
            werewolfConverted = false;
        }

        if (vampireConverted) {
            changed |= clearWerewolfMarkers(villager, state, "clearedWerewolfOnVampireConflict");
            changed |= clearAutomaticHunter(villager, state, "vampireConverted");
            changed |= clearInvalidDefensiveMode(villager, state);
            if (state.isHunterAligned() && hasManualHunterTrait(villager, state)) {
                state.setHunterAligned(false, Long.MIN_VALUE);
                changed = true;
                log(villager, "preservedManualHunterTrait");
            }
            if (state.isInfected() || state.wasSanguinareActive() || villager.hasEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare())) {
                state.clearInfection();
                villager.removeEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare());
                changed = true;
            }
            return changed;
        }

        if (werewolfConverted) {
            changed |= clearVampireMarkers(villager, state, "clearedVampireOnWerewolfConflict");
            changed |= clearAutomaticHunter(villager, state, "werewolfConverted");
            changed |= clearInvalidDefensiveMode(villager, state);
            if (state.isHunterAligned() && hasManualHunterTrait(villager, state)) {
                state.setHunterAligned(false, Long.MIN_VALUE);
                changed = true;
                log(villager, "preservedManualHunterTrait");
            }
        }

        return changed;
    }

    private static boolean normalizeDefensiveState(VillagerEntityMCA villager, VampiricVillagerState state) {
        boolean changed = false;
        if (!state.isDefensiveMode()) {
            return false;
        }

        if (!isValidDefensiveMode(villager, state)) {
            changed |= clearInvalidDefensiveMode(villager, state);
        }

        if (!McaVampirismCompatConfig.defensiveModeGrantsHunterTrait()
                && state.isHunterTraitApplied()) {
            changed |= clearAutomaticHunter(villager, state, "defensiveTraitDisabled");
        }

        if (!McaVampirismCompatConfig.defensiveModeUsesPitchfork()
                && state.isOriginalMainHandItemSaved()) {
            McaDefensiveVillagerService.restoreDefenseWeapon(villager);
            changed = true;
            log(villager, "clearedInvalidDefensiveMode");
        }

        return changed;
    }

    private static boolean normalizeHunterState(VillagerEntityMCA villager, VampiricVillagerState state) {
        boolean validVillageHunter = state.isHunterVillageApplied()
                && McaVillageFactionControlService.shouldVillagerBeHunterByVillageControl(villager);
        boolean validDefensiveHunter = state.isDefensiveMode()
                && isValidDefensiveMode(villager, state)
                && McaVampirismCompatConfig.defensiveModeGrantsHunterTrait();
        boolean manualHunterTrait = hasManualHunterTrait(villager, state);

        if (manualHunterTrait && !validVillageHunter) {
            log(villager, "preservedManualHunterTrait");
        }

        if (validVillageHunter || validDefensiveHunter || manualHunterTrait) {
            return false;
        }

        return clearAutomaticHunter(villager, state, "outsideHunterVillage");
    }

    private static boolean clearWerewolfMarkers(VillagerEntityMCA villager, VampiricVillagerState state, String fix) {
        boolean changed = false;
        boolean preserveVampireCureProgress = state.isCuringVampire()
                || state.hasAnyCureIngredientProgress(villager.level().getGameTime(), false);
        if (state.isWerewolfConverted()
                || state.isWerewolfInfected()
                || state.isWerewolfAppearanceApplied()
                || state.isWerewolfPartialForm()
                || state.isWerewolfTraitApplied()) {
            state.clearWerewolfState();
            changed = true;
        }
        if (McaTraitBridge.hasWerewolfTrait(villager)) {
            McaTraitBridge.removeWerewolfTrait(villager);
            changed = true;
        }
        if (McaWerewolfConversionService.removeLupusSanguinem(villager)) {
            changed = true;
        }
        if (changed) {
            log(villager, fix);
            if (preserveVampireCureProgress) {
                McaVampirismCompat.debugLog(
                        "[CURE] normalizer preserved cure progress type=VAMPIRE villager={}",
                        villager.getUUID()
                );
                McaVampirismCompat.debugLog(
                        "[STATE] normalized conflict removedWerewolf preserveCureProgress type=VAMPIRE villager={}",
                        villager.getUUID()
                );
            }
        }
        return changed;
    }

    private static boolean clearVampireMarkers(VillagerEntityMCA villager, VampiricVillagerState state, String fix) {
        boolean changed = false;
        boolean preserveWerewolfCureProgress = state.isCuringWerewolf()
                || state.hasAnyCureIngredientProgress(villager.level().getGameTime(), true);
        if (state.isConverted()) {
            state.setConverted(false);
            changed = true;
        }
        if (state.isInfected() || state.wasSanguinareActive()) {
            state.clearPendingInfection();
            changed = true;
        }
        if (state.isVampireAppearanceApplied()) {
            state.setVampireAppearanceApplied(false);
            changed = true;
        }
        if (McaTraitBridge.hasVampireTrait(villager)) {
            McaTraitBridge.removeVampireTrait(villager);
            changed = true;
        }
        if (villager.hasEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare())) {
            villager.removeEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare());
            changed = true;
        }
        if (changed) {
            log(villager, fix);
            if (preserveWerewolfCureProgress) {
                McaVampirismCompat.debugLog(
                        "[CURE] normalizer preserved cure progress type=WEREWOLF villager={}",
                        villager.getUUID()
                );
            }
        }
        return changed;
    }

    private static boolean clearAutomaticHunter(VillagerEntityMCA villager, VampiricVillagerState state, String reason) {
        boolean changed = false;
        boolean hunterTrait = McaTraitBridge.hasHunterTrait(villager);
        boolean manualHunterTrait = hasManualHunterTrait(villager, state);
        boolean automaticHunter = state.isHunterAligned()
                || state.isHunterVillageApplied()
                || state.isHunterTraitApplied()
                || state.isOriginalMainHandItemSaved()
                || (hunterTrait && !manualHunterTrait);

        if (!automaticHunter) {
            if (manualHunterTrait && (state.isConverted() || state.isWerewolfConverted())) {
                log(villager, "preservedManualHunterTrait");
            }
            return false;
        }

        if (hunterTrait && !manualHunterTrait) {
            McaTraitBridge.removeHunterTrait(villager);
            changed = true;
        }
        if (state.isHunterVillageApplied()) {
            state.setHunterVillageApplied(false);
            changed = true;
        }
        if (state.isHunterTraitApplied()) {
            state.setHunterTraitApplied(false);
            changed = true;
        }
        if (state.isHunterAligned() && !manualHunterTrait) {
            state.setHunterAligned(false, Long.MIN_VALUE);
            changed = true;
        }
        if (state.isOriginalMainHandItemSaved()) {
            McaDefensiveVillagerService.restoreDefenseWeapon(villager);
            changed = true;
        }
        if (villager.getTarget() != null && !manualHunterTrait) {
            villager.setTarget(null);
            changed = true;
        }

        if (manualHunterTrait) {
            log(villager, "preservedManualHunterTrait");
        }
        if (changed) {
            log(villager, "clearedAutoHunterOutsideHunterVillage");
            McaVampirismCompat.debugLog(
                    "[STATE-NORMALIZER] clearedAutoHunterOutsideHunterVillage uuid={} reason={}",
                    villager.getUUID(),
                    reason
            );
        }
        return changed;
    }

    private static boolean clearInvalidDefensiveMode(VillagerEntityMCA villager, VampiricVillagerState state) {
        boolean changed = false;
        boolean hadInvalidDefense = state.isDefensiveMode() || state.isOriginalMainHandItemSaved();
        if (!hadInvalidDefense) {
            return false;
        }
        if (state.isDefensiveMode()) {
            state.clearDefensiveMode();
            changed = true;
        }
        if (state.isOriginalMainHandItemSaved()) {
            McaDefensiveVillagerService.restoreDefenseWeapon(villager);
            changed = true;
        }
        changed |= clearTarget(villager);
        if (changed) {
            log(villager, "clearedInvalidDefensiveMode");
        }
        return changed;
    }

    private static boolean clearTarget(VillagerEntityMCA villager) {
        if (villager.getTarget() == null) {
            return false;
        }
        villager.setTarget(null);
        return true;
    }

    private static boolean hasManualHunterTrait(VillagerEntityMCA villager, VampiricVillagerState state) {
        return McaVampirismCompatConfig.hunterVillagerKeepManualHunterTrait()
                && McaTraitBridge.hasHunterTrait(villager)
                && !state.isHunterTraitApplied()
                && !state.isHunterVillageApplied();
    }

    private static boolean isValidDefensiveMode(VillagerEntityMCA villager, VampiricVillagerState state) {
        if (!state.isDefensiveMode()) {
            return false;
        }
        if (!McaDefensiveVillagerService.canEnterDefensiveMode(villager)) {
            return false;
        }
        return !McaVampirismCompatConfig.defensiveModeOnlyInHunterVillages()
                || McaVillageFactionControlService.shouldVillagerBeHunterByVillageControl(villager);
    }

    private static void logStart(VillagerEntityMCA villager, VampiricVillagerState state) {
        McaVampirismCompat.debugLog(
                "[STATE-NORMALIZER] start uuid={} type={}",
                villager.getUUID(),
                rawType(villager, state)
        );
    }

    private static void logEnd(VillagerEntityMCA villager, VampiricVillagerState state) {
        McaVampirismCompat.debugLog(
                "[STATE-NORMALIZER] end faction={} uuid={} changed=true",
                rawType(villager, state),
                villager.getUUID()
        );
    }

    private static void log(VillagerEntityMCA villager, String fix) {
        McaVampirismCompat.debugLog(
                "[STATE-NORMALIZER] {} uuid={}",
                fix,
                villager.getUUID()
        );
    }

    private static String rawType(VillagerEntityMCA villager, VampiricVillagerState state) {
        if (state.isCuringVampire()) {
            return "CURING_VAMPIRE";
        }
        if (state.isCuringWerewolf()) {
            return "CURING_WEREWOLF";
        }
        if (state.isConverted() || state.isVampireAppearanceApplied() || McaTraitBridge.hasVampireTrait(villager)) {
            return "VAMPIRE";
        }
        if (state.isWerewolfConverted()
                || state.isWerewolfAppearanceApplied()
                || state.isWerewolfPartialForm()
                || McaTraitBridge.hasWerewolfTrait(villager)) {
            return "WEREWOLF";
        }
        if (McaTraitBridge.hasHunterTrait(villager)
                || state.isHunterAligned()
                || state.isHunterVillageApplied()
                || state.isHunterTraitApplied()) {
            return "HUNTER";
        }
        if (state.isDefensiveMode()) {
            return "DEFENSIVE";
        }
        if (state.isInfected() || state.wasSanguinareActive() || state.isWerewolfInfected()) {
            return "INFECTED";
        }
        return "HUMAN";
    }
}
