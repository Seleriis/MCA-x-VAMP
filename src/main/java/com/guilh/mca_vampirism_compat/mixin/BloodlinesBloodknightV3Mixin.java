package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaVampireFeedProtection;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Bloodlines 3.x moved the vampire-source check to Bloodknight itself. Keep
 * the MCA extension scoped to that entity-drinking branch only, so no other
 * Bloodlines combat, faction or targeting checks identify MCA villagers as
 * vampires.
 */
@Pseudo
@Mixin(targets = "com.thedrofdoctoring.bloodlines.capabilities.bloodlines.vamp.BloodlineBloodknight", remap = false)
public abstract class BloodlinesBloodknightV3Mixin {
    @WrapOperation(
            method = "onEntityBloodDrink(Lnet/minecraft/world/entity/LivingEntity;"
                    + "Lde/teamlapen/vampirism/entity/player/vampire/VampirePlayer;I"
                    + "Lde/teamlapen/vampirism/api/event/BloodDrinkEvent$PlayerDrinkBloodEvent;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/teamlapen/vampirism/util/Helper;isVampire(Lnet/minecraft/world/entity/Entity;)Z"
            ),
            require = 1,
            remap = false
    )
    private boolean mcaVampirismCompat$recognizeMcaVampireDuringBloodknightDrink(
            Entity entity,
            Operation<Boolean> original
    ) {
        LivingEntity target = entity instanceof LivingEntity livingEntity ? livingEntity : null;
        return McaVampireFeedProtection.isBloodlinesVampireFeedTarget(original.call(entity), target);
    }
}
