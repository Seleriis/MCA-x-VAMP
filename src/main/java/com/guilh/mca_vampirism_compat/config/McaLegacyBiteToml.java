package com.guilh.mca_vampirism_compat.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Registers deprecated bite options without exposing them to runtime mechanics.
 *
 * <p>The values intentionally remain in the generated specification so existing
 * TOML files keep their keys and comments.  Since 2.1.8, native Vampirism and
 * its addons own vampire-versus-vampire feeding; player melee is never
 * interpreted as a bite by this compat.</p>
 */
final class McaLegacyBiteToml {
    private McaLegacyBiteToml() {
    }

    static void defineVampireFeedingOption(ModConfigSpec.Builder builder) {
        builder.comment("Legacy option retained for existing TOMLs. Since 2.1.8, native Vampirism and addons decide vampire-versus-vampire feeding.");
        builder.define("preventVampiresBitingVampires", true);
    }

    static void definePlayerMeleeOptions(ModConfigSpec.Builder builder) {
        builder.comment("Legacy option retained for existing TOMLs. Player melee attacks are never classified as bites.");
        builder.define("vampirePlayerBiteDealsDamageToMcaVillagers", false);
        builder.defineInRange("vampirePlayerBiteDamageToMcaVillagers", 0.0D, 0.0D, 40.0D);
    }
}
