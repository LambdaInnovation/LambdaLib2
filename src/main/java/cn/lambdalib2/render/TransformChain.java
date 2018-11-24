package cn.lambdalib2.render;

import org.lwjgl.util.vector.Matrix4f;
import sun.util.resources.cldr.dyo.CalendarData_dyo_SN;

public class TransformChain {

    private static final Matrix4f _temp = new Matrix4f();

    private final Matrix4f _result = new Matrix4f();

    public TransformChain() {
        _result.setIdentity();
    }

    public TransformChain translate(float dx, float dy, float dz) {
        TransformUtils.translate(dx, dy, dz, _temp);
        return applyTemp();
    }

    public TransformChain rotate(float x, float y, float z) {
        TransformUtils.rotateEuler(x, y, z, _temp);
        return applyTemp();
    }

    public TransformChain scale(float sx, float sy, float sz) {
        TransformUtils.scale(sx, sy, sz, _temp);
        return applyTemp();
    }

    public TransformChain scale(float s) {
        TransformUtils.scale(s, _temp);
        return applyTemp();
    }

    private TransformChain applyTemp() {
        Matrix4f.mul(_temp, _result, _result);
        return this;
    }

    public Matrix4f bulid() {
        return _result;
    }
}
