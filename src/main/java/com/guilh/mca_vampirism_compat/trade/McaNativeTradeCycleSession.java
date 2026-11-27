package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.easyvillagers.McaEvCompatBridge;
import com.guilh.mca_vampirism_compat.network.EasyVillagersCycleStatePayload;
import com.guilh.mca_vampirism_compat.network.ModNetwork;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import forge.net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-authoritative Cycle Trades state for ordinary MCA villagers.
 *
 * <p>mca-ev-compat gives stored professional villagers a small amount of XP
 * while rebuilding them. Easy Villagers normally interprets that XP as a
 * completed trade. MCA's guard profession is therefore tracked here by an
 * actual completed offer (or our permanent lock), never by profession XP.</p>
 */
public final class McaNativeTradeCycleSession {
    private static final Map<UUID, McaNativeTradeCycleSession> SESSIONS_BY_PLAYER = new HashMap<>();
    private static final Map<UUID, McaNativeTradeCycleSession> SESSIONS_BY_VILLAGER = new HashMap<>();

    private final ServerPlayer player;
    private final VillagerEntityMCA villager;
    private boolean closed;

    private McaNativeTradeCycleSession(ServerPlayer player, VillagerEntityMCA villager) {
        this.player = player;
        this.villager = villager;
    }

    /** Called only after MCA has opened the real merchant menu. */
    public static void openOrRefresh(ServerPlayer player, VillagerEntityMCA villager) {
        if (player == null || villager == null || !McaEvCompatBridge.isEasyVillagersTradeCyclingEnabled()) {
            return;
        }
        openOrRefresh(player, villager, "OPENED");
    }

    /**
     * Reattaches a session only after the dispatcher has validated that the
     * current MerchantMenu really owns this MCA villager and that Easy
     * Villagers' native Cycle Trades setting is enabled.
     */
    public static boolean recoverForActiveMenu(ServerPlayer player, VillagerEntityMCA villager) {
        return openOrRefresh(player, villager, "RECOVERED");
    }

    private static boolean openOrRefresh(
            ServerPlayer player,
            VillagerEntityMCA villager,
            String sessionState
    ) {
        if (player == null || villager == null) {
            return false;
        }

        // Special factions own their existing price-overlay session. It uses
        // exactly the same server-authoritative eligibility rule.
        if (McaFactionTradeSession.sendCurrentCycleState(player, villager)) {
            closeForPlayer(player.getUUID(), "faction merchant opened");
            return true;
        }

        closeForPlayer(player.getUUID(), "new native MCA merchant");
        closeForVillager(villager.getUUID(), "new native MCA merchant");
        McaNativeTradeCycleSession session = new McaNativeTradeCycleSession(player, villager);
        SESSIONS_BY_PLAYER.put(player.getUUID(), session);
        SESSIONS_BY_VILLAGER.put(villager.getUUID(), session);
        session.sendCurrentCycleState();
        McaVampirismCompat.debugLog(
                "[MCA Reborn x Vampirism Compat] Cycle Trades session={} route=MCA_NATIVE_MENU player={} villager={} offers={}",
                sessionState,
                player.getGameProfile().getName(),
                villager.getUUID(),
                villager.getOffers().size()
        );
        McaVampirismCompat.debugLog(
                "[TRADE] native MCA Cycle Trades session opened player={} villager={} available={}",
                player.getName().getString(), villager.getUUID(), session.isAvailable()
        );
        return true;
    }

    public static boolean handleCycleTrades(ServerPlayer player) {
        McaNativeTradeCycleSession session = player == null ? null : SESSIONS_BY_PLAYER.get(player.getUUID());
        if (session == null || session.closed || !session.ownsActiveMerchantMenu()) {
            return false;
        }
        session.cycleTrades();
        return true;
    }

    /** Visible to the recovery regression test without exposing mutable state. */
    public static boolean hasActiveSessionFor(ServerPlayer player, VillagerEntityMCA villager) {
        McaNativeTradeCycleSession session = player == null ? null : SESSIONS_BY_PLAYER.get(player.getUUID());
        return session != null && !session.closed && session.villager == villager;
    }

    public static void onNativeTradeCompleted(Player player, VillagerEntityMCA villager) {
        if (player == null || villager == null) {
            return;
        }
        McaNativeTradeCycleSession session = SESSIONS_BY_PLAYER.get(player.getUUID());
        if (session == null || session.closed || session.villager != villager) {
            return;
        }
        session.sendCurrentCycleState();
    }

