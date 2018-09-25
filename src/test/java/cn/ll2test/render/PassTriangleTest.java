package cn.ll2test.render;

import cn.lambdalib2.render.Mesh;
import cn.lambdalib2.render.RenderMaterial;
import cn.lambdalib2.render.RenderPass;
import cn.lambdalib2.render.ShaderScript;
import cn.ll2test.common.OfflineTestUtils;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class PassTriangleTest {

    public static void main(String[] args) throws Exception {
        OfflineTestUtils.hackNatives();

        Display.setDisplayMode(new DisplayMode(800, 600));
        Display.create(new PixelFormat(), new ContextAttribs(2, 1));

        System.out.println("OpenGL version: " + glGetString(GL_VERSION));

        RenderPass pass = new RenderPass();
        ShaderScript script = ShaderScript.loadFromResource("/render/hello-tri.glsl");
        RenderMaterial mat = new RenderMaterial(script);

        Mesh mesh = new Mesh();
        mesh.setVertices(new Vector3f[] {
            new Vector3f(-0.5f, -0.5f, 0f),
            new Vector3f(0.5f, -0.5f, 0f),
            new Vector3f(0.5f, 0.5f, 0f),
            new Vector3f(-0.5f, 0.5f, 0f)
        });
        mesh.setIndices(new int[] {
            0, 1, 2, 0, 2, 3
        });

        while (!Display.isCloseRequested()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            pass.draw(mat, mesh);
            pass.dispatch();

            Display.update();
        }

        Display.destroy();
    }

}
