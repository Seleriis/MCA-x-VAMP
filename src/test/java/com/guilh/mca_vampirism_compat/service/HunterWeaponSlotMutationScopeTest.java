package com.guilh.mca_vampirism_compat.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HunterWeaponSlotMutationScopeTest {

    @Test
    void authorizesOnlyTheCurrentScopedMutation() {
        assertFalse(HunterWeaponSlotMutationScope.isAuthorized());

        HunterWeaponSlotMutationScope.run(() -> {
            assertTrue(HunterWeaponSlotMutationScope.isAuthorized());
            HunterWeaponSlotMutationScope.run(() -> assertTrue(HunterWeaponSlotMutationScope.isAuthorized()));
            assertTrue(HunterWeaponSlotMutationScope.isAuthorized());
        });

        assertFalse(HunterWeaponSlotMutationScope.isAuthorized());
    }

    @Test
    void clearsAuthorizationWhenTheOwnedWriteFails() {
        try {
            HunterWeaponSlotMutationScope.run(() -> {
                throw new IllegalStateException("expected test failure");
            });
        } catch (IllegalStateException expected) {
            // expected
        }

        assertFalse(HunterWeaponSlotMutationScope.isAuthorized());
    }
}
