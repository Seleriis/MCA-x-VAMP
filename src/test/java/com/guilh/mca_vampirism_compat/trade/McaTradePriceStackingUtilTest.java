package com.guilh.mca_vampirism_compat.trade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class McaTradePriceStackingUtilTest {
    @Test
    void fifteenPercentHunterDiscountMakesTwentyCostSeventeen() {
        assertEquals(17,
                McaTradePriceStackingUtil.calculateDesiredCostFromCurrentCost(20, 0.85D, 64));
    }

    @Test
    void discountNeverCreatesZeroCostTrade() {
        assertEquals(1,
                McaTradePriceStackingUtil.calculateDesiredCostFromCurrentCost(1, 0.85D, 64));
    }
}
