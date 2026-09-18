package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

/** All world and navigation access runs on the server entity tick. */
public final class McaVampireShelterService {
    private static final int SEARCH_INTERVAL_TICKS = 20;
    private static final int SEARCH_RADIUS = 12;
    private static final int SEARCH_HEIGHT = 4;
    private static final int MAX_SHELTER_PATH_CHECKS = 32;
    private static final Map<VillagerEntityMCA, ShelterState> ACTIVE_SHELTERS = new WeakHashMap<>();
    private static final Map<VillagerEntityMCA, Long> LAST_NO_SHELTER_LOG = new WeakHashMap<>();

    private McaVampireShelterService() {}

    public static void process(VillagerEntityMCA villager) {
        if (villager == null || !(villager.level() instanceof ServerLevel level)
                || !McaVampirismCompatConfig.vampireVillagersAvoidSun()
                || villager.getAgeState() != AgeState.ADULT || !McaVillagerUtil.isMcaVampire(villager)) {
            clear(villager, false);
            return;
        }
        ShelterState active = ACTIVE_SHELTERS.get(villager);
        if (!McaVampireSunlightHandler.isSunlightExposed(villager)) {
            if (active != null) McaVampirismCompat.debugLog("[VAMPIRE-SUN] shelter reached villager={} shelter={}", villager.getUUID(), active.destination);
            clear(villager, false);
            return;
        }
        if (McaVampireNightScheduleService.isUsingNativeDayRest(villager)
                || villager.getTarget() != null || McaCommandRespectService.getCommandState(villager)
                != McaCommandRespectService.McaCommandControlState.NONE) {
            clear(villager, false);
            return;
        }
        // Entity phases spread expensive searches across the server's ticks.
        long now = level.getGameTime();
        if ((now + Math.floorMod(villager.getId(), SEARCH_INTERVAL_TICKS)) % SEARCH_INTERVAL_TICKS != 0) return;

        if (active != null) {
            if (isShelteredPosition(level, active.destination)) {
                var navigation = villager.getNavigation();
                Path existing = navigation.getPath();
                if (villager.position().distanceToSqr(active.lastPosition) > 0.25D) {
                    active.lastPosition = villager.position();
                    active.lastProgress = now;
                }
                if (existing != null && !existing.isDone() && existing.canReach() && !navigation.isStuck()
                        && active.destination.equals(existing.getTarget()) && now - active.lastProgress <= 40L) return;
                Path replacement = navigation.createPath(active.destination, 0);
                if (replacement != null && replacement.canReach()) {
                    start(villager, active.destination, replacement);
                    return;
                }
            }
            clear(villager, true);
        }

        ShelterPath shelter = findReachableShelter(villager, level);
        if (shelter == null) {
            Long lastLog = LAST_NO_SHELTER_LOG.get(villager);
            if (lastLog == null || now - lastLog >= 200L) {
                LAST_NO_SHELTER_LOG.put(villager, now);
                McaVampirismCompat.debugLog("[VAMPIRE-SUN] no reachable shelter villager={} pos={}", villager.getUUID(), villager.blockPosition());
            }
            return;
        }
        ACTIVE_SHELTERS.put(villager, new ShelterState(shelter.destination, villager));
        start(villager, shelter.destination, shelter.path);
        McaVampirismCompat.debugLog("[VAMPIRE-SUN] seeking shelter villager={} from={} shelter={}", villager.getUUID(), villager.blockPosition(), shelter.destination);
    }

    private static void start(VillagerEntityMCA villager, BlockPos destination, Path path) {
        villager.moveTowards(destination, 0.8F, 1);
        villager.getNavigation().moveTo(path, 0.8D);
    }

    private static ShelterPath findReachableShelter(VillagerEntityMCA villager, ServerLevel level) {
        int checks = 0;
        for (BlockPos candidate : BlockPos.withinManhattan(villager.blockPosition(), SEARCH_RADIUS, SEARCH_HEIGHT, SEARCH_RADIUS)) {
            if (!isShelteredPosition(level, candidate)) continue;
            if (++checks > MAX_SHELTER_PATH_CHECKS) break;
            Path path = villager.getNavigation().createPath(candidate, 0);
            if (path != null && path.canReach()) return new ShelterPath(candidate.immutable(), path);
        }
        return null;
    }

    private static boolean isShelteredPosition(ServerLevel level, BlockPos pos) {
        return !level.canSeeSky(pos.above())
                && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    private static void clear(VillagerEntityMCA villager, boolean stopNavigation) {
        if (villager == null) return;
        ShelterState active = ACTIVE_SHELTERS.remove(villager);
        LAST_NO_SHELTER_LOG.remove(villager);
        if (stopNavigation && active != null) {
            villager.getNavigation().stop();
            villager.getBrain().getMemory(MemoryModuleType.WALK_TARGET)
                    .filter(target -> target.getTarget().currentBlockPosition().equals(active.destination))
                    .ifPresent(target -> villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET));
        }
    }

    private record ShelterPath(BlockPos destination, Path path) {}
    private static final class ShelterState {
        final BlockPos destination;
        Vec3 lastPosition;
        long lastProgress;
        ShelterState(BlockPos destination, VillagerEntityMCA villager) {
            this.destination = destination;
            lastPosition = villager.position();
            lastProgress = villager.level().getGameTime();
        }
    }
}
