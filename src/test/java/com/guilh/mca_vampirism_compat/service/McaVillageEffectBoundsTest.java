package com.guilh.mca_vampirism_compat.service;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaVillageEffectBoundsTest {
    @Test
    void convertsInclusiveMcaBoundsWhileKeepingNativeVerticalLimits() {
        AABB nativeArea = new AABB(-20, 60, -20, 20, 100, 20);

        AABB result = McaVillageEffectBounds.fromVillageBox(
                new BoundingBox(-12, 30, -8, 15, 90, 11),
                nativeArea,
                -64,
                320
        );

        assertEquals(-12, result.minX);
        assertEquals(16, result.maxX);
        assertEquals(-8, result.minZ);
        assertEquals(12, result.maxZ);
        assertEquals(60, result.minY);
        assertEquals(100, result.maxY);
    }

    @Test
    void convertsExclusiveLogicalMaximumForVampirismsInclusiveFogBox() {
        AABB logicalArea = new AABB(-12, 60, -8, 16, 100, 12);

        AABB fogArea = McaVillageEffectBounds.toArtificialFogArea(logicalArea);

        assertEquals(-12, fogArea.minX);
        assertEquals(15, fogArea.maxX,
                "The MCA maximum block must remain the final fog block, not gain X+1");
        assertEquals(-8, fogArea.minZ);
        assertEquals(11, fogArea.maxZ,
                "The MCA maximum block must remain the final fog block, not gain Z+1");
        assertEquals(60, fogArea.minY);
        assertEquals(100, fogArea.maxY,
                "The native totem's vertical fog limits must remain unchanged");
    }

    @Test
    void extendsOnlyTheFogByTheConfiguredBorderMargin() {
        AABB logicalArea = new AABB(-12, 60, -8, 16, 100, 12);

        AABB fogArea = McaVillageEffectBounds.toArtificialFogArea(
                McaVillageEffectBounds.withHorizontalFogMargin(logicalArea, 4)
        );

        assertEquals(-16, fogArea.minX);
        assertEquals(19, fogArea.maxX,
                "Four blocks beyond the original final X block must remain fogged");
        assertEquals(-12, fogArea.minZ);
        assertEquals(15, fogArea.maxZ,
                "Four blocks beyond the original final Z block must remain fogged");
        assertEquals(60, fogArea.minY);
        assertEquals(100, fogArea.maxY);
        assertEquals(logicalArea, McaVillageEffectBounds.withHorizontalFogMargin(logicalArea, 0),
                "A zero margin must preserve the exact logical MCA footprint");
    }

    @Test
    void preservesNullWhenThereIsNoLogicalAreaForFog() {
        assertNull(McaVillageEffectBounds.toArtificialFogArea(null));
    }

    @Test
    void serializesTheExactDerivedFogFootprintForTheClient() {
        AABB fogArea = new AABB(-12, 60, -8, 15, 100, 11);

        int[] encoded = McaVillageEffectBounds.encodeArtificialFogArea(fogArea);

        assertEquals(new AABB(-12, 60, -8, 15, 100, 11),
                McaVillageEffectBounds.decodeArtificialFogArea(encoded));
    }

    @Test
    void rejectsMalformedClientFogPayloads() {
        assertNull(McaVillageEffectBounds.decodeArtificialFogArea(null));
        assertNull(McaVillageEffectBounds.decodeArtificialFogArea(new int[]{0, 1, 2}));
        assertNull(McaVillageEffectBounds.decodeArtificialFogArea(new int[]{2, 60, 0, 1, 100, 3}));
        assertNull(McaVillageEffectBounds.decodeArtificialFogArea(new int[]{0, 100, 0, 1, 100, 1}));
    }

    @Test
    void keepsTheCompleteMcaFootprintWithoutAnArtificialRadiusCap() {
        AABB result = McaVillageEffectBounds.fromVillageBox(
                new BoundingBox(-1000, 0, -1000, 1000, 255, 1000),
                new AABB(0, 64, 0, 1, 128, 1),
                -64,
                320
        );

        assertEquals(2001, result.getXsize());
        assertEquals(2001, result.getZsize());
        assertEquals(-1000, result.minX);
        assertEquals(1001, result.maxX);
        assertEquals(-1000, result.minZ);
        assertEquals(1001, result.maxZ);
    }

    @Test
    void returnsNativeBoundsWhenNoMcaBoxIsAvailable() {
        AABB nativeArea = new AABB(1, 2, 3, 4, 5, 6);

        assertEquals(nativeArea, McaVillageEffectBounds.fromVillageBox(
                null,
                nativeArea,
                -64,
                320
        ));
        assertNull(McaVillageEffectBounds.fromVillageBox(
                null,
                null,
                -64,
                320
        ));
    }

    @Test
    void createsAUnionForVillagerReconciliation() {
        AABB result = McaVillageEffectBounds.union(
                new AABB(0, 60, 0, 10, 100, 10),
                new AABB(-5, 55, 2, 15, 110, 20)
        );

        assertEquals(new AABB(-5, 55, 0, 15, 110, 20), result);
    }

    @Test
    void onlyRefreshesTotemsPreviouslyOrNewlyAssociatedWithChangedVillage() {
        assertTrue(McaVillageEffectBounds.isAffectedByVillageChange(4, 4, false),
                "A shrink or final-building removal must refresh a formerly associated totem");
        assertTrue(McaVillageEffectBounds.isAffectedByVillageChange(-1, 4, true),
                "A village expansion must refresh a newly contained totem");
        assertFalse(McaVillageEffectBounds.isAffectedByVillageChange(3, 4, false),
                "Unrelated tracked totems must not be refreshed");
    }
}
