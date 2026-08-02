package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.network.EasyVillagersCycleStatePayload;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Method;
import java.util.Arrays;
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
    private MerchantOffers originalOffers;
    private MerchantOffers adjustedOffers;
    private Map<Integer, Integer> adjustedIndexToOriginalIndex;
    private final PlayerFaction faction;
    private final boolean hunterTrade;
    private final boolean werewolfTrade;
    private boolean closed;
    private static final Map<String, Method> REFLECTED_METHODS = new HashMap<>();

    private McaVampireMerchantSession(ServerPlayer player, VillagerEntityMCA originalVillager, PlayerFaction faction,
                                      boolean hunterTrade, boolean werewolfTrade) {
        this.sessionId = UUID.randomUUID();
        this.player = player;
        this.originalVillager = originalVillager;
        this.originalOffers = originalVillager.getOffers();
        this.faction = faction;
        this.hunterTrade = hunterTrade;
        this.werewolfTrade = werewolfTrade;

        rebuildAdjustedOffers();
    }

    private void rebuildAdjustedOffers() {
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

    public static boolean handleCycleTrades(ServerPlayer player) {
        McaVampireMerchantSession session = SESSIONS_BY_PLAYER.get(player.getUUID());
        if (session == null || session.isClosed()) {
            return false;
        }
        session.cycleTrades();
        return true;
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
        TradeCycleEligibility.lockAfterTrade(originalVillager);
        int adjustedIndex = adjustedOffers.indexOf(adjustedOffer);
        Integer originalIndex = adjustedIndexToOriginalIndex.get(adjustedIndex);
        if (originalIndex == null || originalIndex < 0 || originalIndex >= originalOffers.size()) {
            McaVampirismCompat.debugLog(
                    "[TRADE-WRAPPER] trade completed without original mapping adjustedIndex={} session={}",
                    adjustedIndex,
                    sessionId
            );
            adjustedOffer.increaseUses();
            sendCurrentCycleState();
            return;
        }

        MerchantOffer originalOffer = originalOffers.get(originalIndex);
        originalVillager.notifyTrade(originalOffer);
        adjustedOffer.increaseUses();
        sendCurrentCycleState();
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

    public boolean isCycleTradesAvailable() {
        return TradeCycleEligibility.isAvailable(originalVillager, originalOffers, closed);
    }

    public void sendCycleState(int containerId) {
        PacketDistributor.sendToPlayer(
                player,
                new EasyVillagersCycleStatePayload(
                        containerId,
                        isCycleTradesAvailable()
                )
        );
    }

    private void cycleTrades() {
        if (!(player.containerMenu instanceof MerchantMenu menu)
                || !isCycleTradesAvailable()
                || TradeCycleEligibility.hasActiveOffer(menu)) {
            return;
        }

        menu.setSelectionHint(0);
        originalVillager.setOffers(null);
        originalOffers = originalVillager.getOffers();
        applyNativePlayerPrices();
        rebuildAdjustedOffers();
        menu.setSelectionHint(0);
        menu.slotsChanged(player.getInventory());

        sendCycleState(menu.containerId);
        player.sendMerchantOffers(
                menu.containerId,
                adjustedOffers,
                originalVillager.getVillagerData().getLevel(),
                originalVillager.getVillagerXp(),
                originalVillager.showProgressBar(),
                originalVillager.canRestock()
        );
        McaVampirismCompat.debugLog(
                "[TRADE-WRAPPER] trades cycled player={} villager={} offers={} faction={}",
                player.getName().getString(),
                originalVillager.getUUID(),
                adjustedOffers.size(),
                faction
        );
    }

    private void sendCurrentCycleState() {
        if (player.containerMenu instanceof MerchantMenu menu) {
            sendCycleState(menu.containerId);
        }
    }

    private void applyNativePlayerPrices() {
        try {
            Method updateSpecialPrices = findMethod(
                    originalVillager.getClass(),
                    "updateSpecialPrices",
                    Player.class
            );
            updateSpecialPrices.invoke(originalVillager, player);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            McaVampirismCompat.LOGGER.warn(
                    "[MCA Reborn x Vampirism Compat] Could not apply native MCA reputation prices while cycling trades for {}",
                    originalVillager.getUUID(),
                    exception
            );
        }
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        String key = type.getName() + "#" + name + Arrays.toString(parameterTypes);
        Method cached = REFLECTED_METHODS.get(key);
        if (cached != null) {
            return cached;
        }
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                Method method = cursor.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                REFLECTED_METHODS.put(key, method);
                return method;
            } catch (NoSuchMethodException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        throw new NoSuchMethodException(name);
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
