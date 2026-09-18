package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaVillageEffectBoundsService;
import forge.net.mca.server.world.data.Village;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MCA is authoritative for editable village geometry. Its normal editor,
 * automatic scan and merge flows all call {@code calculateDimensions()}.
 */
@Mixin(value = Village.class, remap = false)
public abstract class McaVillageMixin {
    @Inject(method = "calculateDimensions()V", at = @At("TAIL"))
    private void mcaVampirismCompat$refreshEffectsAfterDimensionChange(CallbackInfo ci) {
        McaVillageEffectBoundsService.onMcaVillageBoundsChanged((Village) (Object) this);
    }

    @Inject(method = "removeBuilding(I)V", at = @At("TAIL"))
    private void mcaVampirismCompat$refreshEffectsAfterLastBuildingRemoval(int buildingId, CallbackInfo ci) {
        Village village = (Village) (Object) this;
        // Non-empty villages already notified from calculateDimensions(). MCA
        // deliberately skips it when the final building is removed.
        if (village.getBuildings().isEmpty()) {
            McaVillageEffectBoundsService.onMcaVillageBoundsChanged(village);
        }
    }
}
