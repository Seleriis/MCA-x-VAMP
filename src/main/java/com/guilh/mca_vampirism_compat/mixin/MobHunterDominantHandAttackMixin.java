package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaDefensiveVillagerService;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Covers MCA releases whose {@code VillagerEntityMCA#doHurtTarget} delegates
 * to the stable vanilla {@link Mob} implementation. MCA 7.6.16 keeps its
 * own attack body and is covered by {@link McaHunterDominantHandAttackMixin};
 * newer 7.6 builds reach this hook instead.
 */
@Mixin(value = Mob.class, remap = false)
public abstract class MobHunterDominantHandAttackMixin {
    @Redirect(
            method = "m_7327_",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;m_21205_()Lnet/minecraft/world/item/ItemStack;"
            ),
            // This is a best-effort compatibility hook. It must never turn a
            // harmless upstream implementation change into a startup crash.
            require = 0,
            remap = false
    )
    private ItemStack mcaVampirismCompat$useDominantHunterWeaponForVanillaAttack(Mob mob) {
        if (mob instanceof VillagerEntityMCA villager
                && McaDefensiveVillagerService.shouldUseDominantHunterWeaponForNativeAttack(villager)) {
            return villager.getItemInHand(villager.getDominantHand());
        }
        return mob.getMainHandItem();
    }
}
