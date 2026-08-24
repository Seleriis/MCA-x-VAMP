package com.guilh.mca_vampirism_compat.book;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.service.McaVillageEffectBoundsService;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.world.ITotem;
import de.teamlapen.vampirism.util.TotemHelper;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Gives the guide exactly once when a player enters an active faction-controlled
 * Vampirism village. Completion of the advancement is the persistent delivery
 * marker.
 */
public final class OccultVillageBookDeliveryService {
    public static final int CHECK_INTERVAL_TICKS = 40;
    static final int FACTION_POI_SEARCH_RADIUS = 128;
    public static final ResourceLocation BOOK_ITEM_ID =
            ResourceLocation.fromNamespaceAndPath(McaVampirismCompat.MOD_ID, "book_occult_arts");
    public static final ResourceLocation ADVANCEMENT_ID =
            ResourceLocation.fromNamespaceAndPath(McaVampirismCompat.MOD_ID, "discover_occult_village");
    public static final ResourceLocation RECIPE_ID =
            ResourceLocation.fromNamespaceAndPath(McaVampirismCompat.MOD_ID, "book_occult_arts");

    private static final String DISCOVERY_CRITERION = "discovered";
    private static final TagKey<PoiType> HAS_FACTION_POI = TagKey.create(
            Registries.POINT_OF_INTEREST_TYPE,
            ResourceLocation.fromNamespaceAndPath("vampirism", "has_faction")
    );
    private static volatile BooleanSupplier enabledSupplier = () -> true;
    private static volatile Supplier<ItemStack> bookStackSupplier =
            OccultVillageBookDeliveryService::createRegisteredBookStack;
    private static boolean missingAdvancementLogged;
    private static boolean missingBookLogged;

    private OccultVillageBookDeliveryService() {
    }

    /**
     * Allows the common config to control discovery without coupling this
     * subscriber to the config class's initialization order.
     */
    public static void setEnabledSupplier(BooleanSupplier supplier) {
        enabledSupplier = Objects.requireNonNull(supplier, "supplier");
    }

    /**
     * Allows the item registry to supply a fully initialized stack while the
     * default implementation remains a registry lookup by id.
     */
    public static void setBookStackSupplier(Supplier<ItemStack> supplier) {
        bookStackSupplier = Objects.requireNonNull(supplier, "supplier");
    }

    public static void onPlayerTick(ServerPlayer player) {
        if (player == null
                || player.isSpectator()
                || player.tickCount % CHECK_INTERVAL_TICKS != 0
                || !isEnabled()) {
            return;
        }

        AdvancementHolder advancement = getDiscoveryAdvancement(player);
        if (advancement == null
                || player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            return;
        }

        if (findEligibleTotem(player).isEmpty()) {
            return;
        }

        ItemStack guide = createBookStack();
        if (guide.isEmpty()) {
            logMissingBookOnce();
            return;
        }
        guide.setCount(1);

        if (player.getInventory().contains(stack -> stack.is(guide.getItem()))) {
            completeDiscovery(player, advancement);
            return;
        }

        boolean delivered = player.getInventory().add(guide);
        if (!guide.isEmpty()) {
            ItemEntity dropped = player.drop(guide, false, false);
            delivered = delivered || dropped != null;
        }
        if (!delivered) {
            return;
        }

        player.getInventory().setChanged();
        completeDiscovery(player, advancement);
        McaVampirismCompat.debugLog(
                "[OCCULT-BOOK] delivered player={} dimension={} position={}",
                player.getUUID(),
                player.level().dimension().location(),
                player.blockPosition()
        );
    }

