package cn.lambdalib2.vis.editor;

import cn.lambdalib2.util.Colors;
import cn.lambdalib2.util.Debug;
import cn.lambdalib2.util.GameTimer;
import cn.lambdalib2.util.MathUtils;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector4f;
import scala.actors.threadpool.Arrays;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_ACTIVE_TEXTURE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.GL_SAMPLER_BINDING;
import static org.lwjgl.opengl.GL33.glBindSampler;

class ImFontTex {
    public byte[] bytes;
    public int width, height;

    public ImFontTex(byte[] bytes, int width, int height) {
        this.bytes = bytes;
        this.width = width;
        this.height = height;
    }
}

class ImDrawCmd {
    public int elemCount;
    public int textureID;
    public float clipX, clipY, clipW, clipH;

    public boolean isUserCallback;
    public int userCallbackID;

    public ImDrawCmd(int elemCount, float clipX, float clipY, float clipW, float clipH, int textureID) {
        this.elemCount = elemCount;
        this.clipX = clipX;
        this.clipY = clipY;
        this.clipW = clipW;
        this.clipH = clipH;
        this.textureID = textureID;
    }

    public ImDrawCmd(int userCallbackID) {
        isUserCallback = true;
        this.userCallbackID = userCallbackID;
    }
}

class ImDrawVert {
    public static final int BYTES = 5 * 4;

    public float x, y;
    public float u, v;
    public int col;

    public ImDrawVert(float x, float y, float u, float v, int col) {
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
        this.col = col;
    }

    public void put(ByteBuffer buf) {
        buf.putFloat(x);
        buf.putFloat(y);
        buf.putFloat(u);
        buf.putFloat(v);
        buf.putInt(col);
    }
}

class ImDrawList {
    public ImDrawCmd[] cmdBuffer;
    public int[] idxBuffer;
    public ImDrawVert[] vtxBuffer;
    public int flags;

    public ImDrawList(ImDrawCmd[] cmdBuffer, int[] idxBuffer, ImDrawVert[] vtxBuffer, int flags) {
        this.cmdBuffer = cmdBuffer;
        this.idxBuffer = idxBuffer;
        this.vtxBuffer = vtxBuffer;
        this.flags = flags;
    }
}

class ImDrawData
{
    public List<ImDrawList> cmdLists = new ArrayList<>();
    public float dispX, dispY;
    public float dispW, dispH;

    @SuppressWarnings("unchecked")
    public ImDrawData(ImDrawList[] cmdLists, float dispX, float dispY, float dispW, float dispH) {
        this.cmdLists.addAll(Arrays.asList(cmdLists));
        this.dispX = dispX;
        this.dispY = dispY;
        this.dispW = dispW;
        this.dispH = dispH;
    }

    public void scaleClipRects(float sx, float sy) {
        for (int i = 0; i < cmdLists.size(); ++i) {
            ImDrawList list = cmdLists.get(i);
            for (int cmd_i = 0; cmd_i < list.cmdBuffer.length; ++cmd_i) {
                ImDrawCmd cmd = list.cmdBuffer[cmd_i];
                cmd.clipX *= sx;
                cmd.clipY *= sy;
                cmd.clipW *= sx;
                cmd.clipH *= sy;
            }
        }
    }
}

public class ImGui {
    public static final int
        WindowFlags_None                   = 0,
        WindowFlags_NoTitleBar             = 1 << 0,   // Disable title-bar
        WindowFlags_NoResize               = 1 << 1,   // Disable user resizing with the lower-right grip
        WindowFlags_NoMove                 = 1 << 2,   // Disable user moving the window
        WindowFlags_NoScrollbar            = 1 << 3,   // Disable scrollbars (window can still scroll with mouse or programatically)
        WindowFlags_NoScrollWithMouse      = 1 << 4,   // Disable user vertically scrolling with mouse wheel. On child window, mouse wheel will be forwarded to the parent unless NoScrollbar is also set.
        WindowFlags_NoCollapse             = 1 << 5,   // Disable user collapsing window by double-clicking on it
        WindowFlags_AlwaysAutoResize       = 1 << 6,   // Resize every window to its content every frame
        WindowFlags_NoBackground           = 1 << 7,   // Disable drawing background color (WindowBg, etc.) and outside border. Similar as using SetNextWindowBgAlpha(0.0f).
        WindowFlags_NoSavedSettings        = 1 << 8,   // Never load/save settings in .ini file
        WindowFlags_NoMouseInputs          = 1 << 9,   // Disable catching mouse, hovering test with pass through.
        WindowFlags_MenuBar                = 1 << 10,  // Has a menu-bar
        WindowFlags_HorizontalScrollbar    = 1 << 11,  // Allow horizontal scrollbar to appear (off by default). You may use SetNextWindowContentSize(ImVec2(width,0.0f)); prior to calling Begin() to specify width. Read code in imgui_demo in the "Horizontal Scrolling" section.
        WindowFlags_NoFocusOnAppearing     = 1 << 12,  // Disable taking focus when transitioning from hidden to visible state
        WindowFlags_NoBringToFrontOnFocus  = 1 << 13,  // Disable bringing window to front when taking focus (e.g. clicking on it or programatically giving it focus)
        WindowFlags_AlwaysVerticalScrollbar= 1 << 14,  // Always show vertical scrollbar (even if ContentSize.y < Size.y)
        WindowFlags_AlwaysHorizontalScrollbar=1<< 15,  // Always show horizontal scrollbar (even if ContentSize.x < Size.x)
        WindowFlags_AlwaysUseWindowPadding = 1 << 16,  // Ensure child windows without border uses style.WindowPadding (ignored by default for non-bordered child windows, because more convenient)
        WindowFlags_NoNavInputs            = 1 << 18,  // No gamepad/keyboard navigation within the window
        WindowFlags_NoNavFocus             = 1 << 19,  // No focusing toward this window with gamepad/keyboard navigation (e.g. skipped by CTRL+TAB)
        WindowFlags_UnsavedDocument        = 1 << 20,  // Append '*' to title without affecting the ID, as a convenience to avoid using the ### operator. When used in a tab/docking context, tab is selected on closure and closure is deferred by one frame to allow code to cancel the closure (with a confirmation popup, etc.) without flicker.
        WindowFlags_NoNav                  = WindowFlags_NoNavInputs | WindowFlags_NoNavFocus,
        WindowFlags_NoDecoration           = WindowFlags_NoTitleBar | WindowFlags_NoResize | WindowFlags_NoScrollbar | WindowFlags_NoCollapse,
        WindowFlags_NoInputs               = WindowFlags_NoMouseInputs | WindowFlags_NoNavInputs | WindowFlags_NoNavFocus;

