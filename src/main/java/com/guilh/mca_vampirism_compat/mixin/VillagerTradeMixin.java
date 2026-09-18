package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.trade.McaFactionTradeService;
import com.guilh.mca_vampirism_compat.trade.McaNativeTradeCycleSession;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MCA 7.6 owns its trade entry point instead of delegating to vanilla's
 * {@code Villager.startTrading}. Hook its copied flow so the real MCA merchant
 * menu and its offer list remain authoritative.
 */
@Mixin(value = VillagerEntityMCA.class, remap = false)
public abstract class VillagerTradeMixin {
    @Inject(method = "copiedBeginTradeWith", at = @At("HEAD"), cancellable = true, remap = false)
    private void mcaVampCompat$blockFactionTrade(Player player, CallbackInfo ci) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        if (McaFactionTradeService.shouldCancelMcaTradeOpening(player, villager)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "copiedBeginTradeWith",
            at = @At(
                    value = "INVOKE",
                    target = "Lforge/net/mca/entity/VillagerEntityMCA;copiedPrepareOffersFor(Lnet/minecraft/world/entity/player/Player;)V",
                    shift = At.Shift.AFTER,
                    remap = false
            ),
            remap = false
    )
    private void mcaVampCompat$applyFactionPriceAfterNativePrices(Player player, CallbackInfo ci) {
        VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
        if (!villager.level().isClientSide) {
            McaFactionTradeService.prepareNativeMcaTrade(player, villager);
        }
    }

    @Inject(method = "copiedBeginTradeWith", at = @At("TAIL"), remap = false)
    private void mcaVampCompat$openCycleTradesSession(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            VillagerEntityMCA villager = (VillagerEntityMCA) (Object) this;
            McaNativeTradeCycleSession.openOrRefresh(serverPlayer, villager);
        }
    }
}
