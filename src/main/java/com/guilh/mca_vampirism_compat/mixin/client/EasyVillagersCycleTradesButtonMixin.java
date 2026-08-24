package com.guilh.mca_vampirism_compat.mixin.client;

import com.guilh.mca_vampirism_compat.easyvillagers.client.EasyVillagersCycleClientState;
import com.guilh.mca_vampirism_compat.trade.TradeCycleEligibility;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "de.maxhenkel.easyvillagers.gui.CycleTradesButton",
        priority = 2000, remap = false)
public abstract class EasyVillagersCycleTradesButtonMixin {
    @Inject(
            method = "canCycle(Lnet/minecraft/world/inventory/MerchantMenu;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private static void mcaVampirismCompat$allowFactionWrapperCycle(
            MerchantMenu menu,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (EasyVillagersCycleClientState.hasStateFor(menu.containerId)) {
            cir.setReturnValue(
                    EasyVillagersCycleClientState.isEnabledFor(menu.containerId)
                            && !TradeCycleEligibility.hasActiveOffer(menu)
            );
        }
    }
}
