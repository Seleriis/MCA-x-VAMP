package com.guilh.mca_vampirism_compat.easyvillagers;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaEvBreedingEligibilityTest {
    @Test
    void recognizesOnlyAnEnabledCanonicalInfertileTrait() {
        CompoundTag canonical = new CompoundTag();
        CompoundTag traits = new CompoundTag();
        traits.putBoolean("mca:infertile", true);
        canonical.put("Traits", traits);
        assertTrue(McaEvBreedingEligibility.hasInfertileTrait(canonical));

        traits.putBoolean("mca:infertile", false);
        assertFalse(McaEvBreedingEligibility.hasInfertileTrait(canonical));

        CompoundTag legacy = new CompoundTag();
        CompoundTag legacyData = new CompoundTag();
        CompoundTag legacyTraits = new CompoundTag();
        legacyTraits.putBoolean("INFERTILE", true);
        legacyData.put("traits", legacyTraits);
        legacy.put("MCAData", legacyData);
        assertTrue(McaEvBreedingEligibility.hasInfertileTrait(legacy));

        CompoundTag unrelated = new CompoundTag();
        CompoundTag unrelatedTraits = new CompoundTag();
        unrelatedTraits.putBoolean("othermod:infertile", true);
        unrelated.put("Traits", unrelatedTraits);
        assertFalse(McaEvBreedingEligibility.hasInfertileTrait(unrelated));
    }
}
