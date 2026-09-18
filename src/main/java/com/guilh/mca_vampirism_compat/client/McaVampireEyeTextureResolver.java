package com.guilh.mca_vampirism_compat.client;

import com.guilh.mca_vampirism_compat.visual.McaVampireVisualData;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.resources.ResourceLocation;

/** Resolves a stable Vampirism eye overlay for MCA 7.6 renderers. */
public final class McaVampireEyeTextureResolver {
    private McaVampireEyeTextureResolver() {
    }

    public static ResourceLocation getEyeTexture(VillagerEntityMCA villager) {
        int index = Math.max(0, Math.min(15, McaVampireVisualData.getEyeIndex(villager)));
        return new ResourceLocation("vampirism", "textures/entity/vanilla/eyes" + index + ".png");
    }
}
