package com.guilh.mca_vampirism_compat.easyvillagers.client;

public final class EasyVillagersCycleClientState {
    private static int containerId = -1;
    private static boolean enabled;
    private static int revision;

    private EasyVillagersCycleClientState() {
    }

    public static void update(int newContainerId, boolean newEnabled) {
        containerId = newContainerId;
        enabled = newEnabled;
        revision++;
    }

    public static boolean isEnabledFor(int currentContainerId) {
        return enabled && containerId == currentContainerId;
    }

    public static boolean hasStateFor(int currentContainerId) {
        return containerId == currentContainerId;
    }

    public static int revisionFor(int currentContainerId) {
        return hasStateFor(currentContainerId) ? revision : -1;
    }

    public static void disable(int currentContainerId) {
        if (hasStateFor(currentContainerId) && enabled) {
            enabled = false;
            revision++;
        }
    }

    public static void clear() {
        containerId = -1;
        enabled = false;
        revision++;
    }
}
