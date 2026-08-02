package com.guilh.mca_vampirism_compat.book;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OccultArtsBookResourcesTest {
    private static final String PREFIX = "book.mca_vamp_compat.occult_arts";

    @Test
    void allLanguagesContainThirtyCompletePages() throws IOException {
        assertEquals(30, OccultArtsBook.TOTAL_PAGE_COUNT);

        for (String language : new String[]{"pt_br", "pt_pt", "en_us"}) {
            JsonObject translations = readJson(
                    "/assets/mca_vamp_compat/lang/" + language + ".json"
            );
            assertTranslation(translations, PREFIX + ".title");
            assertTranslation(translations, PREFIX + ".author");

            for (int page = 2; page <= OccultArtsBook.TOTAL_PAGE_COUNT; page++) {
                String pageNumber = String.format("%02d", page);
                assertTranslation(translations, PREFIX + ".page." + pageNumber + ".title");
                assertTranslation(translations, PREFIX + ".page." + pageNumber + ".body");
            }
        }
    }

    @Test
    void authorIsLocalizedAndTheRemovedTooltipIsNotPackaged() throws IOException {
        assertEquals(
                "Seleris, a Cronista do Véu",
                readJson("/assets/mca_vamp_compat/lang/pt_br.json")
                        .get(PREFIX + ".author").getAsString()
        );
        assertEquals(
                "Seleris, a Cronista do Véu",
                readJson("/assets/mca_vamp_compat/lang/pt_pt.json")
                        .get(PREFIX + ".author").getAsString()
        );
        assertEquals(
                "Seleris, the Chronicler of the Veil",
                readJson("/assets/mca_vamp_compat/lang/en_us.json")
                        .get(PREFIX + ".author").getAsString()
        );

        for (String language : new String[]{"pt_br", "pt_pt", "en_us"}) {
            JsonObject translations = readJson(
                    "/assets/mca_vamp_compat/lang/" + language + ".json"
            );
            assertFalse(
                    translations.has("tooltip.mca_vamp_compat.book_occult_arts"),
                    language + " still packages the removed main-hand tooltip"
            );
            assertTranslation(
                    translations,
                    "message.mca_vamp_compat.occult_arts.main_hand"
            );
        }
    }

    @Test
    void titlePageAuthorIsItalicWithoutInheritedBold() {
        Component titlePage = OccultArtsBook.createTitlePage();
        Component author = titlePage.getSiblings().get(1);

        assertFalse(author.getStyle().isBold());
        assertTrue(author.getStyle().isItalic());
    }

    @Test
    void pageAtlasContainsAllFourMcaArrowCells() throws IOException {
        try (InputStream stream = OccultArtsBookResourcesTest.class.getResourceAsStream(
                "/assets/mca_vamp_compat/textures/gui/books/occult_arts.png"
        )) {
            assertNotNull(stream);
            BufferedImage atlas = ImageIO.read(stream);
            assertEquals(256, atlas.getWidth());
            assertEquals(256, atlas.getHeight());

            assertTrue(hasVisiblePixel(atlas, 0, 192, 23, 13));
            assertTrue(hasVisiblePixel(atlas, 23, 192, 23, 13));
            assertTrue(hasVisiblePixel(atlas, 0, 205, 23, 13));
            assertTrue(hasVisiblePixel(atlas, 23, 205, 23, 13));
        }
    }

    @Test
    void itemAtlasRecipeTagsAndAdvancementArePackaged() {
        assertResource("/assets/mca_vamp_compat/models/item/book_occult_arts.json");
        assertResource("/assets/mca_vamp_compat/textures/item/book_occult_arts.png");
        assertResource("/assets/mca_vamp_compat/textures/gui/books/occult_arts.png");
        assertResource("/data/mca_vamp_compat/recipe/book_occult_arts.json");
        assertResource("/data/mca_vamp_compat/advancement/discover_occult_village.json");
        assertResource("/data/mca/tags/item/books.json");
        assertResource("/data/minecraft/tags/item/bookshelf_books.json");
    }

    private static JsonObject readJson(String path) throws IOException {
        try (InputStream stream = OccultArtsBookResourcesTest.class.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing resource " + path);
            return JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            ).getAsJsonObject();
        }
    }

    private static void assertTranslation(JsonObject translations, String key) {
        assertTrue(translations.has(key), "Missing translation " + key);
        assertFalse(translations.get(key).getAsString().isBlank(), "Blank translation " + key);
    }

    private static void assertResource(String path) {
        assertNotNull(
                OccultArtsBookResourcesTest.class.getResource(path),
                "Missing packaged resource " + path
        );
    }

    private static boolean hasVisiblePixel(
            BufferedImage image,
            int startX,
            int startY,
            int width,
            int height
    ) {
        for (int y = startY; y < startY + height; y++) {
            for (int x = startX; x < startX + width; x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
