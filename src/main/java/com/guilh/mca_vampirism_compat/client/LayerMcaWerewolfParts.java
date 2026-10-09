package com.guilh.mca_vampirism_compat.client;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.visual.McaWerewolfVisualData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import forge.net.conczin.mca.client.model.VillagerEntityModelMCA;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class LayerMcaWerewolfParts extends RenderLayer<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> {
    private static final ResourceLocation DEBUG_VISIBLE_TEXTURE =
            new ResourceLocation("minecraft", "textures/block/gray_wool.png");

    public LayerMcaWerewolfParts(RenderLayerParent<VillagerEntityMCA, VillagerEntityModelMCA<VillagerEntityMCA>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, VillagerEntityMCA villager,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        boolean shouldRender = shouldRender(villager);
        logRenderCheck(villager, shouldRender);
        if (!shouldRender) {
            return;
        }

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(DEBUG_VISIBLE_TEXTURE));
        VillagerEntityModelMCA<VillagerEntityMCA> model = getParentModel();
        if (McaVampirismCompatConfig.werewolfVillagerEarsEnabled()) {
            renderEars(poseStack, consumer, packedLight, model.head);
        }
        if (McaVampirismCompatConfig.werewolfVillagerClawsEnabled()) {
            renderClaws(poseStack, consumer, packedLight, model.rightArm, true);
            renderClaws(poseStack, consumer, packedLight, model.leftArm, false);
        }
        logRendered(villager);
    }

    private boolean shouldRender(VillagerEntityMCA villager) {
        return McaVampirismCompatConfig.werewolfVillagerAppearanceEnabled()
                && (McaVampirismCompatConfig.werewolfVillagerEarsEnabled()
                || McaVampirismCompatConfig.werewolfVillagerClawsEnabled())
                && McaWerewolfVisualData.isWerewolfVisual(villager);
    }

    private static void renderEars(PoseStack poseStack, VertexConsumer consumer, int packedLight, ModelPart head) {
        poseStack.pushPose();
        head.translateAndRotate(poseStack);
        renderBox(poseStack, consumer, packedLight, -0.34F, -0.62F, -0.04F, -0.18F, -0.30F, 0.12F,
                91, 63, 45, 255);
        renderBox(poseStack, consumer, packedLight, 0.18F, -0.62F, -0.04F, 0.34F, -0.30F, 0.12F,
                91, 63, 45, 255);
        renderBox(poseStack, consumer, packedLight, -0.30F, -0.58F, -0.025F, -0.21F, -0.36F, 0.13F,
                134, 96, 71, 255);
        renderBox(poseStack, consumer, packedLight, 0.21F, -0.58F, -0.025F, 0.30F, -0.36F, 0.13F,
                134, 96, 71, 255);
        poseStack.popPose();
    }

    private static void renderClaws(PoseStack poseStack, VertexConsumer consumer, int packedLight, ModelPart arm,
                                    boolean rightArm) {
        poseStack.pushPose();
        arm.translateAndRotate(poseStack);
        float side = rightArm ? -1.0F : 1.0F;
        for (int index = 0; index < 3; index++) {
            float offset = (index - 1) * 0.045F;
            renderBox(poseStack, consumer, packedLight,
                    side * (0.12F + offset), 0.58F, -0.12F,
                    side * (0.16F + offset), 0.78F, -0.04F,
                    218, 218, 204, 255);
        }
        poseStack.popPose();
    }

    private static void renderBox(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                                  float minX, float minY, float minZ,
                                  float maxX, float maxY, float maxZ,
                                  int red, int green, int blue, int alpha) {
        float x1 = Math.min(minX, maxX);
        float x2 = Math.max(minX, maxX);
        float y1 = Math.min(minY, maxY);
        float y2 = Math.max(minY, maxY);
        float z1 = Math.min(minZ, maxZ);
        float z2 = Math.max(minZ, maxZ);

        PoseStack.Pose pose = poseStack.last();
        quad(consumer, pose, packedLight, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, 0.0F, 0.0F, 1.0F, red, green, blue, alpha);
        quad(consumer, pose, packedLight, x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, 0.0F, 0.0F, -1.0F, red, green, blue, alpha);
        quad(consumer, pose, packedLight, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, 0.0F, 1.0F, 0.0F, red, green, blue, alpha);
        quad(consumer, pose, packedLight, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, 0.0F, -1.0F, 0.0F, red, green, blue, alpha);
        quad(consumer, pose, packedLight, x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2, 1.0F, 0.0F, 0.0F, red, green, blue, alpha);
        quad(consumer, pose, packedLight, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, -1.0F, 0.0F, 0.0F, red, green, blue, alpha);
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, int packedLight,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float normalX, float normalY, float normalZ,
                             int red, int green, int blue, int alpha) {
        vertex(consumer, pose, packedLight, x1, y1, z1, 0.0F, 0.0F, normalX, normalY, normalZ, red, green, blue, alpha);
        vertex(consumer, pose, packedLight, x2, y2, z2, 1.0F, 0.0F, normalX, normalY, normalZ, red, green, blue, alpha);
        vertex(consumer, pose, packedLight, x3, y3, z3, 1.0F, 1.0F, normalX, normalY, normalZ, red, green, blue, alpha);
        vertex(consumer, pose, packedLight, x4, y4, z4, 0.0F, 1.0F, normalX, normalY, normalZ, red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, int packedLight,
                               float x, float y, float z,
                               float u, float v,
                               float normalX, float normalY, float normalZ,
                               int red, int green, int blue, int alpha) {
        consumer.vertex(pose.pose(), x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), normalX, normalY, normalZ)
                .endVertex();
    }

    private static void logRenderCheck(VillagerEntityMCA villager, boolean shouldRender) {
        if (!McaVampirismCompatConfig.werewolfVillagerAppearanceDebug() || villager.tickCount % 40 != 0) {
            return;
        }
        McaVampirismCompat.debugLog(
                "[WEREWOLF-APPEARANCE] render check villager={} convertedVisual={} enabled={} render={} texture={}",
                villager.getUUID(),
                McaWerewolfVisualData.isWerewolfVisual(villager),
                McaVampirismCompatConfig.werewolfVillagerAppearanceEnabled(),
                shouldRender,
                DEBUG_VISIBLE_TEXTURE
        );
    }

    private static void logRendered(VillagerEntityMCA villager) {
        if (!McaVampirismCompatConfig.werewolfVillagerAppearanceDebug() || villager.tickCount % 40 != 0) {
            return;
        }
        McaVampirismCompat.debugLog(
                "[WEREWOLF-APPEARANCE] rendered villager={} ears={} claws={}",
                villager.getUUID(),
                McaVampirismCompatConfig.werewolfVillagerEarsEnabled(),
                McaVampirismCompatConfig.werewolfVillagerClawsEnabled()
        );
    }
}
