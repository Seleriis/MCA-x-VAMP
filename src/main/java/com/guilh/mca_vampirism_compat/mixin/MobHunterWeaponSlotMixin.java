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
 * One common boundary for every hand mutation. {@link Mob#setItemSlot} is the
 * implementation reached by vanilla setItemInHand as well as MCA and optional
 * integrations that write equipment slots directly. The service narrows this
 * to the reserved dominant slot of persistent real Hunters only.
 */
@Mixin(Mob.class)
public abstract class MobHunterWeaponSlotMixin {
    // This target is intentionally on Mob (not LivingEntity): both vanilla
    // setItemInHand and MCA's direct equipment writers converge here. Let
    // MixinGradle map the official name to Forge's production SRG name so the
    // same mixin also works in official-name userdev runs.
    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$keepPersistentHunterWeaponInReservedSlot(
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