    public static void closeForPlayer(UUID playerUuid, String reason) {
        McaNativeTradeCycleSession session = SESSIONS_BY_PLAYER.get(playerUuid);
        if (session != null) {
            session.close(reason);
        }
    }

    public static void closeForVillager(UUID villagerUuid, String reason) {
        McaNativeTradeCycleSession session = SESSIONS_BY_VILLAGER.get(villagerUuid);
        if (session != null) {
            session.close(reason);
        }
    }

    public static void closeForMenu(Player player, VillagerEntityMCA villager, String reason) {
        if (player == null || villager == null) {
            return;
        }
        McaNativeTradeCycleSession session = SESSIONS_BY_PLAYER.get(player.getUUID());
        if (session != null && session.villager == villager) {
            session.close(reason);
        }
    }

    private void cycleTrades() {
        if (!(player.containerMenu instanceof MerchantMenu menu)) {
            logCycleSkipped("NOT_MERCHANT_MENU");
            return;
        }
        if (!ownsActiveMerchantMenu()) {
            logCycleSkipped("NOT_CURRENT_MERCHANT");
            return;
        }
        if (!isAvailable()) {
            logCycleSkipped("NOT_ELIGIBLE");
            return;
        }
        if (TradeCycleEligibility.hasActiveOffer(menu)) {
            logCycleSkipped("ACTIVE_TRADE_RESULT");
            return;
        }

        boolean vampireTrade = McaVampireStateService.isVampire(villager);
        if (vampireTrade) {
            McaVampireSpecialTradeService.rebuildForCycle(
                    villager,
                    () -> McaOfferPreparation.rebuild(villager, player),
                    player.getRandom(),
                    "MCA_NATIVE_MENU"
            );
        } else {
            McaOfferPreparation.rebuild(villager, player);
        }
        menu.setSelectionHint(0);
        menu.slotsChanged(player.getInventory());
        MerchantOffers offers = villager.getOffers();
        if (player.connection != null) {
            player.sendMerchantOffers(
                    menu.containerId,
                    offers,
                    villager.getVillagerData().getLevel(),
                    villager.getVillagerXp(),
                    villager.showProgressBar(),
                    villager.canRestock()
            );
        }
        sendCycleState(menu.containerId);
        McaVampirismCompat.debugLog(
                "[MCA Reborn x Vampirism Compat] Cycle Trades handled route=MCA_NATIVE_MENU villager={} eligibleBefore=true rollCompleted={} specialGenerated={} offers={}",
                villager.getUUID(),
                vampireTrade && McaVampireSpecialTradeService.isRollCompleted(villager),
                vampireTrade && McaVampireSpecialTradeService.wasGenerated(villager),
                offers.size()
        );
        McaVampirismCompat.debugLog(
                "[TRADE] native MCA trades cycled player={} villager={} offers={}",
                player.getName().getString(), villager.getUUID(), offers.size()
        );
    }

    private void logCycleSkipped(String reason) {
        McaVampirismCompat.debugLog(
                "[MCA Reborn x Vampirism Compat] Cycle Trades handled route=MCA_NATIVE_MENU villager={} eligibleBefore=false reason={} offers={}",
                villager.getUUID(),
                reason,
                villager.getOffers().size()
        );
    }

    private boolean isAvailable() {
        return TradeCycleEligibility.isAvailable(villager, villager.getOffers(), closed);
    }

    private void sendCurrentCycleState() {
        if (player.containerMenu instanceof MerchantMenu menu) {
            sendCycleState(menu.containerId);
        }
    }

    private void sendCycleState(int containerId) {
        ModNetwork.sendToPlayer(player, new EasyVillagersCycleStatePayload(containerId, isAvailable()));
    }

    private boolean ownsActiveMerchantMenu() {
        if (!(player.containerMenu instanceof MerchantMenu menu)
                || !((Object) menu instanceof MerchantMenuTradeAccess access)) {
            return false;
        }
        return access.mcaVampCompat$getTrader() == villager;
    }

    private void close(String reason) {
        if (closed) {
            return;
        }
        closed = true;
        SESSIONS_BY_PLAYER.remove(player.getUUID(), this);
        SESSIONS_BY_VILLAGER.remove(villager.getUUID(), this);
        McaVampirismCompat.debugLog(
                "[TRADE] native MCA Cycle Trades session closed player={} villager={} reason={}",
                player.getName().getString(), villager.getUUID(), reason
        );
    }
}
