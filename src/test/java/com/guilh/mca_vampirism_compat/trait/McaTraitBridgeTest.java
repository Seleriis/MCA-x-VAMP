package com.guilh.mca_vampirism_compat.trait;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class McaTraitBridgeTest {
    @Test
    void registersLegacyTraitIdsUsedBy120Saves() {
        assertEquals(List.of(
                        "vampirism_mca_compat:vampire",
                        "vampirism_mca_compat:werewolf",
                        "vampirism_mca_compat:hunter"
                ),
                McaTraitBridge.legacyTraitIds());
    }

    @Test
    void keepsLegacyUppercaseTraitKeysForMca76() {
        CompoundTag traits = new CompoundTag();
        traits.putBoolean("VAMPIRISM_MCA_COMPAT:VAMPIRE", true);
        CompoundTag data = new CompoundTag();
        data.put("Traits", traits);

        assertEquals(false, McaTraitBridge.normalizeLegacyTraitNbt(data));
        assertEquals(true, data.getCompound("Traits").getBoolean("VAMPIRISM_MCA_COMPAT:VAMPIRE"));
    }
}
