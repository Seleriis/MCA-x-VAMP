package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeService;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VillagerEntityMCA.class, remap = false)
public abstract class VillagerEntityMCATradeMixin {
    @Inject(method = "m_6071_", at = @At("HEAD"), cancellable = true, remap = false)
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

    /**
     * MCA's direct interaction route opens its merchant from inside
     * {@code m_6071_}. Blocking precisely at that call preserves every other
     * interaction (gifts, items and sneaking) while sharing the same faction
     * rule as the dialogue trade option.
     */
    @Inject(
            method = "m_6071_",
            at = @At(
                    value = "INVOKE",
                    target = "Lforge/net/mca/entity/VillagerEntityMCA;copiedBeginTradeWith(Lnet/minecraft/world/entity/player/Player;)V"
            ),
            cancellable = true,
            remap = false
    )
    private void mcaVampirismCompat$blockDirectFactionTrade(Player player, InteractionHand hand,
                                                             CallbackInfoReturnable<InteractionResult> cir) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        if (!player.level().isClientSide && McaFactionTradeService.shouldCancelMcaTradeOpening(player, villager)) {
            cir.setReturnValue(InteractionResult.CONSUME);
        }
    }

}
