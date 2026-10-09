package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import de.teamlapen.vampirism.fluids.BloodHelper;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandler;


/**
 * Faction-specific recovery for MCA 7.6.  This MCA line does not expose the
 * 1.21 recovery-use hook, so the compat consumes the real item/fluid at a
 * conservative interval rather than injecting into a method that does not
 * exist.  It never manufactures a bottle, food stack, or health without a
 * corresponding successful consumption.
 */
public final class McaFactionDietService {
    public static final int VAMPIRE_BLOOD_PER_RECOVERY = 100;
    private static final ResourceLocation BLOOD_BOTTLE_ID =
            new ResourceLocation("vampirism", "blood_bottle");
    private static final TagKey<Item> WEREWOLF_RAW_FOOD =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, new ResourceLocation("werewolves", "raw_food"));
    private static final TagKey<Item> WEREWOLF_COOKED_FOOD =
            TagKey.create(net.minecraft.core.registries.Registries.ITEM, new ResourceLocation("werewolves", "cooked_food"));
    private static final int RECOVERY_INTERVAL_TICKS = 40;

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
        return BloodHelper.getBlood(stack) >= VAMPIRE_BLOOD_PER_RECOVERY;
    }

    /** Performs one immediate vampire recovery attempt for compatibility callers. */
    public static boolean startVampireRecovery(VillagerEntityMCA villager) {
        return recoverVampire(villager);
    }

    /** MCA 7.6 has no injected recovery completion hook; recovery is immediate. */
    public static boolean completeVampireRecovery(VillagerEntityMCA villager) {
        return false;
    }

    public static void cancelVampireRecovery(VillagerEntityMCA villager) {
        // Kept as a no-op for callers shared with newer versions.
    }

    /** Called by the regular MCA-villager runtime tick. */
    public static void tickRecovery(VillagerEntityMCA villager) {
        if (villager == null || villager.level().isClientSide
                || villager.getHealth() >= villager.getMaxHealth()
                || villager.tickCount % RECOVERY_INTERVAL_TICKS != 0
                || villager.isUsingItem()) {
            return;
        }
        if (usesVampireRecoveryDiet(villager)) {
            recoverVampire(villager);
        } else if (usesWerewolfRecoveryDiet(villager)) {
            recoverWerewolf(villager);
        }
    }

    private static boolean recoverVampire(VillagerEntityMCA villager) {
        if (villager == null || !usesVampireRecoveryDiet(villager) || villager.level().isClientSide) {
            return false;
        }
        InteractionHand hand = villager.getDominantHand();
        ItemStack held = villager.getItemInHand(hand);
        if (drainBlood(held, replacement -> villager.setItemInHand(hand, replacement))) {
            villager.heal(1.0F);
            return true;
        }
        SimpleContainer inventory = villager.getInventory();
        int slot = firstUsableBloodSlot(inventory);
        if (slot < 0) {
            return false;
        }
        ItemStack bottle = inventory.getItem(slot);
        if (!drainBlood(bottle, replacement -> inventory.setItem(slot, replacement))) {
            return false;
        }
        villager.heal(1.0F);
        return true;
    }

    private static boolean recoverWerewolf(VillagerEntityMCA villager) {
        InteractionHand hand = villager.getDominantHand();
        ItemStack held = villager.getItemInHand(hand);
        if (isWerewolfRecoveryFood(held)) {
            held.shrink(1);
            villager.heal(1.0F);
            return true;
        }
        SimpleContainer inventory = villager.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack candidate = inventory.getItem(slot);
            if (isWerewolfRecoveryFood(candidate)) {
                candidate.shrink(1);
                villager.heal(1.0F);
                return true;
            }
        }
        return false;
    }

    private static boolean drainBlood(ItemStack stack, java.util.function.Consumer<ItemStack> replacementConsumer) {
        if (!hasUsableBlood(stack)) {
            return false;
        }
        return BloodHelper.drain(
                stack,
                VAMPIRE_BLOOD_PER_RECOVERY,
                IFluidHandler.FluidAction.EXECUTE,
                true,
                replacementConsumer
        ) >= VAMPIRE_BLOOD_PER_RECOVERY;
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
