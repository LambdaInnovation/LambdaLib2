package cn.ll2test.client.render;

import cn.lambdalib2.render.*;
import cn.lambdalib2.render.mc.EntityRenderUtils;
import cn.lambdalib2.render.mc.RenderEventDispatch;
import cn.ll2test.tileentity.TileEntityStrangeCube;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class TESRStrangeCube extends TileEntitySpecialRenderer<TileEntityStrangeCube> {

    boolean resourceInit = false;

    ShaderScript shader;
    RenderMaterial material;
    Mesh mesh;
    Texture2D texture;

    float t = 0.0f;

    public TESRStrangeCube() {
    }

    private void initResources() {
        shader = ShaderScript.loadFromResource("/assets/ll2test/shader/strange_cube.shader");
        material = new RenderMaterial(shader);
        mesh = createCubeMesh();

        texture = Texture2D.loadFromResource(
            "/assets/ll2test/texture/strange_cube.png",
                new TextureImportSettings(TextureImportSettings.FilterMode.Trilinear, TextureImportSettings.WrapMode.Clamp)
        );

        material.setTexture("uTex", texture);
    }

    private Mesh createCubeMesh() {
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
            indices[n++] = off + 2;
            indices[n++] = off + 1;
            indices[n++] = off + 0;
            indices[n++] = off + 3;
            indices[n++] = off + 2;
            indices[n++] = off + 0;
        }
        mesh.setIndices(indices);

        return mesh;
    }

    @Override
    public void render(TileEntityStrangeCube te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!resourceInit) {
            resourceInit = true;
            initResources();
        }

        t += EntityRenderUtils.getDeltaTime();

        Matrix4f temp = new Matrix4f();
        Matrix4f rotMatrix = TransformUtils.quaternionToMatrix(TransformUtils.eulerToQuaternion(t, 0, 0));
        temp = Matrix4f.mul(rotMatrix, TransformUtils.translate(-0.5f, -0.5f, -0.5f), temp);
        temp = Matrix4f.mul(TransformUtils.translate(0.5f, 0.5f, 0.5f), temp, temp);
        temp = Matrix4f.mul(TransformUtils.translate((float) x, (float) y, (float) z), temp, temp);

        Matrix4f pvpMatrix = EntityRenderUtils.getPVPMatrix();
        Matrix4f mvpMatrix = Matrix4f.mul(pvpMatrix, temp, temp);

        material.setMat4("uMVP", mvpMatrix);

        RenderEventDispatch.entityPass.draw(material, mesh);
    }

}
