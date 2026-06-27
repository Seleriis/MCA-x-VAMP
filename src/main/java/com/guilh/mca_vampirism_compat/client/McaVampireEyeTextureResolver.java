package com.guilh.mca_vampirism_compat.client;

import com.guilh.mca_vampirism_compat.visual.McaVampireVisualData;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.resources.ResourceLocation;

public final class McaVampireEyeTextureResolver {
    private static final int MIN_VAMPIRISM_EYE_TEXTURE = 0;
    private static final int MAX_VAMPIRISM_EYE_TEXTURE = 15;

    private McaVampireEyeTextureResolver() {
    }

    public static ResourceLocation getEyeTexture(VillagerEntityMCA villager) {
        int textureIndex = clamp(McaVampireVisualData.getEyeIndex(villager), MIN_VAMPIRISM_EYE_TEXTURE, MAX_VAMPIRISM_EYE_TEXTURE);
        return new ResourceLocation("vampirism", "textures/entity/vanilla/eyes" + textureIndex + ".png");
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
