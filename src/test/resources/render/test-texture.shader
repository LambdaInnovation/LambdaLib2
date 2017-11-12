Properties {
    VertexLayout {
        aPosition = POSITION;
        aUV = UV1;
    }
    Uniform {
        uTex = sampler2D;
    }
}

Settings {
    Cull Off;
}

Vertex {
#version 330 core

in vec3 aPosition;
in vec2 aUV;

out vec3 vPosition;
out vec2 vUV;

void main() {
    gl_Position = vec4(aPosition, 1);
    vPosition = aPosition;
    vUV = aUV;
}
}

Fragment {
#version 330 core

uniform sampler2D uTex;

in vec3 vPosition;
in vec2 vUV;

out vec4 fragColor;

void main() {
    vec4 c = texture(uTex, vUV);
    fragColor = c;
}
}