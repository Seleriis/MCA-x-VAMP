package com.guilh.mca_vampirism_compat.appearance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaNativeEyeAppearanceBridgeTest {
    @Test
    void capturesAppliesAndRestoresNativeMcaEyeDyes() {
        EyeCarrier carrier = new EyeCarrier(0xFF123456, 0xFF654321);

        assertTrue(McaNativeEyeAppearanceBridge.supportsNativeEyeDyes(carrier));
        McaNativeEyeAppearanceBridge.EyeSnapshot snapshot = McaNativeEyeAppearanceBridge.capture(carrier);
        assertTrue(snapshot.nativeSupported());
        assertEquals(0xFF123456, snapshot.eyeDye());
        assertEquals(0xFF654321, snapshot.eyeLeftDye());

        assertTrue(McaNativeEyeAppearanceBridge.applyVampireEyes(carrier));
        assertEquals(McaNativeEyeAppearanceBridge.VAMPIRE_EYE_DYE, carrier.getEyeDye());
        assertEquals(McaNativeEyeAppearanceBridge.VAMPIRE_EYE_DYE, carrier.getEyeLeftDye());

        assertTrue(McaNativeEyeAppearanceBridge.restoreOriginalEyes(
                carrier, snapshot.eyeDye(), snapshot.eyeLeftDye()));
        assertEquals(0xFF123456, carrier.getEyeDye());
        assertEquals(0xFF654321, carrier.getEyeLeftDye());

        assertTrue(McaNativeEyeAppearanceBridge.restoreOriginalEyes(carrier, -1, -1));
        assertEquals(-1, carrier.getEyeDye());
        assertEquals(-1, carrier.getEyeLeftDye());
    }

    @Test
    void legacyMcaCarrierIsACompleteNoOp() {
        Object legacyCarrier = new Object();

        assertFalse(McaNativeEyeAppearanceBridge.supportsNativeEyeDyes(legacyCarrier));
        assertFalse(McaNativeEyeAppearanceBridge.capture(legacyCarrier).nativeSupported());
        assertFalse(McaNativeEyeAppearanceBridge.applyVampireEyes(legacyCarrier));
        assertFalse(McaNativeEyeAppearanceBridge.restoreOriginalEyes(legacyCarrier, -1, -1));
    }

    /** Models the public MCA 7.6.28 API without linking tests to the beta jar. */
    public static final class EyeCarrier {
        private int eyeDye;
        private int eyeLeftDye;

        EyeCarrier(int eyeDye, int eyeLeftDye) {
            this.eyeDye = eyeDye;
            this.eyeLeftDye = eyeLeftDye;
        }

        public int getEyeDye() {
            return eyeDye;
        }

        public void setEyeDye(int eyeDye) {
            this.eyeDye = eyeDye;
        }

        public void clearEyeDye() {
            this.eyeDye = -1;
        }

        public int getEyeLeftDye() {
            return eyeLeftDye;
        }

        public void setEyeLeftDye(int eyeLeftDye) {
            this.eyeLeftDye = eyeLeftDye;
        }

        public void clearEyeLeftDye() {
            this.eyeLeftDye = -1;
        }
    }
}
