package com.guilh.mca_vampirism_compat.attribute;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class McaVampireAttributeHandler {
    private static final ResourceLocation MAX_HEALTH_ID = id("vampire_max_health");
    private static final ResourceLocation ATTACK_DAMAGE_ID = id("vampire_attack_damage");
    private static final ResourceLocation ARMOR_ID = id("vampire_armor");
    private static final ResourceLocation FOLLOW_RANGE_ID = id("vampire_follow_range");

    private McaVampireAttributeHandler() {
    }

    public static void applyVampireAttributes(LivingEntity entity) {
        applyVampireAttributes(entity, false);
    }

    public static void applyVampireAttributes(LivingEntity entity, boolean fillHealth) {
        if (!McaVillagerUtil.isMcaVampire(entity)) {
            return;
        }

        float healthBefore = entity.getHealth();
        applyModifier(entity, Attributes.MAX_HEALTH, MAX_HEALTH_ID,
                McaVampirismCompatConfig.maxHealthMultiplier() - 1.0D,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        applyModifier(entity, Attributes.ARMOR, ARMOR_ID, McaVampirismCompatConfig.armorBonus(),
                AttributeModifier.Operation.ADD_VALUE);

        if (shouldApplyCombatAttributes(entity)) {
            applyModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID,
                    McaVampirismCompatConfig.attackDamageBonus(), AttributeModifier.Operation.ADD_VALUE);
            applyModifier(entity, Attributes.FOLLOW_RANGE, FOLLOW_RANGE_ID,
                    McaVampirismCompatConfig.followRangeBonus(), AttributeModifier.Operation.ADD_VALUE);
        } else {
            removeModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID);
            removeModifier(entity, Attributes.FOLLOW_RANGE, FOLLOW_RANGE_ID);
        }

        if (fillHealth) {
            entity.setHealth(entity.getMaxHealth());
        } else {
            entity.setHealth(Math.min(healthBefore, entity.getMaxHealth()));
        }
    }

    public static void removeVampireAttributes(LivingEntity entity) {
        removeModifier(entity, Attributes.MAX_HEALTH, MAX_HEALTH_ID);
        removeModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID);
        removeModifier(entity, Attributes.ARMOR, ARMOR_ID);
        removeModifier(entity, Attributes.FOLLOW_RANGE, FOLLOW_RANGE_ID);
        entity.setHealth(Math.min(entity.getHealth(), entity.getMaxHealth()));
    }

    public static boolean hasVampireAttributes(LivingEntity entity) {
        boolean hasSafeAttributes = hasModifierIfAttributeExists(entity, Attributes.MAX_HEALTH, MAX_HEALTH_ID)
                && hasModifierIfAttributeExists(entity, Attributes.ARMOR, ARMOR_ID);
        if (!hasSafeAttributes) {
            return false;
        }
        if (!shouldApplyCombatAttributes(entity)) {
            return true;
        }
        return hasModifierIfAttributeExists(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_ID)
                && hasModifierIfAttributeExists(entity, Attributes.FOLLOW_RANGE, FOLLOW_RANGE_ID);
    }

    private static void applyModifier(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id, double amount,
                                      AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, amount, operation));
    }

    private static void removeModifier(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    private static boolean hasModifierIfAttributeExists(
            LivingEntity entity,
            Holder<Attribute> attribute,
            ResourceLocation id) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance == null || instance.getModifier(id) != null;
    }

    private static boolean isAdultMcaVillager(LivingEntity entity) {
        return entity instanceof VillagerEntityMCA villager && villager.getAgeState() == AgeState.ADULT;
    }

    private static boolean shouldApplyCombatAttributes(LivingEntity entity) {
        return !McaVampirismCompatConfig.applyCombatAttributesOnlyToAdults() || isAdultMcaVillager(entity);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("mca_vamp_compat", path);
    }
}
