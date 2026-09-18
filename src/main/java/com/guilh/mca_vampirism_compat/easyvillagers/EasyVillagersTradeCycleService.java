package com.guilh.mca_vampirism_compat.easyvillagers;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.trade.EntityBackedMerchant;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeSession;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeService;
import com.guilh.mca_vampirism_compat.trade.McaNativeTradeCycleSession;
import com.guilh.mca_vampirism_compat.trade.MerchantMenuTradeAccess;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;

public final class EasyVillagersTradeCycleService {
    private EasyVillagersTradeCycleService() {
    }

    /** Handles the C2S Cycle Trades message without touching GUI-only code. */
    public static boolean handleCycleTrades(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        if (EasyVillagersFactionMerchantSession.handleCycleTrades(player)) {
            logDispatch(player, "EASY_VILLAGERS_TRADER", "ACTIVE");
            return true;
        }
        if (McaFactionTradeSession.handleCycleTrades(player)) {
            logDispatch(player, "MCA_MENU", "ACTIVE");
            return true;
        }
        if (McaNativeTradeCycleSession.handleCycleTrades(player)) {
            logDispatch(player, "MCA_NATIVE_MENU", "ACTIVE");
            return true;
        }
        if (!McaEvCompatBridge.isEasyVillagersTradeCyclingEnabled()) {
            return false;
        }
        return recoverActiveMcaMenuSession(player);
    }

    private static boolean recoverActiveMcaMenuSession(ServerPlayer player) {
        if (!(player.containerMenu instanceof MerchantMenu menu)
                || !((Object) menu instanceof MerchantMenuTradeAccess access)) {
            return false;
        }
        Merchant merchant = access.mcaVampCompat$getTrader();
        if (!(merchant instanceof VillagerEntityMCA villager)) {
            if (merchant instanceof EntityBackedMerchant) {
                McaVampirismCompat.LOGGER.warn(
                        "[MCA Reborn x Vampirism Compat] Cycle Trades received an MCA-backed wrapper without an active session; allowing Easy Villagers fallback player={} merchant={}",
                        player.getGameProfile().getName(),
                        merchant.getClass().getName()
                );
            }
            return false;
        }
        if (McaFactionTradeService.shouldHandleMcaTrade(villager)) {
            McaFactionTradeSession.open(
                    player,
                    villager,
                    McaFactionTradeService.resolveTradeFaction(player, villager)
            );
            logDispatch(player, "MCA_MENU", "RECOVERED");
            return true;
        }
        if (McaNativeTradeCycleSession.recoverForActiveMenu(player, villager)) {
            logDispatch(player, "MCA_NATIVE_MENU", "RECOVERED");
            return true;
        }
        McaVampirismCompat.LOGGER.warn(
                "[MCA Reborn x Vampirism Compat] Cycle Trades could not recover an active MCA menu; allowing Easy Villagers fallback player={} villager={}",
                player.getGameProfile().getName(),
                villager.getUUID()
        );
        return false;
    }

    private static void logDispatch(ServerPlayer player, String route, String session) {
        McaVampirismCompat.debugLog(
                "[MCA Reborn x Vampirism Compat] Cycle Trades dispatched entryPoint=MESSAGE route={} session={} player={} container={}",
                route,
                session,
                player.getGameProfile().getName(),
                player.containerMenu.containerId
        );
    }
}
