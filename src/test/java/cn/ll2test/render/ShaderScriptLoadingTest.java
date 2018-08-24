package cn.ll2test.render;

import cn.lambdalib2.render.ShaderScript;
import cn.ll2test.common.OfflineTestUtils;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.nio.charset.Charset;

import static java.lang.System.out;

public class ShaderScriptLoadingTest {


    public static void main(String[] args) throws Exception {
        OfflineTestUtils.hackNatives();

        // Setup GL context
        Display.setDisplayMode(new DisplayMode(800, 600));
        Display.create();

        ShaderScript script = ShaderScript.loadFromResource("/render/test.glsl");

        out.println("Shader compilation successful");

        Thread.sleep(500);
        Display.destroy();
    }

}
