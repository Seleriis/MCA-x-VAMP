package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.faction.PlayerFactionContext;
import com.guilh.mca_vampirism_compat.trade.McaTradePriceStackingUtil;
import com.guilh.mca_vampirism_compat.util.McaCompatMessages;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public final class McaHunterTradeService {
    private McaHunterTradeService() {
    }

    public static boolean isHunterTradeVillager(VillagerEntityMCA villager) {
        return villager != null
                && !McaVampireStateService.isVampire(villager)
                && McaHunterAlignmentService.isMcaHunterVillager(villager);
    }

    public static TradeDecision decide(Player player) {
        PlayerFactionContext context = McaVampireFactionService.classifyPlayerContext(player);
        if (context.vampireDisguised()) {
            if (McaVampirismCompatConfig.hunterVillagerDisguisedVampireCanTrade()
                    && McaVampirismCompatConfig.hunterVillagerDisguisedVampireUsesHumanPrices()) {
                boolean blocked = !McaVampirismCompatConfig.hunterVillagersTradeWithHumans();
                McaVampirismCompat.debugLog(
                        "[HUNTER-TRADE] disguise active realFaction={} effectiveFaction={} using=HUMAN_PRICES",
                        context.realFaction(),
                        PlayerFaction.HUMAN_DISGUISED
                );
                return new TradeDecision(
                        blocked,
                        blocked ? McaCompatMessages.HUNTER_TRADE_BLOCKED_GENERIC : null,
                        PlayerFaction.HUMAN_DISGUISED,
                        "DISGUISED_AS_HUMAN"
                );
            }
            return vampireDecision();
        }

        PlayerFaction faction = context.realFaction();
        return switch (faction) {
            case HUNTER -> new TradeDecision(
                    !McaVampirismCompatConfig.hunterVillagersTradeWithHunters(),
                    McaCompatMessages.HUNTER_TRADE_BLOCKED_GENERIC,
                    PlayerFaction.HUNTER,
                    "HUNTER"
            );
            case VAMPIRE -> vampireDecision();
            case TRANSFORMED_WEREWOLF -> new TradeDecision(
                    McaVampirismCompatConfig.hunterVillagersBlockTransformedWerewolfTrading(),
                    McaCompatMessages.HUNTER_TRADE_BLOCKED_WEREWOLF,
                    PlayerFaction.TRANSFORMED_WEREWOLF,
                    "TRANSFORMED_WEREWOLF"
            );
            case WEREWOLF, HUMAN_DISGUISED, HUMAN, UNKNOWN -> new TradeDecision(
                    !McaVampirismCompatConfig.hunterVillagersTradeWithHumans(),
                    McaCompatMessages.HUNTER_TRADE_BLOCKED_GENERIC,
                    faction,
                    "HUMAN"
            );
        };
    }

    public static boolean shouldBlockTrade(Player player) {
        return decide(player).blocked();
    }

    public static PlayerFaction tradeFaction(Player player) {
        return decide(player).priceFaction();
    }

    public static String messageFor(Player player) {
        TradeDecision decision = decide(player);
        return decision.messageKey() == null ? McaCompatMessages.HUNTER_TRADE_BLOCKED_GENERIC : decision.messageKey();
    }

    public static double getTradeMultiplier(PlayerFaction faction) {
        return switch (faction) {
            case HUNTER -> Math.max(0.05D, 1.0D - McaVampirismCompatConfig.hunterVillagerHunterDiscount());
            case VAMPIRE -> McaVampirismCompatConfig.hunterVillagerVampirePriceMultiplier();
            case TRANSFORMED_WEREWOLF -> McaVampirismCompatConfig.hunterVillagerWerewolfPriceMultiplier();
            case WEREWOLF, HUMAN_DISGUISED, HUMAN, UNKNOWN -> McaVampirismCompatConfig.hunterVillagerHumanPriceMultiplier();
        };
    }

    public static int adjustedSpecialPrice(MerchantOffer offer, PlayerFaction faction) {
        ItemStack baseStack = offer.getBaseCostA();
        if (baseStack.isEmpty()) {
            return offer.getSpecialPriceDiff();
        }
        int base = baseStack.getCount();
        double multiplier = getTradeMultiplier(faction);
        logPriceCalculation(faction, base, multiplier);
        return McaTradePriceStackingUtil.calculateStackedSpecialPrice(offer, multiplier, "HUNTER", faction);
    }

    private static TradeDecision vampireDecision() {
        return new TradeDecision(
                McaVampirismCompatConfig.hunterVillagersBlockVampireTrading(),
                McaCompatMessages.HUNTER_TRADE_BLOCKED_VAMPIRE,
                PlayerFaction.VAMPIRE,
                "VAMPIRE"
        );
    }

    private static void logPriceCalculation(PlayerFaction faction, int base, double multiplier) {
        if (faction == PlayerFaction.HUNTER) {
            McaVampirismCompat.debugLog(
                    "[HUNTER-TRADE] price calc faction=HUNTER base={} discount={} multiplier={}",
                    base,
                    McaVampirismCompatConfig.hunterVillagerHunterDiscount(),
                    multiplier
            );
            return;
        }
        McaVampirismCompat.debugLog(
                "[HUNTER-TRADE] price calc faction={} base={} multiplier={}",
                faction,
                base,
                multiplier
        );
    }

    public record TradeDecision(boolean blocked, String messageKey, PlayerFaction priceFaction, String reason) {
    }
}
