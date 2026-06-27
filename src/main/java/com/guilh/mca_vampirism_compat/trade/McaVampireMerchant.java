package com.guilh.mca_vampirism_compat.trade;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.service.McaTradeContextService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.OptionalInt;

public final class McaVampireMerchant implements Merchant {
    private final McaVampireMerchantSession session;
    private Player tradingPlayer;

    public McaVampireMerchant(McaVampireMerchantSession session) {
        this.session = session;
    }

    public void open() {
        ServerPlayer player = session.player();
        setTradingPlayer(player);
        OptionalInt containerId = player.openMenu(new SimpleMenuProvider(
                (id, inventory, menuPlayer) -> createMenu(id, inventory),
                session.originalVillager().getDisplayName()
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
        } else {
            McaVampirismCompat.debugLog(
                    "[TRADE-WRAPPER] openMenu failed session={} player={} villager={}",
                    session.sessionId(),
                    player.getName().getString(),
                    session.originalVillager().getUUID()
            );
            session.close("openMenu failed");
        }
    }

    @Override
    public void setTradingPlayer(Player player) {
        this.tradingPlayer = player;
        session.originalVillager().setTradingPlayer(player);
        if (player == null) {
            McaTradeContextService.clearMcaTradeContext(session.player(), session.originalVillager());
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
        // The wrapper owns its copied offers for this session; real villager offers are never replaced here.
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        session.completeTrade(offer);
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
        session.originalVillager().notifyTradeUpdated(stack);
    }

    @Override
    public int getVillagerXp() {
        return session.originalVillager().getVillagerXp();
    }

    @Override
    public void overrideXp(int xp) {
        session.originalVillager().overrideXp(xp);
    }

    @Override
    public boolean showProgressBar() {
        return session.originalVillager().showProgressBar();
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return session.originalVillager().getNotifyTradeSound();
    }

    @Override
    public boolean canRestock() {
        return session.originalVillager().canRestock();
    }

    @Override
    public boolean isClientSide() {
        return session.originalVillager().level().isClientSide;
    }

    private int getVillagerLevel() {
        try {
            return session.originalVillager().getVillagerData().getLevel();
        } catch (RuntimeException exception) {
            McaVampirismCompat.debugLog(
                    "[TRADE-WRAPPER] failed to read MCA villager level session={} error={}",
                    session.sessionId(),
                    exception.toString()
            );
            return 1;
        }
    }

    private MerchantMenu createMenu(int containerId, Inventory inventory) {
        return new MerchantMenu(containerId, inventory, this) {
            @Override
            public boolean stillValid(Player player) {
                boolean samePlayer = McaVampireMerchant.this.tradingPlayer == player;
                boolean villagerAlive = session.originalVillager().isAlive();
                return !session.isClosed() && samePlayer && villagerAlive;
            }

            @Override
            public void removed(Player player) {
                super.removed(player);
            }
        };
    }
}
