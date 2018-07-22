Example Code:

```
Properties {
    [Uniform]
    view_matrix = pass_data(MC_PVP_MATRIX);

    [Instanced]
    offset = vec3(0, 0, 0);
    scale = 1.5;
}

Settings {
    DepthMask On;
    AlphaBlend SrcAlpha OneMinusSrcAlpha;
    DepthTest LessEqual 0.1;
    DrawOrder 123;
}

Vert {
 <vertex shader code>
}

Fragment {
 <fragment shader code>
}
```

For each render pass execution:

```
Group all draw calls with same material and mesh (into batch group);
Sort all draw calls;

for each batch group:
    let material = material of that batch group;
    
    apply material's render states;

    setup material's uniforms into GL;

    [pass's custom callback]
    setup the pass's custom uniform data into GL;

    setup instance data buffer (if there needs to be one);

    setup VAO to use mesh's vertex & indices buffer and instance data buffer;

    emit instanced (or uninstanced) draw call;
```

