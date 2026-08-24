package com.guilh.mca_vampirism_compat.mixin.accessor;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Explicit access to vanilla's MCA 7.7.36 trade preparation. MCA now uses
 * Villager.startTrading instead of its former copied trade methods.
 */
@Mixin(Villager.class)
public interface VillagerTradeAccessor {
    @Invoker("startTrading")
    void mcaVampCompat$startTrading(Player player);

    @Invoker("resetSpecialPrices")
    void mcaVampCompat$resetSpecialPrices();

    @Invoker("updateSpecialPrices")
    void mcaVampCompat$updateSpecialPrices(Player player);

}
