package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.trade.McaFactionTradeService;
import com.guilh.mca_vampirism_compat.trade.McaNativeTradeCycleSession;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MCA 7.7.36 delegates all merchant openings to vanilla Villager.startTrading.
 * Keeping the faction gate and price overlay here preserves vanilla's single,
 * authoritative offer transaction.
 */
@Mixin(Villager.class)
public abstract class VillagerTradeMixin {
    @Inject(method = "startTrading", at = @At("HEAD"), cancellable = true)
    private void mcaVampCompat$blockFactionTrade(Player player, CallbackInfo ci) {
        if ((Object) this instanceof VillagerEntityMCA villager
                && McaFactionTradeService.shouldCancelMcaTradeOpening(player, villager)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "startTrading",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/npc/Villager;setTradingPlayer(Lnet/minecraft/world/entity/player/Player;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void mcaVampCompat$applyFactionPriceAfterNativePrices(Player player, CallbackInfo ci) {
        if ((Object) this instanceof VillagerEntityMCA villager) {
            McaFactionTradeService.prepareNativeMcaTrade(player, villager);
        }
    }

    @Inject(method = "startTrading", at = @At("TAIL"))
    private void mcaVampCompat$openCycleTradesSession(Player player, CallbackInfo ci) {
        if ((Object) this instanceof VillagerEntityMCA villager && player instanceof ServerPlayer serverPlayer) {
            McaNativeTradeCycleSession.openOrRefresh(serverPlayer, villager);
        }
    }
}
