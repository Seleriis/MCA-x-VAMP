package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.advancement.McaAdvancementService;
import com.guilh.mca_vampirism_compat.advancement.McaCompatAdvancements;
import com.guilh.mca_vampirism_compat.mission.OccultVillageMissionService;
import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import com.guilh.mca_vampirism_compat.appearance.McaVampireAppearanceHandler;
import com.guilh.mca_vampirism_compat.attribute.McaSpecialVillagerAttributeService;
import com.guilh.mca_vampirism_compat.attribute.McaVampireAttributeHandler;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.social.McaSocialActionType;
import com.guilh.mca_vampirism_compat.social.McaFactionContextualDialogueService;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.guilh.mca_vampirism_compat.trade.McaVampireSpecialTradeService;
import com.guilh.mca_vampirism_compat.util.McaCompatMessages;
import de.teamlapen.vampirism.core.ModEffects;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

public final class McaVampireCureService {
    private static final ResourceLocation GARLIC_INJECTION_ID =
            new ResourceLocation("vampirism", "injection_garlic");
    private static final String WEAKNESS_ITEM = "minecraft:potion";
    private static final String GOLDEN_APPLE_ITEM = "minecraft:golden_apple";
    private static final String GARLIC_INJECTION_ITEM = "vampirism:injection_garlic";

    private McaVampireCureService() {
    }

    public static boolean handleEntityInteract(PlayerInteractEvent.EntityInteract event) {
        return McaCureInteractionService.handleEntityInteract(event);
    }

    public static InteractionResult handleDirectInteraction(Player player, VillagerEntityMCA villager, InteractionHand hand) {
        return McaCureInteractionService.handleDirectInteraction(player, villager, hand);
    }

    public static boolean handleGiftCommand(ServerPlayer player, VillagerEntityMCA villager) {
        return McaCureInteractionService.handleGiftCommand(player, villager);
    }

    public static boolean isCuring(Entity entity) {
        return entity instanceof VillagerEntityMCA villager && isCuring(villager);
    }

    public static boolean isCuring(VillagerEntityMCA villager) {
        return McaSpecialVillagerClassifier.isCuring(villager);
    }

    public static boolean isCuringVampire(VillagerEntityMCA villager) {
        return McaSpecialVillagerClassifier.classifyCure(villager) == McaSpecialVillagerType.CURING_VAMPIRE;
    }

    public static boolean isCuringWerewolf(VillagerEntityMCA villager) {
        return McaSpecialVillagerClassifier.classifyCure(villager) == McaSpecialVillagerType.CURING_WEREWOLF;
    }

    public static boolean shouldBlockTrade(VillagerEntityMCA villager) {
        return (McaVampirismCompatConfig.vampireCureVillagerCannotTrade() && isCuringVampire(villager))
                || (McaVampirismCompatConfig.werewolfCureVillagerCannotTrade() && isCuringWerewolf(villager));
    }

    public static boolean shouldBlockSocialAction(VillagerEntityMCA villager, McaSocialActionType actionType) {
        return isCuring(villager) && actionType != null && actionType.isStrongPositiveAction();
    }

    public static boolean shouldBlockCuringAttack(Entity source) {
        return source instanceof VillagerEntityMCA villager
                && ((McaVampirismCompatConfig.vampireCureVillagerCannotAttack() && isCuringVampire(villager))
                || (McaVampirismCompatConfig.werewolfCureVillagerCannotAttack() && isCuringWerewolf(villager)));
    }

    public static void processCureTick(VillagerEntityMCA villager) {
        McaCureLifecycleService.processCureTick(villager);
    }

    public static void cancelCure(VillagerEntityMCA villager, String reason) {
        ModCapabilities.get(villager).ifPresent(state -> {
            if (!state.isCuringVampire()) {
                return;
            }
            state.cancelVampireCure();
            McaVampirismCompat.debugLog(
                    "[CURE] vampire cure canceled villager={} reason={}",
                    villager.getUUID(),
                    reason
            );
        });
    }

