package cn.lambdalib2.render.legacy;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.Color;

/**
 * @author WeAthFolD
 *
 */
public abstract class LegacyMaterial {
    
    public ResourceLocation mainTexture;
    public Color color = new Color();

    public abstract void onRenderStage(RenderStage stage);
    
    public LegacyMaterial setTexture(ResourceLocation tex) {
        mainTexture = tex;
        return this;
    }
    
}
