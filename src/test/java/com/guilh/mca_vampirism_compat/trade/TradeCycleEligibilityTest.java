package com.guilh.mca_vampirism_compat.trade;

import net.minecraft.world.item.trading.MerchantOffers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeCycleEligibilityTest {
    @Test
    void allowsAnUntradedProfessionalVillagerAtAnyXpValue() {
        MerchantOffers offers = new MerchantOffers();

        assertTrue(TradeCycleEligibility.isAvailable(false, false, offers));
    }

    @Test
    void rejectsUsedOffersClosedMenusAndExplicitTradeLock() {
        MerchantOffers offers = new MerchantOffers();

        assertFalse(TradeCycleEligibility.isAvailable(true, false, offers));
        assertFalse(TradeCycleEligibility.isAvailable(false, true, offers));
    }

    @Test
    void rejectsMissingOfferState() {
        assertFalse(TradeCycleEligibility.isAvailable(false, false, null));
    }
}
