package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.faction.PlayerFactionContext;
import com.guilh.mca_vampirism_compat.trade.McaTradePriceStackingUtil;
import com.guilh.mca_vampirism_compat.util.McaCompatMessages;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public final class McaWerewolfTradeService {
    private McaWerewolfTradeService() {
    }

    public static boolean isWerewolfTradeVillager(VillagerEntityMCA villager) {
        return villager != null
                && !McaVampireStateService.isVampire(villager)
                && !McaHunterTradeService.isHunterTradeVillager(villager)
                && McaWerewolfStateService.isWerewolf(villager);
    }

    public static TradeDecision decide(Player player) {
        PlayerFactionContext context = McaVampireFactionService.classifyPlayerContext(player);
        if (context.vampireDisguised()) {
            if (McaVampirismCompatConfig.werewolfVillagerDisguisedVampireCanTrade()
                    && McaVampirismCompatConfig.werewolfVillagerDisguisedVampireUsesHumanPrices()) {
                boolean blocked = !McaVampirismCompatConfig.werewolfVillagersTradeWithHumans();
                McaVampirismCompat.debugLog(
                        "[WEREWOLF-TRADE] disguise active realFaction={} effectiveFaction={} using=HUMAN_PRICES",
                        context.realFaction(),
                        PlayerFaction.HUMAN_DISGUISED
                );
                return new TradeDecision(
                        blocked,
                        blocked ? McaCompatMessages.WEREWOLF_TRADE_BLOCKED_GENERIC : null,
                        PlayerFaction.HUMAN_DISGUISED,
                        "DISGUISED_AS_HUMAN"
                );
            }
            return vampireDecision();
        }

        PlayerFaction faction = context.realFaction();
        if (context.werewolf()) {
            return new TradeDecision(
                    !McaVampirismCompatConfig.werewolfVillagersTradeWithWerewolves(),
                    McaCompatMessages.WEREWOLF_TRADE_BLOCKED_GENERIC,
                    faction == PlayerFaction.TRANSFORMED_WEREWOLF ? PlayerFaction.TRANSFORMED_WEREWOLF : PlayerFaction.WEREWOLF,
                    faction == PlayerFaction.TRANSFORMED_WEREWOLF ? "TRANSFORMED_WEREWOLF" : "WEREWOLF"
            );
        }
        return switch (faction) {
            case VAMPIRE -> vampireDecision();
            case HUNTER -> new TradeDecision(
                    McaVampirismCompatConfig.werewolfVillagersBlockHunterTrading(),
                    McaCompatMessages.WEREWOLF_TRADE_BLOCKED_HUNTER,
                    PlayerFaction.HUNTER,
                    "HUNTER"
            );
            case WEREWOLF, TRANSFORMED_WEREWOLF -> new TradeDecision(
                    !McaVampirismCompatConfig.werewolfVillagersTradeWithWerewolves(),
                    McaCompatMessages.WEREWOLF_TRADE_BLOCKED_GENERIC,
                    faction,
                    faction.name()
            );
            case HUMAN_DISGUISED, HUMAN, UNKNOWN -> new TradeDecision(
                    !McaVampirismCompatConfig.werewolfVillagersTradeWithHumans(),
                    McaCompatMessages.WEREWOLF_TRADE_BLOCKED_GENERIC,
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
        return decision.messageKey() == null ? McaCompatMessages.WEREWOLF_TRADE_BLOCKED_GENERIC : decision.messageKey();
    }

    public static double getTradeMultiplier(PlayerFaction faction) {
        return switch (faction) {
            case WEREWOLF, TRANSFORMED_WEREWOLF -> Math.max(0.05D, 1.0D - McaVampirismCompatConfig.werewolfVillagerWerewolfDiscount());
            case VAMPIRE -> McaVampirismCompatConfig.werewolfVillagerVampirePriceMultiplier();
            case HUNTER -> McaVampirismCompatConfig.werewolfVillagerHunterPriceMultiplier();
            case HUMAN_DISGUISED, HUMAN, UNKNOWN -> McaVampirismCompatConfig.werewolfVillagerHumanPriceMultiplier();
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
        return McaTradePriceStackingUtil.calculateStackedSpecialPrice(offer, multiplier, "WEREWOLF", faction);
    }

    private static TradeDecision vampireDecision() {
        return new TradeDecision(
                McaVampirismCompatConfig.werewolfVillagersBlockVampireTrading(),
                McaCompatMessages.WEREWOLF_TRADE_BLOCKED_VAMPIRE,
                PlayerFaction.VAMPIRE,
                "VAMPIRE"
        );
    }

    private static void logPriceCalculation(PlayerFaction faction, int base, double multiplier) {
        if (faction == PlayerFaction.WEREWOLF || faction == PlayerFaction.TRANSFORMED_WEREWOLF) {
            McaVampirismCompat.debugLog(
                    "[WEREWOLF-TRADE] price calc faction={} base={} discount={} multiplier={}",
                    faction,
                    base,
                    McaVampirismCompatConfig.werewolfVillagerWerewolfDiscount(),
                    multiplier
            );
            return;
        }
        McaVampirismCompat.debugLog(
                "[WEREWOLF-TRADE] price calc faction={} base={} multiplier={}",
                faction,
                base,
                multiplier
        );
    }

    public record TradeDecision(boolean blocked, String messageKey, PlayerFaction priceFaction, String reason) {
    }
}
