package com.guilh.mca_vampirism_compat.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BloodlinesBloodknightBridgeTest {
    @Test
    void acceptsRankedBloodknightAtDefaultQuarterHealthLimit() {
        assertTrue(BloodlinesBloodknightBridge.isEligibleBloodknightState(
                "bloodlines:bloodknight", 4, false, 5.0D, 20.0D, 0.25D, 0.60D
        ));
    }

    @Test
    void rejectsOtherBloodlinesAndUnrankedBloodknights() {
        assertFalse(BloodlinesBloodknightBridge.isEligibleBloodknightState(
                "bloodlines:zealot", 4, false, 1.0D, 20.0D, 0.25D, 0.60D
        ));
        assertFalse(BloodlinesBloodknightBridge.isEligibleBloodknightState(
                "bloodlines:bloodknight", 0, false, 1.0D, 20.0D, 0.25D, 0.60D
        ));
    }

    @Test
    void rejectsTargetsAboveTheConfiguredBloodknightLimit() {
        assertFalse(BloodlinesBloodknightBridge.isEligibleBloodknightState(
                "bloodlines:bloodknight", 4, false, 5.1D, 20.0D, 0.25D, 0.60D
        ));
    }

    @Test
    void usesTheFeignedMercyLimitWhenTheSkillIsEnabled() {
        assertTrue(BloodlinesBloodknightBridge.isEligibleBloodknightState(
                "bloodlines:bloodknight", 4, true, 12.0D, 20.0D, 0.25D, 0.60D
        ));
        assertFalse(BloodlinesBloodknightBridge.isEligibleBloodknightState(
                "bloodlines:bloodknight", 4, true, 12.1D, 20.0D, 0.25D, 0.60D
        ));
    }
}
