package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.trade.EntityBackedMerchant;
import com.guilh.mca_vampirism_compat.trade.MerchantMenuTradeAccess;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(MerchantMenu.class)
public abstract class MerchantMenuMixin implements MerchantMenuTradeAccess {
    /**
     * Captured from the stable three-argument constructor instead of shadowing
     * MerchantMenu's private field. Forge's production field names are
     * remapped, while this constructor descriptor is stable in both the
     * official development namespace and the 1.20.1 SRG runtime namespace.
     */
    @Unique
    private Merchant mcaVampCompat$merchant;

    @Unique
    private static final AtomicBoolean MCA_VAMP_COMPAT$MISSING_MERCHANT_LOGGED = new AtomicBoolean();

    @Inject(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/item/trading/Merchant;)V",
            at = @At("RETURN")
    )
    private void mcaVampCompat$captureMerchant(
            int containerId,
            Inventory inventory,
            Merchant merchant,
            CallbackInfo ci
    ) {
        this.mcaVampCompat$merchant = merchant;
    }

    @Override
    public Merchant mcaVampCompat$getTrader() {
        if (mcaVampCompat$merchant == null
                && MCA_VAMP_COMPAT$MISSING_MERCHANT_LOGGED.compareAndSet(false, true)) {
            McaVampirismCompat.LOGGER.warn(
                    "[MCA Reborn x Vampirism Compat] MerchantMenu did not provide a merchant; "
                            + "Cycle Trades will safely fall back to Easy Villagers for this menu"
            );
        }
        return mcaVampCompat$merchant;
    }

    @Inject(method = "playTradeSound", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$playWrapperTradeSound(CallbackInfo ci) {
        Merchant merchant = mcaVampCompat$getTrader();
        if (!(merchant instanceof EntityBackedMerchant entityBackedMerchant)) {
            return;
        }

        Entity soundEntity = entityBackedMerchant.mcaVampCompat$getTradeSoundEntity();
        if (!merchant.isClientSide() && soundEntity != null) {
            soundEntity.level().playLocalSound(
                    soundEntity.getX(),
                    soundEntity.getY(),
                    soundEntity.getZ(),
                    merchant.getNotifyTradeSound(),
                    SoundSource.NEUTRAL,
                    1.0F,
                    1.0F,
                    false
            );
        }
        ci.cancel();
    }
}
