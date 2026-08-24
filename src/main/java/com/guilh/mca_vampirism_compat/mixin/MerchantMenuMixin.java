package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.trade.EntityBackedMerchant;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeSession;
import com.guilh.mca_vampirism_compat.trade.McaNativeTradeCycleSession;
import com.guilh.mca_vampirism_compat.trade.MerchantMenuTradeAccess;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin implements MerchantMenuTradeAccess {
    @Shadow
    @Final
    private Merchant trader;

    @Shadow
    @Final
    private MerchantContainer tradeContainer;

    @Override
    public MerchantContainer mcaVampCompat$getTradeContainer() {
        return tradeContainer;
    }

    @Inject(method = "playTradeSound", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$playWrapperTradeSound(CallbackInfo ci) {
        if (!(trader instanceof EntityBackedMerchant entityBackedMerchant)) {
            return;
        }

        Entity soundEntity = entityBackedMerchant.mcaVampCompat$getTradeSoundEntity();
        if (!trader.isClientSide() && soundEntity != null) {
            soundEntity.level().playLocalSound(
                    soundEntity.getX(),
                    soundEntity.getY(),
                    soundEntity.getZ(),
                    trader.getNotifyTradeSound(),
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F,
                    false
            );
        }
        ci.cancel();
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void mcaVampirismCompat$closeNativeMcaFactionSession(net.minecraft.world.entity.player.Player player,
                                                                 CallbackInfo ci) {
        if (trader instanceof VillagerEntityMCA villager) {
            McaFactionTradeSession.closeForMenu(player, villager, "merchant menu removed");
            McaNativeTradeCycleSession.closeForMenu(player, villager, "merchant menu removed");
        }
    }
}
