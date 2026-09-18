package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.advancement.McaAdvancementService;
import com.guilh.mca_vampirism_compat.advancement.McaCompatAdvancements;
import com.guilh.mca_vampirism_compat.attribute.McaSpecialVillagerAttributeService;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.guilh.mca_vampirism_compat.visual.McaWerewolfVisualData;
import de.teamlapen.vampirism.core.ModEffects;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.Entity;

public final class McaWerewolfStateService {
    private McaWerewolfStateService() {
    }

    public static boolean isWerewolf(Entity entity) {
        if (!McaWerewolvesCompat.isAvailable()) {
            return false;
        }
        if (!(entity instanceof VillagerEntityMCA villager)) {
            return false;
        }
        boolean trait = McaTraitBridge.hasWerewolfTrait(villager);
        boolean stateMarker = ModCapabilities.get(villager)
                .map(state -> {
                    if (!state.isWerewolfConverted()
                            && state.wasCureRecentlyCompleted(
                            villager.level().getGameTime(),
                            McaVampireCureService.CureType.WEREWOLF.name(),
                            McaVampirismCompatConfig.postCureResyncBlockTicks())) {
                        return false;
                    }
                    return state.isWerewolfConverted()
                            || state.isWerewolfAppearanceApplied()
                            || state.isWerewolfPartialForm()
                            || trait;
                })
                .orElse(trait);
        return stateMarker;
    }

    public static boolean isWerewolfInfected(VillagerEntityMCA villager) {
        return McaWerewolvesCompat.isAvailable() && villager != null && ModCapabilities.get(villager)
                .map(state -> state.isWerewolfInfected())
                .orElse(false);
    }

    public static void ensureWerewolfState(VillagerEntityMCA villager) {
        if (!McaWerewolvesCompat.isAvailable()) {
            return;
        }
        normalizeWerewolfState(villager);
        ModCapabilities.get(villager).ifPresent(state -> {
            if (state.isCuringVampire() || state.isCuringWerewolf()) {
                McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
                McaVampirismCompat.debugLog(
                        "[CURE] resync blocked type=WEREWOLF villager={} reason=CURING",
                        villager.getUUID()
                );
                return;
            }
            boolean traitBefore = McaTraitBridge.hasWerewolfTrait(villager);
            boolean convertedBefore = state.isWerewolfConverted();
            boolean appearanceBefore = state.isWerewolfAppearanceApplied();
            boolean marker = convertedBefore || appearanceBefore || state.isWerewolfPartialForm() || traitBefore;
            if (!marker) {
                McaWerewolfVisualData.sync(villager);
                return;
            }
            if (!convertedBefore && state.wasCureRecentlyCompleted(
                    villager.level().getGameTime(),
                    McaVampireCureService.CureType.WEREWOLF.name(),
                    McaVampirismCompatConfig.postCureResyncBlockTicks())) {
                McaTraitBridge.removeWerewolfTrait(villager);
                state.clearWerewolfInfection();
                state.setWerewolfAppearanceApplied(false);
                state.setWerewolfPartialForm(false);
                McaWerewolfAppearanceService.removeWerewolfAppearance(villager);
                McaWerewolfConversionService.removeLupusSanguinem(villager);
                McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
                McaVampirismCompat.debugLog(
                        "[CURE] resync blocked type=WEREWOLF villager={} reason=POST_CURE_GRACE",
                        villager.getUUID()
                );
                return;
            }

            boolean canRestoreFromTrait = traitBefore && McaVampirismCompatConfig.traitCanRestoreCapability();
            if (!state.isWerewolfConverted() && (state.isWerewolfAppearanceApplied() || canRestoreFromTrait)) {
                state.finishWerewolfConversion(villager.level().getGameTime());
            }

            if (state.isWerewolfConverted()) {
                state.clearWerewolfInfection();
                state.setWerewolfAppearanceApplied(true);
                state.setWerewolfTraitApplied(true);
                state.setWerewolfPartialForm(true);
                McaTraitBridge.addWerewolfTrait(villager);
                McaWerewolfAppearanceService.applyWerewolfAppearance(villager);
                McaSpecialVillagerCombatService.registerGoalsIfNeeded(villager);
            } else {
                McaWerewolfVisualData.sync(villager);
            }

            boolean traitAfter = McaTraitBridge.hasWerewolfTrait(villager);
            if (convertedBefore != state.isWerewolfConverted()
                    || traitBefore != traitAfter
                    || appearanceBefore != state.isWerewolfAppearanceApplied()) {
                McaVampirismCompat.debugLog(
                        "[STATE] werewolf state synchronized villager={} converted={} trait={} appearance={}",
                        villager.getUUID(),
                        state.isWerewolfConverted(),
                        traitAfter,
                        state.isWerewolfAppearanceApplied()
                );
            }
        });
    }

