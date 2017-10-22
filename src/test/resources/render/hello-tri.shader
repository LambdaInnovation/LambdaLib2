Properties {
    VertexLayout {
        aPosition = POSITION;
    }
}

Settings {
    Cull Off;
}

Vertex {
#version 330 core

in vec3 aPosition;

out vec3 vPosition;

void main() {
    gl_Position = vec4(aPosition, 1);
    vPosition = aPosition;
}
}

Fragment {
#version 330 core

in vec3 vPosition;

out vec4 fragColor;

void main() {
    fragColor = vec4(vPosition.xy * 2, 0.5, 1);
}
}