    static Optional<ITotem> findEligibleTotem(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        try {
            Optional<ITotem> effectiveAreaTotem = McaVillageEffectBoundsService
                    .findActiveTotemAt(level, player.blockPosition())
                    .map(totem -> (ITotem) totem)
                    .filter(totem -> isEligibleTotem(level, player, totem));
            if (effectiveAreaTotem.isPresent()) {
                return effectiveAreaTotem;
            }
            Optional<ITotem> nearbyTotem = TotemHelper.getTotemNearPos(level, player.blockPosition(), true)
                    .map(totem -> (ITotem) totem)
                    .filter(totem -> isEligibleTotem(level, player, totem));
            if (nearbyTotem.isPresent()) {
                return nearbyTotem;
            }
            return findEligibleTotemFromFactionPois(level, player);
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[OCCULT-BOOK] totem lookup failed player={} position={} error={}",
                    player.getUUID(),
                    player.blockPosition(),
                    exception.toString()
            );
            return Optional.empty();
        }
    }

    private static Optional<ITotem> findEligibleTotemFromFactionPois(ServerLevel level, ServerPlayer player) {
        List<BlockPos> factionPoiPositions = level.getPoiManager()
                .getInRange(
                        holder -> holder.is(HAS_FACTION_POI),
                        player.blockPosition(),
                        FACTION_POI_SEARCH_RADIUS,
                        PoiManager.Occupancy.ANY
                )
                .map(PoiRecord::getPos)
                .distinct()
                .toList();

        for (BlockPos poiPosition : factionPoiPositions) {
            try {
                Optional<ITotem> totem = TotemHelper.getTotemNearPos(level, poiPosition, true)
                        .map(value -> (ITotem) value)
                        .filter(value -> isEligibleTotem(level, player, value));
                if (totem.isPresent()) {
                    return totem;
                }
            } catch (RuntimeException | LinkageError exception) {
                McaVampirismCompat.debugLog(
                        "[OCCULT-BOOK] ignored invalid faction POI position={} error={}",
                        poiPosition,
                        exception.toString()
                );
            }
        }
        return Optional.empty();
    }

    static boolean isEligibleTotem(ServerLevel level, ServerPlayer player, ITotem totem) {
        if (totem == null || totem.getTileWorld() != level) {
            return false;
        }
        if (totem instanceof BlockEntity blockEntity && blockEntity.isRemoved()) {
            return false;
        }
        if (!McaVillageEffectBoundsService.isActiveVillageTotem(totem)) {
            return false;
        }

        IFaction<?> controllingFaction = totem.getControllingFaction();
        if (controllingFaction == null
                || !OccultVillageFactionPolicy.isSupportedFactionId(String.valueOf(controllingFaction.getID()))) {
            return false;
        }

        AABB villageArea = McaVillageEffectBoundsService.getEffectiveArea(totem);
        return villageArea != null
                && villageArea.getSize() > 0.0D
                && villageArea.contains(player.getX(), player.getY(), player.getZ());
    }

    private static boolean isEnabled() {
        try {
            return enabledSupplier.getAsBoolean();
        } catch (RuntimeException exception) {
            McaVampirismCompat.LOGGER.warn(
                    "[MCA Reborn x Vampirism Compat] Failed to read occult-book delivery config",
                    exception
            );
            return false;
        }
    }

    private static AdvancementHolder getDiscoveryAdvancement(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        AdvancementHolder advancement = server == null ? null : server.getAdvancements().get(ADVANCEMENT_ID);
        if (advancement == null && !missingAdvancementLogged) {
            missingAdvancementLogged = true;
            McaVampirismCompat.LOGGER.warn(
                    "[MCA Reborn x Vampirism Compat] Missing advancement {}; occult book delivery is disabled",
                    ADVANCEMENT_ID
            );
        }
        return advancement;
    }

    private static ItemStack createBookStack() {
        try {
            ItemStack stack = bookStackSupplier.get();
            return stack == null ? ItemStack.EMPTY : stack.copy();
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.LOGGER.warn(
                    "[MCA Reborn x Vampirism Compat] Failed to create the occult arts book",
                    exception
            );
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack createRegisteredBookStack() {
        Item item = BuiltInRegistries.ITEM.getOptional(BOOK_ITEM_ID).orElse(null);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static void completeDiscovery(ServerPlayer player, AdvancementHolder advancement) {
        if (!player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            player.getAdvancements().award(advancement, DISCOVERY_CRITERION);
        }
        player.awardRecipesByKey(List.of(RECIPE_ID));
    }

    private static void logMissingBookOnce() {
        if (missingBookLogged) {
            return;
        }
        missingBookLogged = true;
        McaVampirismCompat.LOGGER.warn(
                "[MCA Reborn x Vampirism Compat] Item {} is not registered; occult book delivery is disabled",
                BOOK_ITEM_ID
        );
    }
}
