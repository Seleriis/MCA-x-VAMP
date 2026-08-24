package com.guilh.mca_vampirism_compat.mixin.accessor;

import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = VillagerEntityMCA.class, remap = false)
public interface VillagerEntityMCARecoveryAccessor {
    @Invoker("startRecoveryFoodUse")
    boolean mcaVampirismCompat$startRecoveryFoodUse(FoodProperties food, boolean fromInventory, ItemStack previousMainHand);
}
