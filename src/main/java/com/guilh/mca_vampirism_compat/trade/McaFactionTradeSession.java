package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.advancement.McaAdvancementService;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.network.EasyVillagersCycleStatePayload;
import com.guilh.mca_vampirism_compat.network.ModNetwork;
import com.guilh.mca_vampirism_compat.service.McaHunterTradeService;
import com.guilh.mca_vampirism_compat.service.McaTradeContextService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfTradeService;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks a normal MCA merchant menu without replacing MCA's native merchant.
 */
public final class McaFactionTradeSession {
    private static final Map<UUID, McaFactionTradeSession> SESSIONS_BY_PLAYER = new HashMap<>();
    private static final Map<UUID, McaFactionTradeSession> SESSIONS_BY_VILLAGER = new HashMap<>();

    private final ServerPlayer player;
    private final VillagerEntityMCA villager;
    private final PlayerFaction faction;
    private final McaFactionTradePriceOverlay.TradeKind tradeKind;
    private McaFactionTradePriceOverlay priceOverlay;
    private boolean closed;

    private McaFactionTradeSession(ServerPlayer player, VillagerEntityMCA villager, PlayerFaction faction,
                                   McaFactionTradePriceOverlay.TradeKind tradeKind) {
        this.player = player;
        this.villager = villager;
        this.faction = faction;
        this.tradeKind = tradeKind;
        this.priceOverlay = McaFactionTradePriceOverlay.create(villager.getOffers(), faction, tradeKind);
        this.priceOverlay.apply();
    }

    public static void open(ServerPlayer player, VillagerEntityMCA villager, PlayerFaction faction) {
        open(player, villager, faction, "OPENED");
    }

    /**
     * Opens the price-overlay session for a real MCA merchant. The explicit
     * source is logged because a session can be created either during the
     * normal MCA opening flow or recovered from an already-open MerchantMenu.
     */
    public static void open(
            ServerPlayer player,
            VillagerEntityMCA villager,
            PlayerFaction faction,
            String sessionState
    ) {
        if (player == null || villager == null || faction == null) {
            return;
        }
        closeForPlayer(player.getUUID(), "new MCA trade session");
        closeForVillager(villager.getUUID(), "new MCA trade session");

        McaFactionTradeSession session = new McaFactionTradeSession(player, villager, faction, tradeKindFor(villager));
        SESSIONS_BY_PLAYER.put(player.getUUID(), session);
        SESSIONS_BY_VILLAGER.put(villager.getUUID(), session);
        McaVampirismCompat.debugLog(
                "[MCA Reborn x Vampirism Compat] Cycle Trades session={} route=MCA_MENU player={} villager={} faction={} offers={}",
                sessionState,
                player.getGameProfile().getName(),
                villager.getUUID(),
                faction,
                session.offers().size()
        );
        McaVampirismCompat.debugLog(
                "[TRADE] native MCA faction session opened player={} villager={} kind={} faction={} offers={}",
                player.getName().getString(),
                villager.getUUID(),
                session.tradeKind,
                faction,
                session.offers().size()
        );
    }

    public static void closeForPlayer(UUID playerUuid, String reason) {
        McaFactionTradeSession session = SESSIONS_BY_PLAYER.get(playerUuid);
        if (session != null) {
            session.close(reason);
        }
    }

    public static void closeForVillager(UUID villagerUuid, String reason) {
        McaFactionTradeSession session = SESSIONS_BY_VILLAGER.get(villagerUuid);
        if (session != null) {
            session.close(reason);
        }
    }

    public static void closeForMenu(Player player, VillagerEntityMCA villager, String reason) {
        if (player == null || villager == null) {
            return;
        }
        McaFactionTradeSession session = SESSIONS_BY_PLAYER.get(player.getUUID());
        if (session != null && session.villager == villager) {
            session.close(reason);
        }
    }

    public static void onNativeTradeCompleted(Player player, VillagerEntityMCA villager, MerchantOffer offer) {
        if (player == null || villager == null) {
            return;
        }
        McaFactionTradeSession session = SESSIONS_BY_PLAYER.get(player.getUUID());
        if (session == null || session.closed || session.villager != villager || !session.priceOverlay.contains(offer)) {
            return;
        }
        McaAdvancementService.awardFactionTrade(session.player, session.faction, session.tradeKind);
        session.sendCurrentCycleState();
        McaVampirismCompat.debugLog(
                "[TRADE] native MCA trade completed player={} villager={} offerUses={}",
                player.getName().getString(),
                villager.getUUID(),
                offer.getUses()
        );
    }

    public static boolean handleCycleTrades(ServerPlayer player) {
        McaFactionTradeSession session = player == null ? null : SESSIONS_BY_PLAYER.get(player.getUUID());
        if (session == null || session.closed || !session.ownsActiveMerchantMenu()) {
            return false;
        }
        session.cycleTrades();
        return true;
    }

