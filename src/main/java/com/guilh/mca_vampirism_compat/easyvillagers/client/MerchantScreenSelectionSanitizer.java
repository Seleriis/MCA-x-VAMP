package com.guilh.mca_vampirism_compat.easyvillagers.client;

public final class MerchantScreenSelectionSanitizer {
    private static final int VISIBLE_OFFER_COUNT = 7;

    private MerchantScreenSelectionSanitizer() {
    }

    public static Selection sanitize(int selectedOffer, int scrollOffset, int offerCount) {
        if (offerCount <= 0) {
            return new Selection(0, 0);
        }

        int safeSelected = selectedOffer >= 0 && selectedOffer < offerCount ? selectedOffer : 0;
        int maximumScroll = Math.max(0, offerCount - VISIBLE_OFFER_COUNT);
        int safeScroll = Math.max(0, Math.min(scrollOffset, maximumScroll));
        if (safeSelected < safeScroll) {
            safeScroll = safeSelected;
        } else if (safeSelected >= safeScroll + VISIBLE_OFFER_COUNT) {
            safeScroll = Math.min(maximumScroll, safeSelected - VISIBLE_OFFER_COUNT + 1);
        }
        return new Selection(safeSelected, safeScroll);
    }

    public record Selection(int selectedOffer, int scrollOffset) {
    }
}
