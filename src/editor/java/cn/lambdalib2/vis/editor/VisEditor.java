package cn.lambdalib2.vis.editor;

import cn.lambdalib2.LambdaLib2;
import cn.lambdalib2.cgui.CGuiScreen;
import cn.lambdalib2.cgui.Widget;
import cn.lambdalib2.cgui.component.DrawTexture;
import cn.lambdalib2.cgui.component.TextBox;
import cn.lambdalib2.cgui.component.Tint;
import cn.lambdalib2.cgui.component.Transform.HeightAlign;
import cn.lambdalib2.cgui.event.FrameEvent;
import cn.lambdalib2.cgui.event.LeftClickEvent;
import cn.lambdalib2.cgui.event.LostFocusEvent;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.util.Colors;
import cn.lambdalib2.util.client.font.IFont.FontAlign;
import cn.lambdalib2.util.client.font.IFont.FontOption;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class VisEditor extends CGuiScreen  {

    public class ActivateEvent extends Event {
        public VisEditor getEditor() {
            return VisEditor.this;
        }
    }

    @FunctionalInterface
    interface MenuCallback {
        void handle(SubMenu menu);
    }

    class MenuBar extends Widget {
        static final int LEN = 30;
        static final int HEIGHT = 12;

        private DrawTexture tex;

        private int count;

        private Map<String, List<MenuCallback>> menuCallbacks = new HashMap<>();

        MenuBar() {
            transform.height = HEIGHT;

            tex = new DrawTexture().setTex(null);
            tex.color = Colors.fromFloatMono(0.1f);
            addComponent(tex);

            addComponent(
                Styles.newTextBox(
                    new FontOption(9, FontAlign.RIGHT, Colors.fromFloatMono(0.4f))
                )
                .setContent("VisEditor 0.2 dev   ")
            );


        }

        public void addButton(String name, Consumer<Widget> callback) {
            Widget button = new Widget(5 + (LEN + 5) * count, 0, LEN, HEIGHT);

            Tint tint = new Tint(Colors.fromFloatMono(0.1f), Colors.fromFloatMono(0.3f));
            TextBox text = Styles.newTextBox(new FontOption(12, FontAlign.CENTER));
            text.heightAlign = HeightAlign.CENTER;
            text.setContent(name);

            button.addComponents(tint, text);
            button.listen(LeftClickEvent.class, () -> callback.accept(button));

            addWidget(name, button);
            ++count;
        }

        public void addMenu(String name, MenuCallback callback) {
            List<MenuCallback> list = menuCallbacks.computeIfAbsent(name, key -> new ArrayList<>());
            if (list.isEmpty()) {
                addButton(name, widget -> {
                    SubMenu sub = new SubMenu();
                    list.forEach(it -> it.handle(sub));
                    sub.transform.setPos(widget.transform.x, widget.transform.y);
                    addWidget(sub);
                });
            }
            list.add(callback);
        }
    }

    private static final Logger log = LambdaLib2.log;

    private Widget menuContainer, menuHover;

    private MenuBar menuBar;

    private Widget root;

    public VisEditor() {
        initWidgets();
    }

    private void initWidgets() {
        menuContainer = new Widget();
        menuBar = new MenuBar();
        menuHover = new Widget(0, 0, width, 2);
        root = new Widget();

        menuHover.addComponent(new DrawTexture().setTex(null).setColor(Colors.fromFloatMono(0.1f)));
        menuHover.transform.doesDraw = false;
        menuHover.listen(FrameEvent.class, (w, evt) -> {
            if (evt.hovering) {
                menuHover.transform.doesDraw = false;
                menuBar.transform.doesDraw = true;
            }
        });

        root.transform.doesListenKey = false;

        menuContainer.addWidget(menuBar);
        menuContainer.addWidget(menuHover);

        gui.addWidget(root);
        gui.addWidget(menuContainer);

        menuBar.addMenu("View", menu -> {
            menu.addItem("Hide Menu", () -> {
                menuHover.transform.doesDraw = true;
                menuBar.transform.doesDraw = false;
            });
        });

        MinecraftForge.EVENT_BUS.post(new ActivateEvent());
    }

    public enum VisEditorStartup {
        INSTANCE;

        @StateEventCallback
        private static void init(FMLInitializationEvent ev) {
            MinecraftForge.EVENT_BUS.register(INSTANCE);
        }

        @SubscribeEvent
        public void onKey(KeyInputEvent event) {
            if (Keyboard.getEventKey() == Keyboard.KEY_L) {
                log.info("Starting vis editor");
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.currentScreen == null) {
                    mc.displayGuiScreen(new VisEditor());
                }
            }
        }
    }

}


// Commonly used widgets

class SubMenu extends Widget {

    private static final float HEIGHT = 10.0f;

    private float len = 30.0f;

    private List<Widget> itemList = new ArrayList<>();

    public SubMenu() {
        listen(LostFocusEvent.class, this::dispose);
    }

    public void addItem(String name, Runnable func) {
        Widget itemWidget = new Widget();
        itemWidget.transform.setSize(len, HEIGHT);

        TextBox text = Styles.newTextBox(new FontOption(10));
        text.content = name;
        text.heightAlign = HeightAlign.CENTER;
        len = Math.max(len, text.font.getTextWidth(name, text.option));

        Tint tint = new Tint();
        tint.idleColor = Colors.fromFloatMono(0.1f);
        tint.hoverColor = Colors.fromFloatMono(0.3f);

        itemWidget.addComponents(tint, text);
        itemWidget.listen(LeftClickEvent.class, func);
        itemWidget.transform.setPos(0, itemList.size() * HEIGHT);

        addWidget(itemWidget);
        itemList.add(itemWidget);
    }

    @Override
    public void onAdded() {
        super.onAdded();
        itemList.forEach(w -> {
            w.transform.width = len + 3;
            w.markDirty();
        });
        transform.width = len + 3;
        markDirty();
        getGui().gainFocus(this);
    }

}

