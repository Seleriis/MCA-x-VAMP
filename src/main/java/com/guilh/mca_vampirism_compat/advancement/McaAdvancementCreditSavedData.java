package com.guilh.mca_vampirism_compat.advancement;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Small server save used only when an attributable player is offline. */
final class McaAdvancementCreditSavedData extends SavedData {
    static final String ID = "mca_vamp_compat_advancement_credits";
    static final Factory<McaAdvancementCreditSavedData> FACTORY = new Factory<>(
            McaAdvancementCreditSavedData::new,
            McaAdvancementCreditSavedData::load,
            DataFixTypes.SAVED_DATA_MAP_DATA
    );

    private final Map<UUID, Set<ResourceLocation>> pending = new HashMap<>();

    private static McaAdvancementCreditSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        McaAdvancementCreditSavedData data = new McaAdvancementCreditSavedData();
        ListTag entries = tag.getList("Pending", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            if (!entry.hasUUID("Player")) {
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(entry.getString("Advancement"));
            if (id != null) {
                data.pending.computeIfAbsent(entry.getUUID("Player"), ignored -> new HashSet<>()).add(id);
            }
        }
        return data;
    }

    void add(UUID player, ResourceLocation advancement) {
        if (pending.computeIfAbsent(player, ignored -> new HashSet<>()).add(advancement)) {
            setDirty();
        }
    }

    Set<ResourceLocation> take(UUID player) {
        Set<ResourceLocation> credits = pending.remove(player);
        if (credits == null || credits.isEmpty()) {
            return Set.of();
        }
        setDirty();
        return Set.copyOf(credits);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        pending.forEach((player, ids) -> ids.forEach(id -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", player);
            entry.putString("Advancement", id.toString());
            entries.add(entry);
        }));
        tag.put("Pending", entries);
        return tag;
    }
}
