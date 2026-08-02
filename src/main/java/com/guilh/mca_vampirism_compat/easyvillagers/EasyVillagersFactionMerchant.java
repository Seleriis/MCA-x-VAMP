package com.guilh.mca_vampirism_compat.easyvillagers;

import com.guilh.mca_vampirism_compat.service.McaTradeContextService;
import com.guilh.mca_vampirism_compat.trade.EntityBackedMerchant;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.OptionalInt;

public final class EasyVillagersFactionMerchant implements Merchant, EntityBackedMerchant {
    private final EasyVillagersFactionMerchantSession session;
    private Player tradingPlayer;

    public EasyVillagersFactionMerchant(EasyVillagersFactionMerchantSession session) {
        this.session = session;
    }

    public void open() {
        ServerPlayer player = session.player();
        setTradingPlayer(player);
        OptionalInt containerId = player.openMenu(new SimpleMenuProvider(
                (id, inventory, menuPlayer) -> createMenu(id, inventory),
                session.storedVillager().getDisplayName()
        ));
        if (containerId.isPresent()) {
            MerchantOffers offers = getOffers();
            player.sendMerchantOffers(
                    containerId.getAsInt(),
                    offers,
                    getVillagerLevel(),
                    getVillagerXp(),
                    showProgressBar(),
                    canRestock()
            );
            session.sendCycleState(containerId.getAsInt());
        } else {
            setTradingPlayer(null);
        }
    }

    @Override
    public void setTradingPlayer(Player player) {
        this.tradingPlayer = player;
        session.storedVillager().setTradingPlayer(player);
        if (player == null) {
            McaTradeContextService.clearEasyVillagersBlockTradeContext(session.player(), session.blockPos());
            session.close("menu closed");
        }
    }

    @Override
    public Player getTradingPlayer() {
        return tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        return session.adjustedOffers();
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        // The Easy Villagers wrapper owns copied offers for this menu only.
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        session.completeTrade(offer);
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
        session.storedVillager().notifyTradeUpdated(stack);
    }

    @Override
    public int getVillagerXp() {
        return session.storedVillager().getVillagerXp();
    }

    @Override
    public void overrideXp(int xp) {
        session.overrideXp(xp);
    }

    @Override
    public boolean showProgressBar() {
        return session.storedVillager().showProgressBar();
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return session.storedVillager().getNotifyTradeSound();
    }

    @Override
    public boolean canRestock() {
        return session.storedVillager().canRestock();
    }

    @Override
    public boolean isClientSide() {
        return session.storedVillager().level().isClientSide;
    }

    @Override
    public Villager mcaVampCompat$getTradeSoundEntity() {
        return session.storedVillager();
    }

    private MerchantMenu createMenu(int containerId, Inventory inventory) {
        return new MerchantMenu(containerId, inventory, this) {
            @Override
            public boolean stillValid(Player player) {
                return !session.isClosed()
                        && tradingPlayer == player
                        && !session.blockEntity().isRemoved();
            }

            @Override
            public void removed(Player player) {
                super.removed(player);
            }
        };
    }

    private int getVillagerLevel() {
        try {
            return session.storedVillager().getVillagerData().getLevel();
        } catch (RuntimeException exception) {
            return 1;
        }
    }
}
