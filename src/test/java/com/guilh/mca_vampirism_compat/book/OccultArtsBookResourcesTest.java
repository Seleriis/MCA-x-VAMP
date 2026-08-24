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
    void everyLocaleContainsTheCompleteEnglishKeySet() throws IOException {
        JsonObject english = readJson("/assets/mca_vamp_compat/lang/en_us.json");
        for (String language : new String[]{"pt_br", "pt_pt"}) {
            JsonObject translations = readJson("/assets/mca_vamp_compat/lang/" + language + ".json");
            for (String key : english.keySet()) {
                assertTranslation(translations, key);
            }
        }
    }

    @Test
    void allLanguagesContainThirtyOneCompletePages() throws IOException {
        assertEquals(31, OccultArtsBook.TOTAL_PAGE_COUNT);

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

    @Test
    void occultProgressTreeBackgroundAndTranslationsArePackaged() throws IOException {
        String[] advancements = {
                "discover_occult_village", "vampire_infection", "vampire_conversion", "vampire_trade_discount",
                "hunter_blood", "vampire_specialist", "hunter_trade_discount", "hunter_specialist",
                "werewolf_infection", "werewolf_conversion", "werewolf_trade_discount", "werewolf_specialist",
                "treat_early_infection", "complete_cure", "inherited_baby", "faction_safe_carrying",
                "vampire_village_favor", "hunter_village_favor", "werewolf_village_favor", "urgent_hunter_aid"
        };
        String[] translationNames = {
                "vampire_infection", "vampire_conversion", "vampire_trade", "hunter_blood", "vampire_specialist",
                "hunter_trade", "hunter_specialist", "werewolf_infection", "werewolf_conversion", "werewolf_trade",
                "werewolf_specialist", "treat_early_infection", "complete_cure", "inherited_baby", "faction_safe_carrying",
                "vampire_village_favor", "hunter_village_favor", "werewolf_village_favor", "urgent_hunter_aid"
        };
        for (String advancement : advancements) {
            assertResource("/data/mca_vamp_compat/advancement/" + advancement + ".json");
        }
        for (String language : new String[]{"pt_br", "pt_pt", "en_us"}) {
            JsonObject translations = readJson("/assets/mca_vamp_compat/lang/" + language + ".json");
            for (String name : translationNames) {
                assertTranslation(translations, "advancement.mca_vamp_compat." + name + ".title");
                assertTranslation(translations, "advancement.mca_vamp_compat." + name + ".description");
            }
        }

        JsonObject root = readJson("/data/mca_vamp_compat/advancement/discover_occult_village.json");
        assertTrue(root.getAsJsonObject("display").has("background"));
        assertResource("/assets/mca_vamp_compat/textures/gui/advancements/occult_arts_background.png");
    }

    @Test
    void adminCommandsAndFactionDialogueLinesAreLocalized() throws IOException {
        String[] commandKeys = {
                "mca_vamp_compat.admin.error.not_mca_villager",
                "mca_vamp_compat.admin.error.not_active_totem",
                "mca_vamp_compat.admin.error.player_required",
                "mca_vamp_compat.admin.help.header",
                "mca_vamp_compat.admin.help.inspect",
                "mca_vamp_compat.admin.help.resync",
                "mca_vamp_compat.admin.error.villager_not_found",
                "mca_vamp_compat.admin.error.villager_ambiguous",
                "mca_vamp_compat.admin.help.village",
                "mca_vamp_compat.admin.help.village_header",
                "mca_vamp_compat.admin.help.village_info",
                "mca_vamp_compat.admin.help.village_outline",
                "mca_vamp_compat.admin.inspect.header",
                "mca_vamp_compat.admin.inspect.faction",
                "mca_vamp_compat.admin.inspect.traits",
                "mca_vamp_compat.admin.inspect.profession",
                "mca_vamp_compat.admin.inspect.infection",
                "mca_vamp_compat.admin.inspect.cure",
                "mca_vamp_compat.admin.inspect.runtime",
                "mca_vamp_compat.admin.inspect.hunter",
                "mca_vamp_compat.admin.resync.result",
                "mca_vamp_compat.admin.village.header",
                "mca_vamp_compat.admin.village.native",
                "mca_vamp_compat.admin.village.effective",
                "mca_vamp_compat.admin.village.fog",
                "mca_vamp_compat.admin.village.mca",
                "mca_vamp_compat.admin.village.fog_margin",
                "mca_vamp_compat.admin.village.outline_started"
        };
        String[] events = {"greeting", "trade_refused", "social_refused", "cure_started", "cure_completed"};

        for (String language : new String[]{"pt_br", "pt_pt", "en_us"}) {
            JsonObject translations = readJson("/assets/mca_vamp_compat/lang/" + language + ".json");
            for (String key : commandKeys) {
                assertTranslation(translations, key);
            }
            for (String faction : new String[]{"vampire", "hunter", "werewolf"}) {
                for (String event : events) {
                    assertTranslation(translations, "mca_vamp_compat.dialogue." + faction + "." + event);
                }
            }
        }
    }

    @Test
    void villageMissionAdvancementsAndMessagesAreLocalized() throws IOException {
        String[] missionKeys = {
                "mca_vamp_compat.village_mission.not_enough",
                "mca_vamp_compat.village_mission.completed",
                "dialogue.main.occult_favor_offer",
                "dialogue.main.occult_favor_status",
                "dialogue.occult_favor_pending",
                "dialogue.occult_favor_pending.accept",
                "dialogue.occult_favor_pending.decline",
                "dialogue.occult_favor_active",
                "dialogue.occult_favor_active.progress",
                "dialogue.occult_favor_active.cancel",
                "mca_vamp_compat.village_mission.vampire_blood.request",
                "mca_vamp_compat.village_mission.vampire_blood.accepted",
                "mca_vamp_compat.village_mission.vampire_blood.active",
                "mca_vamp_compat.village_mission.vampire_blood.item",
                "mca_vamp_compat.village_mission.vampire_fang.request",
                "mca_vamp_compat.village_mission.vampire_fang.accepted",
                "mca_vamp_compat.village_mission.vampire_fang.active",
                "mca_vamp_compat.village_mission.hunter_garlic.request",
                "mca_vamp_compat.village_mission.hunter_garlic.accepted",
                "mca_vamp_compat.village_mission.hunter_garlic.active",
                "mca_vamp_compat.village_mission.hunter_garlic_injection.request",
                "mca_vamp_compat.village_mission.hunter_garlic_injection.accepted",
                "mca_vamp_compat.village_mission.hunter_garlic_injection.active",
                "mca_vamp_compat.village_mission.hunter_urgent_aid.request",
                "mca_vamp_compat.village_mission.hunter_urgent_aid.accepted",
                "mca_vamp_compat.village_mission.hunter_urgent_aid.active",
                "mca_vamp_compat.village_mission.werewolf_mutton.request",
                "mca_vamp_compat.village_mission.werewolf_mutton.accepted",
                "mca_vamp_compat.village_mission.werewolf_mutton.active",
                "mca_vamp_compat.village_mission.dialogue.declined",
                "mca_vamp_compat.village_mission.dialogue.canceled",
                "mca_vamp_compat.village_mission.expired",
                "mca_vamp_compat.village_mission.time.days",
                "mca_vamp_compat.village_mission.time.minutes",
                "mca_vamp_compat.village_mission.time.none"
        };
        for (String language : new String[]{"pt_br", "pt_pt", "en_us"}) {
            JsonObject translations = readJson("/assets/mca_vamp_compat/lang/" + language + ".json");
            for (String key : missionKeys) {
                assertTranslation(translations, key);
                assertFalse(translations.get(key).getAsString().contains("%4$s"),
                        "Mission text must not expect the removed villager-name argument: " + key);
            }
            assertFalse(translations.has("mca_vamp_compat.village_mission.resident"),
                    "Mission messages must use MCA's native chat speaker instead of a redundant resident label");
        }
    }

    @Test
    void villageMissionConversationResourcesExposeOnlyNativeDialogueChoices() throws IOException {
        JsonObject main = readJson("/data/mca_vamp_compat/dialogues/main.json");
        assertTrue(main.has("answers"));
        assertEquals("occult_favor_offer", main.getAsJsonArray("answers").get(0)
                .getAsJsonObject().get("name").getAsString());
        assertEquals("occult_favor_status", main.getAsJsonArray("answers").get(1)
                .getAsJsonObject().get("name").getAsString());

        JsonObject pending = readJson("/data/mca_vamp_compat/dialogues/occult_favor_pending.json");
        assertTrue(pending.get("silent").getAsBoolean());
        assertEquals("accept", pending.getAsJsonArray("answers").get(0)
                .getAsJsonObject().get("name").getAsString());
        assertEquals("decline", pending.getAsJsonArray("answers").get(1)
                .getAsJsonObject().get("name").getAsString());

        JsonObject active = readJson("/data/mca_vamp_compat/dialogues/occult_favor_active.json");
        assertTrue(active.get("silent").getAsBoolean());
        assertEquals("progress", active.getAsJsonArray("answers").get(0)
                .getAsJsonObject().get("name").getAsString());
        assertEquals("cancel", active.getAsJsonArray("answers").get(1)
                .getAsJsonObject().get("name").getAsString());
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
