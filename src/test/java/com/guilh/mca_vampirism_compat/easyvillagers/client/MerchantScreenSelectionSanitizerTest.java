package com.guilh.mca_vampirism_compat.easyvillagers.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MerchantScreenSelectionSanitizerTest {
    @Test
    void resetsSelectionWhenCycledListIsShorter() {
        MerchantScreenSelectionSanitizer.Selection selection =
                MerchantScreenSelectionSanitizer.sanitize(2, 0, 2);

        assertEquals(0, selection.selectedOffer());
        assertEquals(0, selection.scrollOffset());
    }

    @Test
    void clampsScrollToTheNewOfferCount() {
        MerchantScreenSelectionSanitizer.Selection selection =
                MerchantScreenSelectionSanitizer.sanitize(3, 8, 4);

        assertEquals(3, selection.selectedOffer());
        assertEquals(0, selection.scrollOffset());
    }

    @Test
    void emptyOfferListAlwaysUsesSafeZeroState() {
        MerchantScreenSelectionSanitizer.Selection selection =
                MerchantScreenSelectionSanitizer.sanitize(5, 4, 0);

        assertEquals(0, selection.selectedOffer());
        assertEquals(0, selection.scrollOffset());
    }
}
