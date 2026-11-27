package com.guilh.mca_vampirism_compat.mixin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalIntegrationMixinPluginTest {
    private static final String MIXIN_PACKAGE = "com.guilh.mca_vampirism_compat.mixin.";

    @Test
    void selectsExactlyOneSupportedBloodknightDrinkHandler() {
        assumeTrue(hasClass("com.thedrofdoctoring.bloodlines.capabilities.bloodlines.vamp.BloodlineBloodknight")
                        || hasClass("com.thedrofdoctoring.bloodlines.capabilities.bloodlines.BloodlineEventHandler"),
                "Bloodlines is not part of this optional validation profile");

        OptionalIntegrationMixinPlugin plugin = new OptionalIntegrationMixinPlugin();

        boolean selectsV3Handler = plugin.shouldApplyMixin(
                "com.thedrofdoctoring.bloodlines.capabilities.bloodlines.vamp.BloodlineBloodknight",
                MIXIN_PACKAGE + "BloodlinesBloodknightV3Mixin"
        );
        boolean selectsV2Handler = plugin.shouldApplyMixin(
                "com.thedrofdoctoring.bloodlines.capabilities.bloodlines.BloodlineEventHandler",
                MIXIN_PACKAGE + "BloodlinesBloodknightMixin"
        );

        assertTrue(selectsV2Handler || selectsV3Handler,
                "A supported Bloodlines validation jar must expose one drink handler");
        assertTrue(selectsV2Handler != selectsV3Handler,
                "Only the mixin that matches Bloodlines' active handler may be applied");
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className, false, OptionalIntegrationMixinPluginTest.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }
}
