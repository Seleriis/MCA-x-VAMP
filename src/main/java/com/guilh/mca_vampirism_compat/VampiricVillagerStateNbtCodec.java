package com.guilh.mca_vampirism_compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/** Version-specific ItemStack persistence for the otherwise version-neutral villager state. */
final class VampiricVillagerStateNbtCodec {
    static final String ORIGINAL_MAIN_HAND_ITEM = "OriginalMainHandItem";

    private VampiricVillagerStateNbtCodec() {
    }

    static void writeOriginalMainHand(
            CompoundTag tag,
            boolean originalMainHandItemSaved,
            ItemStack originalMainHandItem,
            HolderLookup.Provider registries
    ) {
        if (originalMainHandItemSaved && !originalMainHandItem.isEmpty()) {
            tag.put(ORIGINAL_MAIN_HAND_ITEM, originalMainHandItem.save(registries));
        }
    }

    static ItemStack readOriginalMainHand(HolderLookup.Provider registries, CompoundTag tag) {
        if (!tag.contains(ORIGINAL_MAIN_HAND_ITEM, Tag.TAG_COMPOUND)) {
            return ItemStack.EMPTY;
        }
        CompoundTag itemTag = tag.getCompound(ORIGINAL_MAIN_HAND_ITEM);
        return !itemTag.isEmpty() && itemTag.contains("id")
                ? ItemStack.parseOptional(registries, itemTag)
                : ItemStack.EMPTY;
    }
}