    public static int
        TreeNodeFlags_None                 = 0,
        TreeNodeFlags_Selected             = 1 << 0,   // Draw as selected
        TreeNodeFlags_Framed               = 1 << 1,   // Full colored frame (e.g. for CollapsingHeader)
        TreeNodeFlags_AllowItemOverlap     = 1 << 2,   // Hit testing to allow subsequent widgets to overlap this one
        TreeNodeFlags_NoTreePushOnOpen     = 1 << 3,   // Don't do a TreePush() when open (e.g. for CollapsingHeader) = no extra indent nor pushing on ID stack
        TreeNodeFlags_NoAutoOpenOnLog      = 1 << 4,   // Don't automatically and temporarily open node when Logging is active (by default logging will automatically open tree nodes)
        TreeNodeFlags_DefaultOpen          = 1 << 5,   // Default node to be open
        TreeNodeFlags_OpenOnDoubleClick    = 1 << 6,   // Need double-click to open node
        TreeNodeFlags_OpenOnArrow          = 1 << 7,   // Only open when clicking on the arrow part. If TreeNodeFlags_OpenOnDoubleClick is also set, single-click arrow or double-click all box to open.
        TreeNodeFlags_Leaf                 = 1 << 8,   // No collapsing, no arrow (use as a convenience for leaf nodes).
        TreeNodeFlags_Bullet               = 1 << 9,   // Display a bullet instead of arrow
        TreeNodeFlags_FramePadding         = 1 << 10,  // Use FramePadding (even for an unframed text node) to vertically align text baseline to regular widget height. Equivalent to calling AlignTextToFramePadding().
        TreeNodeFlags_CollapsingHeader     = TreeNodeFlags_Framed | TreeNodeFlags_NoTreePushOnOpen | TreeNodeFlags_NoAutoOpenOnLog;

