package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.service.McaAggressiveVillagerProtectionService;
import com.guilh.mca_vampirism_compat.service.McaVillageEffectBoundsService;
import de.teamlapen.vampirism.blockentity.TotemBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TotemBlockEntity.class, remap = false)
public abstract class TotemBlockEntityMixin {
    @Inject(method = "makeAgressive(Lnet/minecraft/world/entity/npc/Villager;)V", at = @At("HEAD"), cancellable = true)
    private static void mcaVampirismCompat$blockBaseAggressiveVillagerReplacement(Villager villager, CallbackInfo ci) {
        if (McaAggressiveVillagerProtectionService.shouldCancelReplacement(
                villager,
                "TotemBlockEntity.makeAgressive",
                "vampirism:villager_angry"
        )) {
            McaVampirismCompat.debugLog(
                    "[AGGRESSIVE-VILLAGERS] base handler canceled reason=MCA_PRESERVE_ENTITY target={}",
                    villager == null ? null : villager.getUUID()
            );
            ci.cancel();
        }
    }

    @Inject(method = "setChanged()V", at = @At("TAIL"))
    private void mcaVampirismCompat$refreshMcaVillageEffects(CallbackInfo ci) {
        McaVillageEffectBoundsService.onTotemChanged((TotemBlockEntity) (Object) this);
    }

    @Inject(method = "loadAdditional(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V", at = @At("TAIL"))
    private void mcaVampirismCompat$registerLoadedTotem(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        TotemBlockEntity totem = (TotemBlockEntity) (Object) this;
        if (totem.getLevel() != null && totem.getLevel().isClientSide) {
            McaVillageEffectBoundsService.applyClientFogArea(totem, tag);
        } else {
            McaVillageEffectBoundsService.onTotemChanged(totem);
        }
    }

    @Inject(
            method = "getUpdateTag(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;",
            at = @At("RETURN")
    )
    private void mcaVampirismCompat$writeDerivedFogForClient(
            HolderLookup.Provider registries,
            CallbackInfoReturnable<CompoundTag> cir
    ) {
        McaVillageEffectBoundsService.writeClientFogArea(
                (TotemBlockEntity) (Object) this,
                cir.getReturnValue()
        );
    }

    @Inject(method = "onChunkUnloaded()V", at = @At("HEAD"))
    private void mcaVampirismCompat$unregisterUnloadedTotem(CallbackInfo ci) {
        McaVillageEffectBoundsService.onTotemChunkUnloaded((TotemBlockEntity) (Object) this);
    }

    @Inject(method = "setRemoved()V", at = @At("TAIL"))
    private void mcaVampirismCompat$unregisterRemovedTotem(CallbackInfo ci) {
        McaVillageEffectBoundsService.onTotemRemoved((TotemBlockEntity) (Object) this);
    }
}
