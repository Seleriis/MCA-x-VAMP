package com.guilh.mca_vampirism_compat.easyvillagers;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

public final class McaEvCompatBridge {
    private static final String[] MCA_EV_COMPAT_MOD_IDS = {"mca_ev_compat", "mcaevcompat"};
    private static final String EASY_VILLAGERS_MOD_ID = "easy_villagers";
    private static final String COMPAT_LOGIC_CLASS = "com.guilh.mcaevcompat.CompatLogic";
    private static final String TRADER_TILEENTITY_BASE =
            "de.maxhenkel.easyvillagers.blocks.tileentity.TraderTileentityBase";
    private static boolean delegationLogged;
    private static boolean tradeHookInvocationLogged;
    private static boolean compatLogicClassResolved;
    private static Class<?> compatLogicClass;
    private static boolean copyStoredDataMethodResolved;
    private static Method copyStoredDataMethod;
    private static boolean copyStoredDataFailureLogged;
    private static boolean storedTypeMethodResolved;
    private static Method storedTypeMethod;

    private McaEvCompatBridge() {
    }

    public static boolean isMcaEvCompatLoaded() {
        ModList modList = modList();
        return modList == null
                ? isClassPresent(COMPAT_LOGIC_CLASS)
                : isKnownModIdLoaded(modList);
    }

    public static String mcaEvCompatVersion() {
        ModList modList = modList();
        if (modList == null) {
            return isClassPresent(COMPAT_LOGIC_CLASS) ? "unknown-classpath" : "not-loaded";
        }
        for (String modId : MCA_EV_COMPAT_MOD_IDS) {
            String version = modList
                    .getModContainerById(modId)
                    .map(container -> container.getModInfo().getVersion().toString())
                    .orElse(null);
            if (version != null) {
                return version;
            }
        }
        return "not-loaded";
    }

