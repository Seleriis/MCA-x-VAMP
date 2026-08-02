package com.guilh.mca_vampirism_compat.mixin.client;

import com.guilh.mca_vampirism_compat.easyvillagers.client.EasyVillagersCycleClientState;
import com.guilh.mca_vampirism_compat.easyvillagers.client.MerchantScreenSelectionSanitizer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.world.inventory.MerchantMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
public abstract class MerchantScreenMixin {
    @Shadow
    private int shopItem;

    @Shadow
    private int scrollOff;

    @Shadow
    private boolean isDragging;

    @Unique
    private int mcaVampCompat$lastCycleRevision = -1;

    @Inject(method = "render", at = @At("HEAD"))
    private void mcaVampCompat$keepOfferSelectionValid(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo ci
    ) {
        MerchantScreen screen = (MerchantScreen) (Object) this;
        MerchantMenu menu = screen.getMenu();
        if (!EasyVillagersCycleClientState.hasStateFor(menu.containerId)) {
            return;
        }

        int revision = EasyVillagersCycleClientState.revisionFor(menu.containerId);
        boolean resetForNewOfferList = revision != mcaVampCompat$lastCycleRevision;
        MerchantScreenSelectionSanitizer.Selection selection =
                MerchantScreenSelectionSanitizer.sanitize(
                        resetForNewOfferList ? 0 : shopItem,
                        resetForNewOfferList ? 0 : scrollOff,
                        menu.getOffers().size()
                );

        boolean selectionChanged = shopItem != selection.selectedOffer();
        boolean scrollChanged = scrollOff != selection.scrollOffset();
        shopItem = selection.selectedOffer();
        scrollOff = selection.scrollOffset();
        if (resetForNewOfferList || scrollChanged) {
            isDragging = false;
        }
        if (resetForNewOfferList || selectionChanged) {
            menu.setSelectionHint(shopItem);
        }
        mcaVampCompat$lastCycleRevision = revision;
    }
}
