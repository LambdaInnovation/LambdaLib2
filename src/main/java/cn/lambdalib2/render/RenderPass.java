package cn.lambdalib2.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RenderPass {

    private List<DrawCall> drawCalls = new ArrayList<>();
    private List<BatchGroup> batchList = new ArrayList<>();

    public void draw(RenderMaterial material, Mesh mesh) {

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

            createBatchGroup(batchingBegin, drawCalls.size());
        }

        for (BatchGroup batch : batchList) {
            // TODO: Do actual draw call emission
        }

        // cleanup
        drawCalls.clear();
        batchList.clear();
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
