package com.guilh.mca_vampirism_compat.advancement;

import com.guilh.mca_vampirism_compat.inheritance.McaBabyInheritancePayload;
import com.guilh.mca_vampirism_compat.inheritance.McaVampireInheritanceHandler.InheritedFaction;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfStateService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Attribution for inherited children without adding any data to villager or baby payloads. */
public final class McaInheritanceAdvancementService {
    private static final double WITNESS_DISTANCE_SQUARED = 16.0D * 16.0D;
    private static final long PENDING_NATURAL_BIRTH_TTL_TICKS = 1_200L;
    private static final Map<UUID, Long> PENDING_NATURAL_BIRTHS = new ConcurrentHashMap<>();
    private static long lastInventoryCheckTick = Long.MIN_VALUE;

    private McaInheritanceAdvancementService() {
    }

    public static void recordNaturalBirth(VillagerEntityMCA child) {
        if (isInherited(child)) {
            PENDING_NATURAL_BIRTHS.put(child.getUUID(), child.level().getGameTime());
        }
    }

    public static void onVillagerJoined(VillagerEntityMCA child) {
        if (PENDING_NATURAL_BIRTHS.remove(child.getUUID()) == null || child.level().isClientSide) {
            return;
        }
        for (ServerPlayer player : child.level().getEntitiesOfClass(ServerPlayer.class,
                child.getBoundingBox().inflate(16.0D))) {
            if (player.distanceToSqr(child) <= WITNESS_DISTANCE_SQUARED && player.hasLineOfSight(child)) {
                McaAdvancementService.award(player, McaCompatAdvancements.INHERITED_BABY);
            }
        }
    }

    public static void onBabyItemBirth(ServerPlayer player, VillagerEntityMCA child) {
        if (isInherited(child)) {
            McaAdvancementService.award(player, McaCompatAdvancements.INHERITED_BABY);
        }
    }

    /**
     * Breeder output is owned by Easy Villagers' container. A low-frequency
     * scan awards the player who takes its inherited Baby Item, without
     * touching the owner snapshot or the payload format.
     */
    public static void observeBabyItems(MinecraftServer server) {
        long gameTime = server.overworld().getGameTime();
        PENDING_NATURAL_BIRTHS.entrySet().removeIf(
                entry -> gameTime - entry.getValue() > PENDING_NATURAL_BIRTH_TTL_TICKS
        );
        if (lastInventoryCheckTick != Long.MIN_VALUE && gameTime - lastInventoryCheckTick < 20L) {
            return;
        }
        lastInventoryCheckTick = gameTime;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (hasInheritedBabyItem(player)) {
                McaAdvancementService.award(player, McaCompatAdvancements.INHERITED_BABY);
            }
        }
    }

    /** Clears process-local state when an integrated or dedicated server stops. */
    public static void resetTransientState() {
        PENDING_NATURAL_BIRTHS.clear();
        lastInventoryCheckTick = Long.MIN_VALUE;
    }

    private static boolean hasInheritedBabyItem(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (McaBabyInheritancePayload.read(stack)
                    .filter(faction -> faction == InheritedFaction.VAMPIRE || faction == InheritedFaction.WEREWOLF)
                    .isPresent()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isInherited(VillagerEntityMCA child) {
        return child != null && (McaVampireStateService.isVampire(child) || McaWerewolfStateService.isWerewolf(child));
    }
}
