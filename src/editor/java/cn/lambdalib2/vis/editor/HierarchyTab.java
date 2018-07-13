package cn.lambdalib2.vis.editor;

import cn.lambdalib2.cgui.Widget;
import cn.lambdalib2.cgui.component.*;
import cn.lambdalib2.cgui.component.DragBar.Axis;
import cn.lambdalib2.cgui.component.Transform.HeightAlign;
import cn.lambdalib2.cgui.component.Transform.WidthAlign;
import cn.lambdalib2.cgui.event.FrameEvent;
import cn.lambdalib2.cgui.event.GuiEvent;
import cn.lambdalib2.cgui.event.LeftClickEvent;
import cn.lambdalib2.util.Colors;
import cn.lambdalib2.util.client.font.IFont.FontAlign;
import cn.lambdalib2.util.client.font.IFont.FontOption;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

interface IHierarchyItem {
    void addElement(Element e);

    default int getLevel() {
        return 0;
    }
}

class Element extends Widget implements IHierarchyItem {

    private static final ResourceLocation
        TEX_MIN = Styles.texture("buttons/minimize"),
        TEX_MAX = Styles.texture("buttons/maximize");

    protected final float height;

    final List<Element> elements = new ArrayList<>();

    Consumer<Widget> onAddChildElement;

    protected final String elementName;

    protected final ResourceLocation icon;

    private final Widget iconArea, textArea;

    private final float _elementIconSz;

    private int _indent = 0;

    private boolean _folded = true;

    private boolean _init = false;

    private boolean _foldButtonInit = false;

    private IHierarchyItem _parentHierarchy;

    public Element(String name, ResourceLocation icon) {
        this(name, icon, 10);
    }

    public Element(String _name, ResourceLocation _icon, float _height) {
        elementName = _name;
        icon = _icon;
        height = _height;

        _elementIconSz = height * 0.8f;

        iconArea = new Widget();
        iconArea.addComponent(new DrawTexture(icon));

        textArea = createText();
    }

    // ACTIONS
    @Override
    public void addElement(Element e) {
        e.onAddedIntoHierarchy(this);
        elements.add(e);
        if (onAddChildElement != null) {
            onAddChildElement.accept(e);
        }
    }

    protected Widget createText() {
        Widget ret = new Widget();
        ret.transform.doesListenKey = true;
        ret.addComponent(new TextBox(new FontOption(0.9f * height)).setContent(elementName));
        return ret;
    }

    // EVENTS
    @Override
    protected void onAdded() {
        super.onAdded();
        if (!_init) {
            _init = true;

            HierarchyTab tab = findTab();
            transform.setSize(tab.transform.width, height);

            float indentOff = getIndentOffset();
            iconArea.transform.setPos(5 + indentOff, 0)
                .setSize(_elementIconSz, _elementIconSz);
            iconArea.transform.alignHeight = HeightAlign.CENTER;
            addWidget("Icon", iconArea);

            textArea.transform
                .setPos(18 + indentOff, 0)
                .setSize(transform.width - 25, height);
            addWidget(textArea);

            DrawTexture dt = new DrawTexture(null, Colors.fromFloat(0, 0, 0, 0));
            addComponent(dt);

            Color hov = Colors.fromFloatMono(0.4f),
                idle = Colors.fromFloatMono(0.12f);

            if (tab.doesRequireSelection()) {
                listen(LeftClickEvent.class, () -> tab.setSelected(this));
            }

            listen(FrameEvent.class, () -> {
                dt.color = tab.getSelected() == this ? hov : idle;
            });
        }
    }

    void onRebuild(ElementList list) {
        if (isFoldable()) {
            initFoldButton();
        }
        if (!_folded) {
            elements.forEach(e -> {
                list.addWidget(e);
                e.onRebuild(list);
            });
        }
    }

    // PROPERTIES
    @Override
    public int getLevel() {
        return _indent;
    }

    boolean isFoldable() {
        return elements.size() > 0;
    }

    HierarchyTab findTab() {
        Widget cur = getWidgetParent();
        while (cur != null && !(cur instanceof HierarchyTab)) {
            cur = cur.getWidgetParent();
        }
        return (HierarchyTab) cur;
    }

    // PRIVATE
    private float getIndentOffset() {
        return 2 * _indent;
    }

    private void initFoldButton() {
        if (!_foldButtonInit) {
            _foldButtonInit = true;

            Widget button = new Widget(-2, 0, _elementIconSz, _elementIconSz)
                .walign(WidthAlign.CENTER).halign(HeightAlign.CENTER);

            DrawTexture tex = new DrawTexture(TEX_MAX);
            button.addComponent(tex);

            Tint tint = Styles.monoTint(0.7f, 0.9f, true);
            button.addComponent(tint);

            button.listen(LeftClickEvent.class, () -> {
                _folded = !_folded;
                tex.setTex(_folded ? TEX_MAX : TEX_MIN);
                findTab().rebuild();
            });

            addWidget(button);
        }
    }

