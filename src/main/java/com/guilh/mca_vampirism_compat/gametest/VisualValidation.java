package com.guilh.mca_vampirism_compat.gametest;

import com.guilh.mca_vampirism_compat.network.MissionJournalPayload;
import com.guilh.mca_vampirism_compat.book.OccultArtsBook;
import forge.net.conczin.mca.client.gui.ExtendedBookScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ScreenEvent;
import de.maxhenkel.easyvillagers.gui.BreederContainer;
import de.maxhenkel.easyvillagers.gui.BreederScreen;
import java.nio.file.*;
import java.util.*;

/** Captures the real client screens, fonts and translations. Test source set only. */
@EventBusSubscriber(modid="mca_vamp_compat",value=Dist.CLIENT)
public final class VisualValidation {
    private static final String[] LANGUAGES={"en_us","pt_br","pt_pt"};
    private static final int[] SCALES={1,2,4};
    private static final int PER_LANGUAGE = 18 + OccultArtsBook.CONTENT_PAGE_COUNT;
    private static final int TOTAL = PER_LANGUAGE * LANGUAGES.length;
    private static int scenario, frames;
    private static int type() { int local = scenario % PER_LANGUAGE; return local < 18 ? local % 6 : 6 + local - 18; }
    private static int scale() { int local = scenario % PER_LANGUAGE; return local < 18 ? SCALES[local / 6] : 4; }
    private static boolean started, reloading, captured;
    private static String selectedLanguage="";
    private static Screen preview;
    private static final Path OUTPUT=Path.of(System.getProperty("mca.visualOutput","visual-validation"));
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if(event.phase!=TickEvent.Phase.END) return;
        if(!Boolean.getBoolean("mca.visualValidation")) return;
        var client=Minecraft.getInstance();
        if(!started) {
            if(client.getOverlay()!=null) return;
            if(client.screen!=null && client.screen.getClass().getSimpleName().equals("AccessibilityOnboardingScreen")) {
                client.setScreen(new TitleScreen());return;
            }
            if(!(client.screen instanceof TitleScreen)) return;
            org.lwjgl.glfw.GLFW.glfwHideWindow(client.getWindow().getWindow());
            started=true;
        }
        if(reloading || client.getOverlay()!=null) return;
        if(captured) {scenario++;preview=null;captured=false;frames=0;}
        if(scenario>=TOTAL) {System.out.println("VISUAL_VALIDATION_COMPLETE "+TOTAL+" real screen captures");client.stop();return;}
        String language=LANGUAGES[scenario/PER_LANGUAGE];
        if(!language.equals(selectedLanguage)) {
            selectedLanguage=language;reloading=true;
            client.getLanguageManager().setSelected(language);client.options.languageCode=language;
            client.reloadResourcePacks().thenRun(() -> reloading=false);return;
        }
        try {
            if(preview==null) {
                Files.createDirectories(OUTPUT);
                client.options.guiScale().set(scale());client.resizeDisplay();
                preview=create(type()); frames=0;
            }
            // The isolated menu preview has no logged-in player. Re-open after its normal close-on-missing-player tick.
            if(client.screen!=preview) client.setScreen(preview);
            if(type()>=6 && frames==0) ((ExtendedBookScreen) preview).setPage(type()-5);
        } catch(Exception exception) { fail(exception); }
    }
    private static Screen create(int type) throws Exception {
        var data=new MissionJournalPayload(123L,false,(type==1 || type==4)?11:0,type==2?0:24,type==2?null:UUID.randomUUID(),false,
                type==2?List.of(Component.translatable("mca_vamp_compat.journal.empty")):List.of(
                Component.translatable("mca_vamp_compat.journal.requester",Component.literal("Alexandrina Esperança de Albuquerque e Vasconcelos, Guardiã da Vila das Montanhas Distantes")),
                Component.translatable("mca_vamp_compat.journal.unavailable"),
                Component.translatable("mca_vamp_compat.journal.location",-1234567,120,7654321),
                Component.literal("Vila das Árvores Antigas e dos Caminhos Esquecidos"),
                Component.translatable("mca_vamp_compat.journal.dimension","minecraft:overworld"),
                Component.translatable("mca_vamp_compat.journal.delivery",4,Component.translatable("mca_vamp_compat.village_mission.vampire_blood.item")),
                Component.translatable("mca_vamp_compat.village_mission.reward",8),
                Component.translatable("mca_vamp_compat.journal.deadline",Component.translatable("mca_vamp_compat.journal.minutes",12))));
        if(type==5) data=new MissionJournalPayload(123L,false,0,3,UUID.randomUUID(),false,List.of(
                Component.translatable("mca_vamp_compat.journal.requester",Component.literal("Anna")),
                Component.translatable("mca_vamp_compat.journal.location",128,64,-240),
                Component.translatable("mca_vamp_compat.journal.dimension","minecraft:overworld"),
                Component.translatable("mca_vamp_compat.journal.delivery",8,Component.translatable("item.minecraft.bread")),
                Component.translatable("mca_vamp_compat.village_mission.reward",8),
                Component.translatable("mca_vamp_compat.journal.deadline",Component.translatable("mca_vamp_compat.journal.minutes",18))));
        if(type<3 || type==4 || type==5 || type>=6) {
            var clazz=Class.forName("com.guilh.mca_vampirism_compat.client.MissionJournalScreen"+(type==0 || type>=6?"$Guide":""));
            var ctor=clazz.getDeclaredConstructor(MissionJournalPayload.class);ctor.setAccessible(true);
            return (Screen)ctor.newInstance(data);
        }
        var inventory=new Inventory(null);
        var menu=new BreederContainer(1,inventory,new SimpleContainer(1),new SimpleContainer(1),ContainerLevelAccess.NULL);
        menu.setData(0,6);menu.setData(1,-1);menu.setData(2,0);
        return new MenuPreview(new BreederScreen(menu,inventory,Component.translatable("block.easy_villagers.breeder")));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void render(ScreenEvent.Render.Post event) {
        if(!Boolean.getBoolean("mca.visualValidation") || event.getScreen()!=preview || reloading || captured || ++frames<3) return;
        try {
            if(type()==4 && frames==3) { for (int i=0;i<50;i++) preview.mouseScrolled(preview.width/2.0,80.0,-100); return; }
            var client=Minecraft.getInstance();var graphics=event.getGuiGraphics();
            AbstractContainerScreen<?> screen=preview instanceof MenuPreview wrapper ? wrapper.delegate : null;
            if(screen!=null) {
                var hover=new ScreenEvent.Render.Post(screen,graphics,screen.getGuiLeft()+162,screen.getGuiTop()+10,event.getPartialTick());
                Class.forName("com.guilh.mcaevcompat.client.OperationStatusOverlay").getMethod("render",ScreenEvent.Render.Post.class).invoke(null,hover);
            }
            graphics.flush();
            String kind=type()>=6 ? String.format("guide-page%02d",type()-4) : new String[]{"guide","journal-long","journal-empty","ev-infertile","journal-scrolled","journal-normal"}[type()];
            String file=LANGUAGES[scenario/PER_LANGUAGE]+"-scale"+scale()+"-"+kind+".png";
            try(var png=Screenshot.takeScreenshot(client.getMainRenderTarget())) { png.writeToFile(OUTPUT.resolve(file)); }
            Files.writeString(OUTPUT.resolve("captures.csv"),file+","+preview.width+","+preview.height+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            if(type()>=6) {
                var page=((ExtendedBookScreen)preview).getBook().getPage(type()-5);
                var method=page.getClass().getDeclaredMethod("selectLayout",net.minecraft.client.gui.Font.class);
                method.setAccessible(true);var layout=method.invoke(page,client.font);
                var scaleMethod=layout.getClass().getDeclaredMethod("scale");scaleMethod.setAccessible(true);
                var linesMethod=layout.getClass().getDeclaredMethod("lines");linesMethod.setAccessible(true);
                float fontScale=(Float)scaleMethod.invoke(layout);
                int lineCount=((List<?>)linesMethod.invoke(layout)).size();
                Files.writeString(OUTPUT.resolve("page-layout.csv"),file+","+fontScale+","+lineCount+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);
                if(lineCount*9*fontScale>126.01F) throw new IllegalStateException("Page overflows: "+file);
            }
            captured=true;
        } catch(Exception exception) { fail(exception); }
    }
    // Render the actual menu without ticking it: this screenshot fixture has no logged-in player.
    private static final class MenuPreview extends Screen {
        private final AbstractContainerScreen<?> delegate;
        MenuPreview(AbstractContainerScreen<?> delegate) { super(Component.empty()); this.delegate=delegate; }
        @Override protected void init() { delegate.init(minecraft,width,height); }
        @Override public void render(net.minecraft.client.gui.GuiGraphics graphics,int mouseX,int mouseY,float partialTick) {
            delegate.render(graphics,mouseX,mouseY,partialTick);
        }
    }
    private static void fail(Exception exception) { exception.printStackTrace();System.out.println("VISUAL_VALIDATION_FAILED "+exception);Minecraft.getInstance().stop(); }
}
