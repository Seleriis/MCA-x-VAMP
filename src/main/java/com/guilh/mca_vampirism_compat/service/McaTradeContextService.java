package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class McaTradeContextService {
    private static final long CONTEXT_TTL_TICKS = 40L;
    private static final Map<TradeContextKey, Long> MCA_TRADE_CONTEXTS = new HashMap<>();
    private static final Map<BlockTradeContextKey, Long> EASY_VILLAGERS_BLOCK_TRADE_CONTEXTS = new HashMap<>();

    private McaTradeContextService() {
    }

    public static void markMcaTradeAttempt(Player player, VillagerEntityMCA villager) {
        if (player == null || villager == null || villager.level().isClientSide) {
            return;
        }
        long now = villager.level().getGameTime();
        purgeExpired(now);
        MCA_TRADE_CONTEXTS.put(new TradeContextKey(player.getUUID(), villager.getUUID()), now + CONTEXT_TTL_TICKS);
        McaVampirismCompat.debugLog(
                "[TRADE-CONTEXT] MCA_TRADE_OPTION player={} villager={} expiresAt={}",
                player.getName().getString(),
                villager.getUUID(),
                now + CONTEXT_TTL_TICKS
        );
    }

    public static void markEasyVillagersBlockTrade(Player player, BlockPos blockPos) {
        if (player == null || blockPos == null || player.level().isClientSide) {
            return;
        }
        long now = player.level().getGameTime();
        purgeExpired(now);
        EASY_VILLAGERS_BLOCK_TRADE_CONTEXTS.put(
                new BlockTradeContextKey(player.getUUID(), blockPos.immutable()),
                now + CONTEXT_TTL_TICKS
        );
        McaVampirismCompat.debugLog(
                "[TRADE-CONTEXT] EASY_VILLAGERS_BLOCK_TRADE player={} pos={} expiresAt={}",
                player.getName().getString(),
                blockPos,
                now + CONTEXT_TTL_TICKS
        );
    }

    public static boolean isMcaTradeContext(Player player, VillagerEntityMCA villager) {
        return isRecentMcaTradeContext(player, villager);
    }

    public static boolean isRecentMcaTradeContext(Player player, VillagerEntityMCA villager) {
        if (player == null || villager == null || villager.level().isClientSide) {
            return false;
        }
        long now = villager.level().getGameTime();
        purgeExpired(now);
        Long expiresAt = MCA_TRADE_CONTEXTS.get(new TradeContextKey(player.getUUID(), villager.getUUID()));
        return expiresAt != null && expiresAt >= now;
    }

    public static boolean isEasyVillagersBlockTradeContext(Player player, BlockPos blockPos) {
        if (player == null || blockPos == null || player.level().isClientSide) {
            return false;
        }
        long now = player.level().getGameTime();
        purgeExpired(now);
        Long expiresAt = EASY_VILLAGERS_BLOCK_TRADE_CONTEXTS.get(
                new BlockTradeContextKey(player.getUUID(), blockPos.immutable())
        );
        return expiresAt != null && expiresAt >= now;
    }

    public static void clearMcaTradeContext(Player player, VillagerEntityMCA villager) {
        if (player == null || villager == null || villager.level().isClientSide) {
            return;
        }
        boolean removed = MCA_TRADE_CONTEXTS.remove(new TradeContextKey(player.getUUID(), villager.getUUID())) != null;
        McaVampirismCompat.debugLog(
                "[MCA-INTERACTION] trade context clear player={} villager={} removed={}",
                player.getName().getString(),
                villager.getUUID(),
                removed
        );
    }

    public static void clearEasyVillagersBlockTradeContext(Player player, BlockPos blockPos) {
        if (player == null || blockPos == null || player.level().isClientSide) {
            return;
        }
        boolean removed = EASY_VILLAGERS_BLOCK_TRADE_CONTEXTS.remove(
                new BlockTradeContextKey(player.getUUID(), blockPos.immutable())
        ) != null;
        McaVampirismCompat.debugLog(
                "[TRADE-CONTEXT] easy villagers block trade context clear player={} pos={} removed={}",
                player.getName().getString(),
                blockPos,
                removed
        );
    }

    public static void clearContext(Player player) {
        if (player == null) {
            return;
        }
        UUID playerUuid = player.getUUID();
        MCA_TRADE_CONTEXTS.keySet().removeIf(key -> key.playerUuid().equals(playerUuid));
        EASY_VILLAGERS_BLOCK_TRADE_CONTEXTS.keySet().removeIf(key -> key.playerUuid().equals(playerUuid));
    }

    private static void purgeExpired(long now) {
        Iterator<Map.Entry<TradeContextKey, Long>> iterator = MCA_TRADE_CONTEXTS.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() < now) {
                iterator.remove();
            }
        }
        Iterator<Map.Entry<BlockTradeContextKey, Long>> blockIterator = EASY_VILLAGERS_BLOCK_TRADE_CONTEXTS.entrySet().iterator();
        while (blockIterator.hasNext()) {
            if (blockIterator.next().getValue() < now) {
                blockIterator.remove();
            }
        }
    }

    private record TradeContextKey(UUID playerUuid, UUID villagerUuid) {
    }

    private record BlockTradeContextKey(UUID playerUuid, BlockPos blockPos) {
    }
}
