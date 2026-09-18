package com.guilh.mca_vampirism_compat.service;

/**
 * Internal bridge exposed by {@code ExtendedCreatureMixin} for MCA entities
 * that Vampirism initialized without a registered blood value.
 */
public interface McaBloodValueMutable {
    void mcaVampirismCompat$setBloodValues(int bloodValue);
}
