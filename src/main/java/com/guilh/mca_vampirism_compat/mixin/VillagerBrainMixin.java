package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.monster.McaSpecialVillagerMonsterIgnoreService;
import forge.net.mca.entity.ai.brain.VillagerBrain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VillagerBrain.class, remap = false)
public abstract class VillagerBrainMixin {
    @Inject(method = "isPanicking", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$suppressVampirePanicFlag(CallbackInfoReturnable<Boolean> cir) {
        if (McaSpecialVillagerMonsterIgnoreService.shouldSuppressPanicFlagFromBrain(this)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "think", at = @At("TAIL"))
    private void mcaVampirismCompat$forceSpecialVillagerPanicFalse(CallbackInfo ci) {
        McaSpecialVillagerMonsterIgnoreService.forceTrackedPanicFalseFromBrain(this);
    }
}
