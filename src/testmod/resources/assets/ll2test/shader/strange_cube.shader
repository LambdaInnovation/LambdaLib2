Properties {
    Uniform {
        uMVP = mat4;
        uTex = sampler2D;
    }
    VertexLayout {
        aPosition = POSITION;
        aUV = UV1;
    }
}

Settings {
    DepthTest LEqual;
}

Vertex {
#version 330 core

uniform mat4 uMVP;

in vec3 aPosition;
in vec2 aUV;

out vec2 vUV;

void main() {
    vec4 pos = uMVP * vec4(aPosition, 1);
    gl_Position = pos;
    vUV = aUV;
}

}

Fragment {
#version 330 core

uniform sampler2D uTex;

in vec2 vUV;

out vec4 fragColor;

void main() {
    vec4 c = texture(uTex, vUV);
    fragColor = c;
    // fragColor = vec4(vUV, 1, 1);
}
}