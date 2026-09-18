package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.mixin.accessor.VillagerEntityMCATradeAccessor;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffers;

/** Rebuilds MCA offers and reapplies vanilla reputation prices after a reroll. */
public final class McaOfferPreparation {
    private McaOfferPreparation() {
    }

    public static boolean prepare(VillagerEntityMCA villager, Player player) {
        if (villager == null || player == null) {
            return false;
        }
        try {
            VillagerEntityMCATradeAccessor accessor = (VillagerEntityMCATradeAccessor) (Object) villager;
            accessor.mcaVampCompat$prepareMcaOffers(player);
            return true;
        } catch (RuntimeException exception) {
            McaVampirismCompat.LOGGER.warn(
                    "[MCA Reborn x Vampirism Compat] Could not prepare MCA 7.6.16 offers while cycling trades for {}",
                    villager.getUUID(),
                    exception
            );
            return false;
        }
    }

    /**
     * Rebuilds MCA's live professional list and reapplies MCA's ordinary
     * player-price preparation. Both faction and ordinary MCA Cycle Trades
     * paths use this exact sequence before the Vampirism roll is evaluated.
     */
    public static MerchantOffers rebuild(VillagerEntityMCA villager, Player player) {
        if (villager == null) {
            return null;
        }
        villager.setOffers(null);
        MerchantOffers offers = villager.getOffers();
        prepare(villager, player);
        return offers;
    }
}
