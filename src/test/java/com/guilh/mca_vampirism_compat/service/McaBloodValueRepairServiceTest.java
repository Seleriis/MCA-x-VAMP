package com.guilh.mca_vampirism_compat.service;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class McaBloodValueRepairServiceTest {
    @Test
    void usesVampirism1201BloodSerializationKeys() {
        CompoundTag tag = McaBloodValueRepairService.createFallbackBloodTag(15);

        assertEquals(15, tag.getInt("maxBlood"));
        assertEquals(15, tag.getInt("bloodLevel"));
        assertFalse(tag.contains("max_blood"));
    }
}
