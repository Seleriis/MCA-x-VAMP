package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.mission.OccultVillageMissionService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.resources.data.dialogue.Question;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Hides the mission choice until the server confirms that this resident can discuss a favor. */
@Mixin(value = Question.class, remap = false)
public abstract class McaDialogueQuestionMixin {
    @Inject(method = "getValidAnswers", at = @At("RETURN"), cancellable = true)
    private void mcaVampirismCompat$filterOccultMissionAnswer(ServerPlayer player, VillagerEntityMCA villager,
                                                               CallbackInfoReturnable<List<String>> cir) {
        Question question = (Question) (Object) this;
        cir.setReturnValue(OccultVillageMissionService.filterDialogueAnswers(
                question.getName(), cir.getReturnValue(), player, villager
        ));
    }
}
