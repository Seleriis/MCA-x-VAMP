package com.guilh.mca_vampirism_compat.easyvillagers;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.easyvillagers.readonly.StoredMcaVillagerInfo;
import com.guilh.mca_vampirism_compat.faction.PlayerFaction;
import com.guilh.mca_vampirism_compat.trade.McaVampireTradeOfferMapper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
    private final MerchantOffers originalOffers;
    private final MerchantOffers adjustedOffers;
    private final Map<Integer, Integer> adjustedIndexToOriginalIndex;
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
        int adjustedIndex = adjustedOffers.indexOf(adjustedOffer);
        Integer originalIndex = adjustedIndexToOriginalIndex.get(adjustedIndex);
        if (originalIndex == null || originalIndex < 0 || originalIndex >= originalOffers.size()) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] trade completed without original mapping adjustedIndex={} session={}",
                    adjustedIndex,
                    sessionId
            );
            adjustedOffer.increaseUses();
            return;
        }

        MerchantOffer originalOffer = originalOffers.get(originalIndex);
        storedVillager.notifyTrade(originalOffer);
        adjustedOffer.increaseUses();
        refreshStoredVillagerItem();
        markBlockEntityChanged();
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
        String key = type.getName() + "#" + name;
        Method cached = REFLECTED_METHODS.get(key);
        if (cached != null) {
            return cached;
        }
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                Method method = cursor.getDeclaredMethod(name);
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
