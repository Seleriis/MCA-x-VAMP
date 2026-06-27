package com.guilh.mca_vampirism_compat.ai;

import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.util.McaVillagerUtil;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;

public class McaVampireSeekShelterGoal extends FleeSunGoal {
    private final PathfinderMob vampire;

    public McaVampireSeekShelterGoal(PathfinderMob vampire, double speedModifier) {
        super(vampire, speedModifier);
        this.vampire = vampire;
    }

    @Override
    public boolean canUse() {
        return McaVampirismCompatConfig.vampireVillagersAvoidSun()
                && McaVillagerUtil.isMcaVampire(vampire)
                && vampire.level().isDay()
                && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return McaVampirismCompatConfig.vampireVillagersAvoidSun()
                && McaVillagerUtil.isMcaVampire(vampire)
                && vampire.level().isDay()
                && super.canContinueToUse();
    }
}
