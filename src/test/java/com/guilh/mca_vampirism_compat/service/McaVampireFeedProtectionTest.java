package com.guilh.mca_vampirism_compat.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaVampireFeedProtectionTest {
    @Test
    void bloodlinesFeedClassificationPreservesNativeVampires() {
        assertTrue(McaVampireFeedProtection.isBloodlinesVampireFeedTarget(true, false));
    }

    @Test
    void bloodlinesFeedClassificationAcceptsOnlyMcaVampireTargets() {
        assertTrue(McaVampireFeedProtection.isBloodlinesVampireFeedTarget(false, true));
        assertFalse(McaVampireFeedProtection.isBloodlinesVampireFeedTarget(false, false));
    }
}
