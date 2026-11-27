package com.guilh.mca_vampirism_compat.easyvillagers.readonly;

import com.guilh.mca_vampirism_compat.easyvillagers.McaEvStatePayload;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The resolver accepts real ItemStack snapshots. Its storage/capture paths
 * are verified under Forge GameTests, where the item and MCA registries are
 * actually bootstrapped.
 */
@Disabled("Requires a Forge-launched ItemStack; covered by McaVampCompatGameTests")
class StoredMcaFactionResolverTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void visualMcaSnapshotHasPriorityOverSanitizedEasyVillagerStack() {
        ItemStack visual = new ItemStack(Items.VILLAGER_SPAWN_EGG);
        ItemStack sanitized = new ItemStack(Items.EMERALD);

        StoredMcaFactionResolver.StackSelection selection =
                StoredMcaFactionResolver.selectPreferredFactionStack(visual, sanitized);

        assertSame(visual, selection.stack());
        assertEquals("MCA_EV_VISUAL", selection.source());
    }

    @Test
    void sanitizedStackRemainsSafeFallbackWhenVisualSnapshotIsMissing() {
        ItemStack sanitized = new ItemStack(Items.VILLAGER_SPAWN_EGG);

        StoredMcaFactionResolver.StackSelection selection =
                StoredMcaFactionResolver.selectPreferredFactionStack(ItemStack.EMPTY, sanitized);

        assertSame(sanitized, selection.stack());
        assertEquals("EASY_VILLAGERS_STORED", selection.source());
    }

    @Test
    void explicitVisualPayloadClassifiesHunterWithoutVanillaEntityState() {
        ItemStack visual = stackWithFaction(false, true, false);

        StoredMcaFactionResolver.StoredFactionSnapshot faction =
                StoredMcaFactionResolver.resolveFactionSnapshot(visual);

        assertTrue(faction.isMca());
        assertTrue(faction.isHunter());
        assertFalse(faction.isVampire());
        assertFalse(faction.isWerewolf());
        assertEquals("PAYLOAD_V2", faction.hunterSource());
    }

    @Test
    void explicitVisualPayloadClassifiesWerewolfParentForBreeder() {
        ItemStack visual = stackWithFaction(false, false, true);

        StoredMcaFactionResolver.StoredFactionSnapshot faction =
                StoredMcaFactionResolver.resolveFactionSnapshot(visual);

        assertTrue(faction.isMca());
        assertTrue(faction.isWerewolf());
        assertFalse(faction.isVampire());
        assertFalse(faction.isHunter());
    }

    @Test
    void legacyFalsePayloadDoesNotHideHunterTraitInVisualSnapshot() {
        ItemStack visual = stackWithFactionAndTrait(false, false, false,
                "vampirism_mca_compat:hunter");

        StoredMcaFactionResolver.StoredFactionSnapshot faction =
                StoredMcaFactionResolver.resolveFactionSnapshot(visual);

        assertTrue(faction.isMca());
        assertTrue(faction.isHunter());
        assertFalse(faction.isVampire());
        assertFalse(faction.isWerewolf());
        assertEquals("TRAIT", faction.hunterSource());
    }

    @Test
    void legacyFalsePayloadDoesNotHidePositivePreservedState() {
        ItemStack visual = stackWithFaction(false, false, false);
        CompoundTag root = visual.getTag().copy();
        root.getCompound(McaEvStatePayload.PAYLOAD_KEY)
                .getCompound("State")
                .putBoolean("HunterAligned", true);
        visual.setTag(root);

        StoredMcaFactionResolver.StoredFactionSnapshot faction =
                StoredMcaFactionResolver.resolveFactionSnapshot(visual);

        assertTrue(faction.isHunter());
        assertEquals("STATE", faction.hunterSource());
    }

    @Test
    void werewolfConvertedStateDoesNotBecomeVampireEvidence() {
        ItemStack visual = stackWithFaction(false, false, false);
        CompoundTag root = visual.getTag().copy();
        root.getCompound(McaEvStatePayload.PAYLOAD_KEY)
                .getCompound("State")
                .putBoolean("WerewolfConverted", true);
        visual.setTag(root);

        StoredMcaFactionResolver.StoredFactionSnapshot faction =
                StoredMcaFactionResolver.resolveFactionSnapshot(visual);

        assertFalse(faction.isVampire());
        assertFalse(faction.isHunter());
        assertTrue(faction.isWerewolf());
        assertEquals("STATE", faction.werewolfSource());
    }

    @Test
    void positiveEvidenceIsCollectedBeforeVampireHunterWerewolfExclusivity() {
        ItemStack visual = stackWithFactionAndTrait(true, true, true,
                "vampirism_mca_compat:werewolf");

        StoredMcaFactionResolver.StoredFactionSnapshot faction =
                StoredMcaFactionResolver.resolveFactionSnapshot(visual);

        assertTrue(faction.isVampire());
        assertFalse(faction.isHunter());
        assertFalse(faction.isWerewolf());
        assertEquals("PAYLOAD_V2", faction.vampireSource());
        assertEquals("PAYLOAD_V2", faction.hunterSource());
        assertEquals("PAYLOAD_V2+TRAIT", faction.werewolfSource());
    }

    @Test
    void fallbackVillagerDataIsReturnedAsDefensiveCopy() {
        ItemStack visual = stackWithFactionAndTrait(false, false, false,
                "vampirism_mca_compat:hunter");

        CompoundTag firstRead = StoredMcaFactionResolver.getVillagerData(visual);
        firstRead.putString("ChangedByTest", "yes");
        CompoundTag secondRead = StoredMcaFactionResolver.getVillagerData(visual);

        assertFalse(secondRead.contains("ChangedByTest"));
    }

    private static ItemStack stackWithFaction(boolean vampire, boolean hunter, boolean werewolf) {
        return stackWithFactionAndTrait(vampire, hunter, werewolf, null);
    }

    private static ItemStack stackWithFactionAndTrait(
            boolean vampire, boolean hunter, boolean werewolf, String trait) {
        CompoundTag payload = new CompoundTag();
        payload.putInt("Version", 2);
        payload.putBoolean("Vampire", vampire);
        payload.putBoolean("Hunter", hunter);
        payload.putBoolean("Werewolf", werewolf);
        payload.put("State", new CompoundTag());
        CompoundTag root = new CompoundTag();
        root.put(McaEvStatePayload.PAYLOAD_KEY, payload);
        if (trait != null) {
            CompoundTag villager = new CompoundTag();
            CompoundTag traits = new CompoundTag();
            traits.putBoolean(trait, true);
            villager.put("Traits", traits);
            root.put("villager", villager);
        }
        ItemStack stack = new ItemStack(Items.VILLAGER_SPAWN_EGG);
        stack.setTag(root);
        return stack;
    }
}
