package cn.lambdalib2.render.legacy;

/**
 * @author WeAthFolD
 *
 */
public class LegacyMeshUtils {
    
    /**
     * Create a billboard mesh that maps to the whole texture in XOY plane. If mesh == null, create a new one.
     */
    public static <T extends LegacyMesh> T createBillboard(T mesh, double x0, double y0, double x1, double y1) {
        if(mesh == null)
            mesh = (T) new LegacyMesh();
        mesh.setVertices(new double[][] {
                { x0, y0, 0 },
                { x1, y0, 0 },
                { x1, y1, 0 },
                { x0, y1, 0 }
        });
        mesh.setUVs(new double[][] {
            { 0, 0 },
            { 1, 0 },
            { 1, 1 },
            { 0, 1 }
        });
        mesh.setQuads(new int[] { 0, 1, 2, 3 });
        mesh.setAllNormals(new float[] { 0, 0, 1 });
        return mesh;
    }
    
    public static LegacyMesh createBox(LegacyMesh mesh, double x0, double y0, double z0, double xw, double yw, double zw) {
        if(mesh == null)
            mesh = new LegacyMesh();
        
        double x1 = x0 + xw;
        double y1 = y0 + yw;
        double z1 = z0 + zw;
        
        mesh.setVertices(new double[][] {
            { x0, y0, z0 },
            { x1, y0, z0 },
            { x1, y0, z1 },
            { x0, y0, z1 },
            { x0, y1, z0 },
            { x1, y1, z0 },
            { x1, y1, z1 },
            { x0, y1, z1 }
        });
        
        mesh.setQuads(new int[] {
            0, 1, 2, 3,
            4, 5, 6, 7,
            5, 1, 2, 6,
            6, 2, 3, 7,
            7, 3, 0, 4,
            4, 0, 1, 5
        });
        
        return mesh;
    }
    
    public static LegacyMesh createBoxWithUV(LegacyMesh mesh, double x0, double y0, double z0, double xw, double yw, double zw) {
        if(mesh == null)
            mesh = new LegacyMesh();
        
        double x1 = x0 + xw;
        double y1 = y0 + yw;
        double z1 = z0 + zw;
        
        double[][] uvs = new double[][] {
                {0, 0},
                {1, 0},
                {1, 1},
                {0, 1}
        };
        
        double[][] vs = new double[][] {
                { x0, y0, z0 },
                { x1, y0, z0 },
                { x1, y0, z1 },
                { x0, y0, z1 },
                { x0, y1, z0 },
                { x1, y1, z0 },
                { x1, y1, z1 },
                { x0, y1, z1 }
        };
        
        int[] triangles = new int[] { 0, 1, 2, 3 };
        
        LegacyMesh sub = new LegacyMesh();
        
        sub.setVertices(new double[][] { vs[0], vs[1], vs[2], vs[3] });
        sub.setUVs(uvs);
        sub.setQuads(triangles);
        mesh.addMesh(sub);
        
        sub = new LegacyMesh();
        sub.setVertices(new double[][] { vs[4], vs[5], vs[6], vs[7] });
        sub.setUVs(uvs);
        sub.setQuads(triangles);
        mesh.addMesh(sub);
        
        sub = new LegacyMesh();
        sub.setVertices(new double[][] { vs[5], vs[1], vs[2], vs[6] });
        sub.setUVs(uvs);
        sub.setQuads(triangles);
        mesh.addMesh(sub);
        
        sub = new LegacyMesh();
        sub.setVertices(new double[][] { vs[6], vs[2], vs[3], vs[7] });
        sub.setUVs(uvs);
        sub.setQuads(triangles);
        mesh.addMesh(sub);
        
        sub = new LegacyMesh();
        sub.setVertices(new double[][] { vs[7], vs[3], vs[0], vs[4] });
        sub.setUVs(uvs);
        sub.setQuads(triangles);
        mesh.addMesh(sub);
        
        sub = new LegacyMesh();
        sub.setVertices(new double[][] { vs[4], vs[0], vs[1], vs[5] });
        sub.setUVs(uvs);
        sub.setQuads(triangles);
        mesh.addMesh(sub);
        
        return mesh;
    }
    
}
