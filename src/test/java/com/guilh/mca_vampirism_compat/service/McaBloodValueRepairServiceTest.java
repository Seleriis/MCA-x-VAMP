package com.guilh.mca_vampirism_compat.service;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class McaBloodValueRepairServiceTest {
    @Test
    void usesVampirism121BloodSerializationKeys() {
        CompoundTag tag = McaBloodValueRepairService.createFallbackBloodTag(15);

        assertEquals(15, tag.getInt("max_blood"));
        assertEquals(15, tag.getInt("bloodLevel"));
        assertFalse(tag.contains("maxBlood"));
    }
}
