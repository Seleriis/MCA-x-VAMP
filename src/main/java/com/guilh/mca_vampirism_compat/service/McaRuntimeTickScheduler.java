package com.guilh.mca_vampirism_compat.service;

/**
 * Keeps low-priority villager maintenance staggered across server ticks.
 * Gameplay-critical work such as active combat, curing and sunlight remains
 * on its normal per-tick path.
 */
final class McaRuntimeTickScheduler {
    static final int DORMANT_STATE_INSPECTION_INTERVAL_TICKS = 20;
    static final int DEFENSIVE_MAINTENANCE_INTERVAL_TICKS = 20;

    private McaRuntimeTickScheduler() {
    }

    static boolean isScheduled(long gameTime, int entityId, int interval) {
        int safeInterval = Math.max(1, interval);
        return gameTime % safeInterval == Math.floorMod(entityId, safeInterval);
    }
}
