package com.guilh.mca_vampirism_compat.appearance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaVampireAppearanceHandlerTest {
    @Test
    void selectsExactlyOneLighterMcaSkinTextureTone() {
        assertEquals(0.0F, McaVampireAppearanceHandler.oneToneLighterSkinGene(0.0F));
        assertEquals(0.0F, McaVampireAppearanceHandler.oneToneLighterSkinGene(0.2F));
        assertEquals(0.2F, McaVampireAppearanceHandler.oneToneLighterSkinGene(0.4F));
        assertEquals(0.6F, McaVampireAppearanceHandler.oneToneLighterSkinGene(1.0F));
    }

    @Test
    void lightensCustomSkinDyeWithoutChangingAlphaOrHueOrder() {
        int original = 0xFF8A624B;
        int lighter = McaVampireAppearanceHandler.oneToneLighterColor(original);

        assertEquals(0xFF, lighter >>> 24);
        assertTrue(((lighter >>> 16) & 0xFF) > ((original >>> 16) & 0xFF));
        assertTrue(((lighter >>> 8) & 0xFF) > ((original >>> 8) & 0xFF));
        assertTrue((lighter & 0xFF) > (original & 0xFF));
        assertEquals(0xFF000000, McaVampireAppearanceHandler.oneToneLighterColor(0xFF000000));
    }
}