    public static void cancelWerewolfCure(VillagerEntityMCA villager, String reason) {
        ModCapabilities.get(villager).ifPresent(state -> {
            if (!state.isCuringWerewolf()) {
                return;
            }
            state.cancelWerewolfCure();
            McaVampirismCompat.debugLog(
                    "[CURE] werewolf cure canceled villager={} reason={}",
                    villager.getUUID(),
                    reason
            );
        });
    }

    public static boolean startVillageCaptureCure(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide || !McaVampireStateService.isVampire(villager)) {
            return false;
        }
        return ModCapabilities.get(villager).map(state -> {
            if (state.isCuringVampire()) {
                return true;
            }
            long gameTime = villager.level().getGameTime();
            state.startVampireCure(gameTime, McaVampirismCompatConfig.vampireCureDurationTicks(), null);
            neutralizeCombat(villager);
            McaVampirismCompat.debugLog(
                    "[CURE] vampire cure started villager={} finishTick={} reason=HUNTER_VILLAGE_CAPTURE",
                    villager.getUUID(),
                    state.getCureFinishTick()
            );
            return true;
        }).orElse(false);
    }

    public static void completeCure(VillagerEntityMCA villager) {
        completeCure(villager, CureType.VAMPIRE);
    }

    public static void completeCure(VillagerEntityMCA villager, CureType type) {
        if (villager == null || villager.level().isClientSide || type == null || type == CureType.NONE) {
            return;
        }
        completeCureInternal(villager, type);
    }

    public static boolean isInPostCureGracePeriod(VillagerEntityMCA villager, CureType type) {
        if (villager == null || villager.level().isClientSide || type == null || type == CureType.NONE) {
            return false;
        }
        long gameTime = villager.level().getGameTime();
        int windowTicks = McaVampirismCompatConfig.postCureResyncBlockTicks();
        return ModCapabilities.get(villager)
                .map(state -> state.wasCureRecentlyCompleted(gameTime, type.name(), windowTicks))
                .orElse(false);
    }

    static boolean handleHeldCureItem(Player player, VillagerEntityMCA villager, InteractionHand hand, ItemStack stack) {
        if (stack.is(Items.MILK_BUCKET)) {
            return handleMilk(player, villager, hand, stack);
        }
        if (!canUseCureRitual(villager)) {
            return false;
        }
        if (isWeaknessPotion(stack)) {
            return handleWeaknessPotion(player, villager, hand, stack);
        }
        if (stack.is(Items.GOLDEN_APPLE)) {
            return handleCureIngredient(player, villager, hand, stack, CureIngredient.GOLDEN_APPLE);
        }
        if (isGarlicInjection(stack)) {
            return handleCureIngredient(player, villager, hand, stack, CureIngredient.GARLIC_INJECTION);
        }
        return false;
    }

    private static boolean handleMilk(Player player, VillagerEntityMCA villager, InteractionHand hand, ItemStack stack) {
        if (!McaVampirismCompatConfig.allowMilkToTreatMcaVillagerSanguinare()) {
            return false;
        }
        if (McaVampireStateService.isVampire(villager)) {
            if (McaVampirismCompatConfig.milkTreatmentOnlyBeforeConversion()) {
                player.displayClientMessage(Component.translatable(McaCompatMessages.CURE_MILK_NOT_ENOUGH), true);
                return true;
            }
            return false;
        }
        if (!isInfectedBeforeConversion(villager)) {
            return false;
        }
        boolean removedSanguinare = villager.removeEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare());
        if (McaVampirismCompatConfig.milkTreatmentRemovesNegativeEffects()) {
            removeNegativeEffects(villager);
        }
        ModCapabilities.get(villager)
                .ifPresent(state -> state.clearPendingInfection());
        if (player instanceof ServerPlayer serverPlayer) {
            McaAdvancementService.award(serverPlayer, McaCompatAdvancements.EARLY_INFECTION_TREATMENT);
            OccultVillageMissionService.onMilkTreatment(serverPlayer, villager);
        }
        if (McaVampirismCompatConfig.milkTreatmentConsumesMilk()) {
            ItemStack returned = McaVampirismCompatConfig.milkTreatmentReturnsBucket()
                    ? new ItemStack(Items.BUCKET)
                    : ItemStack.EMPTY;
            consumeOne(player, hand, stack, returned);
        }
        player.displayClientMessage(
                Component.translatable(McaCompatMessages.CURE_INFECTION_TREATED, villager.getName().getString()),
                true
        );
        McaVampirismCompat.debugLog(
                "[CURE] infection treated villager={} item=minecraft:milk_bucket removedSanguinare={}",
                villager.getUUID(),
                removedSanguinare
        );
        return true;
    }

    /** True only while a milk bucket can still stop a pending Sanguinare infection. */
    public static boolean canTreatInitialInfection(VillagerEntityMCA villager) {
        return villager != null
                && McaVampirismCompatConfig.allowMilkToTreatMcaVillagerSanguinare()
                && !McaVampireStateService.isVampire(villager)
                && isInfectedBeforeConversion(villager);
    }

    private static boolean handleWeaknessPotion(Player player, VillagerEntityMCA villager, InteractionHand hand, ItemStack stack) {
        CureType type = curableType(villager);
        if (type == CureType.NONE) {
            return false;
        }
        if (isCuring(villager)) {
            logAlreadyCuring(villager, type);
            player.displayClientMessage(Component.translatable(cureInProgressMessage(villager)), true);
            return true;
        }
        Optional<MobEffectInstance> weakness = weaknessFromPotion(stack);
        if (weakness.isEmpty()) {
            return false;
        }
        if (!isWeaknessDurationAccepted(villager, weakness.get().getDuration())) {
            player.displayClientMessage(Component.translatable(McaCompatMessages.CURE_WEAKNESS_REQUIRED), true);
            return true;
        }
        villager.addEffect(new MobEffectInstance(weakness.get()), player);
        long gameTime = villager.level().getGameTime();
        ModCapabilities.get(villager)
                .ifPresent(state -> state.markCureWeakness(
                        gameTime,
                        cureIngredientProgressTicks(villager),
                        player.getUUID(),
                        type == CureType.WEREWOLF
                ));
        consumeOne(player, hand, stack, stack.is(Items.POTION) ? new ItemStack(Items.GLASS_BOTTLE) : ItemStack.EMPTY);
        logIngredientProgress(villager, type, CureIngredient.WEAKNESS);
        tryStartCure(player, villager);
        return true;
    }

    private static boolean handleCureIngredient(Player player, VillagerEntityMCA villager, InteractionHand hand,
                                                ItemStack stack, CureIngredient ingredient) {
        CureType type = curableType(villager);
        if (type == CureType.NONE) {
            return false;
        }
        if (isCuring(villager)) {
            logAlreadyCuring(villager, type);
            player.displayClientMessage(Component.translatable(cureInProgressMessage(villager)), true);
            return true;
        }
        boolean consumed = ModCapabilities.get(villager).map(state -> {
            long gameTime = villager.level().getGameTime();
            long progressTicks = cureIngredientProgressTicks(villager);
            boolean werewolf = type == CureType.WEREWOLF;
            if (ingredient == CureIngredient.GOLDEN_APPLE) {
                if (state.hasCureGoldenApple(gameTime, werewolf)) {
                    return false;
                }
                state.markCureGoldenApple(gameTime, progressTicks, player.getUUID(), werewolf);
                return true;
            }
            if (state.hasCureGarlicInjection(gameTime, werewolf)) {
                return false;
            }
            state.markCureGarlicInjection(gameTime, progressTicks, player.getUUID(), werewolf);
            return true;
        }).orElse(false);
        if (consumed) {
            consumeOne(player, hand, stack, ItemStack.EMPTY);
            if (ingredient == CureIngredient.GARLIC_INJECTION && player instanceof ServerPlayer serverPlayer) {
                villager.getVillagerBrain().rewardHearts(serverPlayer, -10);
                player.displayClientMessage(
                        Component.translatable(McaCompatMessages.CURE_GARLIC_RELATION_LOSS, 10),
                        true
                );
            }
        }
        logIngredientProgress(villager, type, ingredient);
        tryStartCure(player, villager);
        return true;
    }

    private static boolean tryStartCure(Player player, VillagerEntityMCA villager) {
        CureType type = curableType(villager);
        if (type == CureType.NONE) {
            return false;
        }
        long gameTime = villager.level().getGameTime();
        return ModCapabilities.get(villager).map(state -> {
            if (state.isCuringVampire() || state.isCuringWerewolf()) {
                logAlreadyCuring(villager, type);
                return true;
            }
            if (!hasAllCureIngredients(villager, state, gameTime, type)) {
                List<String> missing = missingIngredients(villager, state, gameTime, type);
                logWaitingMissing(villager, type, missing);
                McaVampirismCompat.debugLog(
                        "[CURE] start denied type={} reason=MISSING_INGREDIENTS missing={} villager={}",
                        type,
                        missing,
                        villager.getUUID()
                );
                return false;
            }
            logAllIngredientsCollected(villager, type);
            if (type == CureType.WEREWOLF) {
                state.startWerewolfCure(gameTime, McaVampirismCompatConfig.werewolfCureDurationTicks(), player.getUUID());
            } else {
                state.startVampireCure(gameTime, McaVampirismCompatConfig.vampireCureDurationTicks(), player.getUUID());
            }
            neutralizeCombat(villager);
            player.displayClientMessage(
                    Component.translatable(type == CureType.WEREWOLF ? McaCompatMessages.WEREWOLF_CURE_STARTED : McaCompatMessages.CURE_STARTED,
                            villager.getName().getString()),
                    true
            );
            if (player instanceof ServerPlayer serverPlayer) {
                McaFactionContextualDialogueService.onCureStarted(serverPlayer, villager, type == CureType.WEREWOLF);
            }
            McaVampirismCompat.debugLog(
                    "[CURE] started villager={} type={} finishTick={}",
                    villager.getUUID(),
                    type,
                    type == CureType.WEREWOLF ? state.getWerewolfCureFinishTick() : state.getCureFinishTick()
            );
            return true;
        }).orElse(false);
    }

    private static void completeCureInternal(VillagerEntityMCA villager, CureType type) {
        Optional<UUID> initiator = cureInitiator(villager, type);
        if (villager.level() instanceof ServerLevel level) {
            initiator.ifPresent(playerId -> McaAdvancementService.awardOrQueue(
                    level.getServer(), playerId, McaCompatAdvancements.COMPLETE_CURE));
        }
        boolean hadVisual = hadCureVisual(villager, type);
        McaVampirismCompat.debugLog(
                "[CURE] completing type={} villager={}",
                type,
                villager.getUUID()
        );
        if (type == CureType.WEREWOLF) {
            cleanupWerewolfCureState(villager);
        } else {
            cleanupVampireCureState(villager);
        }
        forceStopCombat(villager);
        McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
        McaVillageFactionControlService.syncHunterTraitFromVillageControl(villager);
        McaSpecialStateNormalizer.normalize(villager);
        if (type == CureType.WEREWOLF) {
            notifyWerewolfCureCompleted(villager, initiator);
        } else {
            notifyCureCompleted(villager, initiator);
        }
        postCureValidation(villager, type, true);
        McaVampirismCompat.debugLog(
                "[CURE] completed type={} villager={} removedTrait={} removedVisual={}",
                type,
                villager.getUUID(),
                type == CureType.WEREWOLF ? !McaTraitBridge.hasWerewolfTrait(villager) : !McaTraitBridge.hasVampireTrait(villager),
                hadVisual && !hasCureVisual(villager, type)
        );
    }

    private static Optional<UUID> cureInitiator(VillagerEntityMCA villager, CureType type) {
        return ModCapabilities.get(villager)
                .map(state -> type == CureType.WEREWOLF
                        ? state.getWerewolfCureInitiatorUuid()
                        : state.getCureInitiatorUuid())
                .orElse(Optional.empty());
    }

    private static boolean hadCureVisual(VillagerEntityMCA villager, CureType type) {
        return ModCapabilities.get(villager)
                .map(state -> type == CureType.WEREWOLF
                        ? state.isWerewolfAppearanceApplied() || state.isWerewolfPartialForm()
                        : state.isOriginalAppearanceSaved() || state.isVampireAppearanceApplied())
                .orElse(false);
    }

    private static boolean hasCureVisual(VillagerEntityMCA villager, CureType type) {
        return ModCapabilities.get(villager)
                .map(state -> type == CureType.WEREWOLF
                        ? state.isWerewolfAppearanceApplied() || state.isWerewolfPartialForm()
                        : state.isVampireAppearanceApplied())
                .orElse(false);
    }

    private static void cleanupVampireCureState(VillagerEntityMCA villager) {
        villager.removeEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare());
        villager.removeEffect(MobEffects.WEAKNESS);
        McaVampireSpecialTradeService.removeForNoLongerVampire(villager);
        ModCapabilities.get(villager)
                .ifPresent(state -> {
                    state.clearVampirismAfterCure();
                    state.markCureCompleted(villager.level().getGameTime(), CureType.VAMPIRE.name());
                });
        McaTraitBridge.removeVampireTrait(villager);
        McaVampireAppearanceHandler.restoreOriginalAppearance(villager);
        McaVampireAttributeHandler.removeVampireAttributes(villager);
    }

    private static void cleanupWerewolfCureState(VillagerEntityMCA villager) {
        McaWerewolfConversionService.removeLupusSanguinem(villager);
        villager.removeEffect(MobEffects.WEAKNESS);
        ModCapabilities.get(villager)
                .ifPresent(state -> {
                    state.clearWerewolfAfterCure();
                    state.markCureCompleted(villager.level().getGameTime(), CureType.WEREWOLF.name());
                });
        McaTraitBridge.removeWerewolfTrait(villager);
        McaWerewolfAppearanceService.removeWerewolfAppearance(villager);
        com.guilh.mca_vampirism_compat.visual.McaWerewolfVisualData.sync(villager);
    }

    private static void postCureValidation(VillagerEntityMCA villager, CureType type, boolean retry) {
        boolean failed = type == CureType.WEREWOLF ? postWerewolfCureFailed(villager) : postVampireCureFailed(villager);
        McaSpecialVillagerType classifier = McaSpecialVillagerClassifier.classify(villager);
        McaVampirismCompat.debugLog(
                "[CURE] post-check type={} villager={} failed={} classifier={}",
                type,
                villager.getUUID(),
                failed,
                classifier
        );
        if (!failed) {
            return;
        }
        McaVampirismCompat.LOGGER.warn(
                "[CURE] post-check failed type={} villager={} retry={}",
                type,
                villager.getUUID(),
                retry
        );
        if (!retry) {
            return;
        }
        if (type == CureType.WEREWOLF) {
            cleanupWerewolfCureState(villager);
        } else {
            cleanupVampireCureState(villager);
        }
        forceStopCombat(villager);
        McaSpecialVillagerAttributeService.applyMovementSpeedForState(villager);
        postCureValidation(villager, type, false);
    }

    private static boolean postVampireCureFailed(VillagerEntityMCA villager) {
        boolean rawStateFailed = ModCapabilities.get(villager)
                .map(state -> state.isConverted()
                        || state.isInfected()
                        || state.wasSanguinareActive()
                        || state.isCuringVampire()
                        || state.isVampireAppearanceApplied())
                .orElse(false);
        return rawStateFailed
                || villager.hasEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare())
                || McaTraitBridge.hasVampireTrait(villager)
                || McaSpecialVillagerClassifier.classify(villager) == McaSpecialVillagerType.VAMPIRE;
    }

    private static boolean postWerewolfCureFailed(VillagerEntityMCA villager) {
        boolean rawStateFailed = ModCapabilities.get(villager)
                .map(state -> state.isWerewolfConverted()
                        || state.isWerewolfInfected()
                        || state.isCuringWerewolf()
                        || state.isWerewolfAppearanceApplied()
                        || state.isWerewolfPartialForm())
                .orElse(false);
        return rawStateFailed
                || McaWerewolfConversionService.isLupusSanguinemActive(villager)
                || McaTraitBridge.hasWerewolfTrait(villager)
                || McaSpecialVillagerClassifier.classify(villager) == McaSpecialVillagerType.WEREWOLF;
    }

    private static boolean isInfectedBeforeConversion(VillagerEntityMCA villager) {
        if (McaVampireStateService.isVampire(villager)) {
            return false;
        }
        return villager.hasEffect(com.guilh.mca_vampirism_compat.util.VampirismEffectCompat.sanguinare())
                || ModCapabilities.get(villager)
                .map(state -> state.isInfected())
                .orElse(false);
    }

    private static boolean hasRequiredWeakness(VillagerEntityMCA villager) {
        CureType type = curableType(villager);
        if (type == CureType.NONE) {
            return false;
        }
        long gameTime = villager.level().getGameTime();
        return ModCapabilities.get(villager)
                .map(state -> state.hasCureWeakness(gameTime, type == CureType.WEREWOLF))
                .orElse(false)
                || hasActiveRequiredWeakness(villager, type);
    }

    private static CureType curableType(VillagerEntityMCA villager) {
        if (villager == null) {
            return CureType.NONE;
        }
        return ModCapabilities.get(villager)
                .map(state -> {
                    if (state.isConverted()) {
                        return CureType.VAMPIRE;
                    }
                    if (state.isWerewolfConverted()) {
                        return CureType.WEREWOLF;
                    }
                    return CureType.NONE;
                })
                .orElse(CureType.NONE);
    }

    private static boolean hasAllCureIngredients(VillagerEntityMCA villager, VampiricVillagerState state,
                                                 long gameTime, CureType type) {
        return hasCollectedWeakness(villager, state, gameTime, type)
                && state.hasCureGoldenApple(gameTime, type == CureType.WEREWOLF)
                && state.hasCureGarlicInjection(gameTime, type == CureType.WEREWOLF);
    }

    private static boolean hasCollectedWeakness(VillagerEntityMCA villager, VampiricVillagerState state,
                                                long gameTime, CureType type) {
        return state.hasCureWeakness(gameTime, type == CureType.WEREWOLF) || hasActiveRequiredWeakness(villager, type);
    }

    private static int cureProgress(VillagerEntityMCA villager, VampiricVillagerState state, long gameTime, CureType type) {
        int progress = 0;
        if (hasCollectedWeakness(villager, state, gameTime, type)) {
            progress++;
        }
        boolean werewolf = type == CureType.WEREWOLF;
        if (state.hasCureGoldenApple(gameTime, werewolf)) {
            progress++;
        }
        if (state.hasCureGarlicInjection(gameTime, werewolf)) {
            progress++;
        }
        return progress;
    }

    private static List<String> missingIngredients(VillagerEntityMCA villager, VampiricVillagerState state,
                                                   long gameTime, CureType type) {
        List<String> missing = new ArrayList<>();
        if (!hasCollectedWeakness(villager, state, gameTime, type)) {
            missing.add(CureIngredient.WEAKNESS.name());
        }
        boolean werewolf = type == CureType.WEREWOLF;
        if (!state.hasCureGoldenApple(gameTime, werewolf)) {
            missing.add(CureIngredient.GOLDEN_APPLE.name());
        }
        if (!state.hasCureGarlicInjection(gameTime, werewolf)) {
            missing.add(CureIngredient.GARLIC_INJECTION.name());
        }
        return missing;
    }

    private static boolean hasActiveRequiredWeakness(VillagerEntityMCA villager) {
        return hasActiveRequiredWeakness(villager, curableType(villager));
    }

    private static boolean hasActiveRequiredWeakness(VillagerEntityMCA villager, CureType type) {
        if (type == null || type == CureType.NONE) {
            return false;
        }
        MobEffectInstance weakness = villager.getEffect(MobEffects.WEAKNESS);
        return weakness != null && isWeaknessDurationAccepted(type, weakness.getDuration());
    }

    private static boolean isWeaknessDurationAccepted(VillagerEntityMCA villager, int duration) {
        return isWeaknessDurationAccepted(curableType(villager), duration);
    }

    private static boolean isWeaknessDurationAccepted(CureType type, int duration) {
        if (type == null || type == CureType.NONE) {
            return false;
        }
        boolean werewolf = type == CureType.WEREWOLF;
        return (werewolf
                ? McaVampirismCompatConfig.werewolfCureAllowAnyWeaknessDuration()
                : McaVampirismCompatConfig.vampireCureAllowAnyWeaknessDuration())
                || duration >= (werewolf
                ? McaVampirismCompatConfig.werewolfCureRequiredWeaknessDurationTicks()
                : McaVampirismCompatConfig.vampireCureRequiredWeaknessDurationTicks());
    }

    private static boolean canUseCureRitual(VillagerEntityMCA villager) {
        CureType type = curableType(villager);
        return (type == CureType.VAMPIRE && McaVampirismCompatConfig.enableMcaVampireVillagerCure())
                || (type == CureType.WEREWOLF && McaVampirismCompatConfig.enableMcaWerewolfVillagerCure());
    }

    private static boolean isCurableConverted(VillagerEntityMCA villager) {
        return curableType(villager) != CureType.NONE;
    }

    private static boolean requiresWeakness(boolean werewolf) {
        return werewolf ? McaVampirismCompatConfig.werewolfCureRequiresWeakness() : McaVampirismCompatConfig.vampireCureRequiresWeakness();
    }

    private static boolean requiresGoldenApple(boolean werewolf) {
        return werewolf ? McaVampirismCompatConfig.werewolfCureRequiresGoldenApple() : McaVampirismCompatConfig.vampireCureRequiresGoldenApple();
    }

    private static boolean requiresGarlicInjection(boolean werewolf) {
        return werewolf ? McaVampirismCompatConfig.werewolfCureRequiresGarlicInjection() : McaVampirismCompatConfig.vampireCureRequiresGarlicInjection();
    }

    private static int cureIngredientProgressTicks(VillagerEntityMCA villager) {
        return curableType(villager) == CureType.WEREWOLF
                ? McaVampirismCompatConfig.werewolfCureIngredientProgressExpiresTicks()
                : McaVampirismCompatConfig.vampireCureIngredientProgressExpiresTicks();
    }

    private static boolean isWeaknessPotion(ItemStack stack) {
        return (stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION))
                && weaknessFromPotion(stack).isPresent();
    }

    private static Optional<MobEffectInstance> weaknessFromPotion(ItemStack stack) {
        return PotionUtils.getMobEffects(stack).stream()
                .filter(effect -> effect.getEffect() == MobEffects.WEAKNESS)
                .findFirst();
    }

    private static boolean isGarlicInjection(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return GARLIC_INJECTION_ID.equals(id);
    }

    private static void removeNegativeEffects(LivingEntity entity) {
        List<MobEffect> toRemove = new ArrayList<>();
        for (MobEffectInstance effect : entity.getActiveEffects()) {
            if (!effect.getEffect().isBeneficial()) {
                toRemove.add(effect.getEffect());
            }
        }
        for (MobEffect effect : toRemove) {
            entity.removeEffect(effect);
        }
    }

    private static void consumeOne(Player player, InteractionHand hand, ItemStack stack, ItemStack returnedStack) {
        if (player.getAbilities().instabuild) {
            return;
        }
        stack.shrink(1);
        if (!returnedStack.isEmpty()) {
            if (stack.isEmpty()) {
                player.setItemInHand(hand, returnedStack);
            } else if (!player.getInventory().add(returnedStack)) {
                player.drop(returnedStack, false);
            }
        } else if (stack.isEmpty()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }
    }

    private static void forceStopCombat(VillagerEntityMCA villager) {
        Mob mob = villager;
        mob.setTarget(null);
        mob.getNavigation().stop();
        ModCapabilities.get(villager)
                .ifPresent(state -> state.clearRetaliation());
    }

    static void neutralizeCombat(VillagerEntityMCA villager) {
        boolean blockAttack = (isCuringVampire(villager) && McaVampirismCompatConfig.vampireCureVillagerCannotAttack())
                || (isCuringWerewolf(villager) && McaVampirismCompatConfig.werewolfCureVillagerCannotAttack())
                || (!isCuring(villager) && McaVampirismCompatConfig.vampireCureVillagerCannotAttack());
        if (!blockAttack) {
            return;
        }
        Mob mob = villager;
        mob.setTarget(null);
        mob.getNavigation().stop();
    }

    public enum CureType {
        VAMPIRE,
        WEREWOLF,
        NONE
    }

    private static void notifyCureCompleted(VillagerEntityMCA villager, Optional<UUID> initiator) {
        if (!(villager.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        initiator.map(uuid -> serverLevel.getServer().getPlayerList().getPlayer(uuid))
                .ifPresent(player -> {
                    player.displayClientMessage(
                            Component.translatable(McaCompatMessages.CURE_COMPLETED, villager.getName().getString()),
                            true
                    );
                    McaFactionContextualDialogueService.onCureCompleted(player, villager, false);
                });
    }

    private static void notifyWerewolfCureCompleted(VillagerEntityMCA villager, Optional<UUID> initiator) {
        if (!(villager.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        initiator.map(uuid -> serverLevel.getServer().getPlayerList().getPlayer(uuid))
                .ifPresent(player -> {
                    player.displayClientMessage(
                            Component.translatable(McaCompatMessages.WEREWOLF_CURE_COMPLETED, villager.getName().getString()),
                            true
                    );
                    McaFactionContextualDialogueService.onCureCompleted(player, villager, true);
                });
    }

    static String cureInProgressMessage(VillagerEntityMCA villager) {
        return isCuringWerewolf(villager) ? McaCompatMessages.WEREWOLF_CURE_IN_PROGRESS : McaCompatMessages.CURE_IN_PROGRESS;
    }

    private static void logIngredientProgress(VillagerEntityMCA villager, CureType type, CureIngredient ingredient) {
        long gameTime = villager.level().getGameTime();
        ModCapabilities.get(villager).ifPresent(state -> {
            boolean werewolf = type == CureType.WEREWOLF;
            int progress = cureProgress(villager, state, gameTime, type);
            List<String> missing = missingIngredients(villager, state, gameTime, type);
            boolean hasWeakness = hasCollectedWeakness(villager, state, gameTime, type);
            boolean hasApple = state.hasCureGoldenApple(gameTime, werewolf);
            boolean hasGarlic = state.hasCureGarlicInjection(gameTime, werewolf);
            McaVampirismCompat.debugLog(
                    "[CURE] ingredient progress villager={} type={} ingredient={} progress={}/3",
                    villager.getUUID(),
                    type,
                    ingredient.name(),
                    progress
            );
            McaVampirismCompat.debugLog(
                    "[CURE] ingredient accepted type={} ingredient={} hasWeakness={} hasApple={} hasGarlic={} progress={}/3 missing={} villager={}",
                    type,
                    ingredient.name(),
                    hasWeakness,
                    hasApple,
                    hasGarlic,
                    progress,
                    missing,
                    villager.getUUID()
            );
            McaVampirismCompat.debugLog(
                    "[CURE] ingredient state type={} weakness={} apple={} garlic={} villager={}",
                    type,
                    hasWeakness,
                    hasApple,
                    hasGarlic,
                    villager.getUUID()
            );
        });
    }

    private static void logWaitingMissing(VillagerEntityMCA villager, CureType type, List<String> missing) {
        McaVampirismCompat.debugLog(
                "[CURE] waiting missing={} villager={} type={}",
                missing,
                villager.getUUID(),
                type
        );
        McaVampirismCompat.debugLog(
                "[CURE] missing type={} missing={} villager={}",
                type,
                missing,
                villager.getUUID()
        );
    }

    private static void logAllIngredientsCollected(VillagerEntityMCA villager, CureType type) {
        McaVampirismCompat.debugLog(
                "[CURE] all ingredients collected villager={} type={} startingCure=true",
                villager.getUUID(),
                type
        );
    }

    private static void logAlreadyCuring(VillagerEntityMCA villager, CureType type) {
        McaVampirismCompat.debugLog(
                "[CURE] alreadyCuring villager={} type={}",
                villager.getUUID(),
                type
        );
    }

    private enum CureIngredient {
        WEAKNESS(WEAKNESS_ITEM),
        GOLDEN_APPLE(GOLDEN_APPLE_ITEM),
        GARLIC_INJECTION(GARLIC_INJECTION_ITEM);

        private final String itemId;

        CureIngredient(String itemId) {
            this.itemId = itemId;
        }
    }
}
