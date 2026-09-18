package com.guilh.mca_vampirism_compat.service;

import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaVillageOutlineServiceTest {
    @Test
    void samplesTheEntireHorizontalBoundaryWithoutExceedingTheParticleCap() {
        AABB area = new AABB(-256, -64, -256, 256, 320, 256);
        List<McaVillageOutlineService.OutlinePoint> points = McaVillageOutlineService.outlinePoints(area, 72.15D);

        assertFalse(points.isEmpty());
        assertTrue(points.size() <= 128, "An OP outline must have a strict per-update particle cap");
        assertTrue(points.stream().allMatch(point -> point.y() == 72.15D));
        assertTrue(points.stream().allMatch(point -> isOnBoundary(area, point)));
        assertTrue(points.stream().anyMatch(point -> point.z() == area.minZ));
        assertTrue(points.stream().anyMatch(point -> point.x() == area.maxX));
        assertTrue(points.stream().anyMatch(point -> point.z() == area.maxZ));
        assertTrue(points.stream().anyMatch(point -> point.x() == area.minX));
    }

    @Test
    void rejectsMissingOrFlatAreas() {
        assertTrue(McaVillageOutlineService.outlinePoints(null, 64.0D).isEmpty());
        assertTrue(McaVillageOutlineService.outlinePoints(new AABB(1, 2, 3, 1, 4, 8), 64.0D).isEmpty());
    }

    private static boolean isOnBoundary(AABB area, McaVillageOutlineService.OutlinePoint point) {
        return point.x() == area.minX || point.x() == area.maxX
                || point.z() == area.minZ || point.z() == area.maxZ;
    }
}
