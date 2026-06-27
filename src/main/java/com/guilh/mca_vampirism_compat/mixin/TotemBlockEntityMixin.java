package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.service.McaAggressiveVillagerProtectionService;
import de.teamlapen.vampirism.blockentity.TotemBlockEntity;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TotemBlockEntity.class, remap = false)
public abstract class TotemBlockEntityMixin {
    @Inject(method = "makeAgressive(Lnet/minecraft/world/entity/npc/Villager;)V", at = @At("HEAD"), cancellable = true)
    private static void mcaVampirismCompat$blockBaseAggressiveVillagerReplacement(Villager villager, CallbackInfo ci) {
        if (McaAggressiveVillagerProtectionService.shouldCancelReplacement(
                villager,
                "TotemBlockEntity.makeAgressive",
                "vampirism:villager_angry"
        )) {
            McaVampirismCompat.debugLog(
                    "[AGGRESSIVE-VILLAGERS] base handler canceled reason=MCA_PRESERVE_ENTITY target={}",
                    villager == null ? null : villager.getUUID()
            );
            ci.cancel();
        }
    }
}