    public static void markConverted(VillagerEntityMCA villager) {
        if (!McaWerewolvesCompat.isAvailable() || villager == null || villager.level().isClientSide) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            state.getWerewolfSourceUuid().ifPresent(playerId -> McaAdvancementService.awardOrQueue(
                    ((net.minecraft.server.level.ServerLevel) villager.level()).getServer(),
                    playerId,
                    McaCompatAdvancements.WEREWOLF_CONVERSION));
            villager.removeEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare());
            state.finishWerewolfConversion(villager.level().getGameTime());
            state.clearPendingInfection();
            state.cancelVampireCure();
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-CONVERSION] conversion completed villager={} entityType={}",
                    villager.getUUID(),
                    villager.getType()
            );
        });
        McaSpecialStateNormalizer.normalize(villager);
        ensureWerewolfState(villager);
    }

    public static void clearWerewolfState(VillagerEntityMCA villager, String reason) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        ModCapabilities.get(villager)
                .ifPresent(state -> {
                    state.cancelWerewolfCure();
                    state.clearWerewolfState();
                });
        McaTraitBridge.removeWerewolfTrait(villager);
        McaWerewolfAppearanceService.removeWerewolfAppearance(villager);
        McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
        McaVampirismCompat.debugLog(
                "[STATE] cleared werewolf state villager={} reason={}",
                villager.getUUID(),
                reason
        );
    }

    public static void normalizeWerewolfState(VillagerEntityMCA villager) {
        if (!McaWerewolvesCompat.isAvailable() || villager == null || villager.level().isClientSide) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            boolean werewolfTrait = McaTraitBridge.hasWerewolfTrait(villager);
            long gameTime = villager.level().getGameTime();
            if (McaVampireStateService.isVampire(villager) || state.isCuringVampire()) {
                boolean preserveVampireCureProgress = state.isCuringVampire()
                        || state.hasAnyCureIngredientProgress(gameTime, false);
                state.cancelWerewolfCure();
                state.clearWerewolfState();
                McaTraitBridge.removeWerewolfTrait(villager);
                logNormalize(villager, McaVampireStateService.isVampire(villager)
                        ? "WEREWOLF_VAMPIRE_CONFLICT_REMOVED_WEREWOLF"
                        : "WEREWOLF_CURE_CONFLICT_REMOVED_WEREWOLF");
                if (preserveVampireCureProgress) {
                    McaVampirismCompat.debugLog(
                            "[STATE] normalized conflict removedWerewolf preserveCureProgress type=VAMPIRE villager={}",
                            villager.getUUID()
                    );
                }
                return;
            }

            if (!state.isWerewolfConverted() && state.wasCureRecentlyCompleted(
                    gameTime,
                    McaVampireCureService.CureType.WEREWOLF.name(),
                    McaVampirismCompatConfig.postCureResyncBlockTicks())) {
                boolean changed = false;
                if (state.isWerewolfInfected() || McaWerewolfConversionService.isLupusSanguinemActive(villager)) {
                    state.clearWerewolfInfection();
                    McaWerewolfConversionService.removeLupusSanguinem(villager);
                    changed = true;
                }
                if (state.isWerewolfAppearanceApplied() || state.isWerewolfPartialForm()) {
                    state.setWerewolfAppearanceApplied(false);
                    state.setWerewolfPartialForm(false);
                    McaWerewolfAppearanceService.removeWerewolfAppearance(villager);
                    changed = true;
                }
                if (werewolfTrait) {
                    McaTraitBridge.removeWerewolfTrait(villager);
                    state.setWerewolfTraitApplied(false);
                    werewolfTrait = false;
                    changed = true;
                }
                if (changed) {
                    McaWerewolfVisualData.sync(villager);
                    McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
                    logNormalize(villager, "BLOCKED_WEREWOLF_RESYNC_POST_CURE");
                }
            }

            if (state.isCuringWerewolf() && !state.isWerewolfConverted() && !werewolfTrait) {
                state.cancelWerewolfCure();
                logNormalize(villager, "CLEARED_WEREWOLF_CURE_WITHOUT_WEREWOLF");
            }

            if (state.isWerewolfConverted()) {
                if (state.isInfected() || state.wasSanguinareActive()) {
                    villager.removeEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare());
                    state.clearPendingInfection();
                    logNormalize(villager, "CLEARED_VAMPIRE_INFECTION_ON_WEREWOLF");
                }
                if (state.isCuringVampire()) {
                    state.cancelVampireCure();
                    logNormalize(villager, "CLEARED_VAMPIRE_CURE_ON_WEREWOLF");
                }
                if (state.isHunterVillageApplied() || state.isDefensiveMode()) {
                    McaDefensiveVillagerService.clearDefense(villager, "normalized werewolf");
                    McaVillageFactionControlService.removeHunterVillageState(villager);
                    logNormalize(villager, "CLEARED_HUNTER_STATE_ON_WEREWOLF");
                }
                if (!werewolfTrait && McaVampirismCompatConfig.enableMcaWerewolfTrait()) {
                    McaTraitBridge.addWerewolfTrait(villager);
                    state.setWerewolfTraitApplied(true);
                    logNormalize(villager, "RESTORED_WEREWOLF_TRAIT_FROM_CAPABILITY");
                }
                state.setWerewolfAppearanceApplied(true);
                state.setWerewolfPartialForm(true);
                McaWerewolfAppearanceService.applyWerewolfAppearance(villager);
                return;
            }

            if (werewolfTrait) {
                boolean recentlyCured = state.wasCureRecentlyCompleted(
                        gameTime,
                        McaVampireCureService.CureType.WEREWOLF.name(),
                        McaVampirismCompatConfig.postCureResyncBlockTicks());
                if (!recentlyCured && McaVampirismCompatConfig.traitCanRestoreCapability()) {
                    state.finishWerewolfConversion(villager.level().getGameTime());
                    logNormalize(villager, "RESTORED_WEREWOLF_FROM_TRAIT");
                } else {
                    McaTraitBridge.removeWerewolfTrait(villager);
                    state.setWerewolfTraitApplied(false);
                    logNormalize(villager, recentlyCured ? "REMOVED_RECENTLY_CURED_WEREWOLF_TRAIT" : "REMOVED_ORPHAN_WEREWOLF_TRAIT");
                }
            }
        });
    }

    private static void logNormalize(VillagerEntityMCA villager, String fix) {
        McaVampirismCompat.debugLog(
                "[STATE] normalized villager={} fix={}",
                villager.getUUID(),
                fix
        );
    }
}
