package com.guilh.mca_vampirism_compat.service;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

/**
 * Geometry only: translates MCA's inclusive building box into the AABB used
 * by Vampirism's fog and by this compat's village-aware policies.
 */
public final class McaVillageEffectBounds {
    private McaVillageEffectBounds() {
    }

    /**
     * Keeps MCA's complete horizontal footprint. MCA's {@link BoundingBox}
     * maximum is inclusive whereas {@link AABB}'s maximum is exclusive,
     * hence the {@code + 1} on each horizontal maximum.
     */
    public static AABB fromVillageBox(BoundingBox villageBox, AABB nativeArea,
                                      int fallbackMinY, int fallbackMaxY) {
        if (villageBox == null) {
            return nativeArea;
        }

        int minX = villageBox.minX();
        int maxXExclusive = villageBox.maxX() + 1;
        int minZ = villageBox.minZ();
        int maxZExclusive = villageBox.maxZ() + 1;
        if (maxXExclusive <= minX || maxZExclusive <= minZ) {
            return nativeArea;
        }

        double minY = nativeArea == null ? fallbackMinY : nativeArea.minY;
        double maxY = nativeArea == null ? fallbackMaxY : nativeArea.maxY;
        if (maxY <= minY) {
            return nativeArea;
        }
        return new AABB(minX, minY, minZ, maxXExclusive, maxY, maxZExclusive);
    }

    /**
     * Expands only the visual fog footprint. Callers keep using the original
     * logical area for MCA village policies such as Hunter control, book
     * discovery and the diagnostic outline.
     */
    public static AABB withHorizontalFogMargin(AABB logicalArea, int marginBlocks) {
        if (logicalArea == null) {
            return null;
        }
        int margin = Math.max(0, marginBlocks);
        if (margin == 0) {
            return logicalArea;
        }
        return new AABB(
                logicalArea.minX - margin,
                logicalArea.minY,
                logicalArea.minZ - margin,
                logicalArea.maxX + margin,
                logicalArea.maxY,
                logicalArea.maxZ + margin
        );
    }

    /**
     * Converts the logical MCA footprint into the representation expected by
     * Vampirism's LevelFog. The logical area uses an exclusive maximum so the
     * final MCA building block is contained by normal AABB checks. LevelFog
     * converts its AABB straight into an inclusive BoundingBox, so forwarding
     * that same maximum would add one horizontal block of fog.
     *
     * <p>Only the horizontal maxima are adjusted. The vertical limits remain
     * those supplied by the native totem area.</p>
     */
    public static AABB toArtificialFogArea(AABB logicalArea) {
        if (logicalArea == null) {
            return null;
        }
        return new AABB(
                logicalArea.minX,
                logicalArea.minY,
                logicalArea.minZ,
                logicalArea.maxX - 1.0D,
                logicalArea.maxY,
                logicalArea.maxZ - 1.0D
        );
    }

    /**
     * Encodes the exact fog hand-off area in a compact block-entity update
     * payload. All areas produced by this resolver are block-aligned; keeping
     * the six coordinates as integers avoids a client rebuilding the larger
     * native totem area after it receives an update.
     */
    public static int[] encodeArtificialFogArea(AABB fogArea) {
        if (fogArea == null) {
            return null;
        }
        return new int[]{
                (int) fogArea.minX,
                (int) fogArea.minY,
                (int) fogArea.minZ,
                (int) fogArea.maxX,
                (int) fogArea.maxY,
                (int) fogArea.maxZ
        };
    }

    /**
     * Decodes a derived fog hand-off area. Invalid or legacy data is ignored
     * so Vampirism's native fog remains the safe fallback on the client.
     */
    public static AABB decodeArtificialFogArea(int[] encodedArea) {
        if (encodedArea == null || encodedArea.length != 6
                || encodedArea[3] < encodedArea[0]
                || encodedArea[5] < encodedArea[2]
                || encodedArea[4] <= encodedArea[1]) {
            return null;
        }
        return new AABB(
                encodedArea[0], encodedArea[1], encodedArea[2],
                encodedArea[3], encodedArea[4], encodedArea[5]
        );
    }

    public static AABB union(AABB first, AABB second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return new AABB(
                Math.min(first.minX, second.minX),
                Math.min(first.minY, second.minY),
                Math.min(first.minZ, second.minZ),
                Math.max(first.maxX, second.maxX),
                Math.max(first.maxY, second.maxY),
                Math.max(first.maxZ, second.maxZ)
        );
    }

    /**
     * A tracked totem must be refreshed when it was previously linked to the
     * changed village, or when the newly calculated border now contains it.
     */
    public static boolean isAffectedByVillageChange(
            int trackedVillageId,
            int changedVillageId,
            boolean isInsideChangedVillage
    ) {
        return trackedVillageId == changedVillageId || isInsideChangedVillage;
    }
}
