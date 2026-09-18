package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.mixin.accessor.TotemBlockEntityAccessor;
import com.guilh.mca_vampirism_compat.mixin.accessor.VillageAccessor;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.world.ITotem;
import de.teamlapen.vampirism.blockentity.TotemBlockEntity;
import de.teamlapen.vampirism.util.TotemHelper;
import forge.net.mca.server.world.data.Village;
import forge.net.mca.server.world.data.VillageManager;
import forge.net.mca.util.BlockBoxExtended;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Bridges the editable MCA village footprint to visual fog and compat-owned
 * village policies. It deliberately never writes Vampirism's villageArea: the
 * native totem remains authoritative for capture, raids, POIs and spawning.
 */
public final class McaVillageEffectBoundsService {
    private static final String VAMPIRE_FACTION_ID = "vampirism:vampire";
    private static final String HUNTER_FACTION_ID = "vampirism:hunter";
    /** Sent with the ordinary totem update tag only when MCA bounds override native fog. */
    private static final String CLIENT_FOG_AREA_KEY = "mca_vamp_compat_fog_area";
    private static final int NO_MCA_VILLAGE_ID = -1;
    private static final Map<ServerLevel, Map<BlockPos, TrackedTotem>> TRACKED_TOTEMS = new WeakHashMap<>();
    private static final Set<TotemBlockEntity> REFRESHING = java.util.Collections.newSetFromMap(new IdentityHashMap<>());

    private McaVillageEffectBoundsService() {
    }

    /** Registers a loaded or changed totem and applies the fog without delay. */
    public static void onTotemChanged(TotemBlockEntity totem) {
        if (totem == null || !(totem.getLevel() instanceof ServerLevel level) || totem.isRemoved()) {
            return;
        }
        refresh(level, totem);
    }

    /**
     * Adds the server-resolved fog footprint to Vampirism's normal totem
     * update packet. Vampirism serializes its native village area too; without
     * this companion value the client reconstructs that larger native fog on
     * every block-entity update.
     */
    public static void writeClientFogArea(TotemBlockEntity totem, CompoundTag updateTag) {
        if (totem == null || updateTag == null || !(totem.getLevel() instanceof ServerLevel level)
                || totem.isRemoved()) {
            return;
        }
        AABB nativeArea = safeNativeArea(totem);
        ResolvedArea resolved = resolve(level, totem, nativeArea);
        if (!usesDerivedFog(totem, resolved)) {
            updateTag.remove(CLIENT_FOG_AREA_KEY);
            return;
        }
        AABB fogArea = fogAreaFor(totem, nativeArea, resolved);
        int[] encodedArea = McaVillageEffectBounds.encodeArtificialFogArea(fogArea);
        if (encodedArea == null) {
            updateTag.remove(CLIENT_FOG_AREA_KEY);
            return;
        }
        updateTag.putIntArray(CLIENT_FOG_AREA_KEY, encodedArea);
    }

    /**
     * Reapplies a server-derived footprint after Vampirism has loaded the
     * native area from the same update tag. This runs only on the logical
     * client and leaves all native totem data untouched.
     */
    public static void applyClientFogArea(TotemBlockEntity totem, CompoundTag updateTag) {
        if (totem == null || updateTag == null || totem.getLevel() == null
                || !totem.getLevel().isClientSide
                || !updateTag.contains(CLIENT_FOG_AREA_KEY, Tag.TAG_INT_ARRAY)) {
            return;
        }
        AABB fogArea = McaVillageEffectBounds.decodeArtificialFogArea(
                updateTag.getIntArray(CLIENT_FOG_AREA_KEY)
        );
        if (fogArea == null) {
            McaVampirismCompat.debugLog(
                    "[MCA-VILLAGE-BOUNDS] ignored malformed client fog payload totem={}",
                    totem.getBlockPos()
            );
            return;
        }
        VampirismAPI.getVampirismWorld(totem.getLevel()).ifPresent(
                fog -> fog.updateArtificialFogBoundingBox(totem.getBlockPos(), fogArea)
        );
    }

