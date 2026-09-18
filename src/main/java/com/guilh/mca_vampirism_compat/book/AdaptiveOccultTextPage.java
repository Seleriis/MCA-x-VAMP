package com.guilh.mca_vampirism_compat.book;

import forge.net.mca.client.book.pages.TextPage;
import forge.net.mca.client.gui.ExtendedBookScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Preserves MCA's native book layout and only shrinks text that would otherwise
 * exceed its fourteen-line page area.
 */
final class AdaptiveOccultTextPage extends TextPage {
    private static final int PAGE_WIDTH = 114;
    private static final int PAGE_HEIGHT = 14 * 9;
    private static final int LINE_HEIGHT = 9;
    private static final float SCALE_STEP = 0.05F;
    private static final float MIN_SCALE = 0.50F;

    AdaptiveOccultTextPage(Component content) {
        // MCA 7.6 stores pages as JSON text. Keeping the serialized Component
        // preserves translations and formatting on the client instead of
        // resolving them while the common-side book is built.
        super(Component.Serializer.toJson(content));
    }

    @Override
    public void render(ExtendedBookScreen screen, GuiGraphics graphics,
                       int mouseX, int mouseY, float delta) {
        if (content == null) {
            return;
        }

        Font font = screen.getTextRenderer();
        PageLayout layout = selectLayout(font);
        int left = (screen.width - 192) / 2 + 36;

        graphics.pose().pushPose();
        graphics.pose().translate(left, 32, 0);
        graphics.pose().scale(layout.scale(), layout.scale(), 1.0F);
        for (int lineIndex = 0; lineIndex < layout.lines().size(); lineIndex++) {
            graphics.drawString(
                    font,
                    layout.lines().get(lineIndex),
                    0,
                    lineIndex * LINE_HEIGHT,
                    0,
                    screen.getBook().hasTextShadow()
            );
        }
        graphics.pose().popPose();
    }

    private PageLayout selectLayout(Font font) {
        float scale = 1.0F;
        PageLayout layout;
        do {
            int logicalWidth = Math.max(1, (int) Math.floor(PAGE_WIDTH / scale));
            Component parsed = Component.Serializer.fromJson(content);
            FormattedText displayText = parsed == null ? FormattedText.of(content) : parsed;
            List<FormattedCharSequence> lines = font.split(displayText, logicalWidth);
            int lineCapacity = Math.max(
                    1,
                    (int) Math.floor(PAGE_HEIGHT / (LINE_HEIGHT * scale))
            );
            layout = new PageLayout(scale, lines);
            if (lines.size() <= lineCapacity) {
                return layout;
            }
            scale = roundScale(scale - SCALE_STEP);
        } while (scale >= MIN_SCALE);

        return layout;
    }

    private static float roundScale(float scale) {
        return Math.round(scale * 100.0F) / 100.0F;
    }

    private record PageLayout(float scale, List<FormattedCharSequence> lines) {
    }
}
