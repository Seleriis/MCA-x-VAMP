package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.monster.McaSpecialVillagerMonsterIgnoreService;
import com.guilh.mca_vampirism_compat.service.LegacyStateMigrationService;
import com.guilh.mca_vampirism_compat.service.McaEditorTraitSyncService;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.guilh.mca_vampirism_compat.service.McaNativeWerewolfProtectionService;
import com.guilh.mca_vampirism_compat.service.McaSpecialistProfessionService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.npc.VillagerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VillagerEntityMCA.class, remap = false)
public abstract class VillagerEntityMCAMixin {
    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void mcaVampCompat$normalizeLegacyTraitNbt(CompoundTag data, CallbackInfo ci) {
        McaTraitBridge.normalizeLegacyTraitNbt(data);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void mcaVampirismCompat$migrateLegacyState(CompoundTag tag, CallbackInfo ci) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        LegacyStateMigrationService.migrate(villager, tag);
        McaNativeWerewolfProtectionService.clearLegacyNativeMarker(villager, tag);
    }

    @Inject(method = "readAdditionalSaveDataForEditor", at = @At("TAIL"))
    private void mcaVampirismCompat$reconcileEditorFactionTraits(CompoundTag data, CallbackInfo ci) {
        if (!data.contains("Traits", Tag.TAG_COMPOUND)
                && (!data.contains("MCAData", Tag.TAG_COMPOUND)
                || !data.getCompound("MCAData").contains("Traits", Tag.TAG_COMPOUND))) {
            return;
        }
        McaEditorTraitSyncService.reconcileEditedTraits((VillagerEntityMCA) (Object) this);
    }

    @Inject(method = "playSurprisedSound", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$suppressVampireFearSound(CallbackInfo ci) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        if (McaSpecialVillagerMonsterIgnoreService.shouldSuppressFearDialogue(villager)) {
            ci.cancel();
        }
    }

    @Inject(method = "setVillagerData", at = @At("HEAD"), cancellable = true)
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
