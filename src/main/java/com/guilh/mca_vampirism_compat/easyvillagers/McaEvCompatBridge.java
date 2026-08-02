package com.guilh.mca_vampirism_compat.easyvillagers;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

public final class McaEvCompatBridge {
    private static final String[] MCA_EV_COMPAT_MOD_IDS = {"mca_ev_compat", "mcaevcompat"};
    private static final String COMPAT_LOGIC_CLASS = "com.guilh.mcaevcompat.CompatLogic";
    private static final String TRADER_TILEENTITY_BASE =
            "de.maxhenkel.easyvillagers.blocks.tileentity.TraderTileentityBase";
    private static boolean delegationLogged;
    private static boolean tradeHookInvocationLogged;
    private static boolean compatLogicClassResolved;
    private static Class<?> compatLogicClass;
    private static boolean copyStoredDataMethodResolved;
    private static Method copyStoredDataMethod;
    private static boolean storedTypeMethodResolved;
    private static Method storedTypeMethod;
    private static boolean tradeCyclingConfigResolved;
    private static Object tradeCyclingConfigValue;

    private McaEvCompatBridge() {
    }

    public static boolean isMcaEvCompatLoaded() {
        return isKnownModIdLoaded() || isClassPresent(COMPAT_LOGIC_CLASS);
    }

    public static String mcaEvCompatVersion() {
        ModList modList = ModList.get();
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
        return isClassPresent(COMPAT_LOGIC_CLASS) ? "unknown-classpath" : "not-loaded";
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
            McaVampirismCompat.LOGGER.debug(
                    "[EASY-VILLAGERS] copyStoredMcaVillagerData unavailable error={}",
                    exception.toString()
            );
            return null;
        }
    }

    public static boolean supportsTradeHook() {
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
        Object configValue = tradeCyclingConfigValue();
        if (configValue == null) {
            return false;
        }
        try {
            Method get = configValue.getClass().getMethod("get");
            return Boolean.TRUE.equals(get.invoke(configValue));
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            return false;
        }
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
        return McaVampirismCompatConfig.enableEasyVillagersFactionRules();
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

    private static boolean isKnownModIdLoaded() {
        ModList modList = ModList.get();
        if (modList == null) {
            return false;
        }
        for (String modId : MCA_EV_COMPAT_MOD_IDS) {
            if (modList.isLoaded(modId)) {
                return true;
            }
        }
        return false;
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

    private static Object tradeCyclingConfigValue() {
        if (tradeCyclingConfigResolved) {
            return tradeCyclingConfigValue;
        }
        tradeCyclingConfigResolved = true;
        try {
            Class<?> mainClass = loadClass("de.maxhenkel.easyvillagers.Main");
            if (mainClass == null) {
                return null;
            }
            Object serverConfig = mainClass.getField("SERVER_CONFIG").get(null);
            if (serverConfig != null) {
                tradeCyclingConfigValue = serverConfig.getClass()
                        .getField("tradeCycling")
                        .get(serverConfig);
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            tradeCyclingConfigValue = null;
        }
        return tradeCyclingConfigValue;
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
