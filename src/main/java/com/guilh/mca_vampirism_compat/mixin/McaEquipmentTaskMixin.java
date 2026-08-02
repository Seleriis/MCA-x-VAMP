package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaDefensiveVillagerService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.brain.tasks.EquipmentTask;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MCA owns the visual equipment of its villagers and may clear the dominant
 * hand while refreshing a profession's equipment set. Run after that refresh
 * so a valid Hunter's pitchfork is never visible only until the next compat
 * synchronization tick.
 */
@Mixin(value = EquipmentTask.class, remap = false)
public abstract class McaEquipmentTaskMixin {
    @Inject(
            method = "start(Lnet/minecraft/server/level/ServerLevel;Lnet/conczin/mca/entity/VillagerEntityMCA;J)V",
            at = @At("TAIL")
    )
    private void mcaVampirismCompat$restoreHunterPitchforkAfterEquipmentRefresh(
            ServerLevel level,
            VillagerEntityMCA villager,
            long gameTime,
            CallbackInfo ci
    ) {
        McaDefensiveVillagerService.reconcileHunterWeaponAfterMcaEquipmentTask(villager);
    }
}
