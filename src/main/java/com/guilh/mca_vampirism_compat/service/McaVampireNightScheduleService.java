package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.SchedulesMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;

/**
 * Applies MCA's built-in nocturnal schedules to adult vampire villagers while
 * retaining the exact MCA schedule that was active before their conversion.
 */
public final class McaVampireNightScheduleService {
    private McaVampireNightScheduleService() {
    }

    public static void process(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            if (!shouldUseNightSchedule(villager)) {
                restoreOriginalSchedule(villager, state);
                return;
            }

            Brain<?> brain = villager.getBrain();
            Schedule desiredSchedule = villager.isGuard()
                    ? SchedulesMCA.GUARD_NIGHT
                    : SchedulesMCA.NIGHT_OWL_DEFAULT;
            String desiredId = scheduleId(desiredSchedule);
            Schedule currentSchedule = brain.getSchedule();
            String currentId = scheduleId(currentSchedule);

            if (!state.isVampireNightScheduleApplied()) {
                state.beginVampireNightSchedule(currentId, desiredId);
                McaVampirismCompat.debugLog(
                        "[VAMPIRE-SCHEDULE] enabled villager={} original={} applied={}",
                        villager.getUUID(), currentId, desiredId
                );
            } else if (!state.getLastAppliedVampireSchedule().equals(currentId)
                    && !currentId.isEmpty()
                    && currentSchedule != desiredSchedule) {
                // MCA rebuilt this brain (profession/guard/residency change). Keep
                // the new MCA-selected schedule so a later cure restores it.
                state.updateOriginalMcaSchedule(currentId);
                McaVampirismCompat.debugLog(
                        "[VAMPIRE-SCHEDULE] refreshed original schedule villager={} original={}",
                        villager.getUUID(), currentId
                );
            }

            if (currentSchedule != desiredSchedule) {
                brain.setSchedule(desiredSchedule);
            }
            state.updateLastAppliedVampireSchedule(desiredId);

            if (!hasPriorityActivity(villager)) {
                brain.updateActivityFromSchedule(villager.level().getDayTime(), villager.level().getGameTime());
            }
        });
    }

    /**
     * Lets the MCA rest package own movement toward a remembered home/bed. If
     * there is no home memory, the sunlight shelter service can still find a
     * nearby shaded fallback.
     */
    public static boolean isUsingNativeDayRest(VillagerEntityMCA villager) {
        if (villager == null
                || villager.level().isClientSide
                || !McaVampirismCompatConfig.vampireVillagersUseNightSchedule()
                || !villager.level().dimensionType().hasSkyLight()
                || !villager.level().isDay()
                || !McaVampireStateService.isVampire(villager)
                || villager.getAgeState() != AgeState.ADULT) {
            return false;
        }
        Brain<?> brain = villager.getBrain();
        return brain.isActive(Activity.REST)
                && (villager.isSleeping()
                || brain.getMemoryInternal(MemoryModuleType.HOME).isPresent());
    }

    private static boolean shouldUseNightSchedule(VillagerEntityMCA villager) {
        return McaVampirismCompatConfig.vampireVillagersUseNightSchedule()
                && villager.getAgeState() == AgeState.ADULT
                && villager.level().dimensionType().hasSkyLight()
                && McaVampireStateService.isVampire(villager);
    }

    private static boolean hasPriorityActivity(VillagerEntityMCA villager) {
        return villager.getTarget() != null
                || McaCommandRespectService.getCommandState(villager)
                != McaCommandRespectService.McaCommandControlState.NONE;
    }

    private static void restoreOriginalSchedule(VillagerEntityMCA villager, VampiricVillagerState state) {
        if (!state.isVampireNightScheduleApplied()) {
            return;
        }
        Schedule originalSchedule = scheduleById(state.getOriginalMcaSchedule());
        if (originalSchedule == null) {
            villager.refreshBrain((ServerLevel) villager.level());
        } else {
            Brain<?> brain = villager.getBrain();
            brain.setSchedule(originalSchedule);
            if (!hasPriorityActivity(villager)) {
                brain.updateActivityFromSchedule(villager.level().getDayTime(), villager.level().getGameTime());
            }
        }
        McaVampirismCompat.debugLog(
                "[VAMPIRE-SCHEDULE] restored villager={} schedule={}",
                villager.getUUID(), state.getOriginalMcaSchedule()
        );
        state.clearVampireNightSchedule();
    }

    private static String scheduleId(Schedule schedule) {
        if (schedule == SchedulesMCA.DEFAULT) {
            return "default";
        }
        if (schedule == SchedulesMCA.NIGHT_OWL_DEFAULT) {
            return "night_owl_default";
        }
        if (schedule == SchedulesMCA.GUARD) {
            return "guard";
        }
        if (schedule == SchedulesMCA.GUARD_NIGHT) {
            return "guard_night";
        }
        if (schedule == SchedulesMCA.GUESTS) {
            return "guests";
        }
        if (schedule == Schedule.VILLAGER_BABY) {
            return "villager_baby";
        }
        return "";
    }

    private static Schedule scheduleById(String id) {
        return switch (id == null ? "" : id) {
            case "default" -> SchedulesMCA.DEFAULT;
            case "night_owl_default" -> SchedulesMCA.NIGHT_OWL_DEFAULT;
            case "guard" -> SchedulesMCA.GUARD;
            case "guard_night" -> SchedulesMCA.GUARD_NIGHT;
            case "guests" -> SchedulesMCA.GUESTS;
            case "villager_baby" -> Schedule.VILLAGER_BABY;
            default -> null;
        };
    }
}
