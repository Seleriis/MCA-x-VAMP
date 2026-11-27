package com.guilh.mca_vampirism_compat.attribute;

import com.guilh.mca_vampirism_compat.VampiricVillagerState;
import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerClassifier;
import com.guilh.mca_vampirism_compat.service.McaSpecialVillagerType;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Optional;

public final class McaSpecialVillagerAttributeService {
    private static final ResourceLocation MOVEMENT_SPEED_ID =
            ResourceLocation.fromNamespaceAndPath("mca_vamp_compat", "special_villager_movement_speed");

    private McaSpecialVillagerAttributeService() {
    }

    public static void applyMovementSpeedForState(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide) {
            return;
        }
        McaSpecialVillagerType type = McaSpecialVillagerClassifier.classify(villager);
        double multiplier = movementMultiplier(type);
        AttributeInstance speed = villager.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        Optional<VampiricVillagerState> state = ModCapabilities.get(villager);
        AttributeModifier current = speed.getModifier(MOVEMENT_SPEED_ID);
        if (!shouldHaveMovementModifier(type, multiplier)) {
            if (current != null) {
                speed.removeModifier(MOVEMENT_SPEED_ID);
            }
            state.ifPresent(VampiricVillagerState::clearMovementSpeedModifierApplied);
            return;
        }

        double amount = multiplier - 1.0D;
        if (current != null && Double.compare(current.amount(), amount) == 0) {
            state.ifPresent(value -> value.markMovementSpeedModifierApplied(type.name(), multiplier));
            return;
        }
        if (state.map(value -> value.isMovementSpeedModifierApplied(type.name(), multiplier)).orElse(false)
                && current != null) {
            return;
        }
        if (current != null) {
            speed.removeModifier(MOVEMENT_SPEED_ID);
        }
        speed.addTransientModifier(new AttributeModifier(
                MOVEMENT_SPEED_ID,
                amount,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        ));
        state.ifPresent(value -> value.markMovementSpeedModifierApplied(type.name(), multiplier));
    }

    public static void removeMovementSpeedModifier(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(MOVEMENT_SPEED_ID);
        }
        if (entity instanceof VillagerEntityMCA villager) {
            ModCapabilities.get(villager)
                    .ifPresent(VampiricVillagerState::clearMovementSpeedModifierApplied);
        }
    }

    private static boolean shouldHaveMovementModifier(McaSpecialVillagerType type, double multiplier) {
        boolean canUseMovementModifier = switch (type) {
            case VAMPIRE, HUNTER, WEREWOLF, DEFENSIVE, CURING_VAMPIRE, CURING_WEREWOLF -> true;
            case HUMAN_NORMAL, HUMAN_INFECTED, CHILD, UNKNOWN -> false;
        };
        return canUseMovementModifier && Double.compare(multiplier, 1.0D) != 0;
    }

    private static double movementMultiplier(McaSpecialVillagerType type) {
        return switch (type) {
            case VAMPIRE -> Math.min(McaVampirismCompatConfig.vampireVillagerMovementSpeedMultiplier(), 1.05D);
            case HUNTER -> Math.min(McaVampirismCompatConfig.hunterVillagerMovementSpeedMultiplier(), 1.0D);
            case WEREWOLF -> Math.min(McaVampirismCompatConfig.werewolfVillagerMovementSpeedMultiplier(), 1.08D);
            case DEFENSIVE -> Math.min(McaVampirismCompatConfig.defensiveVillagerMovementSpeedMultiplier(), 1.0D);
            case CURING_VAMPIRE, CURING_WEREWOLF -> McaVampirismCompatConfig.curingVillagerMovementSpeedMultiplier();
            case HUMAN_NORMAL, HUMAN_INFECTED, CHILD, UNKNOWN -> 1.0D;
        };
    }
}
