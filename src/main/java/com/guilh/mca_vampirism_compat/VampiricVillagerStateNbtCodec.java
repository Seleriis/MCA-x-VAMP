package com.guilh.mca_vampirism_compat;

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
            ItemStack originalMainHandItem
    ) {
        if (originalMainHandItemSaved && originalMainHandItem != null && !originalMainHandItem.isEmpty()) {
            tag.put(ORIGINAL_MAIN_HAND_ITEM, originalMainHandItem.save(new CompoundTag()));
        }
    }

    static ItemStack readOriginalMainHand(CompoundTag tag) {
        if (!tag.contains(ORIGINAL_MAIN_HAND_ITEM, Tag.TAG_COMPOUND)) {
            return null;
        }
        CompoundTag itemTag = tag.getCompound(ORIGINAL_MAIN_HAND_ITEM);
        return !itemTag.isEmpty() && itemTag.contains("id") ? ItemStack.of(itemTag) : null;
    }
}
