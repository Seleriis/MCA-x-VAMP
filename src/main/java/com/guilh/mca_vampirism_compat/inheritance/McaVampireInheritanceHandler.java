package com.guilh.mca_vampirism_compat.inheritance;

import com.guilh.mca_vampirism_compat.capability.ModCapabilities;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.trait.McaTraitBridge;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;

public final class McaVampireInheritanceHandler {
    private McaVampireInheritanceHandler() {
    }

    public static void applyInheritance(AgeableMob child, Entity parentA, Entity parentB) {
        if (!McaVillagerUtil.isMcaVillager(child)) {
            return;
        }
        if (!shouldInheritVampirism(child, parentA, parentB)) {
            return;
        }

        child.getCapability(ModCapabilities.VAMPIRIC_VILLAGER).ifPresent(state -> {
            state.markInheritedVampire();
            if (child instanceof VillagerEntityMCA villager) {
                McaTraitBridge.ensureVampireState(villager);
            }
        });
    }

    public static boolean shouldInheritVampirism(AgeableMob child, Entity parentA, Entity parentB) {
        double chance = getInheritanceChance(parentA, parentB);
        return chance >= 1.0D || (chance > 0.0D && child.getRandom().nextDouble() < chance);
    }

    public static double getInheritanceChance(Entity parentA, Entity parentB) {
        boolean parentAVampire = isInheritanceVampire(parentA);
        boolean parentBVampire = isInheritanceVampire(parentB);
        if (parentAVampire && parentBVampire) {
            return McaVampirismCompatConfig.vampireTraitControlsInheritance()
                    ? McaVampirismCompatConfig.twoVampireParentsTraitInheritanceChance()
                    : McaVampirismCompatConfig.twoVampireParentsInheritanceChance();
        }
        if (parentAVampire || parentBVampire) {
            return McaVampirismCompatConfig.vampireTraitControlsInheritance()
                    ? McaVampirismCompatConfig.oneVampireParentTraitInheritanceChance()
                    : McaVampirismCompatConfig.oneVampireParentInheritanceChance();
        }
        return 0.0D;
    }

    private static boolean isInheritanceVampire(Entity entity) {
        if (McaVampirismCompatConfig.vampireTraitControlsInheritance()) {
            return McaTraitBridge.isVampireByTraitOrCapability(entity);
        }
        return entity != null && entity.getCapability(ModCapabilities.VAMPIRIC_VILLAGER)
                .map(state -> state.isConverted())
                .orElse(false);
    }
}
