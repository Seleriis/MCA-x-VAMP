package com.guilh.mca_vampirism_compat.trait;

import net.conczin.mca.entity.ai.Traits;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class McaTraitBridgeTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.bootStrap();
    }

    @Test
    void registersCanonicalLowercaseTraitIds() {
        McaTraitBridge.registerTraits();

        Traits.Trait vampire = Traits.get(ResourceLocation.parse("vampirism_mca_compat:vampire")).orElse(null);
        Traits.Trait werewolf = Traits.get(ResourceLocation.parse("vampirism_mca_compat:werewolf")).orElse(null);
        Traits.Trait hunter = Traits.get(ResourceLocation.parse("vampirism_mca_compat:hunter")).orElse(null);

        assertNotNull(vampire);
        assertNotNull(werewolf);
        assertNotNull(hunter);
        assertEquals("vampirism_mca_compat:vampire", vampire.getId().toString());
        assertEquals("vampirism_mca_compat:werewolf", werewolf.getId().toString());
        assertEquals("vampirism_mca_compat:hunter", hunter.getId().toString());
    }

    @Test
    void normalizesLegacyUppercaseTraitKeysBeforeMcaReadsThem() {
        CompoundTag traits = new CompoundTag();
        traits.putBoolean("VAMPIRISM_MCA_COMPAT:VAMPIRE", true);
        CompoundTag data = new CompoundTag();
        data.put("Traits", traits);

        assertEquals(true, McaTraitBridge.normalizeLegacyTraitNbt(data));
        assertEquals(true, data.getCompound("Traits").getBoolean("vampirism_mca_compat:vampire"));
        assertEquals(false, data.getCompound("Traits").contains("VAMPIRISM_MCA_COMPAT:VAMPIRE"));
    }
}