    /** Visible to the recovery regression test without exposing session state for mutation. */
    public static boolean hasActiveSessionFor(ServerPlayer player, VillagerEntityMCA villager) {
        McaFactionTradeSession session = player == null ? null : SESSIONS_BY_PLAYER.get(player.getUUID());
        return session != null && !session.closed && session.villager == villager;
    }

    /**
     * Sends the state after MCA has installed the merchant menu. Returning
     * {@code true} tells the ordinary-MCA bridge that this faction session is
     * already responsible for the player.
     */
    public static boolean sendCurrentCycleState(ServerPlayer player, VillagerEntityMCA villager) {
        if (player == null || villager == null) {
            return false;
        }
        McaFactionTradeSession session = SESSIONS_BY_PLAYER.get(player.getUUID());
        if (session == null || session.closed || session.villager != villager) {
            return false;
        }
        session.sendCurrentCycleState();
        return true;
    }

    public void close(String reason) {
        if (closed) {
            return;
        }
        priceOverlay.restore();
        closed = true;
        SESSIONS_BY_PLAYER.remove(player.getUUID(), this);
        SESSIONS_BY_VILLAGER.remove(villager.getUUID(), this);
        McaTradeContextService.clearMcaTradeContext(player, villager);
        McaVampirismCompat.debugLog(
                "[TRADE] native MCA faction session closed player={} villager={} reason={}",
                player.getName().getString(),
                villager.getUUID(),
                reason
        );
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
        if (!isCycleTradesAvailable()) {
            logCycleSkipped("NOT_ELIGIBLE");
            return;
        }
        if (TradeCycleEligibility.hasActiveOffer(menu)) {
            logCycleSkipped("ACTIVE_TRADE_RESULT");
            return;
        }

        priceOverlay.restore();
        boolean vampireTrade = McaVampireStateService.isVampire(villager);
        if (vampireTrade) {
            McaVampireSpecialTradeService.rebuildForCycle(
                    villager,
                    () -> McaOfferPreparation.rebuild(villager, player),
                    player.getRandom(),
                    "MCA_MENU"
            );
        } else {
            McaOfferPreparation.rebuild(villager, player);
        }
        priceOverlay = McaFactionTradePriceOverlay.create(villager.getOffers(), faction, tradeKind);
        priceOverlay.apply();

        menu.setSelectionHint(0);
        menu.slotsChanged(player.getInventory());
        sendCycleState(menu.containerId);
        if (player.connection != null) {
            player.sendMerchantOffers(
                    menu.containerId,
                    offers(),
                    villager.getVillagerData().getLevel(),
                    villager.getVillagerXp(),
                    villager.showProgressBar(),
                    villager.canRestock()
            );
        }
        McaVampirismCompat.debugLog(
                "[MCA Reborn x Vampirism Compat] Cycle Trades handled route=MCA_MENU villager={} eligibleBefore=true rollCompleted={} specialGenerated={} offers={} faction={}",
                villager.getUUID(),
                vampireTrade && McaVampireSpecialTradeService.isRollCompleted(villager),
                vampireTrade && McaVampireSpecialTradeService.wasGenerated(villager),
                offers().size(),
                faction
        );
        McaVampirismCompat.debugLog(
                "[TRADE] native MCA trades cycled player={} villager={} offers={} faction={}",
                player.getName().getString(),
                villager.getUUID(),
                offers().size(),
                faction
        );
    }

    private void logCycleSkipped(String reason) {
        McaVampirismCompat.debugLog(
                "[MCA Reborn x Vampirism Compat] Cycle Trades handled route=MCA_MENU villager={} eligibleBefore=false reason={} offers={} faction={}",
                villager.getUUID(),
                reason,
                offers() == null ? -1 : offers().size(),
                faction
        );
    }

    private boolean isCycleTradesAvailable() {
        return TradeCycleEligibility.isAvailable(villager, offers(), closed);
    }

    private MerchantOffers offers() {
        return priceOverlay.offers();
    }

    private void sendCurrentCycleState() {
        if (player.containerMenu instanceof MerchantMenu menu) {
            sendCycleState(menu.containerId);
        }
    }

    private void sendCycleState(int containerId) {
        ModNetwork.sendToPlayer(
                player,
                new EasyVillagersCycleStatePayload(containerId, isCycleTradesAvailable())
        );
    }

    private boolean ownsActiveMerchantMenu() {
        if (!(player.containerMenu instanceof MerchantMenu menu)
                || !((Object) menu instanceof MerchantMenuTradeAccess access)) {
            return false;
        }
        return access.mcaVampCompat$getTrader() == villager;
    }

    private static McaFactionTradePriceOverlay.TradeKind tradeKindFor(VillagerEntityMCA villager) {
        if (McaHunterTradeService.isHunterTradeVillager(villager)) {
            return McaFactionTradePriceOverlay.TradeKind.HUNTER;
        }
        if (McaWerewolfTradeService.isWerewolfTradeVillager(villager)) {
            return McaFactionTradePriceOverlay.TradeKind.WEREWOLF;
        }
        return McaFactionTradePriceOverlay.TradeKind.VAMPIRE;
    }

}
