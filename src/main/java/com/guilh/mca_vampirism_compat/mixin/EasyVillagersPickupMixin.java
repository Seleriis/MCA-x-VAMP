package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.easyvillagers.McaFactionPickupPermissionService;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "de.maxhenkel.easyvillagers.events.VillagerEvents", priority = 2000, remap = false)
public abstract class EasyVillagersPickupMixin {
    @Inject(method = "pickUp", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void vampirismMcaCompat$denyFactionPickup(Villager villager, Player player, CallbackInfo ci) {
        if (McaFactionPickupPermissionService.denyPickupIfNeeded(villager, player, "VillagerEvents.pickUp")) {
            ci.cancel();
        }
    }
}
