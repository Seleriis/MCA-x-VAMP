package com.guilh.mca_vampirism_compat.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HunterWeaponSelectionTest {

    @Test
    void rollsStakeAtTheConfiguredQuarterBoundary() {
        assertEquals(HunterWeaponSelection.STAKE, HunterWeaponSelection.roll(0.0D, 0.25D));
        assertEquals(HunterWeaponSelection.STAKE, HunterWeaponSelection.roll(0.249999D, 0.25D));
        assertEquals(HunterWeaponSelection.PITCHFORK, HunterWeaponSelection.roll(0.25D, 0.25D));
        assertEquals(HunterWeaponSelection.PITCHFORK, HunterWeaponSelection.roll(0.99D, 0.25D));
    }

    @Test
    void clampsInvalidChanceAndParsesOnlyKnownPersistedValues() {
        assertEquals(HunterWeaponSelection.STAKE, HunterWeaponSelection.roll(0.99D, 2.0D));
        assertEquals(HunterWeaponSelection.PITCHFORK, HunterWeaponSelection.roll(0.0D, -1.0D));
        assertEquals(HunterWeaponSelection.STAKE, HunterWeaponSelection.fromSerializedName("stake"));
        assertEquals(HunterWeaponSelection.UNASSIGNED, HunterWeaponSelection.fromSerializedName("unknown"));
    }
}
