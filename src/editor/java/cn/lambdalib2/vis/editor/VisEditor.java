package cn.lambdalib2.vis.editor;

import cn.lambdalib2.LambdaLib2;
import cn.lambdalib2.cgui.CGui;
import cn.lambdalib2.cgui.CGuiScreen;
import cn.lambdalib2.cgui.Widget;
import cn.lambdalib2.cgui.component.DrawTexture;
import cn.lambdalib2.cgui.component.TextBox;
import cn.lambdalib2.cgui.component.TextBox.ConfirmInputEvent;
import cn.lambdalib2.cgui.component.Tint;
import cn.lambdalib2.cgui.component.Transform.HeightAlign;
import cn.lambdalib2.cgui.component.Transform.WidthAlign;
import cn.lambdalib2.cgui.event.*;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.util.Colors;
import cn.lambdalib2.util.Debug;
import cn.lambdalib2.util.client.font.IFont.FontAlign;
import cn.lambdalib2.util.client.font.IFont.FontOption;
import cn.lambdalib2.vis.editor.ObjectEditors.EditBox;
import cn.lambdalib2.vis.editor.Popup.ButtonData;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import static cn.lambdalib2.vis.editor.Styles.*;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
                newTextBox(
                    new FontOption(9, FontAlign.RIGHT, Colors.fromFloatMono(0.4f))
                )
                .setContent("VisEditor 0.2 dev   ")
            );
        }

        public void addButton(String name, Consumer<Widget> callback) {
            Widget button = new Widget(5 + (LEN + 5) * count, 0, LEN, HEIGHT);

            Tint tint = new Tint(Colors.fromFloatMono(0.1f), Colors.fromFloatMono(0.3f));
            TextBox text = newTextBox(new FontOption(12, FontAlign.CENTER));
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
                    sub.transform.setPos(widget.transform.x, widget.transform.y + widget.transform.height);
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
        menuHover = new Widget(0, 0, 0, 2);
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

        menuBar.addMenu("File", menu -> {
            menu.addItem("Work folders...", this::startEditWorkDirs);
        });

        menuBar.addMenu("View", menu -> {
            menu.addItem("Hide Menu", () -> {
                menuHover.transform.doesDraw = true;
                menuBar.transform.doesDraw = false;
            });

            menu.addItem("Toggle Cover", () -> drawBack = !drawBack);
        });

        MinecraftForge.EVENT_BUS.post(new ActivateEvent());
    }

    @Override
    public void drawScreen(int mx, int my, float w) {
        if (width != menuBar.transform.width) {
            updateWidthToScreen(menuBar);
            updateWidthToScreen(menuHover);
        }
        root.transform.setSize(width, height);
        super.drawScreen(mx, my, w);
    }

    private void updateWidthToScreen(Widget w) {
        w.transform.width = width;
        w.markDirty();
    }

    private void startEditWorkDirs() {
        ScreenCover cover = new ScreenCover(VisEditor.this);

        HierarchyTab window = new HierarchyTab(true, 0, 0, 150, 100, "Working Dirs", Window.STYLE_CLOSABLE) {
            @Override
            void rebuild() {
                elements = Arrays.stream(VisConfig.getWorkDirs())
                    .map(it -> new Element(it, elemTexture("folder")))
                    .collect(Collectors.toList());
                super.rebuild();
            }
        };
        window.listen(Window.CloseEvent.class, cover::dispose);
        window.initButton("Add directory", "add", w -> {
            ScreenCover cover2 = new ScreenCover(VisEditor.this);
            Window wndAskPath = new Window("Enter new path...", 0, 0, 100, 30, Window.STYLE_CLOSABLE);

            wndAskPath.listen(Window.CloseEvent.class, cover2::dispose);
            wndAskPath.transform.setCenteredAlign();

            Widget textArea = new Widget(0, 15, 80, 12);
            TextBox textBox = Styles.newTextBox(new FontOption(9, FontAlign.CENTER)).setContent("ENTER: Confirm");
            textArea.addComponent(textBox);

            EditBox input = new EditBox() {
                String str = "";

                {
                    transform.y = -5;
                    transform.width = 70;
                    transform.setCenteredAlign();
                }

                @Override
                String getDisplayValue() {
                    return str;
                }

                @Override
                void setValue(String content) {
                    List<String> workDirs = new ArrayList<>(Arrays.asList(VisConfig.getWorkDirs()));
                    if (new File(content).isDirectory() && !workDirs.contains(content)) {
                        workDirs.add(content);
                        VisConfig.setWorkDirs(workDirs.toArray(new String[0]));
                        window.rebuild();
                        cover2.dispose();
                    } else {
                        textBox.option.color.setColor(Styles.COLOR_ERROR);
                        textBox.content = "Invalid path";
                    }
                }
            };

            wndAskPath.addWidget(textArea);
            wndAskPath.addWidget(input);

            cover2.addWidget(wndAskPath);
            root.addWidget(cover2);
        });
        window.initButton("Remove directory", "remove", w -> {
            Element selected = window.getSelected();
            if (selected != null) {
                List<String> workDirs = new ArrayList<>(Arrays.asList(VisConfig.getWorkDirs()));
                workDirs.remove(selected.elementName);
                VisConfig.setWorkDirs(workDirs.toArray(new String[0]));
                window.rebuild();
            }
        });

        window.transform.setCenteredAlign();
        cover.addWidget(window);
        root.addWidget(cover);
    }

    void createFileWindow(boolean allowNewFile, String name, String buttonName,
                                  Predicate<File> buttonCallback, Runnable abortCallback) {
        Widget window = new FileWindow(this, allowNewFile, name, buttonName, buttonCallback, abortCallback);
        root.addWidget(window);
    }

    void notify(String msg, Runnable onConfirmed) {
        root.addWidget(new Popup(this, "Notification", msg,
            Collections.singletonList(new ButtonData("OK", onConfirmed))));
    }

    void confirm(String msg, Runnable onOK, Runnable onCancel) {
        root.addWidget(new Popup(this, "Notification", msg,
            Arrays.asList(
                new ButtonData("OK", onOK),
                new ButtonData("Cancel", onCancel)
            )));
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

class FileWindow extends ScreenCover {

    private final VisEditor editor;

    private final boolean allowNewFile;
    private final String name;
    private final String buttonName;

    private final Predicate<File> buttonCallback;
    private final Runnable abortCallback;

    private final List<File> allWorkDirs;

    private Optional<File> currentPath;

    private TextBox pathTellerText, pathInputText;

    private HierarchyTab tab;

    FileWindow(VisEditor _editor, boolean _allowNewFile, String _name, String _buttonName,
                                  Predicate<File> _buttonCallback, Runnable _abortCallback) {
        super(_editor);
        editor = _editor;

        allowNewFile = _allowNewFile;
        name = _name;
        buttonName = _buttonName;
        buttonCallback = _buttonCallback;
        abortCallback = _abortCallback;

        allWorkDirs = Arrays.stream(VisConfig.getWorkDirs())
            .map(File::new)
            .collect(Collectors.toList());

        updatePath(VisConfig.getCurrentDir().map(File::new).orElse(null));

        Widget pathTeller = new Widget(2, 1, 146, 10);
        pathTellerText = new TextBox();
        pathTeller.addComponent(monoTint(0.3f, 0.4f, false));
        pathTeller.addComponent(pathTellerText);

        Widget pathInput = new Widget(2, -1, 130, 10);
        pathInput.transform.alignHeight = HeightAlign.BOTTOM;
        pathInputText = newTextBox().allowEdit();
        pathInput.addComponent(new DrawTexture(null, Colors.fromFloatMono(0.3f)));
        pathInput.addComponent(pathInputText);

        pathTeller.listen(LeftClickEvent.class, () -> {
            SubMenu sn = new SubMenu();
            allWorkDirs.stream().filter(it -> Objects.equals(it, currentPath.orElse(null)))
                .forEach(dir -> {
                    sn.addItem(dir.getAbsolutePath(), () -> {
                        currentPath = Optional.of(dir);
                        tab.rebuild();
                        pathTellerText.setContent(dir.getAbsolutePath());
                    });
                });
            sn.transform.y = 10;
            sn.transform.alignWidth = WidthAlign.RIGHT;
            pathTeller.addWidget(sn);
        });

        pathInput.listen(ConfirmInputEvent.class, this::onConfirm);

        tab = new HierarchyTab(true, 0, 0, 150, 86, name, Window.STYLE_CLOSABLE) {

            final Element noPathElem = new Element("Path doesn't exist", Styles.elemTexture("folder_open"));
            final Element prevFolderElem = new Element("..", elemTexture("folder")) {
                {
                    listen(LeftClickEvent.class, 1, (w, e) -> {
                        if (findTab().getSelected() == this) {
                            updatePath(currentPath.get().getParentFile());
                            rebuild();
                        }
                    });
                }
            };

            String selectedPath = "";

            {
                listen(CloseEvent.class, () -> {
                    FileWindow.this.dispose();
                    abortCallback.run();
                });

                listArea.transform.x += 2;
                listArea.transform.width -= 2;

                transform.setCenteredAlign();
                body.transform.height += 12;

            }

            @Override
            public int getTopHeight() {
                return 12;
            }

            @Override
            void rebuild() {
                if (currentPath.isPresent() && currentPath.get().isDirectory()) {
                    File file = currentPath.get();
                    elements = Arrays.stream(Debug.assertNotNull(file.listFiles()))
                        .map(f -> {
                            Element elem = new Element(f.getName(), f.isDirectory() ? Styles.elemTexture("folder") : Styles.elemTexture("file"));
                            elem.listen(LeftClickEvent.class, () -> {
                                if (f.isDirectory() && getSelected() == elem) {
                                    updatePath(f);
                                    rebuild();
                                }
                                if (!f.isDirectory()) {
                                    if (pathInputText.content.equals(f.getName())) {
                                        onConfirm();
                                    }
                                    pathInputText.setContent(f.getName());
                                }
                            });
                            return elem;
                        })
                        .collect(Collectors.toList());
                    if (!allWorkDirs.contains(file)) {
                        elements.add(prevFolderElem);
                    }
                } else {
                    elements.clear();
                    elements.add(noPathElem);
                }
                super.rebuild();
            }
        };

        Widget buttonAct = Styles.newButton(-1, -2, 15, 8, buttonName);
        buttonAct.transform.alignHeight = HeightAlign.BOTTOM;
        buttonAct.transform.alignWidth = WidthAlign.RIGHT;
        buttonAct.listen(LeftClickEvent.class, this::onConfirm);

        Widget body = tab.body;
        body.addWidget(pathTeller);
        body.addWidget(pathInput);
        body.addWidget(buttonAct);

        addWidget(tab);
    }

    void updatePath(File newPath) {
        currentPath = Optional.ofNullable(newPath);
        String pathStr = currentPath.map(File::getAbsolutePath).orElse("<No active path>");
        pathTellerText.setContent(pathStr);
    }

    void onConfirm() {
        if (currentPath.isPresent()) {
            File newFile = new File(currentPath.get(), pathInputText.content);
            if (newFile.isFile() || allowNewFile) {
                if (buttonCallback.test(newFile)) {
                    dispose();
                }
            } else if (newFile.isDirectory()) {
                updatePath(newFile.getAbsoluteFile());
                pathInputText.setContent("");
                tab.rebuild();
            } else {
                editor.notify("Invalid file path " + pathInputText.content, () -> {});
            }
        } else {
            editor.notify("You must select a valid path", () -> {});
        }
    }

}

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

        TextBox text = newTextBox(new FontOption(10));
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

class ScreenCover extends Widget {

    CGuiScreen env;

    public ScreenCover(CGuiScreen env) {
        this(env, true);
    }

    public ScreenCover(CGuiScreen env, boolean blackout) {
        this.env = env;
        if (blackout) {
            addComponent(new DrawTexture()
                .setTex(null)
                .setColor(Colors.fromFloat(0, 0, 0, 0.3f)));
        }
        listen(RefreshEvent.class, this::updateSize);
    }

    private void updateSize() {
        transform.setSize(env.width, env.height);
    }

    @Override
    public void onAdded() {
        this.gainFocus();
    }

}

class Window extends Widget {
    static final int
        STYLE_CLOSABLE = 1 << 0,
        STYLE_MINIMIZABLE = 1 << 1,
        STYLE_DRAGGABLE = 1 << 2,
        STYLE_DEFAULT = STYLE_CLOSABLE | STYLE_MINIMIZABLE | STYLE_DRAGGABLE;

    class CloseEvent implements GuiEvent {}

    final Widget header, body;

    private int buttonCount = 0;

    public Window(String name, float x, float y, float width, float height) {
        this(name, x, y, width, height, STYLE_DEFAULT);
    }

    public Window(String name, float x, float y, float width, float height, int style) {
        super(x, y, width, height);

        header = new Widget(0, -10, width, 10)
            .addComponent(new DrawTexture(null, Colors.fromFloatMono(0.15f)))
            .addComponent(new TextBox(new FontOption(10)).setContent(" " + name).setHeightAlign(HeightAlign.CENTER));
        addWidget("Header", header);

        body = new Widget(0, 0, width, height)
            .addComponent(new DrawTexture(null, Colors.fromFloatMono(0.1f)));
        addWidget("Body", body);

        transform.doesListenKey = false;

        if ((style & STYLE_CLOSABLE) != 0) {
            addButton("close", w -> {
                transform.doesDraw = false;
                post(new CloseEvent());
            });
        }

        if ((style & STYLE_MINIMIZABLE) != 0) {
            ResourceLocation t1 = texture("buttons/minimize"), t2 = texture("buttons/maximize");
            addButton("minimize", w -> {
                DrawTexture dt = w.getComponent(DrawTexture.class);
                body.transform.doesDraw = !body.transform.doesDraw;
                dt.setTex(body.transform.doesDraw ? t1 : t2);
            });
        }

        if ((style & STYLE_DRAGGABLE) != 0) {
            header.listen(DragEvent.class, (w, e) -> {
                CGui gui = header.getGui();
                float ax = gui.getMouseX() - e.offsetX;
                float ay = gui.getMouseY() - e.offsetY;
                gui.moveWidgetToAbsPos(this, ax, ay + 10);
                markDirty();
            });
        }
    }

    public void resize(float width, float height) {
        body.transform.setSize(width, height);
        markDirty();
    }

    private void addButton(String name, Consumer<Widget> callback) {
        final float SIZE = 9;
        final float STEP = SIZE + 1;

        Widget btn = new Widget(-STEP * buttonCount - 1, -0.5f, SIZE, SIZE);
        btn.transform.alignWidth = WidthAlign.RIGHT;
        btn.listen(LeftClickEvent.class, (w, e) -> callback.accept(w));

        btn.addComponent(new DrawTexture(texture("buttons/" + name)));

        btn.addComponent(new Tint(Colors.fromFloatMono(0.7f), Colors.fromFloatMono(1f), true));

        header.addWidget(btn);
        ++buttonCount;
    }

}

class Popup extends ScreenCover {

    private static final int
        FONT_SIZE = 8,
        BUTTON_LEN = 18,
        BUTTON_STEP = BUTTON_LEN + 5;

    public static class ButtonData {
        public final String name;
        public final Runnable callback;

        public ButtonData(String name, Runnable callback) {
            this.name = name;
            this.callback = callback;
        }
    }

    private float strlen, buttonW;

    public Popup(CGuiScreen env, String header, String msg, List<ButtonData> buttons) {
        super(env);

        strlen = 10 + Styles.font.getTextWidth(msg, new FontOption(FONT_SIZE));
        buttonW = BUTTON_STEP * buttons.size() - 5;

        float width = Math.max(strlen, Math.max(80, buttonW));

        Window not = new Window(header, 0, 0, width, 45, 0);
        not.transform.setCenteredAlign();

        Widget textArea = new Widget(0, -10, width, 0);
        textArea.transform.setCenteredAlign();

        textArea.addComponent(newTextBox(new FontOption(FONT_SIZE, FontAlign.CENTER)).setContent(msg));

        for (int i = 0; i < buttons.size(); ++i) {
            ButtonData data = buttons.get(i);
            Widget button = newButton(-buttonW / 2 + i * BUTTON_STEP + BUTTON_LEN / 2, 10, BUTTON_LEN, 7, data.name);
            button.listen(LeftClickEvent.class, () -> {
                data.callback.run();
                Popup.this.dispose();;
            });
            button.transform.setCenteredAlign();
            not.addWidget(button);
        }

        not.addWidget(textArea);
        addWidget(not);
    }
}