    /** Stops tracking a chunk-unloaded totem and clears its derived fog. */
    public static void onTotemChunkUnloaded(TotemBlockEntity totem) {
        unregister(totem, false);
    }

    /** Stops tracking a permanently removed totem and immediately reconciles Hunters. */
    public static void onTotemRemoved(TotemBlockEntity totem) {
        unregister(totem, true);
    }

    private static void unregister(TotemBlockEntity totem, boolean reconcileHunters) {
        if (totem == null || !(totem.getLevel() instanceof ServerLevel level)) {
            return;
        }
        TrackedTotem previous = tracked(level).remove(totem.getBlockPos());
        clearTrackedTotem(level, totem.getBlockPos(), previous, reconcileHunters);
    }

    /**
     * Re-evaluates only totems affected by MCA's own village geometry update.
     * MCA invokes this after player edits, automatic scans and village merges.
     */
    public static void onMcaVillageBoundsChanged(Village village) {
        if (village == null || !McaVampirismCompatConfig.mcaVillageEffectsFollowMcaBounds()) {
            return;
        }
        ServerLevel level = villageWorld(village);
        if (level == null) {
            return;
        }
        Map<BlockPos, TrackedTotem> entries = tracked(level);
        if (entries.isEmpty()) {
            return;
        }

        int villageId = village.getId();
        boolean hasBuildings = !village.getBuildings().isEmpty();
        for (Map.Entry<BlockPos, TrackedTotem> entry : new ArrayList<>(entries.entrySet())) {
            TrackedTotem tracked = entry.getValue();
            TotemBlockEntity totem = tracked.totem();
            if (totem == null || totem.isRemoved() || totem.getLevel() != level) {
                entries.remove(entry.getKey());
                clearTrackedTotem(level, entry.getKey(), tracked, true);
                continue;
            }
            boolean isNowAssociated = hasBuildings && containsVillage(village, totem.getBlockPos());
            if (McaVillageEffectBounds.isAffectedByVillageChange(
                    tracked.mcaVillageId(), villageId, isNowAssociated
            )) {
                refresh(level, totem);
            }
        }
    }

    /**
     * Reapplies the current configuration to loaded totems after the common
     * TOML is reloaded. This remains event-driven and never adds polling.
     */
    public static void refreshTrackedTotems() {
        for (Map.Entry<ServerLevel, Map<BlockPos, TrackedTotem>> levelEntry
                : new ArrayList<>(TRACKED_TOTEMS.entrySet())) {
            ServerLevel level = levelEntry.getKey();
            Map<BlockPos, TrackedTotem> entries = levelEntry.getValue();
            if (level == null || entries == null || entries.isEmpty()) {
                continue;
            }
            for (Map.Entry<BlockPos, TrackedTotem> entry : new ArrayList<>(entries.entrySet())) {
                TrackedTotem tracked = entry.getValue();
                TotemBlockEntity totem = tracked.totem();
                if (totem == null || totem.isRemoved() || totem.getLevel() != level) {
                    entries.remove(entry.getKey());
                    clearTrackedTotem(level, entry.getKey(), tracked, true);
                    continue;
                }
                refresh(level, totem);
            }
        }
    }

    /**
     * Discards process-local references when a server stops or an integrated
     * world is replaced. Totem chunk hooks handle normal gameplay cleanup,
     * while this protects the less common world-switch lifecycle.
     */
    public static void clearTransientState() {
        TRACKED_TOTEMS.clear();
        REFRESHING.clear();
    }

    /**
     * Returns the derived MCA bounds when enabled and associated, otherwise
     * the untouched native totem bounds.
     */
    public static AABB getEffectiveArea(ITotem totem) {
        if (totem == null) {
            return null;
        }
        AABB nativeArea = safeNativeArea(totem);
        if (!McaVampirismCompatConfig.mcaVillageEffectsFollowMcaBounds()
                || !(totem instanceof TotemBlockEntity blockEntity)
                || !(blockEntity.getLevel() instanceof ServerLevel level)) {
            return nativeArea;
        }
        TrackedTotem tracked = tracked(level).get(blockEntity.getBlockPos());
        if (tracked != null && tracked.totem() == blockEntity) {
            return tracked.area();
        }
        return resolve(level, blockEntity, nativeArea).area();
    }

