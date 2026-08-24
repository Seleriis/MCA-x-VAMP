package com.guilh.mca_vampirism_compat.easyvillagers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyEmptyItemStackSanitizerTest {
    @Test
    void removesOnlyLegacyEmptyItemCompounds() {
        CompoundTag root = new CompoundTag();
        root.put("OriginalMainHandItem", new CompoundTag());
        root.put("UnrelatedEmptyData", new CompoundTag());

        ListTag hands = new ListTag();
        hands.add(new CompoundTag());
        CompoundTag validHand = new CompoundTag();
        validHand.putString("id", "minecraft:stick");
        hands.add(validHand);
        root.put("HandItems", hands);

        ListTag inventory = new ListTag();
        inventory.add(new CompoundTag());
        CompoundTag validInventory = new CompoundTag();
        validInventory.putString("id", "minecraft:apple");
        validInventory.putByte("Slot", (byte) 4);
        inventory.add(validInventory);
        root.put("Inventory", inventory);

        assertTrue(LegacyEmptyItemStackSanitizer.sanitize(root));
        assertFalse(root.contains("OriginalMainHandItem"));
        assertTrue(root.contains("UnrelatedEmptyData"));
        assertEquals("minecraft:air", root.getList("HandItems", 10).getCompound(0).getString("id"));
        assertEquals("minecraft:stick", root.getList("HandItems", 10).getCompound(1).getString("id"));
        assertEquals(1, root.getList("Inventory", 10).size());
        assertEquals("minecraft:apple", root.getList("Inventory", 10).getCompound(0).getString("id"));
    }

    @Test
    void leavesValidAndUnrelatedListsUntouched() {
        CompoundTag root = new CompoundTag();
        ListTag unrelated = new ListTag();
        unrelated.add(new CompoundTag());
        CompoundTag kept = new CompoundTag();
        kept.putString("SomeData", "kept");
        unrelated.add(kept);
        root.put("Traits", unrelated);

        CompoundTag valid = new CompoundTag();
        valid.putString("id", "minecraft:iron_sword");
        root.put("OriginalMainHandItem", valid);

        assertFalse(LegacyEmptyItemStackSanitizer.sanitize(root));
        assertEquals(2, root.getList("Traits", 10).size());
        assertEquals("kept", root.getList("Traits", 10).getCompound(1).getString("SomeData"));
        assertEquals("minecraft:iron_sword", root.getCompound("OriginalMainHandItem").getString("id"));
    }
}
