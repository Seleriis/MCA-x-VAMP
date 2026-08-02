package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.service.McaHunterTradeService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfTradeService;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.HashMap;
import java.util.Map;

public final class McaVampireTradeOfferMapper {
    private McaVampireTradeOfferMapper() {
    }

    public static MappedOffers copyAndAdjust(MerchantOffers originalOffers, PlayerFaction faction) {
        return copyAndAdjust(originalOffers, faction, false);
    }

    public static MappedOffers copyAndAdjustForHunter(MerchantOffers originalOffers, PlayerFaction faction) {
        return copyAndAdjust(originalOffers, faction, TradeMode.HUNTER);
    }

    public static MappedOffers copyAndAdjustForWerewolf(MerchantOffers originalOffers, PlayerFaction faction) {
        return copyAndAdjust(originalOffers, faction, TradeMode.WEREWOLF);
    }

    private static MappedOffers copyAndAdjust(MerchantOffers originalOffers, PlayerFaction faction, boolean hunterTrade) {
        return copyAndAdjust(originalOffers, faction, hunterTrade ? TradeMode.HUNTER : TradeMode.VAMPIRE);
    }

    private static MappedOffers copyAndAdjust(MerchantOffers originalOffers, PlayerFaction faction, TradeMode tradeMode) {
        MerchantOffers adjustedOffers = new MerchantOffers();
        Map<Integer, Integer> adjustedToOriginal = new HashMap<>();
        if (originalOffers == null || originalOffers.isEmpty()) {
            return new MappedOffers(adjustedOffers, adjustedToOriginal);
        }

        for (int originalIndex = 0; originalIndex < originalOffers.size(); originalIndex++) {
            MerchantOffer original = originalOffers.get(originalIndex);
            MerchantOffer adjusted = copyOffer(original);
            int adjustedSpecial = switch (tradeMode) {
                case HUNTER -> McaHunterTradeService.adjustedSpecialPrice(adjusted, faction);
                case WEREWOLF -> McaWerewolfTradeService.adjustedSpecialPrice(adjusted, faction);
                case VAMPIRE -> McaVampireTradePriceCalculator.adjustedSpecialPrice(adjusted, faction);
            };
            adjusted.setSpecialPriceDiff(adjustedSpecial);

            int adjustedIndex = adjustedOffers.size();
            adjustedOffers.add(adjusted);
            adjustedToOriginal.put(adjustedIndex, originalIndex);

            McaVampirismCompat.debugLog(
                    "[TRADE-WRAPPER] offer={} originalBase={} adjustedCost={} faction={}",
                    adjustedIndex,
                    original.getBaseCostA().isEmpty() ? 0 : original.getBaseCostA().getCount(),
                    adjusted.getCostA().getCount(),
                    faction
            );
        }
        return new MappedOffers(adjustedOffers, adjustedToOriginal);
    }

    private static MerchantOffer copyOffer(MerchantOffer original) {
        return original.copy();
    }

    public record MappedOffers(MerchantOffers adjustedOffers, Map<Integer, Integer> adjustedIndexToOriginalIndex) {
    }

    private enum TradeMode {
        VAMPIRE,
        HUNTER,
        WEREWOLF
    }
}
