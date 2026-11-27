package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.mixin.accessor.VillagerTradeAccessor;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.player.Player;

/** Rebuilds MCA offers and reapplies vanilla reputation prices after a reroll. */
public final class McaOfferPreparation {
    private McaOfferPreparation() {
    }

    public static boolean prepare(VillagerEntityMCA villager, Player player) {
        if (villager == null || player == null) {
            return false;
        }
        try {
            VillagerTradeAccessor accessor = (VillagerTradeAccessor) (Object) villager;
            accessor.mcaVampCompat$resetSpecialPrices();
            accessor.mcaVampCompat$updateSpecialPrices(player);
            return true;
        } catch (RuntimeException exception) {
            McaVampirismCompat.LOGGER.warn(
                    "[MCA Reborn x Vampirism Compat] Could not prepare MCA 7.7.36 offers while cycling trades for {}",
                    villager.getUUID(),
                    exception
            );
            return false;
        }
    }
}
