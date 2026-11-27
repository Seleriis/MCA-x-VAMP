package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.mixin.accessor.VillagerEntityMCARecoveryAccessor;
import de.teamlapen.vampirism.core.ModDataComponents;
import de.teamlapen.vampirism.items.component.BottleBlood;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** Keeps the custom vampire drink in MCA's normal recovery-life-cycle. */
public final class McaFactionDietService {
    public static final int VAMPIRE_BLOOD_PER_RECOVERY = 100;
    private static final ResourceLocation BLOOD_BOTTLE_ID =
            ResourceLocation.fromNamespaceAndPath("vampirism", "blood_bottle");
    private static final TagKey<Item> WEREWOLF_RAW_FOOD =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.fromNamespaceAndPath("werewolves", "raw_food"));
    private static final TagKey<Item> WEREWOLF_COOKED_FOOD =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.fromNamespaceAndPath("werewolves", "cooked_food"));
    private static final FoodProperties VAMPIRE_RECOVERY_PROPERTIES = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.0F)
            .build();
    private static final Map<VillagerEntityMCA, Boolean> ACTIVE_VAMPIRE_RECOVERY =
            Collections.synchronizedMap(new WeakHashMap<>());

    private McaFactionDietService() {
    }

    public static boolean usesVampireRecoveryDiet(VillagerEntityMCA villager) {
        return McaVampirismCompatConfig.enableFactionRecoveryDiet()
                && McaVampireStateService.isVampire(villager);
    }

    public static boolean usesWerewolfRecoveryDiet(VillagerEntityMCA villager) {
        return McaVampirismCompatConfig.enableFactionRecoveryDiet()
                && !McaHunterAlignmentService.isMcaHunterVillager(villager)
                && McaWerewolfStateService.isWerewolf(villager);
    }

    public static boolean isWerewolfRecoveryFood(ItemStack stack) {
        return stack.is(WEREWOLF_RAW_FOOD) || stack.is(WEREWOLF_COOKED_FOOD);
    }

    public static boolean hasUsableBlood(ItemStack stack) {
        if (!BLOOD_BOTTLE_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            return false;
        }
        BottleBlood blood = stack.getOrDefault(ModDataComponents.BOTTLE_BLOOD.get(), new BottleBlood(0));
        return blood.blood() >= VAMPIRE_BLOOD_PER_RECOVERY;
    }

    /**
     * Reuses MCA's private recovery setup so its safety checks, temporary hand
     * restoration and use-animation lifecycle stay exactly the same.
     */
    public static boolean startVampireRecovery(VillagerEntityMCA villager) {
        if (!usesVampireRecoveryDiet(villager) || villager.level().isClientSide) {
            return false;
        }
        InteractionHand hand = villager.getDominantHand();
        ItemStack current = villager.getItemInHand(hand);
        boolean fromInventory = false;
        ItemStack previous = ItemStack.EMPTY;
        if (!hasUsableBlood(current)) {
            SimpleContainer inventory = villager.getInventory();
            int slot = firstUsableBloodSlot(inventory);
            if (slot < 0) {
                return false;
            }
            current = inventory.removeItem(slot, 1);
            previous = villager.getItemInHand(hand).copy();
            villager.setItemInHand(hand, current);
            fromInventory = true;
        }
        boolean started = ((VillagerEntityMCARecoveryAccessor) villager)
                .mcaVampirismCompat$startRecoveryFoodUse(VAMPIRE_RECOVERY_PROPERTIES, fromInventory, previous);
        if (started) {
            ACTIVE_VAMPIRE_RECOVERY.put(villager, Boolean.TRUE);
        }
        return started;
    }

    /**
     * Drains the bottle only after MCA's real use duration completes, heals
     * one health point, then delegates hand cleanup back to MCA.
     */
    public static boolean completeVampireRecovery(VillagerEntityMCA villager) {
        if (ACTIVE_VAMPIRE_RECOVERY.remove(villager) == null) {
            return false;
        }
        InteractionHand hand = villager.getDominantHand();
        ItemStack bottle = villager.getItemInHand(hand);
        if (hasUsableBlood(bottle)) {
            BottleBlood blood = bottle.getOrDefault(ModDataComponents.BOTTLE_BLOOD.get(), new BottleBlood(0));
            bottle.set(ModDataComponents.BOTTLE_BLOOD.get(), new BottleBlood(blood.blood() - VAMPIRE_BLOOD_PER_RECOVERY));
            villager.heal(1.0F);
        }
        villager.stopUsingItem();
        return true;
    }

    public static void cancelVampireRecovery(VillagerEntityMCA villager) {
        ACTIVE_VAMPIRE_RECOVERY.remove(villager);
    }

    private static int firstUsableBloodSlot(SimpleContainer inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (hasUsableBlood(inventory.getItem(slot))) {
                return slot;
            }
        }
        return -1;
    }
}
