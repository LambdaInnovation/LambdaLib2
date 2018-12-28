package cn.lambdalib2.vis.editor;

import cn.lambdalib2.util.Debug;
import cn.lambdalib2.util.GameTimer;
import cn.lambdalib2.util.MathUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
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

    public ImDrawCmd(int elemCount, float clipX, float clipY, float clipW, float clipH, int textureID) {
        this.elemCount = elemCount;
        this.clipX = clipX;
        this.clipY = clipY;
        this.clipW = clipW;
        this.clipH = clipH;
        this.textureID = textureID;
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

    private static boolean _withoutMC = false;

    public static void setWithoutMC() {
        _withoutMC = true;
    }

    public static void newFrame(float dWheel, char[] inputChars) {
        if (!_init) {
            createContext();
            _init = true;
        }

        if (g_FontTexture == 0) {
            createDeviceObjects();
        }

        float dw, dh;
        DisplayMode dm = Display.getDisplayMode();
        dw = dm.getWidth();
        dh = dm.getHeight();

        float w, h;
        if (_withoutMC) {
            w = dw; h = dh;
        } else {
            // Note: MC's displayWidth(height) actually is framebuffer size,
            //  which correspond to IMGUI's displaySize.
            Minecraft mc = Minecraft.getMinecraft();
            w = mc.displayWidth;
            h = mc.displayHeight;
        }

        double t = GameTimer.getAbsTime();
        float dt = (float) MathUtils.clampd(0, 0.33, t - _lastTime);

        float mx = Mouse.getX();
        float my = dh - Mouse.getY();

        _displayFBScale.set(dw / w, dh / h);

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
    }

    public static boolean showDemoWindow(boolean show) {
        return nShowDemoWindow(show);
    }

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

        // Backup GL state
        int last_active_texture = glGetInteger(GL_ACTIVE_TEXTURE);
        glActiveTexture(GL_TEXTURE0);
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
        boolean clip_origin_lower_left = true;

        // GL45 feature
//        GLenum last_clip_origin = 0; glGetIntegerv(GL_CLIP_ORIGIN, (GLint*)&last_clip_origin); // Support for GL 4.5's glClipControl(GL_UPPER_LEFT)
//        if (last_clip_origin == GL_UPPER_LEFT)
//            clip_origin_lower_left = false;

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
                {
                    Vector4f clip_rect = new Vector4f(
                        pcmd.clipX - pos.x,
                        pcmd.clipY - pos.y,
                        pcmd.clipW - pos.x,
                        pcmd.clipH - pos.y);
                    if (clip_rect.x < fb_width && clip_rect.y < fb_height && clip_rect.z >= 0.0f && clip_rect.w >= 0.0f)
                    {
                        // Apply scissor/clipping rectangle
                        if (clip_origin_lower_left)
                            glScissor((int)clip_rect.x, (int)(fb_height - clip_rect.w), (int)(clip_rect.z - clip_rect.x), (int)(clip_rect.w - clip_rect.y));
                        else
                            glScissor((int)clip_rect.x, (int)clip_rect.y, (int)clip_rect.z, (int)clip_rect.w); // Support for GL 4.5's glClipControl(GL_UPPER_LEFT)

                        // Bind texture, Draw
                        glBindTexture(GL_TEXTURE_2D, pcmd.textureID);
                        glDrawElements(GL_TRIANGLES, pcmd.elemCount, GL_UNSIGNED_INT, idx_buffer_offset * 4);
                    }
                }
                idx_buffer_offset += pcmd.elemCount;
            }
        }
        glDeleteVertexArrays(vao_handle);

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

}
