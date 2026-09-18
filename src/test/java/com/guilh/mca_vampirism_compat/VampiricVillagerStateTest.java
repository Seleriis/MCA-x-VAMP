package com.guilh.mca_vampirism_compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VampiricVillagerStateTest {
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

        CompoundTag serialized = original.serializeNBT();
        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(serialized);

        assertTrue(restored.isConverted());
        assertTrue(restored.isVampireAppearanceApplied());
        assertTrue(restored.isFactionInheritanceProcessed());
        assertEquals(9, restored.getVampireEyeIndex());
        assertEquals("mca:test_skin", restored.getOriginalCustomSkin());
        assertEquals(3, restored.getAppearanceSnapshotVersion());
        assertEquals(0.35F, restored.getOriginalEyeBrightnessGene());
        assertEquals(0xFF336699, restored.getOriginalEyeDye());
        assertEquals(0xFF112233, restored.getOriginalEyeLeftDye());
        assertEquals(0xFF8A624B, restored.getOriginalSkinDye());
        assertTrue(restored.hasCureWeakness(200L));
    }

    @Test
    void upgradesVersionTwoSnapshotWithTheStillLiveOriginalSkinDye() {
        VampiricVillagerState original = new VampiricVillagerState();
        original.saveOriginalAppearance(
                0.4F,
                0.3F,
                0.8F,
                0.2F,
                "mca:test_skin",
                0.35F,
                0xFF336699,
                0xFF112233,
                0xFF000000
        );
        CompoundTag legacyVersionTwo = original.serializeNBT();
        legacyVersionTwo.putInt("AppearanceSnapshotVersion", 2);

        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(legacyVersionTwo);
        restored.upgradeOriginalSkinDyeSnapshot(0xFF8A624B);

        assertEquals(3, restored.getAppearanceSnapshotVersion());
        assertTrue(restored.hasOriginalSkinDyeSnapshot());
        assertEquals(0.4F, restored.getOriginalSkinGene());
        assertEquals(0.3F, restored.getOriginalMelaninGene());
        assertEquals(0.8F, restored.getOriginalHemoglobinGene());
        assertEquals(0xFF336699, restored.getOriginalEyeDye());
        assertEquals(0xFF112233, restored.getOriginalEyeLeftDye());
        assertEquals(0xFF8A624B, restored.getOriginalSkinDye());
    }

    @Test
    void readsLegacy103SharedCureFields() throws Exception {
        InputStream input = getClass().getResourceAsStream("/fixtures/vampiric-villager-1.0.3.snbt");
        assertNotNull(input);
        CompoundTag legacy = TagParser.parseTag(new String(input.readAllBytes(), StandardCharsets.UTF_8));

        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(legacy);

        assertTrue(restored.isConverted());
        assertTrue(restored.isVampireAppearanceApplied());
        assertEquals(7, restored.getVampireEyeIndex());
        assertTrue(restored.hasCureWeakness(11_000L));
        assertTrue(restored.hasCureGarlicInjection(11_000L));
    }

    @Test
    void savedEmptyMainHandRemainsMeaningfulWithoutEncodingAnEmptyItem() {
        VampiricVillagerState original = new VampiricVillagerState();
        original.saveOriginalMainHandItem(null);

        CompoundTag serialized = original.serializeNBT();

        assertTrue(serialized.getBoolean("OriginalMainHandItemSaved"));
        assertFalse(serialized.contains("OriginalMainHandItem"));

        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(serialized);
        assertTrue(restored.isOriginalMainHandItemSaved());
        assertFalse(restored.serializeNBT().contains("OriginalMainHandItem"));
    }

    @Test
    void persistsHunterWeaponSelectionWithoutChangingLegacyStateKeys() {
        VampiricVillagerState original = new VampiricVillagerState();
        original.setHunterWeaponSelection("stake");
        original.setHunterWeaponHand("off_hand");

        CompoundTag serialized = original.serializeNBT();
        assertEquals("stake", serialized.getString("HunterWeaponSelection"));
        assertEquals("off_hand", serialized.getString("HunterWeaponHand"));

        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(serialized);
        assertEquals("stake", restored.getHunterWeaponSelection());
        assertEquals("off_hand", restored.getHunterWeaponHand());
    }

    @Test
    void legacyEmptyMainHandCompoundDoesNotAttemptInvalidItemDecode() {
        CompoundTag legacy = new CompoundTag();
        legacy.putBoolean("OriginalMainHandItemSaved", true);
        legacy.put("OriginalMainHandItem", new CompoundTag());

        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(legacy);

        assertTrue(restored.isOriginalMainHandItemSaved());
        assertFalse(restored.serializeNBT().contains("OriginalMainHandItem"));
    }

    @Test
    void roundTripsOriginalScheduleAroundVampireNightSchedule() {
        VampiricVillagerState original = new VampiricVillagerState();
        original.beginVampireNightSchedule("guard", "guard_night");

        CompoundTag serialized = original.serializeNBT();
        VampiricVillagerState restored = new VampiricVillagerState();
        restored.deserializeNBT(serialized);

        assertTrue(restored.isVampireNightScheduleApplied());
        assertEquals("guard", restored.getOriginalMcaSchedule());
        assertEquals("guard_night", restored.getLastAppliedVampireSchedule());

        restored.clearVampireNightSchedule();
        assertFalse(restored.isVampireNightScheduleApplied());
        assertEquals("", restored.getOriginalMcaSchedule());
        assertEquals("", restored.getLastAppliedVampireSchedule());
    }
}
