package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

public final class McaTradePriceStackingUtil {
    private McaTradePriceStackingUtil() {
    }

    public static int calculateStackedSpecialPrice(MerchantOffer copiedOffer, double multiplier,
                                                   String villagerType, PlayerFaction playerFaction) {
        ItemStack baseStack = copiedOffer.getBaseCostA();
        if (baseStack.isEmpty()) {
            return copiedOffer.getSpecialPriceDiff();
        }

        int existingDiff = copiedOffer.getSpecialPriceDiff();
        int base = baseStack.getCount();
        int current = copiedOffer.getCostA().getCount();
        int desired = calculateDesiredCostFromCurrentCost(current, multiplier, baseStack.getMaxStackSize());
        int factionDelta = desired - current;
        int newDiff = existingDiff + factionDelta;
        int finalCost = calculateCostWithSpecialDiff(copiedOffer, baseStack, newDiff);

        McaVampirismCompat.debugLog(
                "[TRADE] price stacking villagerType={} playerFaction={} base={} current={} existingDiff={} multiplier={} factionDelta={} newDiff={} final={}",
                villagerType,
                playerFaction,
                base,
                current,
                existingDiff,
                multiplier,
                factionDelta,
                newDiff,
                finalCost
        );
        return newDiff;
    }

    public static int calculateDesiredCostFromCurrentCost(int currentCost, double multiplier, int maxStackSize) {
        if (currentCost <= 0) {
            return 0;
        }
        int adjusted = (int) Math.round(currentCost * Math.max(0.05D, multiplier));
        return Math.max(1, Math.min(adjusted, Math.max(1, maxStackSize)));
    }

    private static int calculateCostWithSpecialDiff(MerchantOffer offer, ItemStack baseStack, int specialPriceDiff) {
        int base = baseStack.getCount();
        int demand = Math.max(0, (int) Math.floor(base * offer.getDemand() * offer.getPriceMultiplier()));
        return Math.max(1, Math.min(base + demand + specialPriceDiff, Math.max(1, baseStack.getMaxStackSize())));
    }
}
