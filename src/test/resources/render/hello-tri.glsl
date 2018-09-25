Properties {
    VertexLayout {
        aPosition = POSITION;
    }
}

Settings {
    Cull Off;
}

Vertex {
attribute vec3 aPosition;

varying vec3 vPosition;

void main() {
    gl_Position = vec4(aPosition, 1);
    vPosition = aPosition;
}
}

Fragment {
varying vec3 vPosition;

void main() {
    gl_FragColor = vec4(vPosition.xy * 2, 0.5, 1);
}
}