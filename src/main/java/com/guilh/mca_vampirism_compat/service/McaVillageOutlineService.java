package com.guilh.mca_vampirism_compat.service;

import de.teamlapen.vampirism.blockentity.TotemBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A deliberately small, private particle outline for OP diagnostics. A single
 * player can own one session at a time; no particles are broadcast to other
 * players and the number of points is capped before anything is sent.
 */
public final class McaVillageOutlineService {
    private static final long UPDATE_INTERVAL_TICKS = 10L;
    private static final int MAX_PARTICLES_PER_UPDATE = 128;
    private static final double PREFERRED_POINT_SPACING = 8.0D;
    private static final Map<UUID, OutlineSession> SESSIONS = new HashMap<>();

    private McaVillageOutlineService() {
    }

    public static void start(ServerPlayer player, TotemBlockEntity totem, int durationSeconds) {
        if (player == null || totem == null || !(totem.getLevel() instanceof ServerLevel level)) {
            return;
        }
        long expiresAt = level.getGameTime() + Math.max(5, Math.min(60, durationSeconds)) * 20L;
        SESSIONS.put(player.getUUID(), new OutlineSession(level.dimension(), totem.getBlockPos().immutable(), expiresAt));
    }

    public static void stop(ServerPlayer player) {
        if (player != null) {
            SESSIONS.remove(player.getUUID());
        }
    }

    public static void tick(MinecraftServer server) {
        if (server == null || SESSIONS.isEmpty()) {
            return;
        }
        List<UUID> expired = new ArrayList<>();
        for (Map.Entry<UUID, OutlineSession> entry : SESSIONS.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            OutlineSession session = entry.getValue();
            ServerLevel level = server.getLevel(session.dimension());
            if (player == null || level == null || player.serverLevel() != level || level.getGameTime() >= session.expiresAt()) {
                expired.add(entry.getKey());
                continue;
            }
            if (level.getGameTime() % UPDATE_INTERVAL_TICKS != 0L) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(session.totemPos());
            if (!(blockEntity instanceof TotemBlockEntity totem)
                    || !McaVillageEffectBoundsService.isActiveVillageTotem(totem)) {
                expired.add(entry.getKey());
                continue;
            }
            AABB area = McaVillageEffectBoundsService.getEffectiveArea(totem);
            if (area == null) {
                expired.add(entry.getKey());
                continue;
            }
            render(level, player, area, dustFor(McaVillageEffectBoundsService.factionId(totem)));
        }
        expired.forEach(SESSIONS::remove);
    }

    static List<OutlinePoint> outlinePoints(AABB area, double y) {
        if (area == null || area.getXsize() <= 0.0D || area.getZsize() <= 0.0D) {
            return List.of();
        }
        double width = area.getXsize();
        double depth = area.getZsize();
        double perimeter = 2.0D * (width + depth);
        int pointCount = Math.max(4, Math.min(
                MAX_PARTICLES_PER_UPDATE,
                (int) Math.ceil(perimeter / PREFERRED_POINT_SPACING)
        ));
        List<OutlinePoint> points = new ArrayList<>(pointCount);
        for (int index = 0; index < pointCount; index++) {
            double distance = perimeter * index / pointCount;
            if (distance < width) {
                points.add(new OutlinePoint(area.minX + distance, y, area.minZ));
            } else if ((distance -= width) < depth) {
                points.add(new OutlinePoint(area.maxX, y, area.minZ + distance));
            } else if ((distance -= depth) < width) {
                points.add(new OutlinePoint(area.maxX - distance, y, area.maxZ));
            } else {
                distance -= width;
                points.add(new OutlinePoint(area.minX, y, area.maxZ - distance));
            }
        }
        return points;
    }

    private static void render(ServerLevel level, ServerPlayer player, AABB area, DustParticleOptions dust) {
        double y = Math.max(level.getMinBuildHeight() + 1.0D,
                Math.min(level.getMaxBuildHeight() - 1.0D, player.getY() + 0.15D));
        for (OutlinePoint point : outlinePoints(area, y)) {
            level.sendParticles(player, dust, true,
                    point.x(), point.y(), point.z(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static DustParticleOptions dustFor(String factionId) {
        return switch (factionId == null ? "" : factionId) {
            case "vampirism:vampire" -> dust(0x7A0F19);
            case "vampirism:hunter" -> dust(0xC89B3C);
            case "werewolves:werewolf" -> dust(0xB7BCC5);
            default -> dust(0xD0D0D0);
        };
    }

    private static DustParticleOptions dust(int rgb) {
        return new DustParticleOptions(new Vector3f(
                ((rgb >> 16) & 0xFF) / 255.0F,
                ((rgb >> 8) & 0xFF) / 255.0F,
                (rgb & 0xFF) / 255.0F
        ), 1.0F);
    }

    /** Clears transient sessions when a server world is replaced or stopped. */
    public static void clearAll() {
        SESSIONS.clear();
    }

    record OutlinePoint(double x, double y, double z) {
    }

    private record OutlineSession(ResourceKey<Level> dimension, BlockPos totemPos, long expiresAt) {
    }
}
