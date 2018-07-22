package cn.ll2test.render;

import cn.lambdalib2.render.*;
import cn.ll2test.common.OfflineTestUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class FreeLookTest {
    static  final float SPEED_SCALE = 5;

    public static void main(String[] args) throws Exception {
        OfflineTestUtils.hackNatives();

        Display.setDisplayMode(new DisplayMode(800, 600));
        Display.setVSyncEnabled(true);
        Display.create();

        RenderPass pass = new RenderPass();

        Texture2D tex = Texture2D.loadFromResource("/texture/crate.png",
                new TextureImportSettings(TextureImportSettings.FilterMode.Blinear, TextureImportSettings.WrapMode.Clamp));

        ShaderScript shader = ShaderScript.loadFromResource("/render/free-look.shader");
        RenderMaterial mat = new RenderMaterial(shader);
        Matrix4f projMatrix = TransformUtils.perspective(60, 800.0f / 600, 0.1f, 1000f);
        Matrix4f mvpMatrix = new Matrix4f();

        mat.setTexture("uTex", tex);
        Mesh mesh = createCubeMesh();

        double frameElapsed = 0;
        int frameCount = 0;

        float x = 0, y = 0, z = 0;
        float pitch = 0, yaw = 0;

        System.out.println(shader.renderStates.depthTestMode);
        glClearDepth(1.0f);

        long t = System.nanoTime();
        while (!Display.isCloseRequested()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            long t2 = System.nanoTime();
            double dt = (t2 - t) / 1e9;
            t = t2;

            frameElapsed += dt;
            frameCount++;

            if (frameCount == 30) {
                System.out.println((frameCount / frameElapsed) + " FPS");
                frameCount = 0;
                frameElapsed = 0;
            }

            z += getMoveVertical() * dt * SPEED_SCALE;
            x += getMoveHorizontal() * dt * SPEED_SCALE;

            Matrix4f translateMat = TransformUtils.translate(x, 0, z);
            mat.setMat4("uMVP", Matrix4f.mul(projMatrix, translateMat, mvpMatrix));

            pass.draw(mat, mesh);
            pass.dispatch();

            Display.update();
        }

        Display.destroy();
    }

    static float boolToAxis(boolean b, float val) {
        return b ? val : 0;
    }

    static float getAxis(int keyPos, int keyNeg) {
        return boolToAxis(Keyboard.isKeyDown(keyPos), 1) + boolToAxis(Keyboard.isKeyDown(keyNeg), -1);
    }

    static float getMoveHorizontal() {
        return getAxis(Keyboard.KEY_A, Keyboard.KEY_D);
    }

    static float getMoveVertical() {
        return getAxis(Keyboard.KEY_W, Keyboard.KEY_S);
    }

    static float getLookVertical() {
        return getAxis(Keyboard.KEY_UP, Keyboard.KEY_DOWN);
    }

    static float getLookHorizontal() {
        return getAxis(Keyboard.KEY_LEFT, Keyboard.KEY_RIGHT);
    }

    private static Mesh createCubeMesh() {
        Mesh mesh = new Mesh();

        Vector3f v0 = new Vector3f(0, 0, 0),
                 v1 = new Vector3f(1, 0, 0),
                 v2 = new Vector3f(1, 0, 1),
                 v3 = new Vector3f(0, 0, 1),
                 v4 = new Vector3f(0, 1, 0),
                 v5 = new Vector3f(1, 1, 0),
                 v6 = new Vector3f(1, 1, 1),
                 v7 = new Vector3f(0, 1, 1);

        Vector2f uv0 = new Vector2f(0, 0),
                 uv1 = new Vector2f(1, 0),
                 uv2 = new Vector2f(1, 1),
                 uv3 = new Vector2f(0, 1);

        mesh.setVertices(new Vector3f[] {
            v3, v2, v1, v0,
            v5, v1, v2, v6,
            v6, v2, v3, v7,
            v4, v7, v3, v0,
            v4, v0, v1, v5,
            v4, v5, v6, v7
        });

        mesh.setUVsVec2(0, new Vector2f[]{
                uv0, uv1, uv2, uv3,
                uv0, uv1, uv2, uv3,
                uv0, uv1, uv2, uv3,
                uv0, uv1, uv2, uv3,
                uv0, uv1, uv2, uv3,
                uv0, uv1, uv2, uv3
        });

        int[] indices = new int[36];
        int n = 0;
        for (int i = 0; i < 6; ++i) {
            int off = i * 4;
            indices[n++] = off + 0;
            indices[n++] = off + 1;
            indices[n++] = off + 2;
            indices[n++] = off + 0;
            indices[n++] = off + 2;
            indices[n++] = off + 3;
        }
        mesh.setIndices(indices);

        return mesh;
    }


}
