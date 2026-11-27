package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaDefensiveVillagerService;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MCA's public hand setter delegates to Mob#setItemSlot, but several MCA
 * tasks and MCA x EV write that slot directly. Guard the common destination
 * rather than one caller so a real Hunter never exposes an empty hand between
 * two server equipment packets.
 */
@Mixin(Mob.class)
public abstract class MobHunterWeaponMixin {
    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$keepPersistentHunterWeaponInSlot(
            EquipmentSlot slot,
            ItemStack stack,
            CallbackInfo ci
    ) {
        if (McaDefensiveVillagerService.shouldRejectPersistentHunterWeaponSlotMutation(
                (Mob) (Object) this,
                slot,
                stack
        )) {
            ci.cancel();
        }
    }
}
