package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.service.McaFactionCommandPermissionService;
import com.guilh.mca_vampirism_compat.service.McaFactionGiftService;
import com.guilh.mca_vampirism_compat.service.McaVampireCureService;
import com.guilh.mca_vampirism_compat.mission.OccultVillageMissionService;
import com.guilh.mca_vampirism_compat.social.McaFactionSocialService;
import com.guilh.mca_vampirism_compat.trade.McaFactionTradeService;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.interaction.VillagerCommandHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(value = VillagerCommandHandler.class, remap = false)
public abstract class VillagerCommandHandlerMixin {
    private static Field mcaVampirismCompat$entityField;
    private static boolean mcaVampirismCompat$entityFieldResolved;

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private void mcaVampirismCompat$handleMcaTradeCommand(ServerPlayer player, String command,
                                                          CallbackInfoReturnable<Boolean> cir) {
        Entity entity = mcaVampirismCompat$getEntity();
        if (!(entity instanceof VillagerEntityMCA villager)) {
            return;
        }
        if (McaFactionCommandPermissionService.denyCommandIfNeeded(player, villager, command)) {
            cir.setReturnValue(true);
            return;
        }
        if ("gift".equals(command)) {
            if (OccultVillageMissionService.handleGiftCommand(player, villager)
                    || McaVampireCureService.handleGiftCommand(player, villager)) {
                ((VillagerCommandHandler) (Object) this).stopInteracting();
                cir.setReturnValue(true);
                return;
            }
        }
        if (!"trade".equals(command)) {
            if (McaFactionSocialService.handleCommandAction(player, villager, command)) {
                cir.setReturnValue(true);
                return;
            }
            if ("gift".equals(command) && McaFactionGiftService.handleGiftCommand(player, villager)) {
                ((VillagerCommandHandler) (Object) this).stopInteracting();
                cir.setReturnValue(true);
            }
            return;
        }
        // MCA 7.6.28-beta.10 uses MixinVillagerEntityInvoker#invokeBeginTradeWith
        // here, which bypasses copiedBeginTradeWith. The faction gate must run
        // before that invoker opens a native MerchantMenu.
        if (McaFactionTradeService.shouldCancelMcaDialogueTrade(player, villager)) {
            ((VillagerCommandHandler) (Object) this).stopInteracting();
            cir.setReturnValue(true);
        }
    }

    private Entity mcaVampirismCompat$getEntity() {
        Field field = mcaVampirismCompat$getEntityField();
        if (field == null) {
            return null;
        }
        try {
            Object value = field.get(this);
            return value instanceof Entity entity ? entity : null;
        } catch (IllegalAccessException exception) {
            McaVampirismCompat.debugLog("[MCA-INTERACTION] could not read MCA command entity error={}", exception.toString());
            return null;
        }
    }

    private Field mcaVampirismCompat$getEntityField() {
        if (mcaVampirismCompat$entityFieldResolved) {
            return mcaVampirismCompat$entityField;
        }
        mcaVampirismCompat$entityFieldResolved = true;
        Class<?> type = this.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField("entity");
                field.setAccessible(true);
                mcaVampirismCompat$entityField = field;
                return field;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        McaVampirismCompat.debugLog("[MCA-INTERACTION] MCA command entity field not found");
        return null;
    }
}
