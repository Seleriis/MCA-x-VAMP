package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import forge.net.conczin.mca.entity.ai.Chore;
import forge.net.conczin.mca.entity.ai.MoveState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class McaCommandRespectService {
    private static final long COMMAND_LOG_COOLDOWN_TICKS = 100L;
    private static final int MAX_TRACKED_COMMAND_LOGS = 512;
    private static final Map<String, Long> LAST_COMMAND_LOG_TICKS = new ConcurrentHashMap<>();

    private McaCommandRespectService() {
    }

    public static McaCommandControlState getCommandState(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide || !McaVampirismCompatConfig.respectMcaPlayerCommands()) {
            return McaCommandControlState.NONE;
        }
        try {
            MoveState moveState = villager.getVillagerBrain().getMoveState();
            if (moveState == MoveState.FOLLOW) {
                logCommandState(villager, McaCommandControlState.FOLLOW_PLAYER, moveState);
                return McaCommandControlState.FOLLOW_PLAYER;
            }
            if (moveState == MoveState.STAY) {
                logCommandState(villager, McaCommandControlState.STAY, moveState);
                return McaCommandControlState.STAY;
            }
            Chore currentJob = villager.getVillagerBrain().getCurrentJob();
            if (currentJob != null && currentJob != Chore.NONE) {
                return McaCommandControlState.WORK;
            }
            if (villager.isGuard()) {
                return McaCommandControlState.GUARD;
            }
        } catch (RuntimeException | LinkageError exception) {
            debugThrottled(villager, "command_read_failed", () -> McaVampirismCompat.debugLog(
                    "[MCA-COMMAND] command accessor unavailable villager={} error={}",
                    villager.getUUID(),
                    exception.toString()
            ));
        }
        return McaCommandControlState.NONE;
    }

    public static boolean isFollowingPlayerCommand(VillagerEntityMCA villager) {
        return getCommandState(villager) == McaCommandControlState.FOLLOW_PLAYER;
    }

    public static boolean isStayingByPlayerCommand(VillagerEntityMCA villager) {
        return getCommandState(villager) == McaCommandControlState.STAY;
    }

    public static boolean isUnderPlayerMovementCommand(VillagerEntityMCA villager) {
        McaCommandControlState state = getCommandState(villager);
        return state == McaCommandControlState.FOLLOW_PLAYER || state == McaCommandControlState.STAY;
    }

    public static boolean shouldSuppressAutonomousSpecialAi(VillagerEntityMCA villager) {
        return shouldSuppressAutonomousSpecialAi(getCommandState(villager));
    }

    public static boolean shouldSuppressAutonomousTargeting(VillagerEntityMCA villager) {
        return shouldSuppressAutonomousSpecialAi(villager);
    }

    public static boolean shouldSuppressAutonomousBiteOrHunt(VillagerEntityMCA villager) {
        return shouldSuppressAutonomousSpecialAi(villager);
    }

    public static boolean shouldPreserveMcaMovement(VillagerEntityMCA villager) {
        McaCommandControlState state = getCommandState(villager);
        return state == McaCommandControlState.FOLLOW_PLAYER
                || state == McaCommandControlState.WORK
                || state == McaCommandControlState.HOME;
    }

    public static boolean shouldStopNavigationWhenClearingTarget(VillagerEntityMCA villager) {
        return !shouldPreserveMcaMovement(villager);
    }

    public static boolean shouldDropTargetDueToCommand(VillagerEntityMCA villager, LivingEntity target) {
        if (!McaVampirismCompatConfig.clearAutonomousTargetWhenCommanded()
                || !shouldSuppressAutonomousTargeting(villager)) {
            return false;
        }
        return !isAllowedCommandedRetaliationTarget(villager, target);
    }

    public static boolean canRetaliateWhileCommanded(VillagerEntityMCA villager, Entity attacker) {
        McaCommandControlState state = getCommandState(villager);
        if (!shouldSuppressAutonomousSpecialAi(state)) {
            return true;
        }
        if (!(attacker instanceof LivingEntity livingAttacker) || !livingAttacker.isAlive()) {
            return false;
        }
        boolean allowed = switch (state) {
            case FOLLOW_PLAYER -> McaVampirismCompatConfig.allowShortRetaliationWhileFollowing();
            case STAY -> McaVampirismCompatConfig.allowShortRetaliationWhileStaying();
            case GUARD -> true;
            case WORK, HOME, UNKNOWN_COMMANDED -> false;
            case NONE -> true;
        };
        if (!allowed) {
            return false;
        }
        double maxDistance = McaVampirismCompatConfig.commandedRetaliationMaxDistance();
        return maxDistance <= 0.0D || villager.distanceToSqr(livingAttacker) <= maxDistance * maxDistance;
    }

    public static long retaliationMemoryTicksFor(VillagerEntityMCA villager, long defaultTicks) {
        McaCommandControlState state = getCommandState(villager);
        if (!shouldSuppressAutonomousSpecialAi(state)) {
            return defaultTicks;
        }
        return Math.max(1L, Math.min(defaultTicks, McaVampirismCompatConfig.commandedRetaliationTicks()));
    }

    public static void logSuppressedAi(VillagerEntityMCA villager, String action) {
        McaCommandControlState state = getCommandState(villager);
        if (!shouldSuppressAutonomousSpecialAi(state)) {
            return;
        }
        if (state == McaCommandControlState.FOLLOW_PLAYER) {
            debugThrottled(villager, action, () -> McaVampirismCompat.debugLog(
                    "[MCA-COMMAND] follow mode: suppress special AI, preserve MCA movement villager={}",
                    villager.getUUID()
            ));
            return;
        }
        debugThrottled(villager, action, () -> McaVampirismCompat.debugLog(
                "[MCA-COMMAND] suppress {} villager={} command={} reason=PLAYER_COMMAND",
                action,
                villager.getUUID(),
                state
        ));
    }

    public static void logClearedTarget(VillagerEntityMCA villager, LivingEntity target, boolean stopNavigation) {
        McaCommandControlState state = getCommandState(villager);
        debugThrottled(villager, "clear_target", () -> McaVampirismCompat.debugLog(
                "[MCA-COMMAND] cleared autonomous target villager={} command={} target={} stopNavigation={}",
                villager.getUUID(),
                state,
                target == null ? null : target.getUUID(),
                stopNavigation
        ));
    }

    public static void logRejectedTarget(VillagerEntityMCA villager, LivingEntity target) {
        McaCommandControlState state = getCommandState(villager);
        debugThrottled(villager, "target_rejected", () -> McaVampirismCompat.debugLog(
                "[SPECIAL-AI] target rejected villager={} target={} reason=MCA_COMMAND command={}",
                villager.getUUID(),
                target == null ? null : target.getUUID(),
                state
        ));
    }

    private static boolean shouldSuppressAutonomousSpecialAi(McaCommandControlState state) {
        if (!McaVampirismCompatConfig.respectMcaPlayerCommands()) {
            return false;
        }
        return switch (state) {
            case FOLLOW_PLAYER -> McaVampirismCompatConfig.suppressAutonomousCombatWhileFollowing();
            case STAY -> McaVampirismCompatConfig.suppressAutonomousCombatWhileStaying();
            case WORK, HOME, UNKNOWN_COMMANDED -> true;
            case GUARD, NONE -> false;
        };
    }

    private static boolean isAllowedCommandedRetaliationTarget(VillagerEntityMCA villager, LivingEntity target) {
        if (villager == null || target == null || !canRetaliateWhileCommanded(villager, target)) {
            return false;
        }
        long gameTime = villager.level().getGameTime();
        return ModCapabilities.get(villager)
                .map(state -> state.isRetaliatingAgainst(target.getUUID(), gameTime))
                .orElse(false);
    }

    private static void debugThrottled(VillagerEntityMCA villager, String action, Runnable logger) {
        if (villager == null || logger == null || !McaVampirismCompatConfig.enableDebugLogs()) {
            return;
        }
        long gameTime = villager.level().getGameTime();
        pruneCommandLogs(gameTime);
        String key = villager.getUUID() + ":" + action;
        Long lastLog = LAST_COMMAND_LOG_TICKS.get(key);
        if (lastLog != null && gameTime - lastLog < COMMAND_LOG_COOLDOWN_TICKS) {
            return;
        }
        LAST_COMMAND_LOG_TICKS.put(key, gameTime);
        logger.run();
    }

    private static void pruneCommandLogs(long gameTime) {
        if (LAST_COMMAND_LOG_TICKS.size() < MAX_TRACKED_COMMAND_LOGS) {
            return;
        }
        LAST_COMMAND_LOG_TICKS.entrySet().removeIf(
                entry -> gameTime - entry.getValue() >= COMMAND_LOG_COOLDOWN_TICKS
        );
        if (LAST_COMMAND_LOG_TICKS.size() >= MAX_TRACKED_COMMAND_LOGS) {
            LAST_COMMAND_LOG_TICKS.clear();
        }
    }

    private static void logCommandState(VillagerEntityMCA villager, McaCommandControlState state, MoveState moveState) {
        debugThrottled(villager, "command_state_" + state.name(), () -> McaVampirismCompat.verboseAiLog(
                "[MCA-COMMAND] command state villager={} command={} moveState={}",
                villager.getUUID(),
                state,
                moveState
        ));
    }

    public enum McaCommandControlState {
        NONE,
        FOLLOW_PLAYER,
        STAY,
        GUARD,
        WORK,
        HOME,
        UNKNOWN_COMMANDED
    }
}
