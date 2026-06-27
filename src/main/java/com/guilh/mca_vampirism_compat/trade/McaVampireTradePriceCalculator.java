package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public final class McaVampireTradePriceCalculator {
    private McaVampireTradePriceCalculator() {
    }

    public static float multiplierFor(PlayerFaction faction) {
        return switch (faction) {
            case VAMPIRE -> (float) Math.max(0.05D, 1.0D - McaVampirismCompatConfig.vampirePlayerTradeDiscount());
            case HUNTER -> (float) McaVampirismCompatConfig.hunterTradePriceMultiplier();
            case TRANSFORMED_WEREWOLF -> (float) McaVampirismCompatConfig.werewolfTradePriceMultiplier();
            case WEREWOLF, HUMAN_DISGUISED, HUMAN, UNKNOWN -> (float) McaVampirismCompatConfig.humanTradePriceMultiplier();
        };
    }

    public static int adjustedSpecialPrice(MerchantOffer offer, PlayerFaction faction) {
        ItemStack baseStack = offer.getBaseCostA();
        if (baseStack.isEmpty()) {
            return offer.getSpecialPriceDiff();
        }
        float multiplier = multiplierFor(faction);
        if (faction == PlayerFaction.VAMPIRE) {
            McaVampirismCompat.debugLog(
                    "[TRADE-WRAPPER] price calc faction=VAMPIRE base={} discount={} multiplier={}",
                    baseStack.getCount(),
                    McaVampirismCompatConfig.vampirePlayerTradeDiscount(),
                    multiplier
            );
        }
        return McaTradePriceStackingUtil.calculateStackedSpecialPrice(offer, multiplier, "VAMPIRE", faction);
    }

}
