/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib2.cgui.component;

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL20.glUseProgram;

import cn.lambdalib2.cgui.Widget;
import cn.lambdalib2.cgui.event.FrameEvent;
import cn.lambdalib2.util.Colors;
import cn.lambdalib2.util.HudUtils;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.Color;

/**
 * Draws a squared texture that fills the area of the given widget.
 */
public class DrawTexture extends Component {
    
    public static final ResourceLocation MISSING = new ResourceLocation("lambdalib2:textures/cgui/missing.png");
    
    public ResourceLocation texture;
    
    public Color color;
    
    public double zLevel = 0;
    
    public boolean writeDepth = true;

    public boolean doesUseUV;

    public double u = 0, v = 0, texWidth = 0, texHeight = 0;

    private int shaderId = 0;

    public DrawTexture() {
        this(MISSING);
    }

    public DrawTexture(ResourceLocation texture) {
        this(texture, Colors.white());
    }

    public DrawTexture(String name, ResourceLocation _texture, Color _color) {
        super(name);
        this.texture = _texture;
        this.color = _color;

        listen(FrameEvent.class, (w, e) ->
        {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDisable(GL_ALPHA_TEST);
            glDepthMask(writeDepth);
            glUseProgram(shaderId);

            Colors.bindToGL(color);

            double preLevel = HudUtils.zLevel;
            HudUtils.zLevel = zLevel;

            if(texture != null && !texture.getResourcePath().equals("<null>")) {
                HudUtils.loadTexture(texture);
                if (doesUseUV) {
                    HudUtils.rect(0, 0, u, v, w.transform.width, w.transform.height, texWidth, texHeight);
                } else {
                    HudUtils.rect(0, 0, w.transform.width, w.transform.height);
                }
            } else {
                HudUtils.colorRect(0, 0, w.transform.width, w.transform.height);
            }
            HudUtils.zLevel = preLevel;
            glUseProgram(0);
            glDepthMask(true);
        });
    }

    public DrawTexture(ResourceLocation _texture, Color _color) {
        this("DrawTexture", _texture, _color);
    }
    
    public void setShaderId(int id) {
        shaderId = id;
    }
    
    public DrawTexture setTex(ResourceLocation t) {
        texture = t;
        return this;
    }

    public DrawTexture setUVRect(double u, double v, double texWidth, double texHeight) {
        doesUseUV = true;
        this.u = u;
        this.v = v;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        return this;
    }

    /**
     * Set the color as a **copy** of the given color.
     */
    public DrawTexture setColor(Color c) {
        this.color.setColor(c);
        return this;
    }

    public static DrawTexture get(Widget w) {
        return w.getComponent(DrawTexture.class);
    }

}
