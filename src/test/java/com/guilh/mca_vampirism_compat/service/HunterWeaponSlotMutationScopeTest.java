package com.guilh.mca_vampirism_compat.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HunterWeaponSlotMutationScopeTest {
    @Test
    void authorizationIsScopedAndSupportsNestedCompatWrites() {
        assertFalse(HunterWeaponSlotMutationScope.isAuthorized());

        HunterWeaponSlotMutationScope.run(() -> {
            assertTrue(HunterWeaponSlotMutationScope.isAuthorized());
            HunterWeaponSlotMutationScope.run(() -> assertTrue(HunterWeaponSlotMutationScope.isAuthorized()));
            assertTrue(HunterWeaponSlotMutationScope.isAuthorized());
        });

        assertFalse(HunterWeaponSlotMutationScope.isAuthorized());
    }

    @Test
    void authorizationIsReleasedAfterAnException() {
        try {
            HunterWeaponSlotMutationScope.run(() -> {
                throw new IllegalStateException("test");
            });
        } catch (IllegalStateException ignored) {
            // Expected: the important assertion is made after unwinding.
        }

        assertFalse(HunterWeaponSlotMutationScope.isAuthorized());
    }
}
