package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.easyvillagers.EasyVillagersTradeCycleService;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "de.maxhenkel.easyvillagers.net.MessageCycleTrades",
        priority = 2000, remap = false)
public abstract class EasyVillagersCycleTradesMessageMixin {
    @Inject(
            method = "executeServerSide"
                    + "(Lnet/neoforged/neoforge/network/handling/IPayloadContext;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private void mcaVampirismCompat$cycleFactionMerchant(
            IPayloadContext context,
            CallbackInfo ci
    ) {
        if (context.player() instanceof ServerPlayer player
                && EasyVillagersTradeCycleService.handleCycleTrades(player)) {
            ci.cancel();
        }
    }
}
