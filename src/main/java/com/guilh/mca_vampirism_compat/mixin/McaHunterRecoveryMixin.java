package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaDefensiveVillagerService;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

/**
 * MCA 7.6 normally takes recovery food from its inventory and places it in
 * the dominant hand. Persistent Hunters reserve that hand for their weapon,
 * so consume the same inventory food through MCA's own eat implementation
 * instead and skip only the transfer.
 */
@Mixin(value = VillagerEntityMCA.class, remap = false)
public abstract class McaHunterRecoveryMixin {
    @Inject(method = "findAndEquipToMain", at = @At("HEAD"), cancellable = true, remap = false)
    private void mcaVampirismCompat$consumeRecoveryFoodWithoutDisplacingWeapon(
            Predicate<ItemStack> foodPredicate,
            CallbackInfoReturnable<Boolean> cir
    ) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        if (McaDefensiveVillagerService.consumePersistentHunterRecoveryFood(villager, foodPredicate)) {
            cir.setReturnValue(true);
        }
    }
}
