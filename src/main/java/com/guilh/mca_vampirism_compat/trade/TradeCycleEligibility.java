package com.guilh.mca_vampirism_compat.trade;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public final class TradeCycleEligibility {
    private static final String CYCLE_LOCK_KEY = "McaVampCompatTradeCycleLocked";

    /*
     * mca-ev-compat gives a stored MCA villager one synthetic profession XP
     * so its profession remains stable. Treat that value as untraded while
     * still rejecting villagers with genuine trade progress.
     */
    private static final int MAX_UNTRADED_XP = 1;

    private TradeCycleEligibility() {
    }

    public static boolean isAvailable(Villager villager, MerchantOffers offers, boolean closed) {
        if (villager == null) {
            return false;
        }
        return isAvailable(
                closed,
                villager.showProgressBar(),
                villager.getVillagerXp(),
                villager.getPersistentData().getBoolean(CYCLE_LOCK_KEY),
                offers
        );
    }

    static boolean isAvailable(boolean closed, boolean showProgressBar, int villagerXp,
                               boolean explicitlyLocked, MerchantOffers offers) {
        if (closed || !showProgressBar || explicitlyLocked || villagerXp > MAX_UNTRADED_XP) {
            return false;
        }
        if (offers == null) {
            return false;
        }
        for (MerchantOffer offer : offers) {
            if (offer != null && offer.getUses() > 0) {
                return false;
            }
        }
        return true;
    }

    public static void lockAfterTrade(Villager villager) {
        if (villager != null) {
            villager.getPersistentData().putBoolean(CYCLE_LOCK_KEY, true);
        }
    }

    public static boolean hasActiveOffer(MerchantMenu menu) {
        if (menu instanceof MerchantMenuTradeAccess access) {
            return access.mcaVampCompat$getTradeContainer().getActiveOffer() != null;
        }
        return menu.getSlot(2).hasItem();
    }
}