    static {
        try {
            URL res = ImGui.class.getResource("/imgui_64.dll");
            File f = Paths.get(res.toURI()).toFile();
            System.load(f.getAbsolutePath());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // Window/Input management
    private static boolean _init;

    private static double _lastTime = .0d;

    private static boolean[] _mouseDown = new boolean[5];

    private static boolean[] _keysDown = new boolean[512];

    private static Vector2f _displayFBScale = new Vector2f();

    // Render states
    private static int g_ShaderHandle;
    private static int g_VertHandle;
    private static int g_FragHandle;
    private static int g_FontTexture;

    private static int g_VboHandle;
    private static int g_ElementsHandle;

    private static int g_AttribLocationTex;
    private static int g_AttribLocationProjMtx;
    private static int g_AttribLocationPosition;
    private static int g_AttribLocationUV;
    private static int g_AttribLocationColor;

    // Main

    public static void newFrame(float dWheel, char[] inputChars) {
        if (!_init) {
            createContext();
            _init = true;
        }

        if (g_FontTexture == 0) {
            createDeviceObjects();
        }

        float dw, dh;
        dw = Display.getWidth();
        dh = Display.getHeight();

        float w, h;
        w = dw; h = dh;

        double t = GameTimer.getAbsTime();
        float dt = (float) MathUtils.clampd(0, 0.33, t - _lastTime);
        _lastTime = t;

        float mx = Mouse.getX();
        float my = dh - Mouse.getY();

        _displayFBScale.set(w / dw, h / dh);

        for (int i = 0; i < 5; ++i) {
            _mouseDown[i] = Mouse.isButtonDown(i);
        }

        boolean keyCtrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
        boolean keyShift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
        boolean keyAlt = Keyboard.isKeyDown(Keyboard.KEY_LMENU);
        boolean keySuper = Keyboard.isKeyDown(Keyboard.KEY_LMETA);

        for (int i = 0; i < 256; ++i) {
            _keysDown[i] = Keyboard.isKeyDown(i);
        }

        nFillInput(dw, dh, dt,
            mx, my,
            _mouseDown,
            dWheel,
            keyCtrl, keyShift, keyAlt, keySuper,
            _keysDown,
            inputChars);

        nNewFrame();
    }

    public static void render() {
        ImDrawData drawData = nRender();
        renderImpl(drawData);
        _userCallbacks.clear();
    }

    // Debug, Demo, Information

    public static boolean showDemoWindow(boolean show) {
        return nShowDemoWindow(show);
    }

    // Windows
    public static void begin(String name) {
        begin(name, 0);
    }

    public static void begin(String name, int flags) {
        nBegin2(name, flags);
    }

    public static boolean begin(String name, boolean open) {
        return begin(name, open, 0);
    }

    public static boolean begin(String name, boolean open, int flags) {
        return nBegin(name, open, flags);
    }

    private static native void nBegin2(String name, int flags);

    private static native boolean nBegin(String name, boolean open, int flags);

    public static void end() {
        nEnd();
    }

    private static native void nEnd();

    public static boolean beginChild(String str_id) {
        return beginChild(str_id, new Vector2f(0, 0));
    }

    public static boolean beginChild(String str_id, Vector2f size) {
        return beginChild(str_id, size, false);
    }

    public static boolean beginChild(String str_id, Vector2f size, boolean border) {
        return beginChild(str_id, size, border, 0);
    }

    public static boolean beginChild(String str_id, Vector2f size, boolean border, int flags) {
        return nBeginChild(str_id, size.x, size.y, border, flags);
    }

    private static native boolean nBeginChild(String str_id, float sizeX, float sizeY, boolean border, int flags);

    public static void endChild() {
        nEndChild();
    }

    private static native void nEndChild();

    // Cursor / Layout
    public static void separator() {
        nSeparator();
    }

    private static native void nSeparator();

    public static void sameLine() {
        nSameLine();
    }

    private static native void nSameLine();

    public static void newLine() {
        nNewLine();
    }

    private static native void nNewLine();

    public static void spacing() {
        nSpacing();
    }

    private static native void nSpacing();

    public static void beginGroup() {
        nBeginGroup();
    }

    private static native void nBeginGroup();

    public static void endGroup() {
        nEndGroup();
    }

    private static native void nEndGroup();

    public static void pushItemWidth(float width) {
        nPushItemWidth(width);
    }

    private static native void nPushItemWidth(float width);

    public static void popItemWidth() {
        nPopItemWidth();
    }

    private static native void nPopItemWidth();

    public static void setCursorPosX(float x) {
        nSetCursorPosX(x);
    }

    private static native void nSetCursorPosX(float x);

    public static Vector2f getCursorPos() {
        float[] v = nGetCursorPos();
        return new Vector2f(v[0], v[1]);
    }

    private static native float[] nGetCursorPos();

    // ID stack/scopes
    
    public static void pushID(String id) {
        nPushID(id);
    }
    
    public static void pushID(int int_id) {
        nPushID2(int_id);
    }

    private static native void nPushID(String id);
    private static native void nPushID2(int intID);
    
    public static void popID() {
        nPopID();
    }

    private static native void nPopID();
    
    // Widgets: Text
    
    public static void text(String s, Object... fmt) {
        nText(String.format(s, fmt));
    }

    private static native void nText(String s);
    
    public static void textColored(Color c, String str, Object... fmt) {
        nTextColored(Colors.toRGBA32(c), String.format(str, fmt));
    }

    private static native void nTextColored(int c, String s);

    public static void textWrapped(String s, Object... fmt) {
        nTextWrapped(String.format(s, fmt));
    }

    private static native void nTextWrapped(String s);
    
    public static void labelText(String label, String s, Object... fmt) {
        nLabelText(label, String.format(s, fmt));
    }

    private static native void nLabelText(String l, String s);
    
    public static void bulletText(String s, Object... fmt) {
        nBulletText(String.format(s, fmt));
    }

    private static native void nBulletText(String s);
    
    // Widgets: Main

    public static boolean button(String label) {
        return button(label, new Vector2f(.0f, .0f));
    }

    public static boolean button(String label, Vector2f size) {
        return nButton(label, size.x, size.y);
    }

    private static native boolean nButton(String label, float sizeX, float sizeY);

    public static boolean arrowButton(String id, ImGuiDir dir) {
        return nArrowButton(id, dir.ordinal());
    }

    private static native boolean nArrowButton(String id, int dir);

    public static void image(int textureID, Vector2f size) {
        image(textureID, size, new Vector2f(0, 0), new Vector2f(1, 1));
    }

    public static void image(int textureID, Vector2f size, Vector2f uv0, Vector2f uv1) {
        image(textureID, size, uv0, uv1, Colors.white());
    }

    public static void image(int textureID, Vector2f size, Vector2f uv0, Vector2f uv1, Color tintColor) {
        image(textureID, size, uv0, uv1, tintColor, Colors.white());
    }

    public static void image(int textureID, Vector2f size, Vector2f uv0, Vector2f uv1, Color tintColor, Color borderColor) {
        nImage(textureID, size.x, size.y, uv0.x, uv0.y, uv1.x, uv1.y, Colors.toRGBA32(tintColor), Colors.toRGBA32(borderColor));
    }

    private static native void nImage(int textureID, float sizex, float sizey,
        float u0, float v0, float u1, float v1, int tintColor, int borderColor);

    public static boolean imageButton(int textureID, Vector2f size) {
        return imageButton(textureID, size, new Vector2f(0, 0), new Vector2f(1, 1), -1);
    }

    public static boolean imageButton(int textureID, Vector2f size, Vector2f uv0, Vector2f uv1, int framePadding) {
        return imageButton(textureID, size, uv0, uv1, framePadding, Colors.black(), Colors.white());
    }

    public static boolean imageButton(int textureID, Vector2f size, Vector2f uv0, Vector2f uv1, int framePadding, Color bgCol, Color tintCol) {
        return nImageButton(textureID, size.x, size.y, uv0.x, uv0.y, uv1.x, uv1.y, framePadding, Colors.toRGBA32(bgCol), Colors.toRGBA32(tintCol));
    }

    private static native boolean nImageButton
        (int textureID, float sx, float sy, float u0, float v0, float u1,  float v1, int framePadding, int bgCol, int tintCol);

    public static boolean checkbox(String label, boolean v) {
        return nCheckbox(label, v);
    }

    private static native boolean nCheckbox(String label, boolean v);

    public static boolean radioButton(String label, boolean active) {
        return nRadioButton(label, active);
    }

    private static native boolean nRadioButton(String label, boolean active);

    public static void bullet() {
        nBullet();
    }

    private static native void nBullet();

    // Widget: Combo Box
    public static boolean beginCombo(String label, String previewValue) {
        return beginCombo(label, previewValue, 0);
    }

    // Widget: Combo Box
    public static boolean beginCombo(String label, String previewValue, int flags) {
        return nBeginCombo(label, previewValue, flags);
    }

    private static native boolean nBeginCombo(String label, String previewValue, int flags);
    
    public static void endCombo() {
        nEndCombo();
    }

    private static native void nEndCombo();

    public static int combo(String label, int currentItem, String[] items) {
        return nCombo(label, currentItem, items);
    }

    private static native int nCombo(String label, int currentItem, String[] items);
    
    // Widget: Drags


    // Widget: Sliders
    public static float sliderFloat(String label, float v, float vMin, float vMax) {
        return sliderFloat(label, v, vMin, vMax, "%.3f");
    }

    public static float sliderFloat(String label, float v, float vMin, float vMax, String format) {
        return sliderFloat(label, v, vMin, vMax, format, 1.0f);
    }

    public static float sliderFloat(String label, float v, float vMin, float vMax, String format, float pwr) {
        return nSliderFloat(label, v, vMin, vMax, format, pwr);
    }

    private static native float nSliderFloat(String label, float v, float vMin, float vMax, String format, float pwr);

    public static void sliderVector2(String label, Vector2f v, float vMin, float vMax) {
        float[] arr = new float[] { v.x, v.y };
        sliderFloat2(label, arr, vMin, vMax);
        v.x = arr[0]; v.y = arr[1];
    }

    public static void sliderFloat2(String label, float[] v, float vMin, float vMax) {
        sliderFloat2(label, v, vMin, vMax, "%.3f");
    }

    public static void sliderFloat2(String label, float[] v, float vMin, float vMax, String format) {
        sliderFloat2(label, v, vMin, vMax, format, 1.0f);
    }

    public static void sliderFloat2(String label, float[] v, float vMin, float vMax, String format, float pwr) {
        nSliderFloat2(label, v, vMin, vMax, format, pwr);
    }

    public static native void nSliderFloat2(String label, float[] v, float vMin, float vMax, String format, float pwr);

    public static void sliderFloat3(String label, float[] v, float vMin, float vMax) {
        sliderFloat3(label, v, vMin, vMax, "%.3f");
    }

    public static void sliderFloat3(String label, float[] v, float vMin, float vMax, String format) {
        sliderFloat3(label, v, vMin, vMax, format, 1.0f);
    }

    public static void sliderFloat3(String label, float[] v, float vMin, float vMax, String format, float pwr) {
        nSliderFloat3(label, v, vMin, vMax, format, pwr);
    }

    private static native void nSliderFloat3(String label, float[] v, float vMin, float vMax, String format, float pwr);

    public static void sliderFloat4(String label, float[] v, float vMin, float vMax) {
        sliderFloat4(label, v, vMin, vMax, "%.3f");
    }

    public static void sliderFloat4(String label, float[] v, float vMin, float vMax, String format) {
        sliderFloat4(label, v, vMin, vMax, format, 1.0f);
    }

    public static void sliderFloat4(String label, float[] v, float vMin, float vMax, String format, float pwr) {
        nSliderFloat4(label, v, vMin, vMax, format, pwr);
    }

    private static native void nSliderFloat4(String label, float[] v, float vMin, float vMax, String format, float pwr);

    public static float sliderAngle(String label, float rad) {
        return sliderAngle(label, rad, -360f, 360f);
    }

    public static float sliderAngle(String label, float rad, float degMin, float degMax) {
        return sliderAngle(label, rad, degMin, degMax, "%.0f deg");
    }

    public static float sliderAngle(String label, float rad, float degMin, float degMax, String format)  {
        return nSliderAngle(label, rad, degMin, degMax, format);
    }

    private static native float nSliderAngle(String label, float rad, float degMin, float degMax, String format);

    public static int sliderInt(String label, int v, int vMin, int vMax) {
        return nSliderInt(label, v, vMin, vMax);
    }

    private static native int nSliderInt(String label, int v, int vMin, int vMax);

    // Widget: sliders

    public static String inputText(String label, String text) {
        return inputText(label, text, 0);
    }

    public static String inputText(String label, String text, int flags) {
        return nInputText(label, text, flags);
    }

    private static native String nInputText(String label, String text, int flags);

    public static String inputTextMultiline(String label, String text) {
        return inputTextMultiline(label, text, new Vector2f(0, 0), 0);
    }

    public static String inputTextMultiline(String label, String text, Vector2f size, int flags) {
        return nInputTextMultiline(label, text, size.x, size.y, flags);
    }

    private static native String nInputTextMultiline(String label, String text, float sx, float sy, int flags);

    public static float inputFloat(String label, float v) {
        return inputFloat(label, v, 0);
    }

    public static float inputFloat(String label, float v, int extraFlags) {
        return inputFloat(label, v, "%.3f", extraFlags);
    }

    public static float inputFloat(String label, float v, String format, int extraFlags) {
        return nInputFloat(label, v, format, extraFlags);
    }

    private static native float nInputFloat(String label, float v, String format, int extraFlags);

    public static void inputVector2(String label, Vector2f v) {
        inputVector2(label, v, 0);
    }

    public static void inputVector2(String label, Vector2f v, int extraFlags) {
        inputVector2(label, v, "%.3f", extraFlags);
    }

    public static void inputVector2(String label, Vector2f v, String fmt, int extraFlags) {
        float[] arr = new float[] { v.x, v.y };
        inputFloat2(label, arr, fmt, extraFlags);
        v.x = arr[0];
        v.y = arr[1];
    }

    public static void inputFloat2(String label, float[] v) {
        inputFloat2(label, v, 0);
    }

    public static void inputFloat2(String label, float[] v, int extraFlags) {
        inputFloat2(label, v, "%.3f", extraFlags);
    }

    public static void inputFloat2(String label, float[] v, String format, int extraFlags) {
        nInputFloat2(label, v, format, extraFlags);
    }

    private static native void nInputFloat2(String label, float[] v, String format, int extraFlags);

    public static void inputFloat3(String label, float[] v) {
        inputFloat3(label, v, 0);
    }

    public static void inputFloat3(String label, float[] v, int extraFlags) {
        inputFloat3(label, v, "%.3f", extraFlags);
    }

    public static void inputFloat3(String label, float[] v, String format, int extraFlags) {
        nInputFloat3(label, v, format, extraFlags);
    }

    private static native void nInputFloat3(String label, float[] v, String format, int extraFlags);

    public static void inputFloat4(String label, float[] v) {
        inputFloat4(label, v, 0);
    }

    public static void inputFloat4(String label, float[] v, int extraFlags) {
        inputFloat4(label, v, "%.3f", extraFlags);
    }

    public static void inputFloat4(String label, float[] v, String format, int extraFlags) {
        nInputFloat4(label, v, format, extraFlags);
    }

    private static native void nInputFloat4(String label, float[] v, String format, int extraFlags);

    public static int inputInt(String label, int v) {
        return inputInt(label, v, 0);
    }

    public static int inputInt(String label, int v, int extraFlags) {
        return nInputInt(label, v, extraFlags);
    }

    private static native int nInputInt(String label, int v, int extraFlags);

    public static void inputInt2(String label, int[] v) {
        inputInt2(label, v, 0);
    }

    public static void inputInt2(String label, int[] v, int extraFlags) {
        nInputInt2(label, v, extraFlags);
    }

    private static native void nInputInt2(String label, int[] v, int extraFlags);

    public static void inputInt3(String label, int[] v) {
        inputInt3(label, v, 0);
    }

    public static void inputInt3(String label, int[] v, int extraFlags) {
        nInputInt3(label, v, extraFlags);
    }

    private static native void nInputInt3(String label, int[] v, int extraFlags);

    public static void inputInt4(String label, int[] v) {
        inputInt4(label, v, 0);
    }

    public static void inputInt4(String label, int[] v, int extraFlags) {
        nInputInt4(label, v, extraFlags);
    }

    private static native void nInputInt4(String label, int[] v, int extraFlags);

    public static double inputDouble(String label, double v) {
        return inputDouble(label, v, 0);
    }

    public static double inputDouble(String label, double v, int extraFlags) {
        return nInputDouble(label, v, extraFlags);
    }

    private static native double nInputDouble(String label, double v, int extraFlags);
    
    // Widgets: Color editor/picker
    public static void colorEdit4(String label, Color color) {
        colorEdit4(label, color, 0);
    }

    public static void colorEdit4(String label, Color color, int flags) {
        int c = nColorEdit4(label, Colors.toRGBA32(color), flags);
        Color cc = Colors.fromRGBA32(c);
        color.setColor(cc);
    }

    private static native int nColorEdit4(String label, int color, int flags);

    public static void colorButton(String descID, Color color) {
        colorButton(descID, color, 0);
    }

    public static void colorButton(String descID, Color color, int flags) {
        colorButton(descID, color, flags, new Vector2f(0, 0));
    }

    public static void colorButton(String descID, Color color, int flags, Vector2f size) {
        nColorButton(descID, Colors.toRGBA32(color), flags, size.x, size.y);
    }

    private static native void nColorButton(String descID, int color, int flags, float sx, float sy);

    // Widgets: Trees

    public static boolean treeNode(String label, Object ...fmt) {
        return nTreeNode(String.format(label, fmt));
    }

    public static boolean treeNodeEx(String label, int flags) {
        return nTreeNodeEx(label, flags);
    }

    private static native boolean nTreeNode(String label);

    private static native boolean nTreeNodeEx(String label, int flags);

    public static void treePop() {
        nTreePop();
    }

    private static native void nTreePop();

    public static boolean collapsingHeader(String label) {
        return collapsingHeader(label, 0);
    }

    public static boolean collapsingHeader(String label, int flags) {
        return nCollapsingHeader(label, flags);
    }

    private static native boolean nCollapsingHeader(String label,  int flags);

    public static boolean beginMainMenuBar() {
        return nBeginMainMenuBar();
    }

    private static native boolean nBeginMainMenuBar();

    public static void endMainMenubar() {
        nEndMainMenuBar();
    }

    private static native void nEndMainMenuBar();

    public static boolean beginMenuBar() {
        return nBeginMenuBar();
    }

    private static native boolean nBeginMenuBar();

    public static void endMenuBar() {
        nEndMenuBar();
    }

    public static boolean beginMenu(String label) {
        return beginMenu(label, true);
    }

    public static boolean beginMenu(String label, boolean enabled) {
        return nBeginMenu(label, enabled);
    }

    private static native boolean nBeginMenu(String label, boolean enabled);

    public static void endMenu() {
        nEndMenu();
    }

    private static native void nEndMenu();

    private static native void nEndMenuBar();

    public static boolean menuItem(String label) {
        return menuItem(label, true);
    }

    public static boolean menuItem(String label, boolean enabled) {
        return nMenuItem(label, enabled);
    }

    private static native boolean nMenuItem(String label, boolean enabled);

    public static boolean menuItem(String label, ImBoolRef selected, boolean enabled) {
        if (selected == null)
            return menuItem(label, enabled);
        return nMenuItem2(label, selected, enabled);
    }

    private static native boolean nMenuItem2(String label, ImBoolRef selected, boolean enabled);

    private static final List<Boolean> changeCheckStack = new ArrayList<>();

    public static void beginChangeCheck() {
        changeCheckStack.add(false);
    }

    public static boolean endChangeCheck() {
        if (changeCheckStack.size() == 0) {
            throw new RuntimeException("ChangeCheck stack is empty!!");
        }

        boolean ret = changeCheckStack.get(changeCheckStack.size() - 1);
        changeCheckStack.remove(changeCheckStack.size() - 1);
        return ret;
    }

    // Windows Utilities

    public static Vector4f getWindowRect() {
        float[] arr = nGetWindowRect();
        return new Vector4f(arr[0], arr[1], arr[2], arr[3]);
    }

    private static native float[] nGetWindowRect();

    public static boolean isItemClicked() {
        return isItemClicked(0);
    }

    public static boolean isItemClicked(int btn) {
        return nIsItemClicked(btn);
    }

    private static native boolean nIsItemClicked(int btn);

    // Advanced

    private static final List<Runnable> _userCallbacks = new ArrayList<>();

    public static void addUserCallback(Runnable callback) {
        int ix = _userCallbacks.size();
        _userCallbacks.add(callback);

        nAddUserCallback(ix);
    }

    private static native void nAddUserCallback(int ix);

    //

    private static void createDeviceObjects() {
        // Backup GL state
        int last_texture, last_array_buffer, last_vertex_array;
        last_texture = glGetInteger(GL_TEXTURE_BINDING_2D);
        last_array_buffer = glGetInteger(GL_ARRAY_BUFFER_BINDING);
        last_vertex_array = glGetInteger(GL_VERTEX_ARRAY_BINDING);

        String version = "#version 330 core\n";
        String vertexSrc =
            version +
            "layout (location = 0) in vec2 Position;\n" +
            "layout (location = 1) in vec2 UV;\n" +
            "layout (location = 2) in vec4 Color;\n" +
            "uniform mat4 ProjMtx;\n" +
            "out vec2 Frag_UV;\n" +
            "out vec4 Frag_Color;\n" +
            "void main()\n" +
            "{\n" +
            "    Frag_UV = UV;\n" +
            "    Frag_Color = Color;\n" +
            "    gl_Position = ProjMtx * vec4(Position.xy,0,1);\n" +
            "}\n";

        String fragmentSrc =
            version +
            "in vec2 Frag_UV;\n" +
            "in vec4 Frag_Color;\n" +
            "uniform sampler2D Texture;\n" +
            "layout (location = 0) out vec4 Out_Color;\n" +
            "void main()\n" +
            "{\n" +
            "    Out_Color = Frag_Color * texture(Texture, Frag_UV.st);\n" +
            "}\n";

        // Create shaders
        g_VertHandle = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(g_VertHandle, vertexSrc);
        glCompileShader(g_VertHandle);
        checkShader(g_VertHandle, "vertex shader");

        g_FragHandle = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(g_FragHandle, fragmentSrc);
        glCompileShader(g_FragHandle);
        checkShader(g_FragHandle, "fragment shader");

        g_ShaderHandle = glCreateProgram();
        glAttachShader(g_ShaderHandle, g_VertHandle);
        glAttachShader(g_ShaderHandle, g_FragHandle);
        glLinkProgram(g_ShaderHandle);
        checkProgram(g_ShaderHandle, "shader program");

        g_AttribLocationTex = glGetUniformLocation(g_ShaderHandle, "Texture");
        g_AttribLocationProjMtx = glGetUniformLocation(g_ShaderHandle, "ProjMtx");
        g_AttribLocationPosition = glGetAttribLocation(g_ShaderHandle, "Position");
        g_AttribLocationUV = glGetAttribLocation(g_ShaderHandle, "UV");
        g_AttribLocationColor = glGetAttribLocation(g_ShaderHandle, "Color");

        // Create buffers
        g_VboHandle = glGenBuffers();
        g_ElementsHandle = glGenBuffers();

        createFontsTexture();

        // Restore modified GL state
        glBindTexture(GL_TEXTURE_2D, last_texture);
        glBindBuffer(GL_ARRAY_BUFFER, last_array_buffer);
        glBindVertexArray(last_vertex_array);
    }

    private static void createFontsTexture() {
        // Build texture atlas
        ImFontTex tex = nGetFontTexARGB32();

        // Upload texture to graphics system
        int last_texture;
        last_texture = glGetInteger(GL_TEXTURE_BINDING_2D);
        g_FontTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, g_FontTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);

        ByteBuffer buf = BufferUtils.createByteBuffer(tex.bytes.length);
        buf.put(tex.bytes); buf.flip();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, tex.width, tex.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);

        // Store our identifier
        nSetFontTexID(g_FontTexture);

        // Restore state
        glBindTexture(GL_TEXTURE_2D, last_texture);
    }

