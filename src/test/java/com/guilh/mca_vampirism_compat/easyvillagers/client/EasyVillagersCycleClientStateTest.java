package com.guilh.mca_vampirism_compat.easyvillagers.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EasyVillagersCycleClientStateTest {
    @AfterEach
    void clearState() {
        EasyVillagersCycleClientState.clear();
    }

    @Test
    void enablesOnlyTheMatchingMerchantMenu() {
        EasyVillagersCycleClientState.update(17, true);

        assertTrue(EasyVillagersCycleClientState.hasStateFor(17));
        assertTrue(EasyVillagersCycleClientState.isEnabledFor(17));
        assertFalse(EasyVillagersCycleClientState.hasStateFor(18));
        assertFalse(EasyVillagersCycleClientState.isEnabledFor(18));
    }

    @Test
    void disabledPayloadOwnsTheMenuAndPreventsNativeFallback() {
        EasyVillagersCycleClientState.update(9, false);
        assertTrue(EasyVillagersCycleClientState.hasStateFor(9));
        assertFalse(EasyVillagersCycleClientState.isEnabledFor(9));
    }

    @Test
    void completedTradeDisablesAndAdvancesRevision() {
        EasyVillagersCycleClientState.update(9, true);
        int enabledRevision = EasyVillagersCycleClientState.revisionFor(9);

        EasyVillagersCycleClientState.disable(9);

        assertFalse(EasyVillagersCycleClientState.isEnabledFor(9));
        assertNotEquals(enabledRevision, EasyVillagersCycleClientState.revisionFor(9));
    }

    @Test
    void screenCloseRemovesMenuOwnership() {
        EasyVillagersCycleClientState.update(9, true);
        EasyVillagersCycleClientState.clear();
        assertFalse(EasyVillagersCycleClientState.hasStateFor(9));
        assertFalse(EasyVillagersCycleClientState.isEnabledFor(9));
    }
}
