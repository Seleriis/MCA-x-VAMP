package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class McaVampireMerchantSession {
    private static final Map<UUID, McaVampireMerchantSession> SESSIONS_BY_ID = new HashMap<>();
    private static final Map<UUID, McaVampireMerchantSession> SESSIONS_BY_PLAYER = new HashMap<>();

    private final UUID sessionId;
    private final ServerPlayer player;
    private final VillagerEntityMCA originalVillager;
    private final MerchantOffers originalOffers;
    private final MerchantOffers adjustedOffers;
    private final Map<Integer, Integer> adjustedIndexToOriginalIndex;
    private final PlayerFaction faction;
    private final boolean hunterTrade;
    private final boolean werewolfTrade;
    private boolean closed;

    private McaVampireMerchantSession(ServerPlayer player, VillagerEntityMCA originalVillager, PlayerFaction faction,
                                      boolean hunterTrade, boolean werewolfTrade) {
        this.sessionId = UUID.randomUUID();
        this.player = player;
        this.originalVillager = originalVillager;
        this.originalOffers = originalVillager.getOffers();
        this.faction = faction;
        this.hunterTrade = hunterTrade;
        this.werewolfTrade = werewolfTrade;

        McaVampireTradeOfferMapper.MappedOffers mappedOffers = hunterTrade
                ? McaVampireTradeOfferMapper.copyAndAdjustForHunter(originalOffers, faction)
                : werewolfTrade
                ? McaVampireTradeOfferMapper.copyAndAdjustForWerewolf(originalOffers, faction)
                : McaVampireTradeOfferMapper.copyAndAdjust(originalOffers, faction);
        this.adjustedOffers = mappedOffers.adjustedOffers();
        this.adjustedIndexToOriginalIndex = mappedOffers.adjustedIndexToOriginalIndex();
    }

    public static McaVampireMerchantSession create(ServerPlayer player, VillagerEntityMCA villager, PlayerFaction faction) {
        return create(player, villager, faction, false, false);
    }

    public static McaVampireMerchantSession createForHunter(ServerPlayer player, VillagerEntityMCA villager, PlayerFaction faction) {
        return create(player, villager, faction, true, false);
    }

    public static McaVampireMerchantSession createForWerewolf(ServerPlayer player, VillagerEntityMCA villager, PlayerFaction faction) {
        return create(player, villager, faction, false, true);
    }

    private static McaVampireMerchantSession create(ServerPlayer player, VillagerEntityMCA villager, PlayerFaction faction,
                                                   boolean hunterTrade, boolean werewolfTrade) {
        closeForPlayer(player.getUUID(), "new session");
        McaVampireMerchantSession session = new McaVampireMerchantSession(player, villager, faction, hunterTrade, werewolfTrade);
        SESSIONS_BY_ID.put(session.sessionId(), session);
        SESSIONS_BY_PLAYER.put(player.getUUID(), session);
        McaVampirismCompat.debugLog(
                "[TRADE-WRAPPER] creating session player={} villager={} faction={} hunterTrade={} werewolfTrade={} session={}",
                player.getName().getString(),
                villager.getUUID(),
                faction,
                hunterTrade,
                werewolfTrade,
                session.sessionId()
        );
        McaVampirismCompat.debugLog(
                "[TRADE-WRAPPER] copied offers count={}",
                session.adjustedOffers().size()
        );
        return session;
    }

    public static void closeForPlayer(UUID playerUuid, String reason) {
        McaVampireMerchantSession session = SESSIONS_BY_PLAYER.get(playerUuid);
        if (session != null) {
            session.close(reason);
        }
    }

    public static void closeForVillager(UUID villagerUuid, String reason) {
        List<McaVampireMerchantSession> sessions = new ArrayList<>(SESSIONS_BY_ID.values());
        for (McaVampireMerchantSession session : sessions) {
            if (session.originalVillager().getUUID().equals(villagerUuid)) {
                session.close(reason);
            }
        }
    }

    public void completeTrade(MerchantOffer adjustedOffer) {
        if (closed) {
            return;
        }
        int adjustedIndex = adjustedOffers.indexOf(adjustedOffer);
        Integer originalIndex = adjustedIndexToOriginalIndex.get(adjustedIndex);
        if (originalIndex == null || originalIndex < 0 || originalIndex >= originalOffers.size()) {
            McaVampirismCompat.debugLog(
                    "[TRADE-WRAPPER] trade completed without original mapping adjustedIndex={} session={}",
                    adjustedIndex,
                    sessionId
            );
            adjustedOffer.increaseUses();
            return;
        }

        MerchantOffer originalOffer = originalOffers.get(originalIndex);
        originalVillager.notifyTrade(originalOffer);
        adjustedOffer.increaseUses();
        McaVampirismCompat.debugLog(
                "[TRADE-WRAPPER] trade completed adjustedIndex={} originalIndex={} session={}",
                adjustedIndex,
                originalIndex,
                sessionId
        );
    }

    public void close(String reason) {
        if (closed) {
            return;
        }
        closed = true;
        SESSIONS_BY_ID.remove(sessionId);
        SESSIONS_BY_PLAYER.remove(player.getUUID(), this);
        McaVampirismCompat.debugLog(
                "[TRADE-WRAPPER] session closed session={} player={} villager={} reason={}",
                sessionId,
                player.getName().getString(),
                originalVillager.getUUID(),
                reason
        );
    }

    public boolean isClosed() {
        return closed;
    }

    public UUID sessionId() {
        return sessionId;
    }

    public ServerPlayer player() {
        return player;
    }

    public VillagerEntityMCA originalVillager() {
        return originalVillager;
    }

    public MerchantOffers adjustedOffers() {
        return adjustedOffers;
    }

    public PlayerFaction faction() {
        return faction;
    }

    public boolean hunterTrade() {
        return hunterTrade;
    }

    public boolean werewolfTrade() {
        return werewolfTrade;
    }
}
