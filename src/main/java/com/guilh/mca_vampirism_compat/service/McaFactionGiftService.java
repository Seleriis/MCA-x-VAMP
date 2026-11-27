package com.guilh.mca_vampirism_compat.service;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import com.guilh.mca_vampirism_compat.config.McaVampirismCompatConfig;
import com.guilh.mca_vampirism_compat.util.McaCompatMessages;
import net.conczin.mca.entity.VillagerEntityMCA;
import net.conczin.mca.entity.interaction.gifts.GiftType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Faction taste rules layered in front of MCA's ordinary gift code.
 *
 * <p>The service deliberately does not consume an accepted item or award
 * hearts. MCA's {@code BreedableRelationship.giveGift} remains responsible
 * for both of those operations, including its native GiftSaturation check.
 * The direct {@link GiftType}s returned here only give MCA a stable category
 * id and base satisfaction for a faction-approved item.</p>
 */
public final class McaFactionGiftService {
    private static final String MOD_ID = "mca_vamp_compat";

    private static final ResourceLocation BLOOD_BOTTLE_ID =
            ResourceLocation.fromNamespaceAndPath("vampirism", "blood_bottle");

    private static final TagKey<Item> VAMPIRE_FAVORED = itemTag("vampire_favored_gifts");
    private static final TagKey<Item> VAMPIRE_REJECTED = itemTag("vampire_rejected_gifts");
    private static final TagKey<Item> WEREWOLF_FAVORED_FOOD = itemTag("werewolf_favored_food_gifts");
    private static final TagKey<Item> WEREWOLF_FAVORED_MATERIAL = itemTag("werewolf_favored_material_gifts");
    private static final TagKey<Item> WEREWOLF_REJECTED = itemTag("werewolf_rejected_gifts");
    private static final TagKey<Item> HUNTER_FAVORED_FOOD = itemTag("hunter_favored_food_gifts");
    private static final TagKey<Item> HUNTER_FAVORED_MATERIAL = itemTag("hunter_favored_material_gifts");
    private static final TagKey<Item> HUNTER_FAVORED_EQUIPMENT = itemTag("hunter_favored_equipment_gifts");

    private McaFactionGiftService() {
    }

    /**
     * Handles only a definite faction rejection. Returning false leaves the
     * entire normal MCA gift path intact, including neutral gifts.
     */
    public static boolean handleGiftCommand(ServerPlayer player, VillagerEntityMCA villager) {
        if (!McaVampirismCompatConfig.enableFactionGiftPreferences()) {
            return false;
        }
        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            return false;
        }
        if (McaVampireStateService.isVampire(villager)) {
            boolean favorite = stack.is(VAMPIRE_FAVORED)
                    && (!isBloodBottle(stack) || McaFactionDietService.hasUsableBlood(stack));
            if (!favorite
                    && (stack.is(VAMPIRE_REJECTED) || stack.has(DataComponents.FOOD))
                    && !canTreatWithMilk(villager, stack)) {
                reject(player, villager, McaCompatMessages.GIFT_REJECTED_VAMPIRE);
                return true;
            }
            if (isBloodBottle(stack) && !McaFactionDietService.hasUsableBlood(stack)) {
                reject(player, villager, McaCompatMessages.GIFT_REJECTED_VAMPIRE);
                return true;
            }
            return false;
        }
        if (McaWerewolfStateService.isWerewolf(villager) && !McaHunterAlignmentService.isMcaHunterVillager(villager)) {
            if (stack.is(WEREWOLF_REJECTED)) {
                reject(player, villager, McaCompatMessages.GIFT_REJECTED_WEREWOLF);
                return true;
            }
        }
        return false;
    }

    /**
     * Called by the GiftType mixin. Returning a category lets MCA perform the
     * original transaction while recognizing additions from our data tags.
     */
    public static Optional<GiftType> favoriteGiftType(VillagerEntityMCA villager, ItemStack stack) {
        if (!McaVampirismCompatConfig.enableFactionGiftPreferences() || stack.isEmpty()) {
            return Optional.empty();
        }
        if (McaVampireStateService.isVampire(villager)) {
            if (stack.is(VAMPIRE_FAVORED) && (!isBloodBottle(stack) || McaFactionDietService.hasUsableBlood(stack))) {
                return Optional.of(gift(stack, "vampire_material", 10));
            }
            return Optional.empty();
        }
        if (McaHunterAlignmentService.isMcaHunterVillager(villager)) {
            if (stack.is(HUNTER_FAVORED_EQUIPMENT)) {
                return Optional.of(gift(stack, "hunter_equipment", 15));
            }
            if (stack.is(HUNTER_FAVORED_MATERIAL)) {
                return Optional.of(gift(stack, "hunter_material", 10));
            }
            if (stack.is(HUNTER_FAVORED_FOOD)) {
                return Optional.of(gift(stack, "hunter_food", 8));
            }
            return Optional.empty();
        }
        if (McaWerewolfStateService.isWerewolf(villager)) {
            if (stack.is(WEREWOLF_FAVORED_MATERIAL)) {
                return Optional.of(gift(stack, "werewolf_material", 10));
            }
            if (stack.is(WEREWOLF_FAVORED_FOOD)) {
                return Optional.of(gift(stack, "werewolf_food", 8));
            }
        }
        return Optional.empty();
    }

    private static GiftType gift(ItemStack stack, String category, int satisfaction) {
        return new GiftType(
                stack.getItem(),
                satisfaction,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "faction_gift/" + category)
        );
    }

    private static boolean isBloodBottle(ItemStack stack) {
        return BLOOD_BOTTLE_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static boolean canTreatWithMilk(VillagerEntityMCA villager, ItemStack stack) {
        return stack.is(Items.MILK_BUCKET) && McaVampireCureService.canTreatInitialInfection(villager);
    }

    private static void reject(ServerPlayer player, VillagerEntityMCA villager, String messageKey) {
        player.displayClientMessage(Component.translatable(messageKey), true);
        McaVampirismCompat.debugLog(
                "[GIFT] rejected player={} villager={} reason={} item={}",
                player.getUUID(),
                villager.getUUID(),
                messageKey,
                BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem())
        );
    }

    private static TagKey<Item> itemTag(String path) {
        return TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, path));
    }
}
