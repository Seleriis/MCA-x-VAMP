package com.guilh.mca_vampirism_compat.mission;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Persistent per-player, per-totem mission state. It never stores villager data. */
final class OccultVillageMissionSavedData extends SavedData {
    static final String ID = "mca_vamp_compat_village_missions";
    static final Factory<OccultVillageMissionSavedData> FACTORY = new Factory<>(
            OccultVillageMissionSavedData::new,
            OccultVillageMissionSavedData::load,
            DataFixTypes.SAVED_DATA_MAP_DATA
    );

    private final Map<UUID, Map<MissionKey, MissionRecord>> records = new HashMap<>();

    static OccultVillageMissionSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        OccultVillageMissionSavedData data = new OccultVillageMissionSavedData();
        ListTag entries = tag.getList("Missions", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            if (!entry.hasUUID("Player") || !entry.contains("Totem") || !entry.contains("Dimension", Tag.TAG_STRING)) {
                continue;
            }
            ResourceLocation dimension = ResourceLocation.tryParse(entry.getString("Dimension"));
            if (dimension == null) {
                continue;
            }
            OccultVillageMissionType type = null;
            String rawType = entry.getString("Type");
            if (!rawType.isBlank()) {
                try {
                    type = OccultVillageMissionType.valueOf(rawType);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
            }
            UUID target = entry.hasUUID("Target") ? entry.getUUID("Target") : null;
            if (type != null && target == null) {
                continue;
            }
            MissionKey key = new MissionKey(dimension, BlockPos.of(entry.getLong("Totem")));
            // 2.0.32 did not require an explicit acceptance. Its saved tasks
            // stay completable when upgrading instead of being stranded as
            // unanswered invitations.
            boolean accepted = type != null && (!entry.contains("Accepted", Tag.TAG_BYTE) || entry.getBoolean("Accepted"));
            int requiredCount = type == null ? 0 : type.sanitizeRequiredCount(
                    entry.contains("RequiredCount", Tag.TAG_INT) ? entry.getInt("RequiredCount") : type.requiredCount()
            );
            // Requests saved before deadlines were introduced remain valid.
            // Starting a timer from their old creation tick could expire a
            // player's accepted request immediately after upgrading.
            long expiresAt = entry.contains("ExpiresAt", Tag.TAG_LONG) ? entry.getLong("ExpiresAt") : -1L;
            MissionRecord record = new MissionRecord(
                    type, target, entry.getLong("Created"), entry.getLong("NextAvailable"), accepted, requiredCount,
                    expiresAt
            );
            data.records.computeIfAbsent(entry.getUUID("Player"), ignored -> new HashMap<>()).put(key, record);
        }
        return data;
    }

    Optional<MissionRecord> get(UUID player, MissionKey key) {
        return Optional.ofNullable(records.getOrDefault(player, Map.of()).get(key));
    }

    List<MissionEntry> activeMissions(UUID player) {
        List<MissionEntry> active = new ArrayList<>();
        records.getOrDefault(player, Map.of()).forEach((key, record) -> {
            if (record.isActive()) {
                active.add(new MissionEntry(key, record));
            }
        });
        return active;
    }

    void put(UUID player, MissionKey key, MissionRecord record) {
        records.computeIfAbsent(player, ignored -> new HashMap<>()).put(key, record);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        records.forEach((player, byTotem) -> byTotem.forEach((key, record) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", player);
            entry.putString("Dimension", key.dimension().toString());
            entry.putLong("Totem", key.totemPosition().asLong());
            if (record.type() != null) {
                entry.putString("Type", record.type().name());
                entry.putUUID("Target", record.target());
                entry.putBoolean("Accepted", record.accepted());
                entry.putInt("RequiredCount", record.requiredCount());
                if (record.hasDeadline()) {
                    entry.putLong("ExpiresAt", record.expiresAtTick());
                }
            }
            entry.putLong("Created", record.createdTick());
            entry.putLong("NextAvailable", record.nextAvailableTick());
            entries.add(entry);
        }));
        tag.put("Missions", entries);
        return tag;
    }

    record MissionKey(ResourceLocation dimension, BlockPos totemPosition) {
    }

    record MissionEntry(MissionKey key, MissionRecord record) {
    }

    record MissionRecord(OccultVillageMissionType type, UUID target, long createdTick, long nextAvailableTick,
                         boolean accepted, int requiredCount, long expiresAtTick) {
        MissionRecord(OccultVillageMissionType type, UUID target, long createdTick, long nextAvailableTick,
                      boolean accepted) {
            this(type, target, createdTick, nextAvailableTick, accepted, type == null ? 0 : type.requiredCount(), -1L);
        }

        MissionRecord(OccultVillageMissionType type, UUID target, long createdTick, long nextAvailableTick,
                      boolean accepted, int requiredCount) {
            this(type, target, createdTick, nextAvailableTick, accepted, requiredCount, -1L);
        }

        boolean isActive() {
            return type != null && target != null;
        }

        boolean isPending() {
            return isActive() && !accepted;
        }

        boolean isAccepted() {
            return isActive() && accepted;
        }

        /**
         * 2.0.32–2.0.38 created unanswered requests from the automatic
         * village scan and assigned them a deadline immediately. Conversation
         * requests never receive a deadline before acceptance.
         */
        boolean isLegacyPending() {
            return isPending() && hasDeadline();
        }

        boolean hasDeadline() {
            return expiresAtTick > 0L;
        }

        boolean isExpired(long gameTime) {
            return hasDeadline() && gameTime >= expiresAtTick;
        }
    }
}
