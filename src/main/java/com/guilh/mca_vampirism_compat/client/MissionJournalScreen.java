package com.guilh.mca_vampirism_compat.client;

import com.guilh.mca_vampirism_compat.book.OccultArtsBook;
import com.guilh.mca_vampirism_compat.item.OccultArtsBookItem;
import com.guilh.mca_vampirism_compat.network.*;
import net.conczin.mca.client.book.Book;
import net.conczin.mca.client.book.pages.TextPage;
import net.conczin.mca.client.gui.ExtendedBookScreen;
import net.conczin.mca.client.gui.widget.ExtendedPageTurnWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Another page of MCA's original book, using its texture and navigation. */
@EventBusSubscriber(modid = "mca_vamp_compat", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class MissionJournalScreen extends ExtendedBookScreen {
    private static final int TEXT_TOP = 47, TEXT_BOTTOM = 141, TEXT_WIDTH = 118;
    private static final int TEXT_COLOR = 0x444044;
    private MissionJournalPayload data;
    private final Guide guide;
    private int ticks, scroll, contentHeight, requestedPage;
    private List<Line> lines = List.of();
    private PageButton previous, next;
    private OccultBookWidgets.TextLink track;

    private MissionJournalScreen(MissionJournalPayload payload) { this(payload, new Guide(payload)); }

    private MissionJournalScreen(MissionJournalPayload payload, Guide guide) {
        super(new Book("occult_missions").setBackground(OccultArtsBook.background())
                .setTextFormatting(ChatFormatting.DARK_PURPLE).setTextShadow(false).setPageTurnSound(true)
                .addPage(new TextPage(Component.empty())));
        this.data = payload;
        this.guide = guide;
        requestedPage = payload.page();
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MissionJournalClientBridge.install(MissionJournalScreen::receive));
    }

    private static void receive(MissionJournalPayload payload) {
        Minecraft client = Minecraft.getInstance();
        if (payload.open()) client.setScreen(new Guide(payload));
        else if (client.screen instanceof MissionJournalScreen diary && diary.data.session() == payload.session()) {
            if (diary.data.page() != payload.page() || !Objects.equals(diary.data.target(), payload.target())) diary.scroll = 0;
            diary.data = payload;
            diary.guide.data = payload;
            diary.requestedPage = payload.page();
            diary.wrapLines();
            diary.updateButtons();
        } else if (client.screen instanceof Guide guide && guide.data.session() == payload.session()) guide.data = payload;
    }

    private void request(int page, boolean select, UUID target) {
        requestedPage = page;
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new MissionJournalRequest(data.session(), page, select, target));
    }

    private int bookLeft() { return (width - 192) / 2; }

    @Override protected void init() {
        super.init();
        int left = bookLeft();
        track = addRenderableWidget(new OccultBookWidgets.TextLink(left + 35, 145, 120, 13,
                Component.empty(), () -> request(data.page(), true, data.tracked() ? null : data.target())));
        addRenderableWidget(new OccultBookWidgets.Bookmark(left + 169, 22, OccultBookWidgets.Section.GUIDE, false, this::returnToGuide));
        addRenderableWidget(new OccultBookWidgets.Bookmark(left + 169, 40, OccultBookWidgets.Section.MISSIONS, true, () -> { }));
        wrapLines();
        updateButtons();
    }

    @Override protected void addPageButtons() {
        // Initialize MCA's private buttons too: Page Up/Down dispatches to our overrides.
        super.addPageButtons();
        int left = bookLeft();
        previous = addRenderableWidget(new ExtendedPageTurnWidget(left + 43, 159, false,
                button -> goToPreviousPage(), true, getBook().getBackground()));
        next = addRenderableWidget(new ExtendedPageTurnWidget(left + 116, 159, true,
                button -> goToNextPage(), true, getBook().getBackground()));
    }

    @Override protected void goToPreviousPage() {
        if (data.page() > 0) request(data.page() - 1, false, null);
    }
    @Override protected void goToNextPage() {
        if (data.page() + 1 < data.total()) request(data.page() + 1, false, null);
    }

    private void returnToGuide() {
        guide.data = data;
        minecraft.setScreen(guide); // Retain the previously open guide page.
    }

    private void updateButtons() {
        if (previous == null || track == null) return;
        previous.visible = data.page() > 0;
        next.visible = data.page() + 1 < data.total();
        track.active = data.target() != null;
        track.setMessage(Component.translatable(data.tracked()
                ? "mca_vamp_compat.journal.untrack" : "mca_vamp_compat.journal.track"));
    }

    private void wrapLines() {
        if (font == null) return;
        var wrapped = new ArrayList<Line>();
        int y = 0;
        for (Component original : data.lines()) {
            if (original.getContents() instanceof TranslatableContents translated) {
                String key = translated.getKey();
                if (y > 0 && (key.endsWith("journal.delivery") || key.endsWith("journal.treatment")
                        || key.endsWith("village_mission.reward"))) y += 4;
            }
            for (FormattedCharSequence line : font.split(present(original), TEXT_WIDTH)) {
                wrapped.add(new Line(line, y));
                y += font.lineHeight;
            }
        }
        lines = List.copyOf(wrapped);
        contentHeight = y;
        clampScroll();
    }

    /** Unknown/custom dimensions retain the server's label. */
    private static Component present(Component line) {
        if (line.getContents() instanceof TranslatableContents translated
                && translated.getKey().equals("mca_vamp_compat.journal.dimension")
                && translated.getArgs().length == 1 && translated.getArgs()[0] instanceof String dimension) {
            String key = switch (dimension) {
                case "minecraft:overworld" -> "overworld";
                case "minecraft:the_nether" -> "nether";
                case "minecraft:the_end" -> "end";
                default -> null;
            };
            if (key != null) return Component.translatable(translated.getKey(),
                    Component.translatable("mca_vamp_compat.journal.dimension." + key)).withStyle(line.getStyle());
        }
        return line;
    }

    private void clampScroll() {
        scroll = Math.max(0, Math.min(scroll, Math.max(0, contentHeight - (TEXT_BOTTOM - TEXT_TOP))));
    }

    @Override public void tick() {
        if (minecraft.player == null || !(minecraft.player.getMainHandItem().getItem() instanceof OccultArtsBookItem)) {
            onClose(); return;
        }
        if (++ticks % 20 == 0) request(requestedPage, false, null);
    }
    @Override public boolean isPauseScreen() { return false; }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = bookLeft();
        Component title = Component.translatable("mca_vamp_compat.journal.title").withStyle(ChatFormatting.BOLD);
        graphics.drawString(font, title, left + 96 - font.width(title) / 2, 32, 0xAA00AA, false);
        graphics.enableScissor(left + 34, TEXT_TOP, left + 34 + TEXT_WIDTH, TEXT_BOTTOM);
        for (Line line : lines) {
            int y = TEXT_TOP + line.y() - scroll;
            if (y + font.lineHeight > TEXT_TOP && y < TEXT_BOTTOM)
                graphics.drawString(font, line.text(), left + 34, y, TEXT_COLOR, false);
        }
        graphics.disableScissor();
        Component counter = Component.translatable("book.pageIndicator", data.page() + 1, Math.max(1, data.total()));
        graphics.drawString(font, counter, left + 148 - font.width(counter), 18, 0xAA00AA, false);
        if (contentHeight > TEXT_BOTTOM - TEXT_TOP) {
            int height = TEXT_BOTTOM - TEXT_TOP;
            int thumb = Math.max(8, height * height / contentHeight);
            int top = TEXT_TOP + (height - thumb) * scroll / (contentHeight - height);
            graphics.fill(left + 156, TEXT_TOP, left + 157, TEXT_BOTTOM, 0x33773177);
            graphics.fill(left + 155, top, left + 158, top + thumb, 0xFF9D589A);
            if (mouseX >= left + 152 && mouseX < left + 160 && mouseY >= TEXT_TOP && mouseY < TEXT_BOTTOM)
                graphics.renderTooltip(font, Component.translatable("mca_vamp_compat.journal.scroll"), mouseX, mouseY);
        }
    }

    @Override public boolean mouseScrolled(double x, double y, double horizontal, double amount) {
        if (x >= bookLeft() + 30 && x < bookLeft() + 162 && y >= TEXT_TOP && y < TEXT_BOTTOM) {
            scroll -= (int) Math.signum(amount) * font.lineHeight * 3;
            clampScroll();
            return true;
        }
        return super.mouseScrolled(x, y, horizontal, amount);
    }

    private record Line(FormattedCharSequence text, int y) { }

    private static final class Guide extends ExtendedBookScreen {
        private MissionJournalPayload data;
        Guide(MissionJournalPayload data) { super(OccultArtsBook.create()); this.data = data; }
        @Override protected void init() {
            super.init();
            int left = (width - 192) / 2;
            addRenderableWidget(new OccultBookWidgets.Bookmark(left + 169, 22, OccultBookWidgets.Section.GUIDE, true, () -> { }));
            addRenderableWidget(new OccultBookWidgets.Bookmark(left + 169, 40, OccultBookWidgets.Section.MISSIONS, false, () -> {
                MissionJournalScreen screen = new MissionJournalScreen(data, this);
                minecraft.setScreen(screen);
                screen.request(data.page(), false, null);
            }));
        }
    }
}
