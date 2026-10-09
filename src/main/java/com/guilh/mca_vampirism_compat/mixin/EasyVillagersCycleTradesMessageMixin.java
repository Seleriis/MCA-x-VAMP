package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.easyvillagers.EasyVillagersTradeCycleService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
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
                    + "(Lnet/minecraftforge/network/NetworkEvent$Context;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private void mcaVampirismCompat$cycleFactionMerchant(
            NetworkEvent.Context context,
            CallbackInfo ci
    ) {
        ServerPlayer player = context.getSender();
        // Do the complete, server-only dispatch here. In particular, an MCA
        // merchant whose opening session was lost must be recovered before
        // Easy Villagers calls GuiEvents. GuiEvents also owns client key-bind
        // references in every supported Easy Villagers Forge release, so it
        // is not a safe compat boundary on a dedicated server.
        if (player != null && EasyVillagersTradeCycleService.handleCycleTrades(player, "MESSAGE")) {
            ci.cancel();
        }
    }
}