    private static void checkShader(int handle, String desc)
    {
        int status = 0, log_length = 0;
        status = glGetShaderi(handle, GL_COMPILE_STATUS);
        log_length = glGetShaderi(handle, GL_INFO_LOG_LENGTH);
        if (status == GL_FALSE)
            Debug.error(
                String.format("ERROR: ImGui_CreateDeviceObjects: failed to compile %s!\n", desc));
        if (log_length > 0)
        {
            Debug.error(glGetShaderInfoLog(handle, log_length));
        }
    }

    private static void checkProgram(int handle, String desc)
    {
        int status = 0, log_length = 0;
        status = glGetProgrami(handle, GL_LINK_STATUS);
        log_length = glGetProgrami(handle, GL_INFO_LOG_LENGTH);
        if (status == GL_FALSE)
            Debug.error(String.format("ERROR: ImGui_CreateDeviceObjects: failed to link %s!\n", desc));
        if (log_length > 0)
        {
            String s = glGetProgramInfoLog(handle, log_length);
            Debug.error(s);
        }
    }

    private static void createContext() {
        nCreateContext();
    }

    private static int[] glGetIntegerv(int name, int count) {
        IntBuffer buf = BufferUtils.createIntBuffer(16);
        glGetInteger(name, buf);
        int[] ret = new int[count];
        buf.get(ret);
        return ret;
    }

