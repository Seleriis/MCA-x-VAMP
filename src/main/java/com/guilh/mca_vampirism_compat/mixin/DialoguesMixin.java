package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.social.McaFactionSocialService;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.resources.Dialogues;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Dialogues.class, remap = false)
public abstract class DialoguesMixin {
    @Inject(method = "selectAnswer", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$handleSocialDialogue(VillagerEntityMCA villager, ServerPlayer player,
                                                         String question, String answer, CallbackInfo ci) {
        if (McaFactionSocialService.handleDialogueAction(player, villager, question, answer)) {
            ci.cancel();
        }
    }
}
