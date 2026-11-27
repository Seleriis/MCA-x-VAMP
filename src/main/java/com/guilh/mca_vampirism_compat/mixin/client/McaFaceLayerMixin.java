package com.guilh.mca_vampirism_compat.mixin.client;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.visual.McaVampireVisualData;
import net.conczin.mca.client.render.layer.FaceLayer;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FaceLayer.class, remap = false)
public abstract class McaFaceLayerMixin {
    private static final int OPAQUE_MAX_BRIGHT_RED = 0xFFFF0000;

    @Inject(method = "getEyeColor", at = @At("RETURN"), cancellable = true)
    private void mcaVampirismCompat$useNativeBrightRedEyes(LivingEntity entity, float partialTick, boolean leftEye,
                                                            CallbackInfoReturnable<Integer> cir) {
        if (McaVampirismCompatConfig.vampireEyesEnabled()
                && entity instanceof VillagerEntityMCA villager
                && McaVampireVisualData.isVampireVisual(villager)) {
            // MCA 7.7 renders the iris as a colorized layer. Override its final
            // ARGB color instead of replacing the whole face texture.
            cir.setReturnValue(OPAQUE_MAX_BRIGHT_RED);
        }
    }
}
