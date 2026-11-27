package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.inheritance.McaVampireInheritanceHandler;
import com.guilh.mca_vampirism_compat.advancement.McaInheritanceAdvancementService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.ai.Pregnancy;
import net.conczin.mca.entity.ai.relationship.Gender;
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
            method = "createChild(Lnet/conczin/mca/entity/ai/relationship/Gender;Lnet/conczin/mca/entity/VillagerEntityMCA;)Lnet/conczin/mca/entity/VillagerEntityMCA;",
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
