package com.guilh.mca_vampirism_compat.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TargetPriorityScoreTest {

    @Test
    void preservesCategoryFactionAndDistanceOrdering() {
        TargetPriorityScore mcaTarget = score(0, 8, 100.0D);
        TargetPriorityScore playerTarget = score(1, 0, 1.0D);
        TargetPriorityScore closerPeer = score(0, 8, 25.0D);

        assertTrue(mcaTarget.isPreferredOver(playerTarget));
        assertTrue(closerPeer.isPreferredOver(mcaTarget));
        assertFalse(playerTarget.isPreferredOver(mcaTarget));
    }

    @Test
    void keepsEncounterOrderForExactlyEqualScores() {
        TargetPriorityScore first = score(2, 4, 36.0D);
        TargetPriorityScore equal = score(2, 4, 36.0D);

        assertFalse(equal.isPreferredOver(first));
    }

    private static TargetPriorityScore score(int category, int faction, double distance) {
        return new TargetPriorityScore(category, faction, distance, "test", "test");
    }
}
