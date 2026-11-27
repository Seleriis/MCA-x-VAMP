package com.guilh.mca_vampirism_compat.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaRuntimeTickSchedulerTest {

    @Test
    void schedulesEachVillagerOnItsOwnStableTick() {
        assertTrue(McaRuntimeTickScheduler.isScheduled(45L, 5, 20));
        assertFalse(McaRuntimeTickScheduler.isScheduled(44L, 5, 20));
        assertTrue(McaRuntimeTickScheduler.isScheduled(39L, -1, 20));
    }

    @Test
    void safelyClampsInvalidIntervals() {
        assertTrue(McaRuntimeTickScheduler.isScheduled(123L, 42, 0));
    }
}
