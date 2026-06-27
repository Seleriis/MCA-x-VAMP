package com.guilh.mca_vampirism_compat.easyvillagers;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

public final class McaEvCompatBridge {
    private static final String[] MCA_EV_COMPAT_MOD_IDS = {"mca_ev_compat", "mcaevcompat"};
    private static final String COMPAT_LOGIC_CLASS = "com.guilh.mcaevcompat.CompatLogic";
    private static boolean delegationLogged;
    private static boolean compatLogicClassResolved;
    private static Class<?> compatLogicClass;
    private static boolean storedTypeMethodResolved;
    private static Method storedTypeMethod;

    private McaEvCompatBridge() {
    }

    public static boolean isMcaEvCompatLoaded() {
        return isKnownModIdLoaded() || isClassPresent(COMPAT_LOGIC_CLASS);
    }

    public static String mcaEvCompatVersion() {
        for (String modId : MCA_EV_COMPAT_MOD_IDS) {
            String version = ModList.get()
                    .getModContainerById(modId)
                    .map(container -> container.getModInfo().getVersion().toString())
                    .orElse(null);
            if (version != null) {
                return version;
            }
        }
        return isClassPresent(COMPAT_LOGIC_CLASS) ? "unknown-classpath" : "not-loaded";
    }

    public static boolean supportsFactionPickupRules() {
        return false;
    }

    public static boolean supportsStoredVillagerFactionData() {
        return false;
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
            Object result = method.invoke(null, stack);
            return result instanceof String value ? value : "";
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            return "";
        }
    }

    public static boolean supportsTradeHook() {
        return false;
    }

    public static boolean canApplyFactionRulesToStoredVillager() {
        return McaVampirismCompatConfig.enableEasyVillagersFactionRules()
                && McaVampirismCompatConfig.readStoredVillagerFactionDataOnly();
    }

    public static boolean isDelegatedPickupHandledByMcaEvCompat() {
        return McaVampirismCompatConfig.delegateBaseEasyVillagersCompatToMcaEvCompat()
                && isMcaEvCompatLoaded();
    }

    public static boolean useLegacyPickupHooks() {
        return McaVampirismCompatConfig.useLegacyEasyVillagersPickupHooks()
                && supportsFactionPickupRules();
    }

    public static boolean useLegacyGuiHooks() {
        return McaVampirismCompatConfig.useLegacyEasyVillagersGuiHooks();
    }

    public static void logDelegationStatusOnce() {
        if (delegationLogged) {
            return;
        }
        delegationLogged = true;
        McaVampirismCompat.debugLog(
                "[EASY-VILLAGERS] mca-ev-compat handles base MCA/EV compatibility; mca-vamp-compat only applies faction rules loaded={} version={} delegatedPickup={} storedReadOnly={} tradeHookSupported={}",
                isMcaEvCompatLoaded(),
                mcaEvCompatVersion(),
                isDelegatedPickupHandledByMcaEvCompat(),
                McaVampirismCompatConfig.readStoredVillagerFactionDataOnly(),
                supportsTradeHook()
        );
        McaVampirismCompat.debugLog(
                "[PICKUP] config enabled={} blockCuring={} allowInfected={} showMessage={}",
                McaVampirismCompatConfig.enableEasyVillagersFactionPickupRules(),
                McaVampirismCompatConfig.easyVillagersBlockPickupOfCuringVillagers(),
                McaVampirismCompatConfig.allowPickupOfInfectedHumanVillagers(),
                McaVampirismCompatConfig.showPickupBlockedMessage()
        );
        if (!useLegacyPickupHooks()) {
            McaVampirismCompat.debugLog("[EASY-VILLAGERS] legacy pickup hooks disabled");
        }
        if (!useLegacyGuiHooks()) {
            McaVampirismCompat.debugLog("[EASY-VILLAGERS] legacy GUI hooks disabled");
        }
        if (isDelegatedPickupHandledByMcaEvCompat()) {
            McaVampirismCompat.debugLog("[EASY-VILLAGERS] base MCA/EV handling delegated to mca-ev-compat");
            McaVampirismCompat.debugLog("[EASY-VILLAGERS] base pickup delegated to mca-ev-compat");
        }
    }

    private static boolean isKnownModIdLoaded() {
        for (String modId : MCA_EV_COMPAT_MOD_IDS) {
            if (ModList.get().isLoaded(modId)) {
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
