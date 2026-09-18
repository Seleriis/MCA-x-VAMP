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
 * Extends Bloodlines' vampire-source check only inside the Bloodknight entity
 * drinking handler. This leaves MCA villagers out of every other faction path.
 */
@Pseudo
@Mixin(targets = "com.thedrofdoctoring.bloodlines.capabilities.bloodlines.BloodlineEventHandler", remap = false)
public abstract class BloodlinesBloodknightMixin {
    @WrapOperation(
            method = "lambda$onBloodDrink$2(Lde/teamlapen/vampirism/api/event/BloodDrinkEvent$PlayerDrinkBloodEvent;Lde/teamlapen/vampirism/entity/player/vampire/VampirePlayer;Lcom/thedrofdoctoring/bloodlines/capabilities/bloodlines/BloodlineManager;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lde/teamlapen/vampirism/util/Helper;isVampire(Lnet/minecraft/world/entity/Entity;)Z"
            ),
            require = 1,
            remap = false
    )
    private static boolean mcaVampirismCompat$recognizeMcaVampireDuringBloodknightDrink(
            Entity entity,
            Operation<Boolean> original
    ) {
        LivingEntity target = entity instanceof LivingEntity livingEntity ? livingEntity : null;
        return McaVampireFeedProtection.isBloodlinesVampireFeedTarget(original.call(entity), target);
    }
}
