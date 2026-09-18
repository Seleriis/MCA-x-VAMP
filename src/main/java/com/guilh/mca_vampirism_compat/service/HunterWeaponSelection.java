package com.guilh.mca_vampirism_compat.service;

/**
 * Persistent weapon assignment for real MCA Hunter villagers. The selected
 * value is saved in the compat state so equipment refreshes never reroll it.
 */
public enum HunterWeaponSelection {
    UNASSIGNED(""),
    PITCHFORK("pitchfork"),
    STAKE("stake");

    private final String serializedName;

    HunterWeaponSelection(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public static HunterWeaponSelection fromSerializedName(String value) {
        for (HunterWeaponSelection selection : values()) {
            if (selection.serializedName.equals(value)) {
                return selection;
            }
        }
        return UNASSIGNED;
    }

    public static HunterWeaponSelection roll(double randomValue, double stakeChance) {
        double boundedChance = Math.max(0.0D, Math.min(1.0D, stakeChance));
        return randomValue < boundedChance ? STAKE : PITCHFORK;
    }
}
