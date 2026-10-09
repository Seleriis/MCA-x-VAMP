package com.guilh.mca_vampirism_compat.easyvillagers;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.trade.EntityBackedMerchant;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeSession;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeService;
import com.guilh.mca_vampirism_compat.trade.McaNativeTradeCycleSession;
import com.guilh.mca_vampirism_compat.trade.MerchantMenuTradeAccess;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;

public final class EasyVillagersTradeCycleService {
    private EasyVillagersTradeCycleService() {
    }

    /** Handles Easy Villagers' cycle message for our merchant wrappers. */
    public static boolean handleCycleTrades(ServerPlayer player) {
        return handleCycleTrades(player, "UNKNOWN");
    }

    /**
     * Dispatches Easy Villagers' server packet boundary. Existing sessions
     * never depend on optional-mod reflection. If a real MCA menu arrived
     * after its opening hook was missed, recover its server session before
     * Easy Villagers gets a chance to reroll the list on its own.
     *
     * @return {@code true} only when a compat session consumed the action
     */
    public static boolean handleCycleTrades(ServerPlayer player, String entryPoint) {
        if (player == null) {
            return false;
        }
        if (handleExistingCycleTrades(player, entryPoint)) {
            return true;
        }

        if (!McaEvCompatBridge.canRecoverEasyVillagersCycleTrades()) {
            return false;
        }
        if (!recoverActiveMcaMenuSession(player, entryPoint)) {
            return false;
        }
        return handleExistingCycleTrades(player, entryPoint);
    }

    /**
     * Handles only a session which already exists. Kept separate for
     * diagnostics and regression tests; production packets use
     * {@link #handleCycleTrades(ServerPlayer, String)} so a missing real MCA
     * session is recovered without loading Easy Villagers' mixed client/server
     * GUI handler on a dedicated server.
     */
    public static boolean handleExistingCycleTrades(ServerPlayer player, String entryPoint) {
        if (player == null) {
            return false;
        }
        if (EasyVillagersFactionMerchantSession.handleCycleTrades(player)) {
            logDispatcherResult(player, entryPoint, "EASY_VILLAGERS_TRADER", "ACTIVE");
            return true;
        }
        if (McaFactionTradeSession.handleCycleTrades(player)) {
            logDispatcherResult(player, entryPoint, "MCA_MENU", "ACTIVE");
            return true;
        }
        if (McaNativeTradeCycleSession.handleCycleTrades(player)) {
            logDispatcherResult(player, entryPoint, "MCA_NATIVE_MENU", "ACTIVE");
            return true;
        }
        return false;
    }

    /**
     * Reattaches only the real MCA entity exposed by the active merchant
     * container. Vanilla villagers and wrappers intentionally remain under
     * Easy Villagers' own handler: inventing a session for either would lose
     * their ownership and persistence semantics.
     */
    private static boolean recoverActiveMcaMenuSession(ServerPlayer player, String entryPoint) {
        if (!(player.containerMenu instanceof MerchantMenu menu)
                || !((Object) menu instanceof MerchantMenuTradeAccess access)) {
            return false;
        }

        Merchant merchant = access.mcaVampCompat$getTrader();
        if (!(merchant instanceof VillagerEntityMCA villager)) {
            if (merchant instanceof EntityBackedMerchant) {
                McaVampirismCompat.LOGGER.warn(
                        "[MCA Reborn x Vampirism Compat] Cycle Trades could not recover an MCA-backed wrapper entryPoint={} player={} merchant={}; allowing Easy Villagers fallback",
                        entryPoint,
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
                    McaFactionTradeService.resolveTradeFaction(player, villager),
                    "RECOVERED"
            );
            logRecoveredSession(player, entryPoint, "MCA_MENU", villager);
            return true;
        }

        if (McaNativeTradeCycleSession.recoverForActiveMenu(player, villager)) {
            logRecoveredSession(player, entryPoint, "MCA_NATIVE_MENU", villager);
            return true;
        }

        McaVampirismCompat.LOGGER.warn(
                "[MCA Reborn x Vampirism Compat] Cycle Trades could not recover active MCA menu entryPoint={} player={} villager={}; allowing Easy Villagers fallback",
                entryPoint,
                player.getGameProfile().getName(),
                villager.getUUID()
        );
        return false;
    }

    private static void logDispatcherResult(ServerPlayer player, String entryPoint, String route, String session) {
        McaVampirismCompat.debugLog(
                "[MCA Reborn x Vampirism Compat] Cycle Trades dispatched entryPoint={} route={} session={} player={} container={}",
                entryPoint,
                route,
                session,
                player.getGameProfile().getName(),
                player.containerMenu.containerId
        );
    }

    private static void logRecoveredSession(
            ServerPlayer player,
            String entryPoint,
            String route,
            VillagerEntityMCA villager
    ) {
        McaVampirismCompat.debugLog(
                "[MCA Reborn x Vampirism Compat] Cycle Trades session=RECOVERED entryPoint={} route={} player={} villager={} container={}",
                entryPoint,
                route,
                player.getGameProfile().getName(),
                villager.getUUID(),
                player.containerMenu.containerId
        );
    }
}
