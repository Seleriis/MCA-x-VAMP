package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.pathfinder.Path;

import java.util.Map;
import java.util.WeakHashMap;

public final class McaVampireShelterService {
    private static final int SEARCH_INTERVAL_TICKS = 20;
    private static final int SEARCH_RADIUS = 12;
    private static final int SEARCH_HEIGHT = 4;
    private static final int MAX_SHELTER_PATH_CHECKS = 32;
    private static final Map<VillagerEntityMCA, ShelterState> ACTIVE_SHELTERS = new WeakHashMap<>();
    private static final Map<VillagerEntityMCA, Long> LAST_NO_SHELTER_LOG = new WeakHashMap<>();

    private McaVampireShelterService() {
    }

    public static void process(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide
                || !McaVampirismCompatConfig.vampireVillagersAvoidSun()
                || villager.getAgeState() != AgeState.ADULT
                || !McaVillagerUtil.isMcaVampire(villager)) {
            clear(villager, false);
            return;
        }

        boolean exposed = McaVampireSunlightHandler.isSunlightExposed(villager);
        ShelterState active = ACTIVE_SHELTERS.get(villager);
        if (!exposed) {
            if (active != null) {
                McaVampirismCompat.debugLog(
                        "[VAMPIRE-SUN] shelter reached villager={} shelter={}",
                        villager.getUUID(),
                        active.destination()
                );
            }
            clear(villager, false);
            return;
        }

        if (McaVampireNightScheduleService.isUsingNativeDayRest(villager)) {
            clear(villager, false);
            return;
        }

        if (villager.getTarget() != null
                || McaCommandRespectService.getCommandState(villager)
                != McaCommandRespectService.McaCommandControlState.NONE) {
            clear(villager, false);
            return;
        }

        if (villager.tickCount % SEARCH_INTERVAL_TICKS != 0) {
            return;
        }

        if (active != null && isShelteredPosition((ServerLevel) villager.level(), active.destination())) {
            Path path = villager.getNavigation().createPath(active.destination(), 0);
            if (path != null && path.canReach()) {
                villager.moveTowards(active.destination(), 0.8F, 1);
                return;
            }
            ACTIVE_SHELTERS.remove(villager);
        }

        BlockPos shelter = findReachableShelter(villager);
        if (shelter == null) {
            logNoShelter(villager);
            return;
        }
        ACTIVE_SHELTERS.put(villager, new ShelterState(shelter));
        villager.moveTowards(shelter, 0.8F, 1);
        McaVampirismCompat.debugLog(
                "[VAMPIRE-SUN] seeking shelter villager={} from={} shelter={}",
                villager.getUUID(),
                villager.blockPosition(),
                shelter
        );
    }

    private static BlockPos findReachableShelter(VillagerEntityMCA villager) {
        ServerLevel level = (ServerLevel) villager.level();
        BlockPos origin = villager.blockPosition();
        int pathChecks = 0;
        for (BlockPos candidate : BlockPos.withinManhattan(origin, SEARCH_RADIUS, SEARCH_HEIGHT, SEARCH_RADIUS)) {
            if (!isShelteredPosition(level, candidate)) {
                continue;
            }
            if (++pathChecks > MAX_SHELTER_PATH_CHECKS) {
                break;
            }
            Path path = villager.getNavigation().createPath(candidate, 0);
            if (path != null && path.canReach()) {
                return candidate.immutable();
            }
        }
        return null;
    }

    private static boolean isShelteredPosition(ServerLevel level, BlockPos pos) {
        return !level.canSeeSky(pos.above())
                && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    private static void logNoShelter(VillagerEntityMCA villager) {
        long gameTime = villager.level().getGameTime();
        Long lastLog = LAST_NO_SHELTER_LOG.get(villager);
        if (lastLog != null && gameTime - lastLog < 200L) {
            return;
        }
        LAST_NO_SHELTER_LOG.put(villager, gameTime);
        McaVampirismCompat.debugLog(
                "[VAMPIRE-SUN] no reachable shelter villager={} pos={}",
                villager.getUUID(),
                villager.blockPosition()
        );
    }

    private static void clear(VillagerEntityMCA villager, boolean stopNavigation) {
        if (villager == null) {
            return;
        }
        ACTIVE_SHELTERS.remove(villager);
        LAST_NO_SHELTER_LOG.remove(villager);
        if (stopNavigation) {
            villager.getNavigation().stop();
        }
    }

    private record ShelterState(BlockPos destination) {
    }
}
