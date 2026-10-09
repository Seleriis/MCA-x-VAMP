package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaEditorTraitSyncService;
import forge.net.conczin.mca.network.c2s.VillagerEditorSyncRequest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MCA 7.6 applies a Villager Editor save through this packet's private
 * saveEntity method. Its tail runs after Entity.load has restored ForgeCaps,
 * making the traits selected in the Editor the final authoritative faction
 * decision for that transaction.
 */
@Mixin(value = VillagerEditorSyncRequest.class, remap = false)
public abstract class VillagerEditorSyncRequestMixin {
    @Inject(method = "saveEntity", at = @At("TAIL"), remap = false)
    private void mcaVampirismCompat$reconcileCompletedEditorSave(
            ServerPlayer player,
            Entity entity,
            CompoundTag data,
            CallbackInfo ci
    ) {
        McaEditorTraitSyncService.reconcileCompletedEditorSave(entity);
    }
}
