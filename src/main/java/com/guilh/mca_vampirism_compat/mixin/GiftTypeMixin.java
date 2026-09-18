package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaFactionGiftService;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.interaction.gifts.GiftType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/** Lets faction favorites retain MCA's native gift transaction and saturation. */
@Mixin(value = GiftType.class, remap = false)
public abstract class GiftTypeMixin {
    @Inject(method = "bestMatching", at = @At("HEAD"), cancellable = true)
    private static void mcaVampirismCompat$selectFactionFavorite(
            VillagerEntityMCA villager,
            ItemStack stack,
            ServerPlayer player,
            CallbackInfoReturnable<Optional<GiftType>> cir
    ) {
        McaFactionGiftService.favoriteGiftType(villager, stack).ifPresent(type -> cir.setReturnValue(Optional.of(type)));
    }
}
