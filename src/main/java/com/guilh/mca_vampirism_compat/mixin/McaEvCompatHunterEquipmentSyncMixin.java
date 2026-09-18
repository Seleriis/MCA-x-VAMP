package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaDefensiveVillagerService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CompatLogic can synchronize a proxy entity's equipment into a reconstructed
 * MCA villager before it is captured or saved. A proxy has no authority over
 * a persistent Hunter's reserved hand, so skip that one slot before the
 * source copy and inventory mirror can alter its backing.
 */
@Pseudo
@Mixin(targets = "com.guilh.mcaevcompat.CompatLogic", priority = 2000, remap = false)
public abstract class McaEvCompatHunterEquipmentSyncMixin {
    @Inject(method = "syncEquipmentSlot", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void mcaVampirismCompat$keepReservedHunterEquipmentDuringProxySync(
            LivingEntity source,
            VillagerEntityMCA target,
            EquipmentSlot slot,
            CallbackInfo ci
    ) {
        if (McaDefensiveVillagerService.isPersistentHunterWeaponReserved(target, slot)) {
            ci.cancel();
        }
    }
}
