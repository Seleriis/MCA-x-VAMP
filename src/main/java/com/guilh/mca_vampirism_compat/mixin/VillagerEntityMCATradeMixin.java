package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.service.McaTradeContextService;
import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeService;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VillagerEntityMCA.class, remap = false)
public abstract class VillagerEntityMCATradeMixin {
    @Inject(method = "m_6071_", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$diagnoseMobInteract(Player player, InteractionHand hand,
                                                        CallbackInfoReturnable<InteractionResult> cir) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        InteractionResult cureResult = McaVampireCureService.handleDirectInteraction(player, villager, hand);
        if (cureResult != null) {
            cir.setReturnValue(cureResult);
            return;
        }
        InteractionResult result = McaFactionTradeService.onMobInteractHead(player, villager, hand);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "copiedBeginTradeWith", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$blockFactionTrade(Player player, CallbackInfo ci) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        if (McaTradeContextService.isRecentMcaTradeContext(player, villager)) {
            McaVampirismCompat.debugLog(
                    "[MCA-INTERACTION] copiedBeginTradeWith from MCA trade option"
            );
        }
        if (McaFactionTradeService.onTradeAttempt(player, villager, "copiedBeginTradeWith HEAD")) {
            ci.cancel();
        }
    }
}
