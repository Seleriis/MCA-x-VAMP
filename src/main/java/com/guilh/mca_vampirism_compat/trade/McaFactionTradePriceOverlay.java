package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.service.McaHunterTradeService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfTradeService;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * Applies a faction price only for the lifetime of a merchant screen.
 *
 * <p>The vanilla merchant container must consume the same {@link MerchantOffer}
 * that receives its use and XP updates.  This class therefore adjusts the live
 * offers in place and remembers only their pre-faction special-price values.</p>
 */
public final class McaFactionTradePriceOverlay {
    public enum TradeKind {
        VAMPIRE,
        HUNTER,
        WEREWOLF
    }

    private final MerchantOffers offers;
    private final PlayerFaction faction;
    private final TradeKind tradeKind;
    private final ToIntFunction<MerchantOffer> specialPriceCalculator;
    private final Map<MerchantOffer, Integer> originalSpecialPrices = new IdentityHashMap<>();
    private boolean applied;

    private McaFactionTradePriceOverlay(MerchantOffers offers, PlayerFaction faction, TradeKind tradeKind) {
        this(offers, faction, tradeKind, null);
    }

    private McaFactionTradePriceOverlay(
            MerchantOffers offers,
            PlayerFaction faction,
            TradeKind tradeKind,
            ToIntFunction<MerchantOffer> specialPriceCalculator
    ) {
        this.offers = offers;
        this.faction = faction;
        this.tradeKind = tradeKind;
        this.specialPriceCalculator = specialPriceCalculator;
        if (offers != null) {
            for (MerchantOffer offer : offers) {
                originalSpecialPrices.put(offer, offer.getSpecialPriceDiff());
            }
        }
    }

    public static McaFactionTradePriceOverlay create(
            MerchantOffers offers,
            PlayerFaction faction,
            TradeKind tradeKind
    ) {
        return new McaFactionTradePriceOverlay(offers, faction, tradeKind);
    }

    static McaFactionTradePriceOverlay createForTest(
            MerchantOffers offers,
            ToIntFunction<MerchantOffer> specialPriceCalculator
    ) {
        return new McaFactionTradePriceOverlay(
                offers,
                PlayerFaction.UNKNOWN,
                TradeKind.VAMPIRE,
                specialPriceCalculator
        );
    }

    public void apply() {
        if (applied) {
            return;
        }
        for (Map.Entry<MerchantOffer, Integer> entry : originalSpecialPrices.entrySet()) {
            MerchantOffer offer = entry.getKey();
            offer.setSpecialPriceDiff(entry.getValue());
            offer.setSpecialPriceDiff(adjustedSpecialPrice(offer));
        }
        applied = true;
    }

    public void restore() {
        if (!applied) {
            return;
        }
        for (Map.Entry<MerchantOffer, Integer> entry : originalSpecialPrices.entrySet()) {
            entry.getKey().setSpecialPriceDiff(entry.getValue());
        }
        applied = false;
    }

    public boolean contains(MerchantOffer offer) {
        return originalSpecialPrices.containsKey(offer);
    }

    public MerchantOffers offers() {
        return offers;
    }

    private int adjustedSpecialPrice(MerchantOffer offer) {
        if (specialPriceCalculator != null) {
            return specialPriceCalculator.applyAsInt(offer);
        }
        return switch (tradeKind) {
            case HUNTER -> McaHunterTradeService.adjustedSpecialPrice(offer, faction);
            case WEREWOLF -> McaWerewolfTradeService.adjustedSpecialPrice(offer, faction);
            case VAMPIRE -> McaVampireTradePriceCalculator.adjustedSpecialPrice(offer, faction);
        };
    }
}
