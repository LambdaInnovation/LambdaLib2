Properties {
    Uniform {
        view_matrix = pass_data(MC_PVP_MATRIX);
    }
    Instance {
        offset = vec3(0, 0, 0);
        scale = 1.5;
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

void main() {
    gl_Position = view_matrix * position;
}
}

Fragment {
#version 330 core

out vec4 fragColor;

void main() {
    fragColor = vec4(0.5, 1.0, 1.0, 1.0);
}
}