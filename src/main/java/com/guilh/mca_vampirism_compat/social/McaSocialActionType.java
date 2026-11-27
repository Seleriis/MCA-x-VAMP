package com.guilh.mca_vampirism_compat.social;

public enum McaSocialActionType {
    GENERIC_INTERACTION,
    CHAT,
    SMALL_TALK,
    JOKE,
    COMPLIMENT,
    GIFT,
    FLIRT,
    KISS,
    ROMANCE,
    PROPOSE,
    MARRIAGE,
    DIVORCE,
    UNKNOWN;

    public boolean isRomanticOrMarriage() {
        return this == FLIRT
                || this == KISS
                || this == ROMANCE
                || this == PROPOSE
                || this == MARRIAGE;
    }

    public boolean isMarriageLike() {
        return this == PROPOSE || this == MARRIAGE;
    }

    public boolean isPositiveSocialAction() {
        return this == JOKE
                || this == COMPLIMENT
                || this == GIFT
                || isRomanticOrMarriage();
    }

    public boolean isStrongPositiveAction() {
        return this == GIFT || isRomanticOrMarriage();
    }
}
