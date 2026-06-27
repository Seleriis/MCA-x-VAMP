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
@Mixin(targets = "com.guilh.mcaevcompat.CompatLogic", priority = 2000, remap = false)
public abstract class McaEvCompatCaptureMixin {
    @Inject(method = "captureMcaVillager", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void vampirismMcaCompat$denyFactionPickup(Villager villager, Player player, CallbackInfo ci) {
        if (McaFactionPickupPermissionService.denyPickupIfNeeded(villager, player, "McaEvCompat CompatLogic.captureMcaVillager")) {
            ci.cancel();
        }
    }
}
