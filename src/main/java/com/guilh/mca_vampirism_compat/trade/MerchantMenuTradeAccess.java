package com.guilh.mca_vampirism_compat.trade;

import net.minecraft.world.item.trading.Merchant;

/**
 * Read-only merchant identity captured while a vanilla MerchantMenu is
 * constructed. The implementation intentionally does not expose private
 * MerchantMenu fields, whose runtime names differ from development mappings.
 */
public interface MerchantMenuTradeAccess {
    Merchant mcaVampCompat$getTrader();
}