    /**
     * Read-only data used by OP diagnostics and the temporary outline. Keeping
     * it beside the resolver guarantees that every surface describes the same
     * bounds used by fog, Hunter control and book discovery.
     */
    public static Optional<VillageAreaInfo> describeArea(TotemBlockEntity totem) {
        if (totem == null || !(totem.getLevel() instanceof ServerLevel level) || totem.isRemoved()) {
            return Optional.empty();
        }
        AABB nativeArea = safeNativeArea(totem);
        ResolvedArea resolved = resolve(level, totem, nativeArea);
        Optional<Village> village = resolved.usesMcaBounds()
                ? findContainingVillage(level, totem.getBlockPos())
                : Optional.empty();
        AABB villageArea = village
                .map(Village::getBox)
                .map(box -> new AABB(
                        box.minX(), level.getMinBuildHeight(), box.minZ(),
                        box.maxX() + 1.0D, level.getMaxBuildHeight(), box.maxZ() + 1.0D
                ))
                .orElse(null);
        return Optional.of(new VillageAreaInfo(
                factionId(totem),
                nativeArea,
                resolved.area(),
                fogAreaFor(totem, nativeArea, resolved),
                villageArea,
                resolved.usesMcaBounds(),
                resolved.mcaVillageId()
        ));
    }

    /** Returns a stable identifier without creating a hard Werewolves API link. */
    public static String factionId(ITotem totem) {
        if (totem == null) {
            return "unknown";
        }
        try {
            IFaction<?> faction = totem.getControllingFaction();
            return faction == null ? "unknown" : String.valueOf(faction.getID());
        } catch (RuntimeException | LinkageError ignored) {
            return "unknown";
        }
    }

