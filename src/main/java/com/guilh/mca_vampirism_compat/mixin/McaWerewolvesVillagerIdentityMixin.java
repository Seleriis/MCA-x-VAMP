package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaNativeWerewolfProtectionService;
import de.teamlapen.werewolves.api.entities.werewolf.TransformType;
import de.teamlapen.werewolves.api.entities.werewolf.WerewolfTransformable;
import forge.net.mca.entity.VillagerEntityMCA;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * Overrides the Werewolves methods inherited from vanilla Villager only for
 * MCA villagers. The mixin is skipped completely when Werewolves is absent.
 */
@Pseudo
@Mixin(value = VillagerEntityMCA.class, remap = false)
public abstract class McaWerewolvesVillagerIdentityMixin {
    /**
     * The native mark lacks the context needed to create a compat werewolf.
     */
    @SuppressWarnings("unused")
    public void setWerewolfFaction(boolean marked) {
        McaNativeWerewolfProtectionService.ignoreNativeFactionMark(
                (VillagerEntityMCA) (Object) this,
                marked
        );
    }

    /**
     * Keeps native Werewolves from replacing the MCA entity with a beast.
     */
    @SuppressWarnings("unused")
    public WerewolfTransformable transformToWerewolf(TransformType transformType) {
        return (WerewolfTransformable) McaNativeWerewolfProtectionService.blockNativeTransformation(
                (VillagerEntityMCA) (Object) this,
                transformType
        );
    }
}
