package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.BiteableEntry;
import de.teamlapen.vampirism.api.entity.IExtendedCreatureVampirism;
import de.teamlapen.vampirism.entity.ExtendedCreature;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public final class McaBloodValueRepairService {
    private static final ResourceLocation MCA_FEMALE_VILLAGER =
            new ResourceLocation("mca", "female_villager");
    private static final ResourceLocation MCA_MALE_VILLAGER =
            new ResourceLocation("mca", "male_villager");
    private static final int MCA_VILLAGER_FALLBACK_BLOOD = 15;
    private static final String KEY_BLOOD = "bloodLevel";
    // Vampirism Forge 1.20.1 reads this camel-case key from
    // ExtendedCreature#loadData. The NeoForge 1.21 line uses a different
    // serialization shape, so this must remain specific to the Forge port.
    private static final String KEY_MAX_BLOOD = "maxBlood";

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
            extendedCreature.loadData(tag);
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

    /**
     * Ensures a human MCA villager has usable Vampirism blood before
     * {@code VampirePlayer.determineBiteType} evaluates the target. The
     * native method chooses whether the fangs are shown before its later bite
     * callbacks run, so repairing only during {@code canBeBitten} can leave a
     * freshly synchronized MCA villager looking like an invalid target on the
     * client.
     *
     * <p>This deliberately does not refill an empty, previously drained
     * source: it only initializes an absent/invalid capability value through
     * {@link #repairMcaBloodValue(PathfinderMob, IExtendedCreatureVampirism)}.
     * MCA Vampires remain outside this fallback and continue through the
     * configured protection policy.</p>
     *
     * @return {@code true} when the target is an initialized MCA blood source
     *         with blood available for the native Vampirism flow
     */
    public static boolean prepareMcaBloodSourceForBite(LivingEntity target) {
        if (!(target instanceof VillagerEntityMCA villager) || McaVillagerUtil.isMcaVampire(villager)) {
            return false;
        }
        return ExtendedCreature.getSafe(villager)
                .map(creature -> {
                    repairMcaBloodValue(villager, creature);
                    return creature.getBlood() > 0;
                })
                .orElse(false);
    }

    private static int resolveBloodValue(PathfinderMob entity) {
        BiteableEntry entry = VampirismAPI.entityRegistry().getEntry(entity);
        if (entry != null && entry.blood > 0) {
            return entry.blood;
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
