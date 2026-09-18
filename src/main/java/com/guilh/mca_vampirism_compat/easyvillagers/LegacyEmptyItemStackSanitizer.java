package com.guilh.mca_vampirism_compat.easyvillagers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Set;

/**
 * Repairs the 1.20.1-style empty item compounds left in older MCA and
 * Easy-Villagers snapshots. In 1.21.1 positional entity-equipment lists
 * deliberately encode an empty slot as {@code {}}; {@code ItemStack}
 * decodes that through its optional codec. It must therefore remain empty --
 * rewriting it to {@code minecraft:air} is invalid in the non-air codec and
 * floods the log.
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
                if (INVENTORY_LIST_FIELDS.contains(key)) {
                    changed |= sanitizeInventoryList(list);
                } else {
                    changed |= sanitizeNestedCompounds(list);
                }
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
}
