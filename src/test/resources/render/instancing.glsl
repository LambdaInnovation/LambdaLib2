Properties {
    VertexLayout {
        aPosition = POSITION;
        aUV = UV1;
    }
    Uniform {
        uTex = sampler2D;
    }
    Instance {
        iOffset = vec3(0, 0, 0);
        iScl = 1.0;
    }
}

Settings {
    Cull Off;
}

Vertex {
#version 330 core

// vertex attribute
in vec3 aPosition;
in vec2 aUV;

// instanced
in vec3 iOffset;
in float iScl;

out vec3 vPosition;
out vec2 vUV;

void main() {
    gl_Position = vec4(aPosition * iScl + iOffset, 1);
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