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
    void normalizesLegacyUppercaseTraitKeysForMca77() {
        CompoundTag traits = new CompoundTag();
        traits.putBoolean("VAMPIRISM_MCA_COMPAT:VAMPIRE", true);
        CompoundTag data = new CompoundTag();
        data.put("Traits", traits);

        assertEquals(true, McaTraitBridge.normalizeLegacyTraitNbt(data));
        assertEquals(true, data.getCompound("Traits").getBoolean("vampirism_mca_compat:vampire"));
        assertEquals(false, data.getCompound("Traits").contains("VAMPIRISM_MCA_COMPAT:VAMPIRE"));
    }

    @Test
    void normalizesLegacyTraitsInsideMcaEditorData() {
        CompoundTag mcaTraits = new CompoundTag();
        mcaTraits.putBoolean("VAMPIRISM_MCA_COMPAT:HUNTER", true);
        CompoundTag mcaData = new CompoundTag();
        mcaData.put("Traits", mcaTraits);
        CompoundTag data = new CompoundTag();
        data.put("MCAData", mcaData);

        assertEquals(true, McaTraitBridge.normalizeLegacyTraitNbt(data));
        assertEquals(true, data.getCompound("MCAData").getCompound("Traits")
                .getBoolean("vampirism_mca_compat:hunter"));
        assertEquals(false, data.getCompound("MCAData").getCompound("Traits")
                .contains("VAMPIRISM_MCA_COMPAT:HUNTER"));
    }
}
