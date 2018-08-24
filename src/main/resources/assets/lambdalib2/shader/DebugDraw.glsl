Properties {
    Uniform {
        uMVP = mat4;
        uColor = vec4(0, 0, 0, 0);
    }
    VertexLayout {
        aPosition = POSITION;
    }
}

Settings {
    Cull Off;
}

Vertex {
uniform mat4 uMVP;

in vec3 aPosition;

void main() {
    vec4 pos = uMVP * vec4(aPosition, 1);
    gl_Position = pos;
}
}

Fragment {
uniform vec4 uColor;

out vec4 fragColor;

void main() {
    fragColor = uColor;
}
}