package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.advancement.McaAdvancementService;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.network.EasyVillagersCycleStatePayload;
import com.guilh.mca_vampirism_compat.service.McaHunterTradeService;
import com.guilh.mca_vampirism_compat.service.McaTradeContextService;
import com.guilh.mca_vampirism_compat.service.McaVampireStateService;
import com.guilh.mca_vampirism_compat.service.McaWerewolfTradeService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.network.PacketDistributor;

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
        closeForPlayer(player.getUUID(), "new MCA trade session");
        closeForVillager(villager.getUUID(), "new MCA trade session");

        McaFactionTradeSession session = new McaFactionTradeSession(player, villager, faction, tradeKindFor(villager));
        SESSIONS_BY_PLAYER.put(player.getUUID(), session);
        SESSIONS_BY_VILLAGER.put(villager.getUUID(), session);
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
        McaFactionTradeSession session = SESSIONS_BY_PLAYER.get(player.getUUID());
        if (session == null || session.closed) {
            return false;
        }
        session.cycleTrades();
        return true;
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
        if (!(player.containerMenu instanceof MerchantMenu menu)
                || !isCycleTradesAvailable()
                || TradeCycleEligibility.hasActiveOffer(menu)) {
            return;
        }

        priceOverlay.restore();
        boolean vampireTrade = McaVampireStateService.isVampire(villager);
        if (vampireTrade) {
            McaVampireSpecialTradeService.resetForOfferReroll(villager);
        }
        villager.setOffers(null);
        McaOfferPreparation.prepare(villager, player);
        if (vampireTrade) {
            McaVampireSpecialTradeService.ensureForVampire(villager);
        }
        priceOverlay = McaFactionTradePriceOverlay.create(villager.getOffers(), faction, tradeKind);
        priceOverlay.apply();

        menu.setSelectionHint(0);
        menu.slotsChanged(player.getInventory());
        sendCycleState(menu.containerId);
        player.sendMerchantOffers(
                menu.containerId,
                offers(),
                villager.getVillagerData().getLevel(),
                villager.getVillagerXp(),
                villager.showProgressBar(),
                villager.canRestock()
        );
        McaVampirismCompat.debugLog(
                "[TRADE] native MCA trades cycled player={} villager={} offers={} faction={}",
                player.getName().getString(),
                villager.getUUID(),
                offers().size(),
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
        PacketDistributor.sendToPlayer(player, new EasyVillagersCycleStatePayload(containerId, isCycleTradesAvailable()));
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
