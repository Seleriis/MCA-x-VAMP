package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.easyvillagers.EasyVillagersBlockTradeService;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Easy Villagers 1.20.1 changes the Trader through its mod-owned
 * {@code setWorkstation} method. Hooking that stable boundary avoids the
 * 1.21-only {@code useItemOn} API while still protecting automation paths.
 */
@Pseudo
@Mixin(targets = "de.maxhenkel.easyvillagers.blocks.tileentity.TraderTileentityBase",
        priority = 2000, remap = false)
public abstract class EasyVillagersTraderWorkstationMixin {
    @Inject(
            method = "setWorkstation(Lnet/minecraft/world/level/block/Block;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void mcaVampirismCompat$rejectWrongFactionSpecialistWorkstation(
            Block workstation,
            CallbackInfo ci
    ) {
        if (EasyVillagersBlockTradeService.denyDirectTraderSpecialistWorkstation(
                (BlockEntity) (Object) this,
                workstation
        )) {
            ci.cancel();
        }
    }
}
