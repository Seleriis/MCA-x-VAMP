package com.guilh.mca_vampirism_compat;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.loading.LoadingModList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VampiricVillagerStateTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        if (LoadingModList.get() == null) {
            LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void roundTripsFactionCureAndAppearanceState() {
        VampiricVillagerState original = new VampiricVillagerState();
        original.setConverted(true);
        original.setVampireAppearanceApplied(true);
        original.markFactionInheritanceProcessed();
        original.assignVampireEyeIndex(9);
        original.saveOriginalAppearance(
                0.4F,
                0.3F,
                0.8F,
                0.2F,
                "mca:test_skin",
                0.35F,
                0xFF336699,
                0xFF112233,
                0xFF8A624B
        );
        original.markCureWeakness(100L, 500L, UUID.fromString("8d7ce326-613f-4d47-a393-1932f70bfcbb"));

        CompoundTag serialized = original.serializeNBT(RegistryAccess.EMPTY);
        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(RegistryAccess.EMPTY, serialized);

        assertTrue(restored.isConverted());
        assertTrue(restored.isVampireAppearanceApplied());
        assertTrue(restored.isFactionInheritanceProcessed());
        assertEquals(9, restored.getVampireEyeIndex());
        assertEquals("mca:test_skin", restored.getOriginalCustomSkin());
        assertEquals(2, restored.getAppearanceSnapshotVersion());
        assertEquals(0.35F, restored.getOriginalEyeBrightnessGene());
        assertEquals(0xFF336699, restored.getOriginalEyeDye());
        assertEquals(0xFF112233, restored.getOriginalEyeLeftDye());
        assertEquals(0xFF8A624B, restored.getOriginalSkinDye());
        assertTrue(restored.hasCureWeakness(200L));
    }

    @Test
    void readsLegacy103SharedCureFields() throws Exception {
        InputStream input = getClass().getResourceAsStream("/fixtures/vampiric-villager-1.0.3.snbt");
        assertNotNull(input);
        CompoundTag legacy = TagParser.parseTag(new String(input.readAllBytes(), StandardCharsets.UTF_8));

        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(RegistryAccess.EMPTY, legacy);

        assertTrue(restored.isConverted());
        assertTrue(restored.isVampireAppearanceApplied());
        assertEquals(7, restored.getVampireEyeIndex());
        assertTrue(restored.hasCureWeakness(11_000L));
        assertTrue(restored.hasCureGarlicInjection(11_000L));
    }

    @Test
    void savedEmptyMainHandRemainsMeaningfulWithoutEncodingAnEmptyItem() {
        VampiricVillagerState original = new VampiricVillagerState();
        original.saveOriginalMainHandItem(net.minecraft.world.item.ItemStack.EMPTY);

        CompoundTag serialized = original.serializeNBT(RegistryAccess.EMPTY);

        assertTrue(serialized.getBoolean("OriginalMainHandItemSaved"));
        assertFalse(serialized.contains("OriginalMainHandItem"));

        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(RegistryAccess.EMPTY, serialized);
        assertTrue(restored.isOriginalMainHandItemSaved());
        assertTrue(restored.getOriginalMainHandItem().isEmpty());
    }

    @Test
    void legacyEmptyMainHandCompoundDoesNotAttemptInvalidItemDecode() {
        CompoundTag legacy = new CompoundTag();
        legacy.putBoolean("OriginalMainHandItemSaved", true);
        legacy.put("OriginalMainHandItem", new CompoundTag());

        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(RegistryAccess.EMPTY, legacy);

        assertTrue(restored.isOriginalMainHandItemSaved());
        assertTrue(restored.getOriginalMainHandItem().isEmpty());
    }

    @Test
    void roundTripsOriginalScheduleAroundVampireNightSchedule() {
        VampiricVillagerState original = new VampiricVillagerState();
        original.beginVampireNightSchedule("guard", "guard_night");

        CompoundTag serialized = original.serializeNBT(RegistryAccess.EMPTY);
        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(RegistryAccess.EMPTY, serialized);

        assertTrue(restored.isVampireNightScheduleApplied());
        assertEquals("guard", restored.getOriginalMcaSchedule());
        assertEquals("guard_night", restored.getLastAppliedVampireSchedule());

        restored.clearVampireNightSchedule();
        assertFalse(restored.isVampireNightScheduleApplied());
        assertEquals("", restored.getOriginalMcaSchedule());
        assertEquals("", restored.getLastAppliedVampireSchedule());
    }
}