    private static void renderImpl(ImDrawData dd) {
        int fb_width = (int) (dd.dispW * _displayFBScale.x);
        int fb_height = (int) (dd.dispH * _displayFBScale.y);

        // Avoid rendering when minimized, scale coordinates for retina displays (screen coordinates != framebuffer coordinates)
        dd.scaleClipRects(_displayFBScale.x, _displayFBScale.y);

        StoredGLState storedGLState = new StoredGLState();
        glActiveTexture(GL_TEXTURE0);

        // Setup render state: alpha-blending enabled, no face culling, no depth testing, scissor enabled, polygon fill
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_SCISSOR_TEST);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        // Setup viewport, orthographic projection matrix
        // Our visible imgui space lies from dd.DisplayPos (top left) to dd.DisplayPos+data_data.DisplaySize (bottom right). DisplayMin is typically (0,0) for single viewport apps.
        glViewport(0, 0, fb_width, fb_height);
        float L = dd.dispX;
        float R = dd.dispX + dd.dispW;
        float T = dd.dispY;
        float B = dd.dispY + dd.dispH;
        FloatBuffer ortho_projection = BufferUtils.createFloatBuffer(16);
        ortho_projection.put(new float[] {
            2.0f/(R-L),   0.0f,         0.0f,   0.0f,
            0.0f,         2.0f/(T-B),   0.0f,   0.0f,
            0.0f,         0.0f,        -1.0f,   0.0f,
            (R+L)/(L-R),  (T+B)/(B-T),  0.0f,   1.0f,
        });
        ortho_projection.flip();

