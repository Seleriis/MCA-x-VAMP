package com.guilh.mca_vampirism_compat.easyvillagers;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.easyvillagers.readonly.StoredMcaVillagerInfo;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.network.EasyVillagersCycleStatePayload;
import com.guilh.mca_vampirism_compat.trade.McaVampireTradeOfferMapper;
import com.guilh.mca_vampirism_compat.trade.TradeCycleEligibility;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EasyVillagersFactionMerchantSession {
    private static final Map<UUID, EasyVillagersFactionMerchantSession> SESSIONS_BY_ID = new HashMap<>();
    private static final Map<UUID, EasyVillagersFactionMerchantSession> SESSIONS_BY_PLAYER = new HashMap<>();
    private static final Map<String, Method> REFLECTED_METHODS = new HashMap<>();

    private final UUID sessionId;
    private final ServerPlayer player;
    private final BlockEntity blockEntity;
    private final BlockPos blockPos;
    private final StoredMcaVillagerInfo storedVillagerInfo;
    private final Villager storedVillager;
    private MerchantOffers originalOffers;
    private MerchantOffers adjustedOffers;
    private Map<Integer, Integer> adjustedIndexToOriginalIndex;
    private final PlayerFaction faction;
    private final boolean hunterTrade;
    private final boolean werewolfTrade;
    private boolean closed;

    private EasyVillagersFactionMerchantSession(ServerPlayer player, StoredMcaVillagerInfo storedVillagerInfo,
                                               PlayerFaction faction, boolean hunterTrade, boolean werewolfTrade) {
        this.sessionId = UUID.randomUUID();
        this.player = player;
        this.blockEntity = storedVillagerInfo.blockEntity();
        this.blockPos = blockEntity.getBlockPos().immutable();
        this.storedVillagerInfo = storedVillagerInfo;
        this.storedVillager = storedVillagerInfo.villagerEntity();
        this.originalOffers = storedVillagerInfo.offers();
        this.faction = faction;
        this.hunterTrade = hunterTrade;
        this.werewolfTrade = werewolfTrade;

        applyNativePlayerPrices();
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

    public static EasyVillagersFactionMerchantSession create(ServerPlayer player, StoredMcaVillagerInfo info, PlayerFaction faction) {
        return create(player, info, faction, false, false);
    }

    public static EasyVillagersFactionMerchantSession createForHunter(ServerPlayer player, StoredMcaVillagerInfo info, PlayerFaction faction) {
        return create(player, info, faction, true, false);
    }

    public static EasyVillagersFactionMerchantSession createForWerewolf(ServerPlayer player, StoredMcaVillagerInfo info, PlayerFaction faction) {
        return create(player, info, faction, false, true);
    }

    private static EasyVillagersFactionMerchantSession create(ServerPlayer player, StoredMcaVillagerInfo info,
                                                             PlayerFaction faction, boolean hunterTrade, boolean werewolfTrade) {
        closeForPlayer(player.getUUID(), "new easy villagers session");
        EasyVillagersFactionMerchantSession session = new EasyVillagersFactionMerchantSession(player, info, faction, hunterTrade, werewolfTrade);
        SESSIONS_BY_ID.put(session.sessionId(), session);
        SESSIONS_BY_PLAYER.put(player.getUUID(), session);
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS] creating session player={} block={} pos={} faction={} hunterTrade={} werewolfTrade={} session={}",
                player.getName().getString(),
                session.blockEntity().getClass().getName(),
                session.blockPos(),
                faction,
                hunterTrade,
                werewolfTrade,
                session.sessionId()
        );
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS] copied offers count={}",
                session.adjustedOffers().size()
        );
        for (int i = 0; i < session.adjustedOffers().size(); i++) {
            MerchantOffer original = session.originalOffers().get(session.adjustedIndexToOriginalIndex().get(i));
            MerchantOffer adjusted = session.adjustedOffers().get(i);
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] price apply offer={} base={} adjusted={} faction={}",
                    i,
                    original.getBaseCostA().isEmpty() ? 0 : original.getBaseCostA().getCount(),
                    adjusted.getCostA().getCount(),
                    faction
            );
        }
        return session;
    }

    public static void closeForPlayer(UUID playerUuid, String reason) {
        EasyVillagersFactionMerchantSession session = SESSIONS_BY_PLAYER.get(playerUuid);
        if (session != null) {
            session.close(reason);
        }
    }

    public static boolean handleCycleTrades(ServerPlayer player) {
        EasyVillagersFactionMerchantSession session = SESSIONS_BY_PLAYER.get(player.getUUID());
        if (session == null || session.isClosed()) {
            return false;
        }
        session.cycleTrades();
        return true;
    }

    public static void closeForBlock(BlockPos blockPos, String reason) {
        List<EasyVillagersFactionMerchantSession> sessions = new ArrayList<>(SESSIONS_BY_ID.values());
        for (EasyVillagersFactionMerchantSession session : sessions) {
            if (session.blockPos().equals(blockPos)) {
                session.close(reason);
            }
        }
    }

    public void completeTrade(MerchantOffer adjustedOffer) {
        if (closed) {
            return;
        }
        TradeCycleEligibility.lockAfterTrade(storedVillager);
        int adjustedIndex = adjustedOffers.indexOf(adjustedOffer);
        Integer originalIndex = adjustedIndexToOriginalIndex.get(adjustedIndex);
        if (originalIndex == null || originalIndex < 0 || originalIndex >= originalOffers.size()) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] trade completed without original mapping adjustedIndex={} session={}",
                    adjustedIndex,
                    sessionId
            );
            adjustedOffer.increaseUses();
            persistStoredVillager("trade completed without offer mapping");
            sendCurrentCycleState();
            return;
        }

        MerchantOffer originalOffer = originalOffers.get(originalIndex);
        storedVillager.notifyTrade(originalOffer);
        adjustedOffer.increaseUses();
        persistStoredVillager("trade completed");
        sendCurrentCycleState();
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS] trade completed adjustedIndex={} originalIndex={} session={}",
                adjustedIndex,
                originalIndex,
                sessionId
        );
        McaVampirismCompat.debugLog("[EASY-VILLAGERS] original offer uses updated");
    }

    public void close(String reason) {
        if (closed) {
            return;
        }
        if (storedVillager.getTradingPlayer() == player) {
            storedVillager.setTradingPlayer(null);
        }
        persistStoredVillager("session closing: " + reason);
        closed = true;
        SESSIONS_BY_ID.remove(sessionId);
        SESSIONS_BY_PLAYER.remove(player.getUUID(), this);
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS] session closed session={} player={} pos={} reason={}",
                sessionId,
                player.getName().getString(),
                blockPos,
                reason
        );
    }

    public void overrideXp(int xp) {
        if (closed) {
            return;
        }
        storedVillager.overrideXp(xp);
        persistStoredVillager("xp updated");
    }

    public boolean isCycleTradesAvailable() {
        return TradeCycleEligibility.isAvailable(storedVillager, originalOffers, closed);
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
        storedVillager.setOffers(null);
        originalOffers = storedVillager.getOffers();
        applyNativePlayerPrices();
        rebuildAdjustedOffers();
        menu.setSelectionHint(0);
        menu.slotsChanged(player.getInventory());
        persistStoredVillager("trades cycled");

        // Reset the client selection before replacing the list. Otherwise a
        // previously selected index can point past the end of the new offers.
        sendCycleState(menu.containerId);
        player.sendMerchantOffers(
                menu.containerId,
                adjustedOffers,
                storedVillager.getVillagerData().getLevel(),
                storedVillager.getVillagerXp(),
                storedVillager.showProgressBar(),
                storedVillager.canRestock()
        );
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS] trades cycled player={} pos={} offers={} faction={}",
                player.getName().getString(),
                blockPos,
                adjustedOffers.size(),
                faction
        );
    }

    private void sendCurrentCycleState() {
        if (player.containerMenu instanceof MerchantMenu menu) {
            sendCycleState(menu.containerId);
        }
    }

    public void persistStoredVillager(String reason) {
        if (blockEntity.isRemoved()) {
            return;
        }
        refreshStoredVillagerItem();
        markBlockEntityChanged();
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS] stored trader persisted player={} pos={} reason={}",
                player.getName().getString(),
                blockPos,
                reason
        );
    }

    private void refreshStoredVillagerItem() {
        try {
            Method getVillager = findMethod(blockEntity.getClass(), "getVillager");
            getVillager.invoke(blockEntity);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] failed to refresh stored villager item pos={} error={}",
                    blockPos,
                    exception.toString()
            );
        }
    }

    /**
     * Mirrors the private vanilla/Easy Villagers preparation normally reached
     * through startTrading, without opening Easy Villagers' original menu.
     * This applies reputation and Hero of the Village before the faction
     * multiplier is calculated from the current effective price.
     */
    private void applyNativePlayerPrices() {
        try {
            Method updateSpecialPrices = findMethod(
                    storedVillager.getClass(),
                    "updateSpecialPrices",
                    Player.class
            );
            updateSpecialPrices.invoke(storedVillager, player);
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] native player prices applied before faction multiplier player={} pos={}",
                    player.getName().getString(),
                    blockPos
            );
        } catch (ReflectiveOperationException | RuntimeException exception) {
            McaVampirismCompat.LOGGER.warn(
                    "[MCA Reborn x Vampirism Compat] Could not apply native Trader reputation prices at {}",
                    blockPos,
                    exception
            );
        }
    }

    private void markBlockEntityChanged() {
        blockEntity.setChanged();
        try {
            Method sync = findMethod(blockEntity.getClass(), "sync");
            sync.invoke(blockEntity);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // setChanged is enough for persistence; sync only updates Easy Villagers client state when available.
        }
        McaVampirismCompat.debugLog("[EASY-VILLAGERS] block entity marked changed");
    }

    private static Method findMethod(Class<?> type, String name) throws NoSuchMethodException {
        return findMethod(type, name, new Class<?>[0]);
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

    public UUID sessionId() {
        return sessionId;
    }

    public ServerPlayer player() {
        return player;
    }

    public BlockEntity blockEntity() {
        return blockEntity;
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public StoredMcaVillagerInfo storedVillagerInfo() {
        return storedVillagerInfo;
    }

    public Villager storedVillager() {
        return storedVillager;
    }

    public MerchantOffers originalOffers() {
        return originalOffers;
    }

    public MerchantOffers adjustedOffers() {
        return adjustedOffers;
    }

    public Map<Integer, Integer> adjustedIndexToOriginalIndex() {
        return adjustedIndexToOriginalIndex;
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

    public boolean isClosed() {
        return closed;
    }
}
