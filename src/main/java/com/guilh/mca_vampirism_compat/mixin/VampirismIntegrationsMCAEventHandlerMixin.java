package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.service.McaAggressiveVillagerProtectionService;
import de.teamlapen.vampirism.api.event.VampirismVillageEvent;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "de.teamlapen.vampirism_integrations.mca.MCAEventHandler", remap = false)
public abstract class VampirismIntegrationsMCAEventHandlerMixin {
    @Inject(method = "onCreateAggressiveVillager", at = @At("HEAD"), cancellable = true, require = 0)
    private void mcaVampirismCompat$blockAggressiveMcaReplacement(VampirismVillageEvent.MakeAggressive event, CallbackInfo ci) {
        Villager oldVillager = event.getOldVillager();
        String newType = oldVillager instanceof VillagerEntityMCA mcaVillager
                ? McaAggressiveVillagerProtectionService.predictedMcaAggressiveType(mcaVillager)
                : "unknown";
        if (McaAggressiveVillagerProtectionService.shouldCancelReplacement(
                oldVillager,
                "MCAEventHandler.onCreateAggressiveVillager",
                newType
        )) {
            event.setCanceled(true);
            McaVampirismCompat.debugLog(
                    "[AGGRESSIVE-VILLAGERS] integration handler canceled reason=MCA_PRESERVE_ENTITY target={}",
                    oldVillager == null ? null : oldVillager.getUUID()
            );
            ci.cancel();
        }
    }
}
