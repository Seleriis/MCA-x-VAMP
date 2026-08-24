package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeService;
import com.guilh.mca_vampirism_compat.trade.McaVampireSpecialTradeService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VillagerEntityMCA.class, remap = false)
public abstract class VillagerEntityMCATradeMixin {
    /**
     * MCA creates professional offers lazily. Applying the Vampirism roll at
     * this return point means every real MCA offer generation is covered,
     * including profession changes and Cycle Trades, rather than relying only
     * on a merchant-screen opening hook.
     */
    @Inject(method = "getOffers", at = @At("RETURN"))
    private void mcaVampirismCompat$addVampirismSpecialOffer(
            CallbackInfoReturnable<MerchantOffers> cir
    ) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        if (!villager.level().isClientSide && McaVampireStateService.isVampire(villager)) {
            McaVampireSpecialTradeService.ensureForVampire(villager, cir.getReturnValue());
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
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

}
