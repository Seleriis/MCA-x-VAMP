package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaFactionDietService;
import com.guilh.mca_vampirism_compat.service.McaDefensiveVillagerService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies faction diets without replacing MCA's recovery lifecycle. */
@Mixin(value = VillagerEntityMCA.class, remap = false)
public abstract class VillagerEntityMCARecoveryMixin {
    @Shadow
    private static boolean canEat(ItemStack stack) {
        throw new AssertionError();
    }

    @Inject(method = "startRecoveryFoodUse()Z", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$startSpecialRecovery(CallbackInfoReturnable<Boolean> cir) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        if (McaDefensiveVillagerService.consumeHunterRecoveryFoodWithoutChangingHand(villager)) {
            cir.setReturnValue(true);
            return;
        }
        if (McaFactionDietService.usesVampireRecoveryDiet(villager)) {
            cir.setReturnValue(McaFactionDietService.startVampireRecovery(villager));
        }
    }

    @Redirect(
            method = "startRecoveryFoodUse()Z",
            at = @At(value = "INVOKE", target = "Lnet/conczin/mca/entity/VillagerEntityMCA;canEat(Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean mcaVampirismCompat$onlyAllowWerewolfRecoveryFood(ItemStack stack) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        return canEat(stack) && (!McaFactionDietService.usesWerewolfRecoveryDiet(villager)
                || McaFactionDietService.isWerewolfRecoveryFood(stack));
    }

    @Inject(method = "completeUsingItem", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$completeVampireBloodRecovery(CallbackInfo ci) {
        if (McaFactionDietService.completeVampireRecovery((VillagerEntityMCA) (Object) this)) {
            ci.cancel();
        }
    }

    @Inject(method = "stopUsingItem", at = @At("TAIL"))
    private void mcaVampirismCompat$clearCanceledVampireRecovery(CallbackInfo ci) {
        McaFactionDietService.cancelVampireRecovery((VillagerEntityMCA) (Object) this);
    }
}
