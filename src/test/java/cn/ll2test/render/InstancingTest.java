package cn.ll2test.render;

import cn.lambdalib2.render.*;
import cn.ll2test.common.OfflineTestUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import scala.actors.threadpool.Arrays;

import static java.lang.System.out;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class InstancingTest {

    public static void main(String[] args) throws Exception {
        OfflineTestUtils.hackNatives();

        Display.setDisplayMode(new DisplayMode(800, 600));
        Display.create();

        RenderPass pass = new RenderPass();

        ShaderScript script = ShaderScript.loadFromResource("/render/instancing.glsl");
        RenderMaterial mat = new RenderMaterial(script);

        Texture2D texture = Texture2D.loadFromResource("/texture/ew.png",
                new TextureImportSettings(TextureImportSettings.FilterMode.Point, TextureImportSettings.WrapMode.Clamp));

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
        mesh.setUVsVec2(0, Arrays.asList(new Vector2f[] {
                new Vector2f(1, 1),
                new Vector2f(0, 1),
                new Vector2f(0, 0),
                new Vector2f(1, 0)
        }));

        mat.setTexture("uTex", texture);

        Vector3f[] poses = new Vector3f[] {
            new Vector3f(0, 0.5f, 0),
            new Vector3f(0.5f, 0, 0),
            new Vector3f(0, -1, 0),
            new Vector3f(-0.3f, 0, 0),
        };

        float[] scls = new float[] {
            0.5f, 0.6f, 1.0f, 1.1f
        };

        while (!Display.isCloseRequested()) {
            glClear(GL_COLOR_BUFFER_BIT);

            for (int i = 0; i < poses.length; ++i) {
                Vector3f off = poses[i];
                float scl = scls[i];
                InstanceData data = new InstanceData();
                data.setVec3("iOffset", off);
                data.setFloat("iScl", scl);
                pass.draw(mat, mesh, data);
            }

            pass.dispatch();

            Display.update();
        }

        Display.destroy();
    }

}
