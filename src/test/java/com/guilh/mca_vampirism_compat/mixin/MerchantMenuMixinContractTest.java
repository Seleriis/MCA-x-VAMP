package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.trade.MerchantMenuTradeAccess;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MerchantMenuMixinContractTest {

    @Test
    void merchantMenuMixinDoesNotDependOnPrivateFieldShadows() {
        for (Field field : MerchantMenuMixin.class.getDeclaredFields()) {
            assertFalse(field.isAnnotationPresent(Shadow.class),
                    () -> "MerchantMenuMixin must not shadow the runtime-sensitive field " + field.getName());
        }
    }

    @Test
    void menuAccessExposesOnlyTheConstructorCapturedMerchant() {
        assertEquals(1, MerchantMenuTradeAccess.class.getDeclaredMethods().length,
                "Trade eligibility must use the public result slot instead of a private MerchantContainer field");
        assertEquals("mcaVampCompat$getTrader", MerchantMenuTradeAccess.class.getDeclaredMethods()[0].getName());
    }
}
