package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.easyvillagers.EasyVillagersBlockTradeService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepts the concrete direct Trader block before Easy Villagers can call
 * its original GUI. The tile-entity hook remains as a second line of defense
 * for callers that invoke openTradingGUI directly.
 */
@Pseudo
@Mixin(targets = "de.maxhenkel.easyvillagers.blocks.TraderBlock",
        priority = 2000, remap = false)
public abstract class EasyVillagersTraderBlockMixin {
    @Inject(
            method = "openGUI(Lde/maxhenkel/easyvillagers/blocks/tileentity/TraderTileentityBase;"
                    + "Lnet/minecraft/world/entity/player/Player;"
                    + "Lnet/minecraft/world/level/Level;"
                    + "Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private void mcaVampirismCompat$openFactionMerchant(
            @Coerce Object trader,
            Player player,
            Level level,
            BlockPos blockPos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (trader instanceof BlockEntity blockEntity
                && EasyVillagersBlockTradeService.tryOpenDirectTrader(blockEntity, player)) {
            cir.setReturnValue(true);
        }
    }
}
