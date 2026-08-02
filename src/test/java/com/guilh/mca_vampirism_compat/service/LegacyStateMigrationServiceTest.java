package com.guilh.mca_vampirism_compat.service;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LegacyStateMigrationServiceTest {
    @Test
    void findsLegacyStateInsideForgeCapsEnvelope() {
        CompoundTag state = new CompoundTag();
        state.putBoolean("Converted", true);
        CompoundTag forgeCaps = new CompoundTag();
        forgeCaps.put("mca_vampirism_compat:vampiric_villager", state);
        CompoundTag entity = new CompoundTag();
        entity.put("ForgeCaps", forgeCaps);

        CompoundTag found = LegacyStateMigrationService.findLegacyState(entity);

        assertNotNull(found);
        assertTrue(found.getBoolean("Converted"));
    }

    @Test
    void doesNotTreatUnrelatedCapabilitiesAsCompatState() {
        CompoundTag forgeCaps = new CompoundTag();
        forgeCaps.put("other_mod:data", new CompoundTag());
        CompoundTag entity = new CompoundTag();
        entity.put("ForgeCaps", forgeCaps);

        assertNull(LegacyStateMigrationService.findLegacyState(entity));
    }
}