        glUseProgram(g_ShaderHandle);
        glUniform1i(g_AttribLocationTex, 0);
        glUniformMatrix4(g_AttribLocationProjMtx, false, ortho_projection);
        glBindSampler(0, 0); // We use combined texture/sampler state. Applications using GL 3.3 may set that otherwise.

        // Recreate the VAO every time
        // (This is to easily allow multiple GL contexts. VAO are not shared among GL contexts, and we don't track creation/deletion of windows so we don't have an obvious key to use to cache them.)
        int vao_handle = glGenVertexArrays();
        glBindVertexArray(vao_handle);
        glBindBuffer(GL_ARRAY_BUFFER, g_VboHandle);
        glEnableVertexAttribArray(g_AttribLocationPosition);
        glEnableVertexAttribArray(g_AttribLocationUV);
        glEnableVertexAttribArray(g_AttribLocationColor);
        glVertexAttribPointer(g_AttribLocationPosition, 2, GL_FLOAT, false, ImDrawVert.BYTES, 0);
        glVertexAttribPointer(g_AttribLocationUV, 2, GL_FLOAT, false, ImDrawVert.BYTES, 2 * 4);
        glVertexAttribPointer(g_AttribLocationColor, 4, GL_UNSIGNED_BYTE, true, ImDrawVert.BYTES, 4 * 4);

