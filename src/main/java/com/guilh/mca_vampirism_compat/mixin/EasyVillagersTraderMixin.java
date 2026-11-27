package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.easyvillagers.EasyVillagersBlockTradeService;
import com.guilh.mca_vampirism_compat.easyvillagers.McaEvCompatBridge;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "de.maxhenkel.easyvillagers.blocks.tileentity.TraderTileentityBase",
        priority = 2000, remap = false)
public abstract class EasyVillagersTraderMixin {
    @Inject(
            method = "openTradingGUI(Lnet/minecraft/world/entity/player/Player;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void mcaVampirismCompat$openFactionMerchant(Player player,
                                                        CallbackInfoReturnable<Boolean> cir) {
        McaEvCompatBridge.logTradeHookInvocationOnce(this);
        if (EasyVillagersBlockTradeService.tryOpenDirectTrader((BlockEntity) (Object) this, player)) {
            cir.setReturnValue(true);
        }
    }
}
