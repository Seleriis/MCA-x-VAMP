package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.advancement.McaAdvancementService;
import com.guilh.mca_vampirism_compat.advancement.McaCompatAdvancements;
import com.guilh.mca_vampirism_compat.appearance.McaVampireAppearanceHandler;
import com.guilh.mca_vampirism_compat.attribute.McaVampireAttributeHandler;
import com.guilh.mca_vampirism_compat.attribute.McaSpecialVillagerAttributeService;
import com.guilh.mca_vampirism_compat.ai.McaVampireAi;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.faction.hunter.McaHunterAggressionPolicy;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import de.teamlapen.vampirism.core.ModEffects;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.VillagerLike;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public final class McaVampireStateService {
    private McaVampireStateService() {
    }

    public static boolean isMcaVillager(Entity entity) {
        return entity instanceof VillagerEntityMCA || entity instanceof VillagerLike<?>;
    }

    public static boolean isMcaVillager(LivingEntity entity) {
        return isMcaVillager((Entity) entity);
    }

    public static boolean isVampire(Entity entity) {
        if (!(entity instanceof VillagerEntityMCA villager)) {
            return false;
        }
        boolean trait = McaTraitBridge.hasVampireTrait(villager);
        return ModCapabilities.get(villager)
                .map(state -> {
                    if (!state.isConverted()
                            && state.wasCureRecentlyCompleted(
                            villager.level().getGameTime(),
                            McaVampireCureService.CureType.VAMPIRE.name(),
                            McaVampirismCompatConfig.postCureResyncBlockTicks())) {
                        return false;
                    }
                    // Appearance is derived state, never faction authority. Treating
                    // the old visual flag as vampirism made human newborns unbiteable.
                    return state.isConverted() || trait;
                })
                .orElse(trait);
    }

    public static void ensureVampireState(VillagerEntityMCA villager) {
        normalizeVillagerState(villager);
        ModCapabilities.get(villager).ifPresent(state -> {
            if (state.isCuringVampire() || state.isCuringWerewolf()) {
                McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
                McaVampirismCompat.debugLog(
                        "[CURE] resync blocked type=VAMPIRE villager={} reason=CURING",
                        villager.getUUID()
                );
                return;
            }
            boolean traitBefore = McaTraitBridge.hasVampireTrait(villager);
            boolean convertedBefore = state.isConverted();
            boolean appearanceBefore = state.isVampireAppearanceApplied();

            boolean hasVampireMarker = convertedBefore || traitBefore;
            if (!hasVampireMarker) {
                if (appearanceBefore) {
                    McaVampireAppearanceHandler.restoreOriginalAppearance(villager);
                    state.setVampireAppearanceApplied(false);
                    logNormalize(villager, "CLEARED_ORPHAN_VAMPIRE_APPEARANCE");
                }
                return;
            }
            if (!convertedBefore && state.wasCureRecentlyCompleted(
                    villager.level().getGameTime(),
                    McaVampireCureService.CureType.VAMPIRE.name(),
                    McaVampirismCompatConfig.postCureResyncBlockTicks())) {
                McaTraitBridge.removeVampireTrait(villager);
                state.setVampireAppearanceApplied(false);
                state.clearPendingInfection();
                villager.removeEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare());
                McaVampireAttributeHandler.removeVampireAttributes(villager);
                McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
                McaVampirismCompat.debugLog(
                        "[CURE] resync blocked type=VAMPIRE villager={} reason=POST_CURE_GRACE",
                        villager.getUUID()
                );
                return;
            }

            boolean canRestoreFromTrait = traitBefore && McaVampirismCompatConfig.traitCanRestoreCapability();
            if (!state.isConverted() && (state.isVampireAppearanceApplied() || canRestoreFromTrait)) {
                state.finishConversion();
            }
            if (state.isConverted()) {
                state.clearInfection();
                McaTraitBridge.addVampireTrait(villager);
                McaVampireAppearanceHandler.applyVampireAppearance(villager);
                McaVampireAttributeHandler.applyVampireAttributes(villager);
                McaVampireAi.registerGoalsIfNeeded(villager);
            }

            boolean traitAfter = McaTraitBridge.hasVampireTrait(villager);
            if (convertedBefore != state.isConverted() || traitBefore != traitAfter
                    || appearanceBefore != state.isVampireAppearanceApplied()) {
                McaVampirismCompat.debugLog(
                        "[STATE] vampire state synchronized villager={} converted={} trait={} appearance={}",
                        villager.getUUID(),
                        state.isConverted(),
                        traitAfter,
                        state.isVampireAppearanceApplied()
                );
            }
        });
    }

    public static void normalizeVillagerState(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            boolean vampireTrait = McaTraitBridge.hasVampireTrait(villager);
            boolean hunterTrait = McaTraitBridge.hasHunterTrait(villager);
            boolean converted = state.isConverted();
            boolean vampireAppearance = state.isVampireAppearanceApplied();
            long gameTime = villager.level().getGameTime();

            if (!converted && !vampireTrait && vampireAppearance) {
                McaVampireAppearanceHandler.restoreOriginalAppearance(villager);
                state.setVampireAppearanceApplied(false);
                vampireAppearance = false;
                logNormalize(villager, "CLEARED_NON_AUTHORITATIVE_VAMPIRE_APPEARANCE");
            }

            if (!converted && state.wasCureRecentlyCompleted(
                    gameTime,
                    McaVampireCureService.CureType.VAMPIRE.name(),
                    McaVampirismCompatConfig.postCureResyncBlockTicks())) {
                boolean changed = false;
                if (state.isInfected() || state.wasSanguinareActive() || villager.hasEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare())) {
                    state.clearPendingInfection();
                    villager.removeEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare());
                    changed = true;
                }
                if (vampireTrait) {
                    McaTraitBridge.removeVampireTrait(villager);
                    vampireTrait = false;
                    changed = true;
                }
                if (vampireAppearance) {
                    state.setVampireAppearanceApplied(false);
                    McaVampireAppearanceHandler.restoreOriginalAppearance(villager);
                    vampireAppearance = false;
                    changed = true;
                }
                if (changed) {
                    McaVampireAttributeHandler.removeVampireAttributes(villager);
                    logNormalize(villager, "BLOCKED_VAMPIRE_RESYNC_POST_CURE");
                }
            }

            if (!converted && !vampireTrait && !vampireAppearance && state.isCuringVampire()) {
                state.cancelVampireCure();
                logNormalize(villager, "CLEARED_CURE_ON_NON_VAMPIRE");
            }

            if (!converted && vampireTrait) {
                boolean recentlyCured = state.wasCureRecentlyCompleted(
                        gameTime,
                        McaVampireCureService.CureType.VAMPIRE.name(),
                        McaVampirismCompatConfig.postCureResyncBlockTicks());
                if (!recentlyCured && McaVampirismCompatConfig.traitCanRestoreCapability()) {
                    state.finishConversion();
                    logNormalize(villager, "RESTORED_CONVERTED_FROM_TRAIT");
                    converted = true;
                } else {
                    McaTraitBridge.removeVampireTrait(villager);
                    logNormalize(villager, recentlyCured ? "REMOVED_RECENTLY_CURED_VAMPIRE_TRAIT" : "REMOVED_ORPHAN_VAMPIRE_TRAIT");
                    vampireTrait = false;
                }
            }
            if (!converted) {
                McaVampireAttributeHandler.removeVampireAttributes(villager);
            }

            if (converted) {
                if (state.isInfected() || state.wasSanguinareActive() || villager.hasEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare())) {
                    state.clearInfection();
                    villager.removeEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare());
                    logNormalize(villager, "CLEARED_INFECTION_ON_CONVERTED");
                }
                if (!vampireTrait && McaVampirismCompatConfig.enableMcaVampireTrait()) {
                    McaTraitBridge.addVampireTrait(villager);
                    logNormalize(villager, "RESTORED_VAMPIRE_TRAIT_FROM_CAPABILITY");
                    vampireTrait = true;
                }
                if (state.isDefensiveMode()) {
                    McaDefensiveVillagerService.clearDefense(villager, "normalized converted vampire");
                    logNormalize(villager, "CLEARED_DEFENSE_ON_VAMPIRE");
                }
                if (state.isHunterAligned() || state.isHunterVillageApplied() || state.isHunterTraitApplied()) {
                    if ((state.isHunterVillageApplied() || state.isHunterTraitApplied()) && hunterTrait) {
                        McaTraitBridge.removeHunterTrait(villager);
                    }
                    state.setHunterVillageApplied(false);
                    state.setHunterTraitApplied(false);
                    state.setHunterAligned(false, Long.MIN_VALUE);
                    logNormalize(villager, "CLEARED_HUNTER_ALIGNMENT_ON_VAMPIRE");
                }
                if (state.isWerewolfConverted() || state.isWerewolfInfected()
                        || state.isWerewolfAppearanceApplied() || McaTraitBridge.hasWerewolfTrait(villager)) {
                    state.clearWerewolfState();
                    McaTraitBridge.removeWerewolfTrait(villager);
                    logNormalize(villager, "CLEARED_WEREWOLF_STATE_ON_VAMPIRE");
                }
                return;
            }

            if (state.isHunterVillageApplied()
                    && !McaVillageFactionControlService.shouldVillagerBeHunterByVillageControl(villager)) {
                McaVillageFactionControlService.removeHunterVillageState(villager);
                logNormalize(villager, "SYNCED_LOST_HUNTER_VILLAGE_STATE");
            }

            boolean defensiveExpired = state.isDefensiveMode() && gameTime > state.getDefensiveModeUntilTick();
            if (state.isDefensiveMode() && (defensiveExpired || !McaDefensiveVillagerService.canEnterDefensiveMode(villager))) {
                McaDefensiveVillagerService.clearDefense(villager, defensiveExpired ? "expired" : "invalid");
                logNormalize(villager, "CLEARED_STALE_DEFENSE");
            }

            hunterTrait = McaTraitBridge.hasHunterTrait(villager);
            if (state.isHunterTraitApplied()
                    && !state.isHunterVillageApplied()
                    && !McaHunterAggressionPolicy.canBecomeHunterAligned(
                    villager,
                    McaHunterAggressionPolicy.HunterSource.DEFENSIVE_MODE)) {
                if (hunterTrait) {
                    McaTraitBridge.removeHunterTrait(villager);
                    hunterTrait = false;
                }
                state.setHunterTraitApplied(false);
                state.setHunterAligned(false, Long.MIN_VALUE);
                McaDefensiveVillagerService.restoreDefenseWeapon(villager);
                villager.setTarget(null);
                logNormalize(villager, "CLEARED_INVALID_AUTOMATIC_HUNTER_TRAIT");
            }

            boolean activeDefensiveHunter = state.isDefensiveMode()
                    && gameTime <= state.getDefensiveModeUntilTick()
                    && McaHunterAggressionPolicy.canEnterDefensiveAggressiveMode(
                    villager,
                    McaHunterAggressionPolicy.DefensiveReason.TEMPORARY_DEFENSE);
            boolean hasHunterSource = hunterTrait || state.isHunterVillageApplied() || activeDefensiveHunter;
            if (!hasHunterSource) {
                boolean changed = false;
                if (state.isHunterAligned()) {
                    state.setHunterAligned(false, Long.MIN_VALUE);
                    changed = true;
                }
                if (state.isHunterTraitApplied()) {
                    state.setHunterTraitApplied(false);
                    changed = true;
                }
                if (villager.getTarget() != null) {
                    villager.setTarget(null);
                    changed = true;
                }
                if (state.isOriginalMainHandItemSaved()) {
                    McaDefensiveVillagerService.restoreDefenseWeapon(villager);
                    changed = true;
                }
                if (changed) {
                    logNormalize(villager, "CLEARED_STALE_HUNTER_STATE");
                }
            }
        });
    }

    public static void markConverted(VillagerEntityMCA villager, UUID source) {
        McaDefensiveVillagerService.clearDefense(villager, "converted");
        ModCapabilities.get(villager).ifPresent(state -> {
            state.finishConversion();
            if (source != null) {
                state.setSource(source);
            }
            if (villager.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                // The editor and village capture can also call markConverted;
                // only a recorded player conversion bite earns this progress.
                state.getLastVampirePlayerBiteUuid().ifPresent(playerId -> McaAdvancementService.awardOrQueue(
                        serverLevel.getServer(), playerId, McaCompatAdvancements.VAMPIRE_CONVERSION));
            }
            McaVampirismCompat.debugLog("[STATE] markConverted villager={} source={}", villager.getUUID(), source);
        });
        McaSpecialStateNormalizer.normalize(villager);
        ensureVampireState(villager);
        McaVillageVampireThreatService.recordConversionThreat(villager);
    }

    /**
     * Removes every vampire-specific marker without creating the post-cure
     * grace window. This is used by an explicit MCA editor change, which is an
     * immediate administrative transformation rather than the gameplay cure.
     */
    public static void clearVampireState(VillagerEntityMCA villager, String reason) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        McaVampireAppearanceHandler.restoreOriginalAppearance(villager);
        ModCapabilities.get(villager).ifPresent(state -> state.clearVampirismAfterCure());
        McaTraitBridge.removeVampireTrait(villager);
        villager.removeEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare());
        McaVampireAttributeHandler.removeVampireAttributes(villager);
        McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
        McaVampireNightScheduleService.process(villager);
        McaVampirismCompat.debugLog(
                "[STATE] cleared vampire state villager={} reason={}",
                villager.getUUID(),
                reason
        );
    }

    public static void markInfected(VillagerEntityMCA villager, UUID source, long gameTime) {
        ModCapabilities.get(villager).ifPresent(state -> {
            state.startInfection(gameTime, source);
            McaVampirismCompat.debugLog("[STATE] markInfected villager={} source={} tick={}", villager.getUUID(), source, gameTime);
        });
    }

    public static void syncTraitAndCapability(VillagerEntityMCA villager) {
        ensureVampireState(villager);
    }

    private static void logNormalize(VillagerEntityMCA villager, String fix) {
        McaVampirismCompat.debugLog(
                "[STATE] normalized villager={} fix={}",
                villager.getUUID(),
                fix
        );
    }
}
