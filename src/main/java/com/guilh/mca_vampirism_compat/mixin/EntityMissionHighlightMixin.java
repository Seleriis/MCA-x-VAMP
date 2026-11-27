package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.mission.MissionHighlightClientState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds the outline only in the receiving client's local entity view. */
@Mixin(Entity.class)
public abstract class EntityMissionHighlightMixin {
    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void mcaVampCompat$showAcceptedMissionTargetOnlyLocally(CallbackInfoReturnable<Boolean> cir) {
        // Queried for every rendered entity each frame; almost always nothing
        // is highlighted, so leave before touching the entity.
        if (MissionHighlightClientState.isEmpty()) {
            return;
        }
        Entity entity = (Entity) (Object) this;
        if (entity.level().isClientSide && MissionHighlightClientState.isHighlighted(entity.getUUID())) {
            cir.setReturnValue(true);
        }
    }
}
