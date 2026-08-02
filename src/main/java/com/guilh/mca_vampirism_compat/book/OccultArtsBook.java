package com.guilh.mca_vampirism_compat.book;

import com.guilh.mca_vampirism_compat.McaVampirismCompat;
import net.conczin.mca.client.book.Book;
import net.conczin.mca.client.book.pages.CenteredTextPage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

/**
 * Builds the immutable, translated guide consumed by MCA's extended book screen.
 */
public final class OccultArtsBook {
    public static final String TRANSLATION_PREFIX = "book.mca_vamp_compat.occult_arts";
    public static final int CONTENT_PAGE_COUNT = 29;
    public static final int TOTAL_PAGE_COUNT = CONTENT_PAGE_COUNT + 1;

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
            McaVampirismCompat.MOD_ID,
            "textures/gui/books/occult_arts.png"
    );

    private OccultArtsBook() {
    }

    public static Book create() {
        Book book = new Book(
                "occult_arts",
                Component.translatable(TRANSLATION_PREFIX + ".author")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
        )
                .setBackground(BACKGROUND)
                .setTextFormatting(ChatFormatting.DARK_PURPLE)
                .setTextShadow(false)
                .setPageTurnSound(true)
                .addPage(new CenteredTextPage(createTitlePage()));

        for (int page = 2; page <= TOTAL_PAGE_COUNT; page++) {
            book.addPage(new AdaptiveOccultTextPage(createPage(page)));
        }
        return book;
    }

    static Component createTitlePage() {
        return Component.translatable(TRANSLATION_PREFIX + ".title")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)
                .append(Component.literal("\n\n"))
                .append(Component.translatable(TRANSLATION_PREFIX + ".author")
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.DARK_GRAY)
                                .withBold(false)
                                .withItalic(true)));
    }

    private static Component createPage(int page) {
        String pageNumber = String.format("%02d", page);
        MutableComponent heading = Component.translatable(
                TRANSLATION_PREFIX + ".page." + pageNumber + ".title"
        ).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);

        return heading
                .append(Component.literal("\n\n"))
                .append(Component.translatable(
                        TRANSLATION_PREFIX + ".page." + pageNumber + ".body"
                ).withStyle(ChatFormatting.DARK_GRAY));
    }
}
