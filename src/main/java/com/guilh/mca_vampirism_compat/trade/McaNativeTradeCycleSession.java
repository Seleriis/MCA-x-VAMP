package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.easyvillagers.McaEvCompatBridge;
import com.guilh.mca_vampirism_compat.network.EasyVillagersCycleStatePayload;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.network.PacketDistributor;

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

        // Special factions own their existing price-overlay session. It uses
        // exactly the same server-authoritative eligibility rule.
        if (McaFactionTradeSession.sendCurrentCycleState(player, villager)) {
            closeForPlayer(player.getUUID(), "faction merchant opened");
            return;
        }

        closeForPlayer(player.getUUID(), "new native MCA merchant");
        closeForVillager(villager.getUUID(), "new native MCA merchant");
        McaNativeTradeCycleSession session = new McaNativeTradeCycleSession(player, villager);
        SESSIONS_BY_PLAYER.put(player.getUUID(), session);
        SESSIONS_BY_VILLAGER.put(villager.getUUID(), session);
        session.sendCurrentCycleState();
        McaVampirismCompat.debugLog(
                "[TRADE] native MCA Cycle Trades session opened player={} villager={} available={}",
                player.getName().getString(), villager.getUUID(), session.isAvailable()
        );
    }

    /**
     * Recreates only the session belonging to the active real MCA merchant.
     * The dispatcher has already verified the menu owner; vanilla and wrapper
     * merchants must never be converted into an MCA session here.
     */
    public static boolean recoverForActiveMenu(ServerPlayer player, VillagerEntityMCA villager) {
        if (player == null || villager == null || !McaEvCompatBridge.isEasyVillagersTradeCyclingEnabled()) {
            return false;
        }
        openOrRefresh(player, villager);
        return hasActiveSessionFor(player, villager);
    }

    public static boolean handleCycleTrades(ServerPlayer player) {
        McaNativeTradeCycleSession session = player == null ? null : SESSIONS_BY_PLAYER.get(player.getUUID());
        if (session == null || session.closed) {
            return false;
        }
        session.cycleTrades();
        return true;
    }

    /** Visible to the dispatcher and its regression test without exposing mutable state. */
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
        if (!(player.containerMenu instanceof MerchantMenu menu)
                || !isAvailable()
                || TradeCycleEligibility.hasActiveOffer(menu)) {
            return;
        }

        villager.setOffers(null);
        McaOfferPreparation.prepare(villager, player);
        menu.setSelectionHint(0);
        menu.slotsChanged(player.getInventory());
        MerchantOffers offers = villager.getOffers();
        player.sendMerchantOffers(
                menu.containerId,
                offers,
                villager.getVillagerData().getLevel(),
                villager.getVillagerXp(),
                villager.showProgressBar(),
                villager.canRestock()
        );
        sendCycleState(menu.containerId);
        McaVampirismCompat.debugLog(
                "[TRADE] native MCA trades cycled player={} villager={} offers={}",
                player.getName().getString(), villager.getUUID(), offers.size()
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
        // GameTests and server-side synthetic players intentionally have no
        // network listener. The session is still valid and authoritative;
        // only the optional client button state has nowhere to be sent.
        if (player.connection == null) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new EasyVillagersCycleStatePayload(containerId, isAvailable()));
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
