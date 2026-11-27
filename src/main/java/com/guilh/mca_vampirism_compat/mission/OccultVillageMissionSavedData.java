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

    static final int FORMAT_VERSION = 2;
    private final Map<UUID, Map<MissionKey, MissionRecord>> records = new HashMap<>();
    private final Map<UUID, Map<MissionKey, MissionRecord>> active = new HashMap<>();
    private final Map<UUID, java.util.Set<OwnerKey>> byTarget = new HashMap<>();
    private final java.util.ArrayDeque<OwnerKey> cleanup = new java.util.ArrayDeque<>();
    private final java.util.ArrayDeque<OwnerKey> validation = new java.util.ArrayDeque<>();
    private final Map<MissionKey, Long> attacks = new HashMap<>();
    private final java.util.Set<OwnerKey> queuedValidation = new java.util.HashSet<>();
    private final java.util.Set<OwnerKey> queuedCleanup = new java.util.HashSet<>();
    private final java.util.ArrayDeque<MissionKey> attackCleanup = new java.util.ArrayDeque<>();
    private final Map<UUID, UUID> tracked = new HashMap<>();
    record OwnerKey(UUID player, MissionKey key) {}

    void recordAttack(MissionKey key, long now) {
        if (!attacks.containsKey(key)) attackCleanup.addLast(key);
        attacks.put(key, now); setDirty();
    }
    boolean attackedRecently(MissionKey key, long now) {
        Long tick = attacks.get(key);
        return tick != null && now >= tick && now - tick < 24000L;
    }
    UUID tracked(UUID player) { return tracked.get(player); }
    void track(UUID player, UUID target) {
        if (target == null) tracked.remove(player); else tracked.put(player, target);
        setDirty();
    }
    java.util.Set<OwnerKey> missionsFor(UUID target) {
        return java.util.Set.copyOf(byTarget.getOrDefault(target, java.util.Set.of()));
    }
    java.util.List<OwnerKey> validationBatch(int budget) {
        java.util.List<OwnerKey> batch = new ArrayList<>();
        int size = Math.min(budget, validation.size());
        while (size-- > 0) {
            OwnerKey owner = validation.removeFirst();
            if (get(owner.player(), owner.key()).filter(MissionRecord::isActive).isPresent()) {
                batch.add(owner); validation.addLast(owner);
            } else queuedValidation.remove(owner);
        }
        return batch;
    }
    int prune(long now, int budget) {
        int removed = 0;
        int count = Math.min(Math.max(0, budget), cleanup.size());
        while (count-- > 0) {
            OwnerKey owner = cleanup.removeFirst();
            queuedCleanup.remove(owner);
            Map<MissionKey, MissionRecord> map = records.get(owner.player());
            MissionRecord record = map == null ? null : map.get(owner.key());
            if (record == null || record.isActive()) continue;
            if (now < record.nextAvailableTick()) { cleanup.addLast(owner); queuedCleanup.add(owner); continue; }
            map.remove(owner.key());
            if (map.isEmpty()) records.remove(owner.player());
            removed++;
        }
        int attackBudget = Math.min(Math.max(0, budget), attackCleanup.size());
        while (attackBudget-- > 0) {
            MissionKey key = attackCleanup.removeFirst();
            if (attackedRecently(key, now)) attackCleanup.addLast(key);
            else { attacks.remove(key); setDirty(); }
        }
        if (removed > 0) setDirty();
        return removed;
    }

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
                    expiresAt,
                    entry.contains("Reward", Tag.TAG_INT) ? entry.getInt("Reward") : 5,
                    entry.getString("TargetName"), entry.getString("VillageName")
            );
            data.put(entry.getUUID("Player"), key, record);
        }
        ListTag attackEntries = tag.getList("RecentAttacks", Tag.TAG_COMPOUND);
        for (int i = 0; i < attackEntries.size(); i++) {
            CompoundTag entry = attackEntries.getCompound(i);
            ResourceLocation dimension = ResourceLocation.tryParse(entry.getString("Dimension"));
            if (dimension != null) data.recordAttack(new MissionKey(dimension, BlockPos.of(entry.getLong("Totem"))), entry.getLong("Tick"));
        }
        ListTag tracking = tag.getList("TrackedMissions", Tag.TAG_COMPOUND);
        for (int i = 0; i < tracking.size(); i++) {
            CompoundTag entry = tracking.getCompound(i);
            if (entry.hasUUID("Player") && entry.hasUUID("Target")) data.tracked.put(entry.getUUID("Player"), entry.getUUID("Target"));
        }
        return data;
    }

    Optional<MissionRecord> get(UUID player, MissionKey key) {
        return Optional.ofNullable(records.getOrDefault(player, Map.of()).get(key));
    }

    List<MissionEntry> activeMissions(UUID player) {
        List<MissionEntry> result = new ArrayList<>();
        active.getOrDefault(player, Map.of()).forEach((key, record) -> result.add(new MissionEntry(key, record)));
        return result;
    }

    void put(UUID player, MissionKey key, MissionRecord record) {
        OwnerKey owner = new OwnerKey(player, key);
        MissionRecord previous = records.computeIfAbsent(player, ignored -> new HashMap<>()).put(key, record);
        if (previous != null && previous.isActive()) {
            java.util.Set<OwnerKey> targets = byTarget.get(previous.target());
            if (targets != null) { targets.remove(owner); if (targets.isEmpty()) byTarget.remove(previous.target()); }
        }
        if (record.isActive()) {
            active.computeIfAbsent(player, ignored -> new HashMap<>()).put(key, record);
            byTarget.computeIfAbsent(record.target(), ignored -> new java.util.HashSet<>()).add(owner);
            if (queuedValidation.add(owner)) validation.addLast(owner);
        } else {
            Map<MissionKey, MissionRecord> map = active.get(player);
            if (map != null) { map.remove(key); if (map.isEmpty()) active.remove(player); }
            if (previous != null && previous.target() != null && previous.target().equals(tracked.get(player))) tracked.remove(player);
            if (queuedCleanup.add(owner)) cleanup.addLast(owner);
        }
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
                entry.putInt("Reward", record.reward());
                entry.putString("TargetName", record.targetName());
                entry.putString("VillageName", record.villageName());
                if (record.hasDeadline()) {
                    entry.putLong("ExpiresAt", record.expiresAtTick());
                }
            }
            entry.putLong("Created", record.createdTick());
            entry.putLong("NextAvailable", record.nextAvailableTick());
            entries.add(entry);
        }));
        tag.put("Missions", entries);
        tag.putInt("Version", FORMAT_VERSION);
        ListTag attackEntries = new ListTag();
        attacks.forEach((key, tick) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Dimension", key.dimension().toString()); entry.putLong("Totem", key.totemPosition().asLong());
            entry.putLong("Tick", tick); attackEntries.add(entry);
        });
        tag.put("RecentAttacks", attackEntries);
        ListTag tracking = new ListTag();
        tracked.forEach((player, target) -> {
            CompoundTag entry = new CompoundTag(); entry.putUUID("Player", player); entry.putUUID("Target", target); tracking.add(entry);
        });
        tag.put("TrackedMissions", tracking);
        return tag;
    }

    record MissionKey(ResourceLocation dimension, BlockPos totemPosition) {
    }

    record MissionEntry(MissionKey key, MissionRecord record) {
    }

    record MissionRecord(OccultVillageMissionType type, UUID target, long createdTick, long nextAvailableTick,
                         boolean accepted, int requiredCount, long expiresAtTick, int reward, String targetName, String villageName) {
        MissionRecord {
            reward = Math.max(0, Math.min(100, reward));
            targetName = targetName == null ? "" : targetName.substring(0, Math.min(256, targetName.length()));
            villageName = villageName == null ? "" : villageName.substring(0, Math.min(256, villageName.length()));
        }
        MissionRecord(OccultVillageMissionType type, UUID target, long createdTick, long nextAvailableTick,
                      boolean accepted, int requiredCount, long expiresAtTick) {
            this(type, target, createdTick, nextAvailableTick, accepted, requiredCount, expiresAtTick, 5, "", "");
        }
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
         * 2.0.32 through 2.0.38 created unanswered requests from the automatic
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
