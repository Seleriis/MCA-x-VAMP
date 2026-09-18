package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.monster.McaSpecialVillagerMonsterIgnoreService;
import com.guilh.mca_vampirism_compat.service.LegacyStateMigrationService;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.guilh.mca_vampirism_compat.service.McaNativeWerewolfProtectionService;
import com.guilh.mca_vampirism_compat.service.McaSpecialistProfessionService;
import com.guilh.mca_vampirism_compat.visual.McaVampireVisualData;
import com.guilh.mca_vampirism_compat.visual.McaWerewolfVisualData;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VillagerEntityMCA.class, remap = false)
public abstract class VillagerEntityMCAMixin {
    @Inject(method = "defineSynchedData", at = @At("TAIL"), remap = true)
    private void mcaVampirismCompat$defineSpecialVillagerVisualData(CallbackInfo ci) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        McaVampireVisualData.defineSynchedData(villager);
        McaWerewolfVisualData.defineSynchedData(villager);
    }

    // MCA's published jar uses obfuscated names, while the Forge development
    // runtime uses official names. Keep the official selector here so the
    // generated refmap translates it in the distributable jar.
    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"), remap = true)
    private void mcaVampCompat$normalizeLegacyTraitNbt(CompoundTag data, CallbackInfo ci) {
        McaTraitBridge.normalizeLegacyTraitNbt(data);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"), remap = true)
    private void mcaVampirismCompat$migrateLegacyState(CompoundTag tag, CallbackInfo ci) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        LegacyStateMigrationService.migrate(villager, tag);
        McaNativeWerewolfProtectionService.clearLegacyNativeMarker(villager, tag);
    }

    @Inject(method = "playSurprisedSound", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$suppressVampireFearSound(CallbackInfo ci) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        if (McaSpecialVillagerMonsterIgnoreService.shouldSuppressFearDialogue(villager)) {
            ci.cancel();
        }
    }

    @Inject(method = "setVillagerData", at = @At("HEAD"), cancellable = true, remap = true)
    private void mcaVampirismCompat$requireMatchingFactionForSpecialistProfession(
            VillagerData data,
            CallbackInfo ci
    ) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        if (McaSpecialistProfessionService.shouldRejectMcaProfessionChange(villager, data.getProfession())) {
            ci.cancel();
        }
    }
}