        // Draw
        for (int n = 0; n < dd.cmdLists.size(); n++)
        {
            final ImDrawList cmd_list = dd.cmdLists.get(n);

            glBindBuffer(GL_ARRAY_BUFFER, g_VboHandle);
            {
                ByteBuffer buf = BufferUtils.createByteBuffer(cmd_list.vtxBuffer.length * ImDrawVert.BYTES);
                for (ImDrawVert v : cmd_list.vtxBuffer) {
                    v.put(buf);
                }
                buf.flip();
                glBufferData(GL_ARRAY_BUFFER, buf, GL_STREAM_DRAW);
            }

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, g_ElementsHandle);
            {
                ByteBuffer buf = BufferUtils.createByteBuffer(4 * cmd_list.idxBuffer.length);
                for (int idx : cmd_list.idxBuffer) {
                    buf.putInt(idx);
                }
                buf.flip();
                glBufferData(GL_ELEMENT_ARRAY_BUFFER, buf, GL_STREAM_DRAW);
            }

            Vector2f pos = new Vector2f(dd.dispX, dd.dispY);
            int idx_buffer_offset = 0;
            for (int cmd_i = 0; cmd_i < cmd_list.cmdBuffer.length; cmd_i++)
            {
                ImDrawCmd pcmd = cmd_list.cmdBuffer[cmd_i];
                if (!pcmd.isUserCallback) {
                    Vector4f clip_rect = new Vector4f(
                        pcmd.clipX - pos.x,
                        pcmd.clipY - pos.y,
                        pcmd.clipW - pos.x,
                        pcmd.clipH - pos.y);
                    if (clip_rect.x < fb_width && clip_rect.y < fb_height && clip_rect.z >= 0.0f && clip_rect.w >= 0.0f)
                    {
                        glScissor((int)clip_rect.x, (int)(fb_height - clip_rect.w), (int)(clip_rect.z - clip_rect.x), (int)(clip_rect.w - clip_rect.y));

                        // Bind texture, Draw
                        glBindTexture(GL_TEXTURE_2D, pcmd.textureID);
                        glDrawElements(GL_TRIANGLES, pcmd.elemCount, GL_UNSIGNED_INT, idx_buffer_offset * 4);
                    }
                } else {
                    _userCallbacks.get(pcmd.userCallbackID).run();
                }
                idx_buffer_offset += pcmd.elemCount;
            }
        }
        glDeleteVertexArrays(vao_handle);
        storedGLState.restore();
    }

    public static class StoredGLState {

        // Backup GL state
        int last_active_texture = glGetInteger(GL_ACTIVE_TEXTURE);
        int last_program = glGetInteger(GL_CURRENT_PROGRAM);
        int last_texture = glGetInteger(GL_TEXTURE_BINDING_2D);
        int last_sampler = glGetInteger(GL_SAMPLER_BINDING);
        int last_array_buffer = glGetInteger(GL_ARRAY_BUFFER_BINDING);
        int last_vertex_array = glGetInteger(GL_VERTEX_ARRAY_BINDING);
        int[] last_polygon_mode = glGetIntegerv(GL_POLYGON_MODE, 2);
        int[] last_viewport = glGetIntegerv(GL_VIEWPORT, 4);
        int[] last_scissor_box = glGetIntegerv(GL_SCISSOR_BOX, 4);
        int last_blend_src_rgb = glGetInteger(GL_BLEND_SRC_RGB);
        int last_blend_dst_rgb = glGetInteger(GL_BLEND_DST_RGB);
        int last_blend_src_alpha = glGetInteger(GL_BLEND_SRC_ALPHA);
        int last_blend_dst_alpha = glGetInteger(GL_BLEND_DST_ALPHA);
        int last_blend_equation_rgb = glGetInteger(GL_BLEND_EQUATION_RGB);
        int last_blend_equation_alpha = glGetInteger(GL_BLEND_EQUATION_ALPHA);
        boolean last_enable_blend = glIsEnabled(GL_BLEND);
        boolean last_enable_cull_face = glIsEnabled(GL_CULL_FACE);
        boolean last_enable_depth_test = glIsEnabled(GL_DEPTH_TEST);
        boolean last_enable_scissor_test = glIsEnabled(GL_SCISSOR_TEST);

        public void restore() {
            // Restore modified GL state
            glUseProgram(last_program);
            glBindTexture(GL_TEXTURE_2D, last_texture);
            glBindSampler(0, last_sampler);
            glActiveTexture(last_active_texture);
            glBindVertexArray(last_vertex_array);
            glBindBuffer(GL_ARRAY_BUFFER, last_array_buffer);
            glBlendEquationSeparate(last_blend_equation_rgb, last_blend_equation_alpha);
            glBlendFuncSeparate(last_blend_src_rgb, last_blend_dst_rgb, last_blend_src_alpha, last_blend_dst_alpha);
            if (last_enable_blend) glEnable(GL_BLEND); else glDisable(GL_BLEND);
            if (last_enable_cull_face) glEnable(GL_CULL_FACE); else glDisable(GL_CULL_FACE);
            if (last_enable_depth_test) glEnable(GL_DEPTH_TEST); else glDisable(GL_DEPTH_TEST);
            if (last_enable_scissor_test) glEnable(GL_SCISSOR_TEST); else glDisable(GL_SCISSOR_TEST);
            glPolygonMode(GL_FRONT_AND_BACK, last_polygon_mode[0]);
            glViewport(last_viewport[0], last_viewport[1], last_viewport[2], last_viewport[3]);
            glScissor(last_scissor_box[0], last_scissor_box[1], last_scissor_box[2], last_scissor_box[3]);
        }
    }

    // NATIVE section
    private static native void nCreateContext();

    private static native boolean nShowDemoWindow(boolean show);

    private static native void nFillInput(
        float dispWidth, float dispHeight,
        float deltaTime,
        float mouseX, float mouseY,
        boolean[] mouseDown,
        float mouseWheel,
        boolean keyCtrl,
        boolean keyShift,
        boolean keyAlt,
        boolean keySuper,
        boolean[] keysDown,
        char[] inputCharacters
    );

    private static native void nNewFrame();

    private static native ImDrawData nRender();

    private static native ImFontTex nGetFontTexARGB32();

    private static native void nSetFontTexID(int texID);

    // Methods for cpp
    private static String getClipboardContent() {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        if(cb.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            try {
                return (String) cb.getData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    private static void setClipboardContent(String content) {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection ss = new StringSelection(content);
        cb.setContents(ss, ss);
    }

    private static void markChanged() {
        for (int i = 0; i < changeCheckStack.size(); ++i) {
            changeCheckStack.set(i, true);
        }
    }

}
