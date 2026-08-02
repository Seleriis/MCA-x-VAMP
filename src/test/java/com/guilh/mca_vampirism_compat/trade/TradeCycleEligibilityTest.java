package com.guilh.mca_vampirism_compat.trade;

import net.minecraft.world.item.trading.MerchantOffers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeCycleEligibilityTest {
    @Test
    void allowsFreshVanillaAndSyntheticMcaProfessionXp() {
        MerchantOffers offers = new MerchantOffers();

        assertTrue(TradeCycleEligibility.isAvailable(false, true, 0, false, offers));
        assertTrue(TradeCycleEligibility.isAvailable(false, true, 1, false, offers));
    }

    @Test
    void rejectsRealProgressClosedMenusAndExplicitTradeLock() {
        MerchantOffers offers = new MerchantOffers();

        assertFalse(TradeCycleEligibility.isAvailable(false, true, 2, false, offers));
        assertFalse(TradeCycleEligibility.isAvailable(true, true, 0, false, offers));
        assertFalse(TradeCycleEligibility.isAvailable(false, true, 0, true, offers));
        assertFalse(TradeCycleEligibility.isAvailable(false, false, 0, false, offers));
    }

    @Test
    void rejectsMissingOfferState() {
        assertFalse(TradeCycleEligibility.isAvailable(false, true, 0, false, null));
    }
}
