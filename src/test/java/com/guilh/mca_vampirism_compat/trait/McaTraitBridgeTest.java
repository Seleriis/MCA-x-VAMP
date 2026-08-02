package com.guilh.mca_vampirism_compat.trait;

import net.conczin.mca.entity.ai.Traits;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class McaTraitBridgeTest {
    @Test
    void registersCanonicalLowercaseTraitIds() {
        McaTraitBridge.registerTraits();

        Traits.Trait vampire = Traits.Trait.valueOf("vampirism_mca_compat:vampire");
        Traits.Trait werewolf = Traits.Trait.valueOf("vampirism_mca_compat:werewolf");
        Traits.Trait hunter = Traits.Trait.valueOf("vampirism_mca_compat:hunter");

        assertNotNull(vampire);
        assertNotNull(werewolf);
        assertNotNull(hunter);
        assertEquals("vampirism_mca_compat:vampire", vampire.id());
        assertEquals("vampirism_mca_compat:werewolf", werewolf.id());
        assertEquals("vampirism_mca_compat:hunter", hunter.id());
    }
}
