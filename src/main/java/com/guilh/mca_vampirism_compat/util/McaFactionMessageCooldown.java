package com.guilh.mca_vampirism_compat.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** Bounds duplicate faction messages on the logical server thread. */
public final class McaFactionMessageCooldown {
    private static final long COOLDOWN_TICKS = 40L;
    private static final int MAX_ENTRIES_PER_SERVER = 512;

    private final Map<MinecraftServer, LinkedHashMap<MessageKey, Long>> ticksByServer = new WeakHashMap<>();

    public boolean tryAcquire(Player player, Entity villager, Enum<?> reason) {
        MinecraftServer server = villager.getServer();
        if (server == null) {
            return false;
        }

        // All dimensions on this server use the same clock for these messages.
        long now = server.overworld().getGameTime();
        LinkedHashMap<MessageKey, Long> ticks = ticksByServer.computeIfAbsent(server, ignored -> new LinkedHashMap<>());
        MessageKey key = new MessageKey(player.getUUID(), villager.getUUID(), reason);
        Long previous = ticks.get(key);
        if (previous != null && now >= previous && now - previous < COOLDOWN_TICKS) {
            return false;
        }

        // Only a sent message changes the order used to evict the oldest entry.
        ticks.remove(key);
        ticks.put(key, now);
        if (ticks.size() > MAX_ENTRIES_PER_SERVER) {
            ticks.remove(ticks.keySet().iterator().next());
        }
        return true;
    }

    private record MessageKey(UUID player, UUID villager, Enum<?> reason) {}
}
