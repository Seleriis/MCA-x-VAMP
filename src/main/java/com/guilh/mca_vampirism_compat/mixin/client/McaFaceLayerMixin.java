package com.guilh.mca_vampirism_compat.mixin.client;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.visual.McaVampireVisualData;
import com.mojang.blaze3d.vertex.PoseStack;
import forge.net.mca.client.render.layer.FaceLayer;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FaceLayer.class, remap = false)
public abstract class McaFaceLayerMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$hideMcaFaceForVampires(PoseStack poseStack, MultiBufferSource bufferSource,
                                                           int packedLight, LivingEntity entity,
                                                           float limbSwing, float limbSwingAmount, float partialTick,
                                                           float ageInTicks, float netHeadYaw, float headPitch,
                                                           CallbackInfo ci) {
        if (McaVampirismCompatConfig.removeMcaFaceForVampires()
                && McaVampirismCompatConfig.useTransparentMcaFaceForVampires()
                && entity instanceof VillagerEntityMCA villager
                && McaVampireVisualData.isVampireVisual(villager)) {
            ci.cancel();
        }
    }
}
