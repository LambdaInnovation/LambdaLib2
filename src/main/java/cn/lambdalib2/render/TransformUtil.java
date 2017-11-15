package cn.lambdalib2.render;

import org.lwjgl.util.vector.Matrix4f;

public class TransformUtil {

    public static Matrix4f perspective(float fov, float aspect, float zNear, float zFar) {
        Matrix4f mat = new Matrix4f();

        float yScale = (float) (1 / (Math.tan(Math.toRadians(fov / 2))));
        float xScale = yScale / aspect;
        float frustrumLength = zFar - zNear;

        mat.m00 = xScale;
        mat.m11 = yScale;
        mat.m22 = -((zFar + zNear) / frustrumLength);
        mat.m23 = -1;
        mat.m32  = -((2 * zFar * zNear) / frustrumLength);
        mat.m33 = 0;

        return mat;
    }

    public static Matrix4f translate(float dx, float dy, float dz) {
        Matrix4f mat = new Matrix4f();
        mat.m30 = dx;
        mat.m31 = dy;
        mat.m32 = dz;
        return mat;
    }

}
