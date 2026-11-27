package com.guilh.mca_vampirism_compat.attribute;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import forge.net.conczin.mca.entity.ai.relationship.AgeState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public final class McaVampireAttributeHandler {
    // Keep the legacy Forge UUIDs so an existing 1.20.1 entity receives the
    // same modifier identity after the 2.1.7 update.
    private static final UUID MAX_HEALTH_UUID = UUID.fromString("2b2ff74b-64cf-40bf-a8ec-3c9f4a1b24a1");
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("19e0a97f-3f07-462c-9f3c-30a3ccbb1485");
    private static final UUID ARMOR_UUID = UUID.fromString("762f9d41-0897-41cf-bf20-39c2275b7a5d");
    private static final UUID FOLLOW_RANGE_UUID = UUID.fromString("0625cccf-28a3-46d8-bc0f-8538540dd379");

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
        applyModifier(entity, Attributes.MAX_HEALTH, MAX_HEALTH_UUID, "MCA vampire max health",
                McaVampirismCompatConfig.maxHealthMultiplier() - 1.0D,
                AttributeModifier.Operation.MULTIPLY_BASE);
        applyModifier(entity, Attributes.ARMOR, ARMOR_UUID, "MCA vampire armor",
                McaVampirismCompatConfig.armorBonus(), AttributeModifier.Operation.ADDITION);

        if (shouldApplyCombatAttributes(entity)) {
            applyModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID, "MCA vampire attack damage",
                    McaVampirismCompatConfig.attackDamageBonus(), AttributeModifier.Operation.ADDITION);
            applyModifier(entity, Attributes.FOLLOW_RANGE, FOLLOW_RANGE_UUID, "MCA vampire follow range",
                    McaVampirismCompatConfig.followRangeBonus(), AttributeModifier.Operation.ADDITION);
        } else {
            removeModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID);
            removeModifier(entity, Attributes.FOLLOW_RANGE, FOLLOW_RANGE_UUID);
        }

        if (fillHealth) {
            entity.setHealth(entity.getMaxHealth());
        } else {
            entity.setHealth(Math.min(healthBefore, entity.getMaxHealth()));
        }
    }

    public static void removeVampireAttributes(LivingEntity entity) {
        removeModifier(entity, Attributes.MAX_HEALTH, MAX_HEALTH_UUID);
        removeModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID);
        removeModifier(entity, Attributes.ARMOR, ARMOR_UUID);
        removeModifier(entity, Attributes.FOLLOW_RANGE, FOLLOW_RANGE_UUID);
        entity.setHealth(Math.min(entity.getHealth(), entity.getMaxHealth()));
    }

    public static boolean hasVampireAttributes(LivingEntity entity) {
        boolean hasSafeAttributes = hasModifierIfAttributeExists(entity, Attributes.MAX_HEALTH, MAX_HEALTH_UUID)
                && hasModifierIfAttributeExists(entity, Attributes.ARMOR, ARMOR_UUID);
        if (!hasSafeAttributes) {
            return false;
        }
        if (!shouldApplyCombatAttributes(entity)) {
            return true;
        }
        return hasModifierIfAttributeExists(entity, Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE_UUID)
                && hasModifierIfAttributeExists(entity, Attributes.FOLLOW_RANGE, FOLLOW_RANGE_UUID);
    }

    private static void applyModifier(LivingEntity entity, Attribute attribute, UUID uuid, String name, double amount,
                                      AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        instance.removeModifier(uuid);
        instance.addTransientModifier(new AttributeModifier(uuid, name, amount, operation));
    }

    private static void removeModifier(LivingEntity entity, Attribute attribute, UUID uuid) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(uuid);
        }
    }

    private static boolean hasModifierIfAttributeExists(
            LivingEntity entity,
            Attribute attribute,
            UUID uuid) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance == null || instance.getModifier(uuid) != null;
    }

    private static boolean isAdultMcaVillager(LivingEntity entity) {
        return entity instanceof VillagerEntityMCA villager && villager.getAgeState() == AgeState.ADULT;
    }

    private static boolean shouldApplyCombatAttributes(LivingEntity entity) {
        return !McaVampirismCompatConfig.applyCombatAttributesOnlyToAdults() || isAdultMcaVillager(entity);
    }
}
