package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.monster.McaSpecialVillagerMonsterIgnoreService;
import com.guilh.mca_vampirism_compat.service.LegacyStateMigrationService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VillagerEntityMCA.class, remap = false)
public abstract class VillagerEntityMCAMixin {
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void mcaVampirismCompat$migrateLegacyState(CompoundTag tag, CallbackInfo ci) {
        LegacyStateMigrationService.migrate((VillagerEntityMCA) (Object) this, tag);
    }

    @Inject(method = "playSurprisedSound", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$suppressVampireFearSound(CallbackInfo ci) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        if (McaSpecialVillagerMonsterIgnoreService.shouldSuppressFearDialogue(villager)) {
            ci.cancel();
        }
    }
}
