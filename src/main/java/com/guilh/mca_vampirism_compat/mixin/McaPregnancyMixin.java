package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.inheritance.McaVampireInheritanceHandler;
import com.guilh.mca_vampirism_compat.advancement.McaInheritanceAdvancementService;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Pregnancy;
import forge.net.mca.entity.ai.relationship.Gender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Pregnancy.class, remap = false)
public abstract class McaPregnancyMixin {
    @Shadow
    @Final
    private VillagerEntityMCA mother;

    @Inject(
            method = "createChild(Lforge/net/mca/entity/ai/relationship/Gender;Lforge/net/mca/entity/VillagerEntityMCA;)Lforge/net/mca/entity/VillagerEntityMCA;",
            at = @At("RETURN"),
            remap = false
    )
    private void mcaVampirismCompat$inheritFaction(Gender gender, VillagerEntityMCA father,
                                                   CallbackInfoReturnable<VillagerEntityMCA> cir) {
        VillagerEntityMCA child = cir.getReturnValue();
        if (child != null && !child.level().isClientSide) {
            McaVampireInheritanceHandler.applyInheritance(child, mother, father);
            McaInheritanceAdvancementService.recordNaturalBirth(child);
        }
    }
}
