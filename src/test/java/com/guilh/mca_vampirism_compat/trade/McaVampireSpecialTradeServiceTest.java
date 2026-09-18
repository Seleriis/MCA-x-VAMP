package com.guilh.mca_vampirism_compat.trade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaVampireSpecialTradeServiceTest {
    @Test
    void mirrorsVampirismsOneInThreeConvertedVillagerRoll() {
        assertTrue(McaVampireSpecialTradeService.shouldAddForNativeRoll(0));
        assertFalse(McaVampireSpecialTradeService.shouldAddForNativeRoll(1));
        assertFalse(McaVampireSpecialTradeService.shouldAddForNativeRoll(2));
    }

    @Test
    void cycleTradeRollsRemainIndependentAfterAMiss() {
        int[] deterministicRerolls = {0, 1, 0};

        assertTrue(McaVampireSpecialTradeService.shouldAddForNativeRoll(deterministicRerolls[0]));
        assertFalse(McaVampireSpecialTradeService.shouldAddForNativeRoll(deterministicRerolls[1]));
        assertTrue(McaVampireSpecialTradeService.shouldAddForNativeRoll(deterministicRerolls[2]));
    }
}