    /** Finds an active loaded totem whose compat-effective area contains the position. */
    public static Optional<TotemBlockEntity> findActiveTotemAt(ServerLevel level, BlockPos position) {
        if (level == null || position == null) {
            return Optional.empty();
        }
        Optional<TotemBlockEntity> tracked = tracked(level).values().stream()
                .map(TrackedTotem::totem)
                .filter(totem -> totem != null && !totem.isRemoved())
                .filter(McaVillageEffectBoundsService::isActiveVillageTotem)
                .filter(totem -> contains(getTrackedOrResolvedArea(level, totem), position))
                .min(Comparator.comparingDouble(totem -> distanceSquared(totem.getBlockPos(), position)));
        if (tracked.isPresent()) {
            return tracked;
        }

        try {
            return TotemHelper.getTotemNearPos(level, position, false)
                    .filter(TotemBlockEntity.class::isInstance)
                    .map(TotemBlockEntity.class::cast)
                    .filter(McaVillageEffectBoundsService::isActiveVillageTotem)
                    .filter(totem -> contains(getEffectiveArea(totem), position));
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[MCA-VILLAGE-BOUNDS] native totem lookup failed level={} pos={} error={}",
                    level.dimension().location(), position, exception.toString()
            );
            return Optional.empty();
        }
    }

    public static boolean isActiveVillageTotem(ITotem totem) {
        if (!(totem instanceof TotemBlockEntityAccessor accessor)
                || !(totem instanceof BlockEntity blockEntity)
                || blockEntity.isRemoved()) {
            return false;
        }
        return accessor.mcaVampCompat$isComplete()
                && accessor.mcaVampCompat$isInsideVillage()
                && !accessor.mcaVampCompat$isDisabled()
                && accessor.mcaVampCompat$getVillagePois() != null
                && !accessor.mcaVampCompat$getVillagePois().isEmpty();
    }

    private static void refresh(ServerLevel level, TotemBlockEntity totem) {
        if (!REFRESHING.add(totem)) {
            return;
        }
        try {
            AABB nativeArea = safeNativeArea(totem);
            ResolvedArea resolved = resolve(level, totem, nativeArea);
            Map<BlockPos, TrackedTotem> entries = tracked(level);
            boolean hunterControlled = isHunterFaction(totem.getControllingFaction());
            AABB fogArea = fogAreaFor(totem, nativeArea, resolved);
            TrackedTotem previous = entries.put(
                    totem.getBlockPos(),
                    new TrackedTotem(
                            totem,
                            resolved.area(),
                            fogArea,
                            resolved.usesMcaBounds(),
                            hunterControlled,
                            resolved.mcaVillageId()
                    )
            );

            updateFog(level, totem.getBlockPos(), fogArea);
            boolean hunterControlChanged = previous != null && previous.hunterControlled() != hunterControlled;
            if ((previous == null || !sameArea(previous.area(), resolved.area()) || hunterControlChanged)
                    && ((previous != null && previous.hunterControlled()) || hunterControlled)) {
                McaVillageFactionControlService.syncVillagersForEffectiveBoundsChange(
                        level,
                        previous == null ? null : previous.area(),
                        resolved.area()
                );
            }

            // MCA village edits do not change the native totem NBT. Emit a
            // normal block-entity update when the derived fog changed so the
            // client receives the exact MCA footprint immediately as well.
            if (previous == null
                    || previous.usesMcaBounds() != resolved.usesMcaBounds()
                    || !sameArea(previous.fogArea(), fogArea)) {
                totem.setChanged();
            }
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[MCA-VILLAGE-BOUNDS] refresh failed level={} totem={} error={}",
                    level.dimension().location(), totem.getBlockPos(), exception.toString()
            );
        } finally {
            REFRESHING.remove(totem);
        }
    }

    private static void updateFog(ServerLevel level, BlockPos totemPos, AABB fogArea) {
        VampirismAPI.getVampirismWorld(level).ifPresent(fog -> fog.updateArtificialFogBoundingBox(totemPos, fogArea));
    }

    /**
     * LevelFog converts AABBs to inclusive BoundingBoxes. MCA's derived
     * logical footprint uses exclusive maxima, so only this fog hand-off
     * applies the configurable visual margin and horizontal conversion.
     * Hunter control, book discovery and outlines keep using the logical area.
     */
    private static AABB fogAreaFor(TotemBlockEntity totem, AABB nativeArea, ResolvedArea resolved) {
        return isVampireFaction(totem.getControllingFaction())
                ? usesDerivedFog(totem, resolved)
                ? McaVillageEffectBounds.toArtificialFogArea(
                        McaVillageEffectBounds.withHorizontalFogMargin(
                                resolved.area(),
                                McaVampirismCompatConfig.mcaVillageFogBorderMarginBlocks()
                        )
                )
                : nativeArea
                : null;
    }

    private static boolean usesDerivedFog(TotemBlockEntity totem, ResolvedArea resolved) {
        return isVampireFaction(totem.getControllingFaction())
                && McaVampirismCompatConfig.mcaVillageEffectsFollowMcaBounds()
                && resolved.usesMcaBounds()
                && isActiveVillageTotem(totem);
    }

    private static ResolvedArea resolve(ServerLevel level, TotemBlockEntity totem, AABB nativeArea) {
        if (!McaVampirismCompatConfig.mcaVillageEffectsFollowMcaBounds()) {
            return new ResolvedArea(nativeArea, false, NO_MCA_VILLAGE_ID);
        }
        Optional<Village> associatedVillage = findContainingVillage(level, totem.getBlockPos());
        if (associatedVillage.isEmpty()) {
            return new ResolvedArea(nativeArea, false, NO_MCA_VILLAGE_ID);
        }
        Village village = associatedVillage.get();
        BlockBoxExtended villageBox = village.getBox();
        AABB derived = McaVillageEffectBounds.fromVillageBox(
                villageBox,
                nativeArea,
                level.getMinBuildHeight(),
                level.getMaxBuildHeight()
        );
        return new ResolvedArea(
                derived == null ? nativeArea : derived,
                derived != null,
                derived == null ? NO_MCA_VILLAGE_ID : village.getId()
        );
    }

    private static Optional<Village> findContainingVillage(ServerLevel level, BlockPos totemPos) {
        try {
            return VillageManager.get(level)
                    .findVillages(village -> !village.getBuildings().isEmpty() && containsVillage(village, totemPos))
                    .min(Comparator.comparingLong(village -> distanceSquared(village.getCenter(), totemPos)));
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[MCA-VILLAGE-BOUNDS] MCA village lookup failed level={} totem={} error={}",
                    level.dimension().location(), totemPos, exception.toString()
            );
            return Optional.empty();
        }
    }

    private static AABB getTrackedOrResolvedArea(ServerLevel level, TotemBlockEntity totem) {
        TrackedTotem tracked = tracked(level).get(totem.getBlockPos());
        return McaVampirismCompatConfig.mcaVillageEffectsFollowMcaBounds() && tracked != null
                ? tracked.area()
                : getEffectiveArea(totem);
    }

    private static AABB safeNativeArea(ITotem totem) {
        try {
            return totem.getVillageArea();
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog("[MCA-VILLAGE-BOUNDS] native area lookup failed error={}", exception.toString());
            return null;
        }
    }

    private static Map<BlockPos, TrackedTotem> tracked(ServerLevel level) {
        return TRACKED_TOTEMS.computeIfAbsent(level, ignored -> new HashMap<>());
    }

    private static void clearTrackedTotem(
            ServerLevel level,
            BlockPos totemPos,
            TrackedTotem previous,
            boolean reconcileHunters
    ) {
        if (previous == null) {
            return;
        }
        if (previous.usesMcaBounds()) {
            VampirismAPI.getVampirismWorld(level).ifPresent(fog -> fog.updateArtificialFogBoundingBox(totemPos, null));
        }
        if (reconcileHunters && previous.hunterControlled()) {
            McaVillageFactionControlService.syncVillagersForEffectiveBoundsChange(level, previous.area(), null);
        }
    }

    private static ServerLevel villageWorld(Village village) {
        try {
            return ((VillageAccessor) (Object) village).mcaVampCompat$getWorld();
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[MCA-VILLAGE-BOUNDS] MCA village world lookup failed village={} error={}",
                    village.getId(), exception.toString()
            );
            return null;
        }
    }

    private static boolean containsVillage(Village village, BlockPos position) {
        try {
            return village.getBox() != null && village.isWithinBorder(position, 0);
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[MCA-VILLAGE-BOUNDS] MCA village containment lookup failed village={} pos={} error={}",
                    village.getId(), position, exception.toString()
            );
            return false;
        }
    }

    private static boolean contains(AABB area, BlockPos position) {
        return area != null && area.contains(position.getX() + 0.5D, position.getY() + 0.5D, position.getZ() + 0.5D);
    }

    private static boolean sameArea(AABB first, AABB second) {
        return first == second || (first != null && first.equals(second));
    }

    private static long distanceSquared(Vec3i first, Vec3i second) {
        long x = (long) first.getX() - second.getX();
        long y = (long) first.getY() - second.getY();
        long z = (long) first.getZ() - second.getZ();
        return x * x + y * y + z * z;
    }

    private static boolean isVampireFaction(IFaction<?> faction) {
        return hasFactionId(faction, VAMPIRE_FACTION_ID);
    }

    private static boolean isHunterFaction(IFaction<?> faction) {
        return hasFactionId(faction, HUNTER_FACTION_ID);
    }

    private static boolean hasFactionId(IFaction<?> faction, String factionId) {
        try {
            return faction != null && factionId.equals(String.valueOf(faction.getID()));
        } catch (RuntimeException | LinkageError ignored) {
            return false;
        }
    }

    public record VillageAreaInfo(
            String factionId,
            AABB nativeArea,
            AABB effectiveArea,
            AABB fogArea,
            AABB mcaVillageArea,
            boolean usesMcaBounds,
            int mcaVillageId
    ) {
    }

    private record ResolvedArea(AABB area, boolean usesMcaBounds, int mcaVillageId) {
    }

    private record TrackedTotem(
            TotemBlockEntity totem,
            AABB area,
            AABB fogArea,
            boolean usesMcaBounds,
            boolean hunterControlled,
            int mcaVillageId
    ) {
    }
}
