package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import de.teamlapen.vampirism.api.VampirismCapabilities;
import de.teamlapen.vampirism.api.entity.vampire.IVampire;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class VampireSkillIntegrationService {
    public static final ResourceLocation DISGUISE_VAMPIRE_ACTION_ID =
            new ResourceLocation("vampirism", "disguise_vampire");
    public static final ResourceLocation INFECT_ACTION_ID =
            new ResourceLocation("vampirism", "infect");

    private VampireSkillIntegrationService() {
    }

    public static boolean isVampireDisguised(Player player) {
        if (player == null) {
            return false;
        }
        try {
            return player.getCapability(VampirismCapabilities.VAMPIRE_PLAYER)
                    .map(vampire -> vampire.getLevel() > 0 && vampire.isDisguised())
                    .orElse(false);
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[DISGUISE] detection failed safely player={} error={}",
                    player.getName().getString(),
                    exception.toString()
            );
            return false;
        }
    }

    public static LivingEntity representingEntity(IVampire vampire) {
        try {
            Entity entity = vampire == null ? null : vampire.getRepresentingEntity();
            return entity instanceof LivingEntity livingEntity ? livingEntity : null;
        } catch (RuntimeException | LinkageError exception) {
            McaVampirismCompat.debugLog(
                    "[BITE] vampire source lookup failed safely error={}",
                    exception.toString()
            );
            return null;
        }
    }
}