    public static String getStoredMcaEntityTypeIdReadOnly(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !isMcaEvCompatLoaded()) {
            return "";
        }
        try {
            Method method = storedTypeMethod();
            if (method == null) {
                return "";
            }
            Object result = method.invoke(null, stack.copy());
            return result instanceof String value ? value : "";
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            return "";
        }
    }

    /**
     * Obtains the complete MCA data copy exposed by mca-ev-compat without ever
     * passing its owner stack to code that could mutate it. The extra copies
     * also keep callers from modifying the tag returned by the optional mod.
     */
    public static CompoundTag copyStoredMcaVillagerDataReadOnly(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !isMcaEvCompatLoaded()) {
            return null;
        }
        try {
            Method method = copyStoredDataMethod();
            if (method == null) {
                return null;
            }
            Object result = method.invoke(null, stack.copy());
            return result instanceof CompoundTag tag ? tag.copy() : null;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            logCopyStoredDataFailureOnce(exception);
            return null;
        }
    }

    public static boolean supportsTradeHook() {
        if (!isEasyVillagersLoaded()) {
            return false;
        }
        Class<?> traderClass = loadClass(TRADER_TILEENTITY_BASE);
        if (traderClass == null) {
            return false;
        }
        try {
            Method method = traderClass.getMethod("openTradingGUI", Player.class);
            return method.getReturnType() == boolean.class;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            return false;
        }
    }

    public static boolean isEasyVillagersTradeCyclingEnabled() {
        // Easy Villagers puts its SERVER_CONFIG next to client KeyMapping
        // fields in Main. Loading that class on a dedicated server is invalid.
        // The client only sends MessageCycleTrades while its own option is on,
        // so server-side compat must use the C2S packet as the positive gate.
        return isEasyVillagersLoaded();
    }

    /**
     * A received Cycle Trades packet is the authoritative positive signal.
     * Do not reflect Easy Villagers' Main/SERVER_CONFIG here: that class has
     * client-only fields and must never be loaded by common server code.
     */
    public static boolean canRecoverEasyVillagersCycleTrades() {
        return isEasyVillagersLoaded();
    }

    public static void logTradeHookInvocationOnce(Object trader) {
        if (tradeHookInvocationLogged) {
            return;
        }
        tradeHookInvocationLogged = true;
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS] direct Trader hook applied target={} descriptor=openTradingGUI(Player)Z mcaEvCompatVersion={}",
                trader == null ? TRADER_TILEENTITY_BASE : trader.getClass().getName(),
                mcaEvCompatVersion()
        );
    }

    public static boolean canApplyFactionRulesToStoredVillager() {
        return McaVampirismCompatConfig.enableEasyVillagersFactionRules()
                && isMcaEvCompatLoaded();
    }

    public static boolean isDelegatedPickupHandledByMcaEvCompat() {
        return isMcaEvCompatLoaded();
    }

    public static void logDelegationStatusOnce() {
        if (delegationLogged) {
            return;
        }
        delegationLogged = true;
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS] integration loaded={} mcaEvCompatVersion={} baseDelegated={} factionRules={} tradeHookSupported={} tradeCycling={}",
                isMcaEvCompatLoaded(),
                mcaEvCompatVersion(),
                isDelegatedPickupHandledByMcaEvCompat(),
                McaVampirismCompatConfig.enableEasyVillagersFactionRules(),
                supportsTradeHook(),
                isEasyVillagersTradeCyclingEnabled()
        );
        McaVampirismCompat.debugLog(
                "[PICKUP] config enabled={} blockCuring={} allowInfected={} showMessage={}",
                McaVampirismCompatConfig.enableEasyVillagersFactionPickupRules(),
                McaVampirismCompatConfig.easyVillagersBlockPickupOfCuringVillagers(),
                McaVampirismCompatConfig.allowPickupOfInfectedHumanVillagers(),
                McaVampirismCompatConfig.showPickupBlockedMessage()
        );
        if (isDelegatedPickupHandledByMcaEvCompat()) {
            McaVampirismCompat.debugLog(
                    "[EASY-VILLAGERS] mca-ev-compat owns capture/restore; mca-vamp-compat owns faction policy and its versioned payload"
            );
        }
    }

    private static boolean isKnownModIdLoaded(ModList modList) {
        for (String modId : MCA_EV_COMPAT_MOD_IDS) {
            if (modList.isLoaded(modId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEasyVillagersLoaded() {
        ModList modList = modList();
        return modList == null
                ? isClassPresent(TRADER_TILEENTITY_BASE)
                : modList.isLoaded(EASY_VILLAGERS_MOD_ID);
    }

    private static ModList modList() {
        try {
            return ModList.get();
        } catch (RuntimeException | LinkageError ignored) {
            // Unit tests and dedicated-server early bootstrap may not have a
            // Forge ModList yet. The classpath probe remains a safe fallback.
            return null;
        }
    }

    private static boolean isClassPresent(String className) {
        return COMPAT_LOGIC_CLASS.equals(className) ? compatLogicClass() != null : loadClass(className) != null;
    }

    private static Method storedTypeMethod() {
        if (storedTypeMethodResolved) {
            return storedTypeMethod;
        }
        storedTypeMethodResolved = true;
        Class<?> compatLogic = compatLogicClass();
        if (compatLogic == null) {
            return null;
        }
        try {
            storedTypeMethod = compatLogic.getMethod("getStoredMcaEntityTypeId", ItemStack.class);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            storedTypeMethod = null;
        }
        return storedTypeMethod;
    }

    private static Method copyStoredDataMethod() {
        if (copyStoredDataMethodResolved) {
            return copyStoredDataMethod;
        }
        copyStoredDataMethodResolved = true;
        Class<?> compatLogic = compatLogicClass();
        if (compatLogic == null) {
            return null;
        }
        try {
            copyStoredDataMethod = compatLogic.getMethod("copyStoredMcaVillagerData", ItemStack.class);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            copyStoredDataMethod = null;
        }
        return copyStoredDataMethod;
    }

    private static void logCopyStoredDataFailureOnce(Throwable exception) {
        if (copyStoredDataFailureLogged) {
            return;
        }
        copyStoredDataFailureLogged = true;
        Throwable cause = exception.getCause();
        McaVampirismCompat.LOGGER.debug(
                "[EASY-VILLAGERS] copyStoredMcaVillagerData unavailable once error={} cause={}",
                exception.toString(),
                cause == null ? "none" : cause.toString()
        );
    }

    private static Class<?> compatLogicClass() {
        if (compatLogicClassResolved) {
            return compatLogicClass;
        }
        compatLogicClassResolved = true;
        compatLogicClass = loadClass(COMPAT_LOGIC_CLASS);
        return compatLogicClass;
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className, false, classLoader());
        } catch (ClassNotFoundException | LinkageError exception) {
            return null;
        }
    }

    private static ClassLoader classLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader == null ? McaEvCompatBridge.class.getClassLoader() : classLoader;
    }

}
