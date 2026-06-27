package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.service.McaAggressiveVillagerProtectionService;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "de.teamlapen.vampirism_integrations.mca.AggressiveVillagerEntityMCA", remap = false)
public abstract class AggressiveVillagerEntityMCAMixin {
    @Inject(method = "makeAngry", at = @At("HEAD"), cancellable = true, require = 0)
    private static void mcaVampirismCompat$blockMakeAngry(VillagerEntityMCA villager, CallbackInfoReturnable<Villager> cir) {
        String newType = villager == null ? "vampirism_integrations:unknown_villager_mca_angry"
                : McaAggressiveVillagerProtectionService.predictedMcaAggressiveType(villager);
        if (McaAggressiveVillagerProtectionService.shouldCancelReplacement(
                villager,
                "AggressiveVillagerEntityMCA.makeAngry",
                newType
        )) {
            McaVampirismCompat.debugLog(
                    "[AGGRESSIVE-VILLAGERS] makeAngry blocked reason=MCA_PRESERVE_ENTITY target={}",
                    villager == null ? null : villager.getUUID()
            );
            cir.setReturnValue(null);
        }
    }
}
