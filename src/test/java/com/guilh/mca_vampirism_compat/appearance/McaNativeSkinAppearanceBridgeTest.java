package com.guilh.mca_vampirism_compat.appearance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaNativeSkinAppearanceBridgeTest {
    @Test
    void clearsExplicitSkinDyeAndRestoresTheCapturedColour() {
        SkinCarrier carrier = new SkinCarrier(0xFF8A624B);

        assertTrue(McaNativeSkinAppearanceBridge.supportsNativeSkinDye(carrier));
        McaNativeSkinAppearanceBridge.SkinSnapshot snapshot = McaNativeSkinAppearanceBridge.capture(carrier);
        assertTrue(snapshot.nativeSupported());
        assertEquals(0xFF8A624B, snapshot.skinDye());

        assertTrue(McaNativeSkinAppearanceBridge.clearForVampireAppearance(carrier));
        assertEquals(McaNativeSkinAppearanceBridge.NO_SKIN_DYE, carrier.getSkinDye());

        assertTrue(McaNativeSkinAppearanceBridge.restoreOriginalSkinDye(carrier, snapshot.skinDye()));
        assertEquals(0xFF8A624B, carrier.getSkinDye());

        assertTrue(McaNativeSkinAppearanceBridge.restoreOriginalSkinDye(
                carrier, McaNativeSkinAppearanceBridge.NO_SKIN_DYE));
        assertEquals(McaNativeSkinAppearanceBridge.NO_SKIN_DYE, carrier.getSkinDye());
    }

    @Test
    void legacyMcaCarrierIsACompleteNoOp() {
        Object legacyCarrier = new Object();

        assertFalse(McaNativeSkinAppearanceBridge.supportsNativeSkinDye(legacyCarrier));
        assertFalse(McaNativeSkinAppearanceBridge.capture(legacyCarrier).nativeSupported());
        assertFalse(McaNativeSkinAppearanceBridge.clearForVampireAppearance(legacyCarrier));
        assertFalse(McaNativeSkinAppearanceBridge.restoreOriginalSkinDye(
                legacyCarrier, McaNativeSkinAppearanceBridge.NO_SKIN_DYE));
    }

    /** Models MCA 7.6.28's public skin-dye API without linking tests to the beta jar. */
    public static final class SkinCarrier {
        private int skinDye;

        SkinCarrier(int skinDye) {
            this.skinDye = skinDye;
        }

        public int getSkinDye() {
            return skinDye;
        }

        public void setSkinDye(int skinDye) {
            this.skinDye = skinDye;
        }

        public void clearSkinDye() {
            this.skinDye = McaNativeSkinAppearanceBridge.NO_SKIN_DYE;
        }
    }
}
