package com.guilh.mca_vampirism_compat.easyvillagers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * Read-only breeding rule for MCA snapshots owned by MCA x Easy Villagers.
 *
 * <p>This deliberately uses the optional addon's public copy API instead of
 * inspecting or rewriting its proprietary stack components.</p>
 */
public final class McaEvBreedingEligibility {
    private static final String MCA_INFERTILE_TRAIT_ID = "mca:infertile";
    private static final String LEGACY_INFERTILE_TRAIT_ID = "infertile";

    private McaEvBreedingEligibility() {
    }

    public static boolean isInfertileMcaParent(ItemStack stack) {
        CompoundTag data = McaEvCompatBridge.copyStoredMcaVillagerDataReadOnly(stack);
        return data != null && hasInfertileTrait(data);
    }

    static boolean hasInfertileTrait(CompoundTag villagerData) {
        if (villagerData == null) {
            return false;
        }
        return traitMapContainsInfertile(villagerData)
                || villagerData.contains("MCAData", Tag.TAG_COMPOUND)
                && traitMapContainsInfertile(villagerData.getCompound("MCAData"));
    }

    private static boolean traitMapContainsInfertile(CompoundTag data) {
        for (String key : data.getAllKeys()) {
            if (!"traits".equals(key.toLowerCase(Locale.ROOT))
                    || !(data.get(key) instanceof CompoundTag traits)) {
                continue;
            }
            for (String traitKey : traits.getAllKeys()) {
                if (isInfertileTraitId(traitKey) && traits.getBoolean(traitKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isInfertileTraitId(String traitId) {
        String normalized = traitId == null ? "" : traitId.toLowerCase(Locale.ROOT);
        return MCA_INFERTILE_TRAIT_ID.equals(normalized)
                || LEGACY_INFERTILE_TRAIT_ID.equals(normalized);
    }
}
