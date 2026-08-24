package com.guilh.mca_vampirism_compat.trade;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.loading.LoadingModList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McaFactionTradePriceOverlayTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        if (LoadingModList.get() == null) {
            LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        }
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void adjustsAndRestoresTheSameOfferInstance() {
        MerchantOffer offer = new MerchantOffer(
                new ItemCost(Items.EMERALD, 20),
                new ItemStack(Items.BREAD, 8),
                12,
                1,
                0.05F
        );
        MerchantOffers offers = new MerchantOffers();
        offers.add(offer);

        McaFactionTradePriceOverlay overlay = McaFactionTradePriceOverlay.createForTest(
                offers,
                candidate -> candidate.getSpecialPriceDiff() - 3
        );
        overlay.apply();

        assertSame(offer, overlay.offers().getFirst(), "Faction pricing must never replace the live offer");
        assertTrue(overlay.contains(offer), "The active live offer must remain owned by the overlay");
        assertEquals(17, offer.getCostA().getCount(), "Hunter discount must be calculated from the live offer");

        ItemStack payment = new ItemStack(Items.EMERALD, 17);
        assertTrue(offer.take(payment, ItemStack.EMPTY), "The live offer must consume its displayed discounted price");
        assertTrue(payment.isEmpty(), "A completed trade must consume all exact payment");
        offer.increaseUses();

        overlay.restore();
        assertEquals(0, offer.getSpecialPriceDiff(), "Closing must restore the pre-session special price");
        assertEquals(20, offer.getCostA().getCount(), "Closing must restore the native offer price");
        assertEquals(1, offer.getUses(), "Restoring a price overlay must not roll back trade use state");
    }
}
