Properties {
    Uniform {
        view_matrix = pass_data(MC_PVP_MATRIX);
    }
    Instance {
        offset = vec3(0, 0, 0);
        scale = 1.5;
    }
    VertexLayout {
        position = POSITION;
    }
}

Settings {
    DepthMask On;
    Blend Off;
    BlendFunc SrcAlpha OneMinusSrcAlpha;
    AlphaTest LEqual 0.1;
    DepthTest Less;
    DrawOrder 123;
}

Vertex {
#version 330 core

uniform mat4 view_matrix;

in vec3 position;

void main() {
    gl_Position = view_matrix * vec4(position, 0.0);
}
}

Fragment {
#version 330 core

out vec4 fragColor;

void main() {
    fragColor = vec4(0.5, 1.0, 1.0, 1.0);
}
}