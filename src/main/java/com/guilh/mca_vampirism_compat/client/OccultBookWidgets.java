package com.guilh.mca_vampirism_compat.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

/** Small native widgets in the occult book's pixel palette; no per-frame image loading. */
final class OccultBookWidgets {
    private OccultBookWidgets() { }

    enum Section { GUIDE, MISSIONS }

    static final class Bookmark extends AbstractButton {
        private final boolean selected;
        private final Section section;
        private final Runnable action;
        Bookmark(int x, int y, Section section, boolean selected, Runnable action) {
            super(x, y, 22, 13, Component.translatable(section == Section.GUIDE
                    ? "mca_vamp_compat.journal.guide" : "mca_vamp_compat.journal.title"));
            this.selected = selected;
            this.section = section;
            this.action = action;
            setTooltip(Tooltip.create(getMessage()));
        }
        @Override public void onPress() { action.run(); }
        @Override protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
        @Override public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = getX(), y = getY();
            int fill = selected ? 0xFF97458E : 0xFF783272;
            int light = isHoveredOrFocused() ? 0xFFF5DEEF : 0xFFD295CC;
            // Right-side cut-out. The horizontal 10 x 7 scroll keeps its approved size.
            for (int row = 0; row < 13; row++) {
                int right = 22 - (6 - Math.abs(row - 6));
                graphics.fill(x, y + row, x + right, y + row + 1, 0xFF32112F);
                if (row > 0 && row < 12) {
                    graphics.fill(x + 1, y + row, x + right - 1, y + row + 1, fill);
                    graphics.fill(x + right - 2, y + row, x + right - 1, y + row + 1, light);
                }
            }
            graphics.fill(x + 2, y + 1, x + 19, y + 2, light);
            graphics.fill(x + 2, y + 10, x + 19, y + 11, 0xFF9E5496);
            if (section == Section.GUIDE) {
                // Open book: two light pages separated by the darker spine.
                graphics.fill(x + 5, y + 3, x + 15, y + 10, 0xFF502349);
                graphics.fill(x + 6, y + 4, x + 10, y + 9, 0xFFEFDAE9);
                graphics.fill(x + 11, y + 4, x + 14, y + 9, 0xFFEFDAE9);
                graphics.fill(x + 10, y + 3, x + 11, y + 10, 0xFFB17AA6);
                graphics.fill(x + 7, y + 5, x + 9, y + 6, 0xFFC69ABE);
                graphics.fill(x + 12, y + 5, x + 14, y + 6, 0xFFC69ABE);
                return;
            }
            // Dark outline and four rolled corners keep the small scroll legible.
            graphics.fill(x + 5, y + 3, x + 15, y + 10, 0xFF502349);
            graphics.fill(x + 7, y + 4, x + 13, y + 9, 0xFFEFDAE9);
            graphics.fill(x + 6, y + 4, x + 7, y + 9, 0xFFD295CC);
            graphics.fill(x + 13, y + 4, x + 14, y + 9, 0xFFD295CC);
            for (int edge = 5; edge <= 13; edge += 8) {
                graphics.fill(x + edge, y + 3, x + edge + 2, y + 5, 0xFFF5DEEF);
                graphics.fill(x + edge, y + 8, x + edge + 2, y + 10, 0xFFF5DEEF);
            }
            graphics.fill(x + 7, y + 8, x + 13, y + 9, 0xFFCCA8C9);
        }
    }

    static final class TextLink extends AbstractButton {
        private final Runnable action;
        TextLink(int x, int y, int width, int height, Component message, Runnable action) {
            super(x, y, width, height, message);
            this.action = action;
        }
        @Override public void onPress() { action.run(); }
        @Override protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
        @Override public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            var font = Minecraft.getInstance().font;
            int textWidth = font.width(getMessage());
            int x = getX() + (width - textWidth) / 2;
            int y = getY() + (height - font.lineHeight) / 2;
            int color = !active ? 0x987E95 : isHoveredOrFocused() ? 0xC020B6 : 0x94258C;
            graphics.drawString(font, getMessage(), x, y, color, false);
            if (active && isHoveredOrFocused()) graphics.fill(x, y + font.lineHeight, x + textWidth, y + font.lineHeight + 1, 0xFF000000 | color);
        }
    }
}
