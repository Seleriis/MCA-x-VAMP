package com.guilh.mca_vampirism_compat.mixin.client;

import com.guilh.mca_vampirism_compat.easyvillagers.client.EasyVillagersCycleClientState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantResultSlot.class)
public abstract class MerchantResultSlotMixin {
    @Inject(method = "onTake", at = @At("TAIL"))
    private void mcaVampCompat$hideCycleAfterFirstTrade(
            Player player,
            ItemStack stack,
            CallbackInfo ci
    ) {
        if (player.level().isClientSide
                && player.containerMenu instanceof MerchantMenu menu
                && EasyVillagersCycleClientState.hasStateFor(menu.containerId)) {
            EasyVillagersCycleClientState.disable(menu.containerId);
        }
    }
}