    private void onAddedIntoHierarchy(IHierarchyItem hier) {
        _indent = hier.getLevel() + 1;
        _parentHierarchy = hier;
    }
}

class HierarchyTab extends Window implements IHierarchyItem {

    class SelectionChangeEvent implements GuiEvent {
        final Element prev;
        final Element next;

        public SelectionChangeEvent(Element prev, Element next) {
            this.prev = prev;
            this.next = next;
        }
    }

    private static final FontOption HINT_OPTION = new FontOption(8, FontAlign.CENTER);

    private final int topHeight;

    private final List<Element> elements = new ArrayList<>();

    private final Widget listArea;

    private ElementList listCom;

    private Element selected;

    private Widget bar;

    private int buttonSz;


    public HierarchyTab(boolean hasButton,
                        float x, float y,
                        float w, float h,
                        String name) {
        this(hasButton, x, y, w, h, name, Window.STYLE_DEFAULT);
    }

    public HierarchyTab(boolean hasButton,
                        float x, float y,
                        float w, float h,
                        String name,
                        int style) {
        super(name, x, y, w, h, style);
        topHeight = hasButton ? 10 : 0;

        listArea = new Widget(0, topHeight, w, h - topHeight);
        listArea.addComponent(new DrawTexture(null, Colors.fromFloatMono(0.2f)));

        body.addWidget(listArea);
        body.listen(LeftClickEvent.class, () -> setSelected(null));

    }

    // ACTIONS
    @Override
    public void addElement(Element e) {
        elements.add(e);
    }

    public final void initButton(String hint, String icon, Consumer<Widget> callback) {
        final float SIZE = 8, STEP = SIZE + 2;

        Widget button = new Widget(2 + STEP * buttonSz, 1, SIZE, SIZE);
        button.addComponent(new DrawTexture(Styles.texture("buttons/" + icon)));
        button.addComponent(Styles.monoTint(0.7f, 0.9f, true));
        button.listen(FrameEvent.class, (w, e) -> {
            if (e.hovering) {
                glPushMatrix();
                glTranslatef(0, 0, 1);
                Styles.font.draw(hint, button.transform.width / 2, -5, HINT_OPTION);
                glPopMatrix();
            }
        });
        button.listen(LeftClickEvent.class, () -> callback.accept(button));

        body.addWidget(button);
        ++buttonSz;
    }

    // EVENTS
    @Override
    public void onAdded() {
        super.onAdded();
        rebuild();
    }

    final void rebuild() {
        float width = listArea.transform.width;

        // Clear current selection, but retain if still present
        if (!elements.contains(selected)) {
            setSelected(null);
        }

        ElementList oldList = listCom;
        if (oldList != null) {
            listArea.removeComponent(listCom);
        }

        if (bar != null) {
            bar.dispose();
        }

        listCom = new ElementList();
        elements.forEach(e -> {
            listCom.addWidget(e);
            e.onRebuild(listCom);
        });

        listArea.addComponent(listCom);

        if (oldList != null && listCom.shouldScroll()) {
            listCom.setProgress(oldList.getProgress());
        }

        // Check scroll bar
        float elementWidth;
        if (listCom.shouldScroll()) {
            final int BAR_LEN = 8;
            elementWidth = width - BAR_LEN;

            bar = new Widget(width - BAR_LEN, 0, BAR_LEN, listArea.transform.height * 0.3f);
            bar.addComponent(new Tint(Colors.fromFloatMono(0.3f), Colors.fromFloatMono(0.5f)));

            DragBar dragBar = new DragBar(Axis.Y, 0, listArea.transform.height - bar.transform.height);
            bar.addComponent(dragBar);
            bar.listen(DragBar.DraggedEvent.class, (w, ev) -> {
                listCom.setProgress((int) (listCom.getMaxProgress() * dragBar.getProgress()));
            });
            dragBar.setProgress((float) listCom.getProgress() / listCom.getMaxProgress());

            listArea.addWidget(bar);
        } else {
            elementWidth = width;
        }
        listCom.getSubWidgets().forEach(w -> {
            w.transform.width = elementWidth;
            w.markDirty();
        });
    }

    // PROPERTIES
    public Element getSelected() {
        return selected;
    }

    public void setSelected(Element selected) {
        Element prev = selected;
        this.selected = selected;
        post(new SelectionChangeEvent(prev, selected));
    }

    public boolean doesRequireSelection() {
        return true;
    }

    // PRIVATE

}
