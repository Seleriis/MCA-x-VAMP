package com.guilh.mca_vampirism_compat.client;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.appearance.McaNativeEyeAppearanceBridge;
import com.guilh.mca_vampirism_compat.visual.McaVampireVisualData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import forge.net.conczin.mca.client.model.VillagerEntityModelMCA;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

/** Client-only red-eye overlay used when MCA 7.6 lacks native iris tinting. */
public final class LayerMcaVampireEyes
        extends RenderLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> {
    public LayerMcaVampireEyes(
            RenderLayerParent<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> parent
    ) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, VillagerEntityMCA villager,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!McaVampirismCompatConfig.vampireEyesEnabled()
                || !McaVampireVisualData.isVampireVisual(villager)
                // MCA 7.6.28 renders eye dyes in its own layer. Rendering a
                // complete illuminated head over it caused the black/red
                // square visible in the updated editor.
                || McaNativeEyeAppearanceBridge.supportsNativeEyeDyes(villager)) {
            return;
        }
        VillagerEntityModelMCA<VillagerEntityMCA> model = getParentModel();
        VisibilityState visibility = VisibilityState.capture(model);
        try {
            model.head.visible = true;
            model.hat.visible = false;
            model.body.visible = false;
            model.rightArm.visible = false;
            model.leftArm.visible = false;
            model.rightLeg.visible = false;
            model.leftLeg.visible = false;
            VertexConsumer consumer = buffers.getBuffer(RenderType.eyes(McaVampireEyeTextureResolver.getEyeTexture(villager)));
            model.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F);
        } finally {
            visibility.restore(model);
        }
    }

    private record VisibilityState(boolean head, boolean hat, boolean body, boolean rightArm,
                                   boolean leftArm, boolean rightLeg, boolean leftLeg) {
        static VisibilityState capture(HumanoidModel<?> model) {
            return new VisibilityState(model.head.visible, model.hat.visible, model.body.visible,
                    model.rightArm.visible, model.leftArm.visible, model.rightLeg.visible, model.leftLeg.visible);
        }

        void restore(HumanoidModel<?> model) {
            model.head.visible = head;
            model.hat.visible = hat;
            model.body.visible = body;
            model.rightArm.visible = rightArm;
            model.leftArm.visible = leftArm;
            model.rightLeg.visible = rightLeg;
            model.leftLeg.visible = leftLeg;
        }
    }
}
