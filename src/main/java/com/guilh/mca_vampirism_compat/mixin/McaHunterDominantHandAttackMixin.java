package com.guilh.mca_vampirism_compat.mixin;

import com.guilh.mca_vampirism_compat.service.McaDefensiveVillagerService;
import forge.net.mca.entity.VillagerEntityMCA;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * MCA 7.6's native attack reads MAIN_HAND for enchantment bonuses even though
 * its own dominant hand can be OFF_HAND for a left-handed villager. Restrict
 * the correction to the persisted Hunter weapon path so ordinary MCA combat
 * retains its exact behavior.
 */
@Mixin(value = VillagerEntityMCA.class, remap = false)
public abstract class McaHunterDominantHandAttackMixin {
    @Redirect(
            method = "m_7327_",
            at = @At(
                    value = "INVOKE",
                    target = "Lforge/net/mca/entity/VillagerEntityMCA;m_21205_()Lnet/minecraft/world/item/ItemStack;"
            ),
            // MCA 7.6.28 delegates this method to Mob instead of keeping its
            // own attack body. Keep the legacy hook optional so that API
            // change cannot prevent the game from booting.
            require = 0
    )
    private ItemStack mcaVampirismCompat$useDominantHunterWeaponForNativeAttack(VillagerEntityMCA villager) {
        if (McaDefensiveVillagerService.shouldUseDominantHunterWeaponForNativeAttack(villager)) {
            return villager.getItemInHand(villager.getDominantHand());
        }
        return villager.getMainHandItem();
    }
}
