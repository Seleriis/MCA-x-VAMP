package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.relationship.AgeState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class McaVampireSunlightHandler {
    private static final int SUN_DAMAGE_INTERVAL_TICKS = 20;
    private static final float SUN_DAMAGE = 1.0F;

    private McaVampireSunlightHandler() {
    }

    public static void processSunlightDamage(LivingEntity entity) {
        if (!McaVampirismCompatConfig.vampireVillagersBurnInSun()
                || entity.level().isClientSide
                || entity.tickCount % SUN_DAMAGE_INTERVAL_TICKS != 0
                || !(entity instanceof VillagerEntityMCA villager)
                || villager.getAgeState() != AgeState.ADULT
                || !McaVillagerUtil.isMcaVampire(entity)
                || !isSunlightExposed(entity)) {
            return;
        }

        entity.hurt(entity.damageSources().onFire(), SUN_DAMAGE);
    }

    public static boolean isSunlightExposed(LivingEntity entity) {
        Level level = entity.level();
        if (!level.dimensionType().hasSkyLight() || !level.isDay() || !level.canSeeSky(entity.blockPosition())) {
            return false;
        }
        if (entity.isInWaterRainOrBubble()) {
            return false;
        }
        ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
        return headItem.isEmpty();
    }
}
