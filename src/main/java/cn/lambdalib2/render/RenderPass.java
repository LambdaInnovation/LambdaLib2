package cn.lambdalib2.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.out;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class RenderPass {

    private List<DrawCall> drawCalls = new ArrayList<>();
    private List<BatchGroup> batchList = new ArrayList<>();

    public void draw(RenderMaterial material, Mesh mesh) {
        draw(material, mesh, null);
    }

    public void draw(RenderMaterial material, Mesh mesh, InstanceData instanceData) {
        DrawCall drawCall = new DrawCall();
        drawCall.material = material;
        drawCall.mesh = mesh;
        drawCall.instanceData = instanceData;
        drawCalls.add(drawCall);
    }

    public void clear() {
        drawCalls.clear();
    }

    public void dispatch() {
        drawCalls.sort((lhs, rhs) -> compareDict(
             Integer.compare(lhs.material.getDrawOrder(), rhs.material.getDrawOrder()),
             Integer.compare(lhs.material.hashCode(), rhs.material.hashCode()),
             Integer.compare(lhs.mesh.hashCode(), rhs.mesh.hashCode())
        ));

        // Batch draw calls
        {
            int batchingBegin = -1;
            RenderMaterial batchedMaterial = null;
            Mesh batchedMesh = null;

            for (int i = 0; i < drawCalls.size(); ++i) {
                DrawCall drawCall = drawCalls.get(i);
                if (drawCall.material != batchedMaterial || drawCall.mesh != batchedMesh) {
                    if (batchingBegin != -1) {
                        createBatchGroup(batchingBegin, i);
                    }
                    batchingBegin = i;
                    batchedMaterial = drawCall.material;
                    batchedMesh = drawCall.mesh;
                }
            }

            if (batchingBegin != -1)
                createBatchGroup(batchingBegin, drawCalls.size());
        }

        for (BatchGroup batch : batchList) {
            // TODO: Currently assumes there is no InstanceData assigned. Support instancing.
            emitDrawCall(batch);
        }

        // cleanup
        drawCalls.clear();
        batchList.clear();
    }

    private void emitDrawCall(BatchGroup batch) {
        RenderMaterial mat = batch.material;
        ShaderScript shader = mat.shader;
        Mesh mesh = batch.mesh;

        // Activate shader program
        glUseProgram(shader.glProgramID);

        // Create vao and setup
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Update material uniform
        mat.updateUniformsOnCurrentProgram();

        // Render states setup
        shader.renderStates.apply();

        // Update mesh buffers (if needed)
        mesh.ensureBuffers();

        // VBO setup
        glBindBuffer(GL_ARRAY_BUFFER, mesh.getVBO());
        List<Mesh.DataLayoutItem> dataLayout = mesh.getDataLayout();

        Map<Mesh.DataType, Integer> layoutMap = new HashMap<>();
        dataLayout.forEach(it -> layoutMap.put(it.dataType, it.offset));

        for (ShaderScript.VertexAttribute va : shader.vertexLayout.values()) {
            Integer offset = layoutMap.get(va.semantic);

            if (offset != null) {
                glEnableVertexAttribArray(va.index);
                glVertexAttribPointer(va.index, va.type.components, GL_FLOAT, false,
                    4 * mesh.getFloatsPerVertex(), offset * 4);
            }
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // IBO setup
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh.getIBO());

        // Draw!
        glDrawElements(GL_TRIANGLES, mesh.getIndicesCount(), GL_UNSIGNED_INT, 0);

        // Cleanup VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vao);

        // Cleanup program
        glUseProgram(0);
    }

    private static class DrawCall {
        public RenderMaterial material;
        public Mesh mesh;
        public InstanceData instanceData;
    }

    private static class BatchGroup {
        public RenderMaterial material;
        public Mesh mesh;
        public final List<InstanceData> instances = new ArrayList<>();

        public BatchGroup(RenderMaterial material, Mesh mesh) {
            this.material = material;
            this.mesh = mesh;
        }

        public int count() {
            return instances.size();
        }
    }

    private static int compareDict(int... compareResults) {
        for (int r : compareResults) {
            if (r != 0) return r;
        }
        return 0;
    }

    private void createBatchGroup(int beginIdx, int endIdx) {
        BatchGroup group = new BatchGroup(drawCalls.get(beginIdx).material, drawCalls.get(beginIdx).mesh);
        for (int i = beginIdx; i < endIdx; ++i) {
            group.instances.add(drawCalls.get(i).instanceData);
        }

        batchList.add(group);
    }

}
