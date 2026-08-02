package com.guilh.mca_vampirism_compat.trade;

import net.minecraft.world.inventory.MerchantContainer;

/**
 * Read-only access to the vanilla trade container used by both live and
 * Easy Villagers-backed faction merchants.
 */
public interface MerchantMenuTradeAccess {
    MerchantContainer mcaVampCompat$getTradeContainer();
}
