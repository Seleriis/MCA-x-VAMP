package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.inheritance.McaBabyInheritancePayload;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.item.BabyItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BabyItem.class, remap = false)
public abstract class McaBabyItemMixin {
    @Inject(method = "createItem", at = @At("RETURN"), remap = false)
    private static void mcaVampirismCompat$storeInheritance(Entity mother, Entity father, long seed,
                                                            CallbackInfoReturnable<ItemStack> cir) {
        McaBabyInheritancePayload.decideAndWrite(cir.getReturnValue(), mother, father);
    }

    @Inject(method = "birthChild", at = @At("RETURN"), remap = false)
    private void mcaVampirismCompat$restoreInheritance(ItemStack stack, ServerLevel level, ServerPlayer player,
                                                       CallbackInfoReturnable<VillagerEntityMCA> cir) {
        McaBabyInheritancePayload.apply(stack, cir.getReturnValue());
    }
}
