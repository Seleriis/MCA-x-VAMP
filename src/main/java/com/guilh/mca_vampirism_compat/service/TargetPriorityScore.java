package com.guilh.mca_vampirism_compat.service;

/** Internal immutable score used to select the best already-valid combat target. */
record TargetPriorityScore(
        int categoryPriority,
        int factionPriority,
        double distanceSqr,
        Object reason,
        String targetType
) {
    boolean isPreferredOver(TargetPriorityScore other) {
        if (other == null) {
            return true;
        }
        if (categoryPriority != other.categoryPriority) {
            return categoryPriority < other.categoryPriority;
        }
        if (factionPriority != other.factionPriority) {
            return factionPriority < other.factionPriority;
        }
        return distanceSqr < other.distanceSqr;
    }

    boolean outranksIgnoringDistance(TargetPriorityScore other) {
        return categoryPriority < other.categoryPriority
                || (categoryPriority == other.categoryPriority && factionPriority < other.factionPriority);
    }

    int logScore() {
        int distanceScore = (int) Math.min(99, Math.floor(Math.sqrt(Math.max(0.0D, distanceSqr))));
        return categoryPriority * 1000 + factionPriority * 100 + distanceScore;
    }
}
