package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.datamaps.IEntityBlood;
import de.teamlapen.vampirism.api.entity.IExtendedCreatureVampirism;
import de.teamlapen.vampirism.entity.ExtendedCreature;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PathfinderMob;

public final class McaBloodValueRepairService {
    private static final ResourceLocation MCA_FEMALE_VILLAGER =
            ResourceLocation.fromNamespaceAndPath("mca", "female_villager");
    private static final ResourceLocation MCA_MALE_VILLAGER =
            ResourceLocation.fromNamespaceAndPath("mca", "male_villager");
    private static final int MCA_VILLAGER_FALLBACK_BLOOD = 15;
    private static final String KEY_BLOOD = "bloodLevel";
    private static final String KEY_MAX_BLOOD = "max_blood";

    private McaBloodValueRepairService() {
    }

    public static void repairMcaBloodValue(PathfinderMob entity, IExtendedCreatureVampirism creature) {
        if (!(entity instanceof VillagerEntityMCA) || creature == null || McaVillagerUtil.isMcaVampire(entity)) {
            return;
        }
        if (creature.getMaxBlood() > 0 && creature.getBlood() >= 0) {
            return;
        }

        int bloodValue = resolveBloodValue(entity);
        if (bloodValue <= 0) {
            McaVampirismCompat.debugLog(
                    "[BITE] MCA blood repair skipped entity={} type={} reason=NO_BLOOD_VALUE",
                    entity.getUUID(),
                    entity.getType()
            );
            return;
        }

        CompoundTag tag = createFallbackBloodTag(bloodValue);
        if (creature instanceof ExtendedCreature extendedCreature) {
            extendedCreature.deserializeNBT(entity.level().registryAccess(), tag);
        } else {
            creature.setBlood(bloodValue);
        }
        McaVampirismCompat.debugLog(
                "[BITE] MCA bloodvalue repaired entity={} type={} blood={}",
                entity.getUUID(),
                entity.getType(),
                bloodValue
        );
    }

    private static int resolveBloodValue(PathfinderMob entity) {
        IEntityBlood entry = VampirismAPI.entityRegistry().getEntry(entity);
        if (entry != null && entry.blood() > 0) {
            return entry.blood();
        }
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (MCA_FEMALE_VILLAGER.equals(entityId) || MCA_MALE_VILLAGER.equals(entityId)) {
            return MCA_VILLAGER_FALLBACK_BLOOD;
        }
        return 0;
    }

    static CompoundTag createFallbackBloodTag(int bloodValue) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(KEY_MAX_BLOOD, bloodValue);
        tag.putInt(KEY_BLOOD, bloodValue);
        return tag;
    }
}
