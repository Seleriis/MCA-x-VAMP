package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.trade.McaVampireSpecialTradeService;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@link AbstractVillager#getOffers()} is the shared lazy-generation boundary
 * in MCA 7.6. Applying the special offer to that returned live list preserves
 * native transactions and the persistent idempotence markers.
 */
@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerTradeMixin {
    @Inject(method = "getOffers", at = @At("RETURN"))
    private void mcaVampCompat$addVampirismSpecialOffer(CallbackInfoReturnable<MerchantOffers> cir) {
        if (!((Object) this instanceof VillagerEntityMCA villager)
                || villager.level().isClientSide
                || McaVampireSpecialTradeService.isAutomaticRollDeferred(villager)
                || !McaVampireStateService.isVampire(villager)) {
            return;
        }
        McaVampireSpecialTradeService.ensureForVampire(villager, cir.getReturnValue());
    }
}
