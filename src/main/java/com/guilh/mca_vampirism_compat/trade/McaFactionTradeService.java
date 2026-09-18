package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.faction.PlayerFactionContext;
import com.guilh.mca_vampirism_compat.service.McaHunterTradeService;
import com.guilh.mca_vampirism_compat.service.McaTradeContextService;
import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.service.McaVampireFactionService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfTradeService;
import com.guilh.mca_vampirism_compat.social.McaFactionContextualDialogueService;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.guilh.mca_vampirism_compat.util.McaCompatMessages;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class McaFactionTradeService {
    public static final String VAMPIRE_ALLY = "mca_vamp_compat.trade.vampire_ally";
    public static final String CONVERSION_PENALTY_CLEARED = "mca_vamp_compat.trade.conversion_penalty_cleared";
    public static final String HUMAN_MARKUP_ACTIVE = "mca_vamp_compat.trade.human_markup_active";
    public static final String WEREWOLF_BLOCKED = "mca_vamp_compat.trade.werewolf_blocked";
    public static final String HUNTER_BLOCKED = "mca_vamp_compat.trade.hunter_blocked";
    public static final String ONLY_VAMPIRES = "mca_vamp_compat.trade.only_vampires";

    private McaFactionTradeService() {
    }

    public static void onInteractionEvent(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide || !(event.getTarget() instanceof VillagerEntityMCA villager)) {
            return;
        }
        if (isSneaking(event.getEntity())) {
            if (McaVampireStateService.isVampire(villager)) {
                McaVampireStateService.ensureVampireState(villager);
            }
            return;
        }
        if (!shouldHandleMcaTrade(villager)) {
            return;
        }
        debugTradeState("PlayerInteractEvent", event.getEntity(), villager);
    }

    public static InteractionResult onMobInteractHead(Player player, VillagerEntityMCA villager, InteractionHand hand) {
        if (isSneaking(player)) {
            return null;
        }
        if (hand == InteractionHand.MAIN_HAND && shouldHandleMcaTrade(villager)) {
            debugTradeState("MCA social interaction", player, villager);
        }
        return null;
    }

    /**
     * Runs before MCA prepares its merchant offers. Returning {@code true}
     * prevents the native screen from opening for an invalid faction.
     */
    public static boolean shouldCancelMcaTradeOpening(Player player, VillagerEntityMCA villager) {
        debugTradeState("MCA trade option selected", player, villager);
        if (!shouldHandleMcaTrade(villager)) {
            return false;
        }
        if (shouldBlockTrade(player, villager)) {
            player.displayClientMessage(Component.translatable(messageFor(player, villager)), true);
            if (player instanceof ServerPlayer serverPlayer) {
                McaFactionContextualDialogueService.onTradeRefused(serverPlayer, villager);
            }
            logTradeBlocked(player, villager);
            McaTradeContextService.clearMcaTradeContext(player, villager);
            return true;
        }
        return false;
    }

    /**
     * Runs after MCA has prepared reputation prices and set its trading player,
     * immediately before MCA opens its native {@link net.minecraft.world.inventory.MerchantMenu}.
     */
    public static void prepareNativeMcaTrade(Player player, VillagerEntityMCA villager) {
        if (!shouldHandleMcaTrade(villager) || shouldBlockTrade(player, villager)) {
            return;
        }
        McaTradeContextService.markMcaTradeAttempt(player, villager);
        PlayerFaction faction = tradeFaction(player, villager);
        // MCA deliberately keeps a transformed villager as VillagerEntityMCA,
        // so Vampirism's ConvertedVillagerEntity#updateTrades never runs for
        // it. Add the native converted-villager opportunity before the live
        // faction price overlay is opened.
        if (McaVampireStateService.isVampire(villager)) {
            McaVampireSpecialTradeService.ensureForVampire(villager);
        }
        McaVampireFactionService.debugClassification(player, faction, "MCA_TRADE_OPTION");
        McaVampirismCompat.debugLog(
                "[TRADE] native MCA trade allowed faction={} villager={}",
                faction,
                villager.getUUID()
        );
        if (player instanceof ServerPlayer serverPlayer) {
            McaFactionTradeSession.open(serverPlayer, villager, faction);
            return;
        }
        McaVampirismCompat.debugLog(
                "[TRADE] native MCA session skipped reason=NOT_SERVER_PLAYER player={} villager={}",
                player.getName().getString(),
                villager.getUUID()
        );
    }

    public static boolean shouldBlockTrade(Player player, VillagerEntityMCA villager) {
        if (McaVampireCureService.shouldBlockTrade(villager)) {
            return true;
        }
        if (McaHunterTradeService.isHunterTradeVillager(villager)) {
            return McaHunterTradeService.shouldBlockTrade(player);
        }
        if (McaWerewolfTradeService.isWerewolfTradeVillager(villager)) {
            return McaWerewolfTradeService.shouldBlockTrade(player);
        }
        return McaVampireFactionService.isTradeBlocked(player, villager);
    }

    public static float getTradePriceMultiplier(Player player, VillagerEntityMCA villager) {
        if (McaHunterTradeService.isHunterTradeVillager(villager)) {
            return (float) McaHunterTradeService.getTradeMultiplier(McaHunterTradeService.tradeFaction(player));
        }
        if (McaWerewolfTradeService.isWerewolfTradeVillager(villager)) {
            return (float) McaWerewolfTradeService.getTradeMultiplier(McaWerewolfTradeService.tradeFaction(player));
        }
        return (float) McaVampireFactionService.getTradeMultiplier(player, villager);
    }

    public static boolean shouldHandleMcaTrade(VillagerEntityMCA villager) {
        return McaVampireCureService.isCuring(villager)
                || McaVampireStateService.isVampire(villager)
                || McaHunterTradeService.isHunterTradeVillager(villager)
                || McaWerewolfTradeService.isWerewolfTradeVillager(villager);
    }

    /**
     * Resolves the same faction used by the normal MCA trade-opening path.
     * The Cycle Trades packet recovery uses this only after it has validated
     * that the active merchant is a real MCA villager.
     */
    public static PlayerFaction resolveTradeFaction(Player player, VillagerEntityMCA villager) {
        return tradeFaction(player, villager);
    }

    public static void closeWrappedTradeSession(Player player) {
        if (player != null) {
            McaFactionTradeSession.closeForPlayer(player.getUUID(), "player logout");
            McaNativeTradeCycleSession.closeForPlayer(player.getUUID(), "player logout");
        }
    }

    public static void clearTradeContext(Player player, VillagerEntityMCA villager) {
        McaTradeContextService.clearMcaTradeContext(player, villager);
    }

    public static void debugTradeState(String method, Player player, VillagerEntityMCA villager) {
        logPlayerFactionContext(player);
        // AbstractVillager#getOffers deliberately throws on the logical client in
        // 1.21.1. mobInteract runs on both sides, so even debug arguments must not
        // query offers there.
        boolean hasTradeOffers = !villager.level().isClientSide && villager.hasTradeOffers();
        boolean vampireConverted = ModCapabilities.get(villager)
                .map(state -> state.isConverted())
                .orElse(false);
        boolean werewolfConverted = ModCapabilities.get(villager)
                .map(state -> state.isWerewolfConverted())
                .orElse(false);
        McaVampirismCompat.debugLog(
                "[TRADE] method={} player={} faction={} villager={} isVampire={} isHunter={} isWerewolf={} vampireTrait={} vampireConverted={} werewolfTrait={} werewolfConverted={} hasTradeOffers={}",
                method,
                player.getName().getString(),
                tradeFaction(player, villager),
                villager.getUUID(),
                McaVampireStateService.isVampire(villager),
                McaHunterTradeService.isHunterTradeVillager(villager),
                McaWerewolfTradeService.isWerewolfTradeVillager(villager),
                McaTraitBridge.hasVampireTrait(villager),
                vampireConverted,
                McaTraitBridge.hasWerewolfTrait(villager),
                werewolfConverted,
                hasTradeOffers
        );
    }

    public static String messageFor(Player player, VillagerEntityMCA villager) {
        if (McaVampireCureService.shouldBlockTrade(villager)) {
            return McaCompatMessages.CURE_IN_PROGRESS;
        }
        if (McaHunterTradeService.isHunterTradeVillager(villager)) {
            return McaHunterTradeService.messageFor(player);
        }
        if (McaWerewolfTradeService.isWerewolfTradeVillager(villager)) {
            return McaWerewolfTradeService.messageFor(player);
        }
        if (clearConversionBitePenaltyIfSafe(player, villager)) {
            return CONVERSION_PENALTY_CLEARED;
        }
        PlayerFaction faction = McaVampireFactionService.tradeFaction(player);
        if (McaVampirismCompatConfig.vampireVillagersTradeOnlyWithVampires() && faction != PlayerFaction.VAMPIRE) {
            return ONLY_VAMPIRES;
        }
        return switch (faction) {
            case VAMPIRE -> VAMPIRE_ALLY;
            case HUNTER -> McaVampirismCompatConfig.blockHunterTrading() ? HUNTER_BLOCKED : HUMAN_MARKUP_ACTIVE;
            case TRANSFORMED_WEREWOLF -> McaVampirismCompatConfig.blockWerewolfTrading() ? WEREWOLF_BLOCKED : HUMAN_MARKUP_ACTIVE;
            case WEREWOLF -> HUMAN_MARKUP_ACTIVE;
            case HUMAN_DISGUISED, HUMAN, UNKNOWN -> HUMAN_MARKUP_ACTIVE;
        };
    }

    public static boolean wasConvertedByPlayer(Player player, VillagerEntityMCA villager) {
        if (player == null || !McaVampireStateService.isVampire(villager)) {
            return false;
        }
        return ModCapabilities.get(villager)
                .map(state -> state.wasBittenByVampirePlayer()
                        && state.isBiteWasConversionCause()
                        && state.getLastVampirePlayerBiteUuid()
                        .map(player.getUUID()::equals)
                        .orElse(false))
                .orElse(false);
    }

    public static boolean clearConversionBitePenaltyIfSafe(Player player, VillagerEntityMCA villager) {
        boolean cleared = McaVampirismCompatConfig.clearVampireBiteReputationPenaltyOnConversion()
                && McaVampirismCompatConfig.neutralizeConversionBitePenaltyForVampires()
                && wasConvertedByPlayer(player, villager);
        if (cleared) {
            McaVampirismCompat.debugLog(
                    "[TRADE] conversion bite penalty neutralized player={} villager={}",
                    player.getName().getString(),
                    villager.getUUID()
            );
        }
        return cleared;
    }

    private static String blockReason(Player player) {
        return switch (McaVampireFactionService.tradeFaction(player)) {
            case HUNTER -> "HUNTER";
            case WEREWOLF -> "WEREWOLF_NOT_TRANSFORMED";
            case TRANSFORMED_WEREWOLF -> "TRANSFORMED_WEREWOLF";
            case VAMPIRE, HUMAN_DISGUISED, HUMAN, UNKNOWN -> "CONFIG";
        };
    }

    private static PlayerFaction tradeFaction(Player player, VillagerEntityMCA villager) {
        if (McaHunterTradeService.isHunterTradeVillager(villager)) {
            return McaHunterTradeService.tradeFaction(player);
        }
        if (McaWerewolfTradeService.isWerewolfTradeVillager(villager)) {
            return McaWerewolfTradeService.tradeFaction(player);
        }
        return McaVampireFactionService.tradeFaction(player);
    }

    private static void logPlayerFactionContext(Player player) {
        PlayerFactionContext context = McaVampireFactionService.classifyPlayerContext(player);
        McaVampirismCompat.debugLog(
                "[TRADE] faction context player={} realFaction={} transformed={} effectiveFaction={}",
                player.getName().getString(),
                context.realFaction(),
                context.transformedWerewolf(),
                context.effectiveFaction()
        );
    }

    private static void logTradeBlocked(Player player, VillagerEntityMCA villager) {
        if (McaVampireCureService.shouldBlockTrade(villager)) {
            McaVampirismCompat.debugLog(
                    "[CURE] trade blocked villager={} reason=CURING",
                    villager.getUUID()
            );
            return;
        }
        if (McaHunterTradeService.isHunterTradeVillager(villager)) {
            McaHunterTradeService.TradeDecision decision = McaHunterTradeService.decide(player);
            McaVampirismCompat.debugLog(
                    "[HUNTER-TRADE] trade blocked faction={} reason=HUNTER_VILLAGER_REFUSES_{}",
                    decision.priceFaction(),
                    decision.reason()
            );
            return;
        }
        if (McaWerewolfTradeService.isWerewolfTradeVillager(villager)) {
            McaWerewolfTradeService.TradeDecision decision = McaWerewolfTradeService.decide(player);
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-TRADE] trade blocked faction={} reason=WEREWOLF_VILLAGER_REFUSES_{}",
                    decision.priceFaction(),
                    decision.reason()
            );
            return;
        }
        McaVampirismCompat.debugLog(
                "[TRADE] MCA TRADE BLOCKED reason={}",
                blockReason(player)
        );
    }

    private static boolean isSneaking(Player player) {
        return player.isShiftKeyDown() || player.isCrouching();
    }
}
