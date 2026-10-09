package com.guilh.mca_vampirism_compat.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaNativeHunterAttackCompatibilityTest {

    @Test
    void keepsTheLegacyGuardMultiplierOnlyForTheOriginalAttackLayout() {
        assertTrue(McaSpecialVillagerCombatService.usesLegacyMcaGuardAttackMultiplier("7.6.16+1.20.1"));
        assertFalse(McaSpecialVillagerCombatService.usesLegacyMcaGuardAttackMultiplier("7.6.28-beta.10+1.20.1"));
        assertTrue(McaSpecialVillagerCombatService.usesLegacyMcaGuardAttackMultiplier("7.6.29-beta.1+1.20.1"));
        assertFalse(McaSpecialVillagerCombatService.usesLegacyMcaGuardAttackMultiplier("7.7.1-beta.3+1.20.1"));
        assertTrue(McaSpecialVillagerCombatService.usesLegacyMcaGuardAttackMultiplier(null));
    }
}
