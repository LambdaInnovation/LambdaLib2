package cn.lambdalib2.render;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class RenderMaterial {

    final ShaderScript shader;

    private Map<Integer, Object> valueMapping = new HashMap<>();

    /**
     * Creates a new material from the shader
     * @param shader The shader that this material is based from
     */
    public RenderMaterial(ShaderScript shader) {
        this.shader = shader;
    }

    /**
     * Create a duplicate of given material.
     * @param src Material to be copied
     */
    public RenderMaterial(RenderMaterial src) {
        this(src.shader);
    }

    public int getDrawOrder() {
        return 0;
    }

    public void setFloat(String name, float value) {
        setObj(name, value);
    }

    public void setVec2(String name, Vector2f value) {
        setObj(name, value);
    }

    public void setVec3(String name, Vector3f value) {
        setObj(name, value);
    }

    public void setVec4(String name, Vector4f value) {
        setObj(name, value);
    }

    private void setObj(String name, Object value) {
        int location = shader.getUniformLocation(name);
        if (location != -1) {
            valueMapping.put(location, value);
        }
    }

    void updateUniformsOnCurrentProgram() {
        for (ShaderScript.Property property : shader.uniformProperties) {
            int location = property.uniformLocation;
            if (location != -1) {
                Object value = property.value;
                if (valueMapping.containsKey(location)) {
                    value = valueMapping.get(location);
                }

                if (value instanceof Float) {
                    glUniform1f(location, (Float) value);
                } else if (value instanceof Vector2f) {
                    Vector2f v = (Vector2f) value;
                    glUniform2f(location, v.x, v.y);
                } else if (value instanceof Vector3f) {
                    Vector3f v = (Vector3f) value;
                    glUniform3f(location, v.x, v.y, v.z);
                } else if (value instanceof Vector4f) {
                    Vector4f v = (Vector4f) value;
                    glUniform4f(location, v.x, v.y, v.z, v.w);
                } else {
                    throw new RuntimeException("Invalid uniform type " + value);
                }
            }
        }
    }

}
