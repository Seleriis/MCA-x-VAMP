package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/**
 * Reads the 1.0.3 Forge capability envelope without replacing the MCA entity.
 */
public final class LegacyStateMigrationService {
    private static final String LEGACY_CAPABILITY = "mca_vampirism_compat:vampiric_villager";
    private static final String MOD_ID_ALIAS_CAPABILITY = "mca_vamp_compat:vampiric_villager";

    private LegacyStateMigrationService() {
    }

    public static void migrate(VillagerEntityMCA villager, CompoundTag entityTag) {
        if (!McaVampirismCompatConfig.enableLegacy103Migration() || entityTag == null) {
            return;
        }
        CompoundTag legacyState = findLegacyState(entityTag);
        if (legacyState == null) {
            return;
        }
        ModCapabilities.get(villager).ifPresent(state -> {
            if (hasIdentityState(state)) {
                return;
            }
            state.deserializeNBT(villager.level().registryAccess(), legacyState);
            McaVampirismCompat.LOGGER.info(
                    "Migrated MCA villager {} from the 1.0.3 Forge state payload",
                    villager.getUUID()
            );
        });
    }

    static CompoundTag findLegacyState(CompoundTag entityTag) {
        CompoundTag direct = findById(entityTag);
        if (direct != null) {
            return direct;
        }
        for (String envelope : new String[]{"ForgeCaps", "forge:capabilities", "Capabilities"}) {
            if (entityTag.contains(envelope, Tag.TAG_COMPOUND)) {
                CompoundTag nested = findById(entityTag.getCompound(envelope));
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static CompoundTag findById(CompoundTag container) {
        if (container.contains(LEGACY_CAPABILITY, Tag.TAG_COMPOUND)) {
            return container.getCompound(LEGACY_CAPABILITY).copy();
        }
        if (container.contains(MOD_ID_ALIAS_CAPABILITY, Tag.TAG_COMPOUND)) {
            return container.getCompound(MOD_ID_ALIAS_CAPABILITY).copy();
        }
        return null;
    }

    private static boolean hasIdentityState(VampiricVillagerState state) {
        return state.isInfected()
                || state.isConverted()
                || state.isVampireAppearanceApplied()
                || state.isHunterAligned()
                || state.isHunterTraitApplied()
                || state.isHunterVillageApplied()
                || state.isWerewolfInfected()
                || state.isWerewolfConverted()
                || state.isWerewolfAppearanceApplied()
                || state.isWerewolfTraitApplied()
                || state.isCuringVampire()
                || state.isCuringWerewolf();
    }
}
