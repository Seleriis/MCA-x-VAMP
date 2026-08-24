package com.guilh.mca_vampirism_compat.easyvillagers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Set;

/**
 * Repairs the 1.20.1-style empty item compounds left in older MCA and
 * Easy-Villagers snapshots. In 1.21.1 an empty ItemStack cannot be encoded
 * as {@code {}}, and attempting to decode one floods the client log.
 *
 * <p>The repair is deliberately narrow: it only touches empty compounds in
 * known item fields. All regular MCA, Easy Villagers and compat data remains
 * byte-for-byte intact.</p>
 */
public final class LegacyEmptyItemStackSanitizer {
    private static final Set<String> OPTIONAL_ITEM_FIELDS = Set.of(
            "OriginalMainHandItem",
            "OriginalOffHandItem"
    );
    private static final Set<String> EQUIPMENT_LIST_FIELDS = Set.of(
            "HandItems",
            "ArmorItems"
    );
    private static final Set<String> INVENTORY_LIST_FIELDS = Set.of(
            "Inventory",
            "Items"
    );

    private LegacyEmptyItemStackSanitizer() {
    }

    /**
     * Removes only malformed empty item compounds from an MCA entity/snapshot
     * tag. Returns whether the supplied tag was changed.
     */
    public static boolean sanitize(CompoundTag root) {
        return root != null && sanitizeCompound(root);
    }

    private static boolean sanitizeCompound(CompoundTag tag) {
        boolean changed = false;
        for (String key : Set.copyOf(tag.getAllKeys())) {
            Tag value = tag.get(key);
            if (value instanceof CompoundTag nested) {
                if (OPTIONAL_ITEM_FIELDS.contains(key) && nested.isEmpty()) {
                    tag.remove(key);
                    changed = true;
                } else {
                    changed |= sanitizeCompound(nested);
                }
            } else if (value instanceof ListTag list) {
                if (EQUIPMENT_LIST_FIELDS.contains(key)) {
                    changed |= sanitizeEquipmentList(list);
                } else if (INVENTORY_LIST_FIELDS.contains(key)) {
                    changed |= sanitizeInventoryList(list);
                } else {
                    changed |= sanitizeNestedCompounds(list);
                }
            }
        }
        return changed;
    }

    /** Equipment lists are positional, so invalid entries become air. */
    private static boolean sanitizeEquipmentList(ListTag list) {
        boolean changed = false;
        for (int index = 0; index < list.size(); index++) {
            Tag entry = list.get(index);
            if (isMalformedEmptyStack(entry)) {
                list.set(index, airStackTag());
                changed = true;
            } else if (entry instanceof CompoundTag nested) {
                changed |= sanitizeCompound(nested);
            }
        }
        return changed;
    }

    /** Inventory entries carry their own slot and can safely be removed. */
    private static boolean sanitizeInventoryList(ListTag list) {
        boolean changed = false;
        for (int index = list.size() - 1; index >= 0; index--) {
            Tag entry = list.get(index);
            if (isMalformedEmptyStack(entry)) {
                list.remove(index);
                changed = true;
            } else if (entry instanceof CompoundTag nested) {
                changed |= sanitizeCompound(nested);
            }
        }
        return changed;
    }

    private static boolean sanitizeNestedCompounds(ListTag list) {
        boolean changed = false;
        for (Tag entry : list) {
            if (entry instanceof CompoundTag nested) {
                changed |= sanitizeCompound(nested);
            }
        }
        return changed;
    }

    private static boolean isMalformedEmptyStack(Tag tag) {
        return tag instanceof CompoundTag compound && compound.isEmpty();
    }

    private static CompoundTag airStackTag() {
        CompoundTag air = new CompoundTag();
        air.putString("id", "minecraft:air");
        return air;
    }
}
