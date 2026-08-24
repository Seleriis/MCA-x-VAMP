package com.guilh.mca_vampirism_compat.easyvillagers;

import com.guilh.mca_vampirism_compat.trade.McaFactionTradeSession;
import com.guilh.mca_vampirism_compat.trade.McaNativeTradeCycleSession;
import net.minecraft.server.level.ServerPlayer;

public final class EasyVillagersTradeCycleService {
    private EasyVillagersTradeCycleService() {
    }

    /**
     * Handles Easy Villagers' cycle message for our merchant wrappers. Native
     * Villager merchants return false and remain owned by Easy Villagers.
     */
    public static boolean handleCycleTrades(ServerPlayer player) {
        if (player == null || !McaEvCompatBridge.isEasyVillagersTradeCyclingEnabled()) {
            return false;
        }
        if (EasyVillagersFactionMerchantSession.handleCycleTrades(player)) {
            return true;
        }
        if (McaFactionTradeSession.handleCycleTrades(player)) {
            return true;
        }
        return McaNativeTradeCycleSession.handleCycleTrades(player);
    }
}
