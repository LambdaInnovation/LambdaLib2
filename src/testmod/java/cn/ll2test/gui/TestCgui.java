package cn.ll2test.gui;

import cn.lambdalib2.cgui.CGuiScreen;
import cn.lambdalib2.cgui.Widget;
import cn.lambdalib2.cgui.component.DrawTexture;
import cn.lambdalib2.cgui.component.TextBox;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.render.font.IFont.FontAlign;
import cn.lambdalib2.render.font.IFont.FontOption;
import cn.lambdalib2.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import org.lwjgl.input.Keyboard;

public class TestCgui {

    @StateEventCallback
    private static void init(FMLInitializationEvent ev) {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    public static class EventHandler {

        @SubscribeEvent
        public void onClientTick(ClientTickEvent ev) {
            if (ev.phase != Phase.END)
                return;

            Keyboard.poll();
            if (Keyboard.isKeyDown(Keyboard.KEY_P) && Minecraft.getMinecraft().currentScreen == null) {
                Minecraft.getMinecraft().displayGuiScreen(new TestCgui().screen);
            }
        }

    }


    public CGuiScreen screen = new CGuiScreen();

    private TestCgui() {
        Widget root = new Widget(0, 0, 100, 100).centered()
            .addComponent(new DrawTexture()
                    .setColor(Colors.white())
                    .setTex(null));

        root.addWidget(new Widget(0, 0, 200, 100).centered()
                .addComponent(new TextBox(new FontOption(10, FontAlign.CENTER, Colors.fromRGBA32(0x1A44CEFF))).allowEdit()
                        .setContent("Hello World!"))
                .addComponent(new DrawTexture()));

        root.addWidget(new Widget(0, 100, 50, 50).centered()
            .addComponent(new DrawTexture(DrawTexture.MISSING).setUVRect(0, 0, 500, 500).setColor(Colors.white())));

        screen.getGui().addWidget(root);
    }

}
