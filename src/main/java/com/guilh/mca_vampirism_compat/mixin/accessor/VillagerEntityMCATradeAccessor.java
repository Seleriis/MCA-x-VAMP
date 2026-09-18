package com.guilh.mca_vampirism_compat.mixin.accessor;

import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * MCA 7.6.x keeps its trade-price preparation in a private copy of the
 * vanilla helper. Reusing it avoids rebuilding an offer list with vanilla
 * reputation rules during Cycle Trades.
 */
@Mixin(value = VillagerEntityMCA.class, remap = false)
public interface VillagerEntityMCATradeAccessor {
    @Invoker("copiedPrepareOffersFor")
    void mcaVampCompat$prepareMcaOffers(Player player);
}
