package cn.lambdalib2.vis.editor;

import cn.lambdalib2.cgui.Widget;
import cn.lambdalib2.cgui.component.TextBox;
import cn.lambdalib2.cgui.component.Tint;
import cn.lambdalib2.util.Colors;
import cn.lambdalib2.render.font.IFont.FontAlign;
import cn.lambdalib2.render.font.IFont.FontOption;
import cn.lambdalib2.render.font.TrueTypeFont;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.Color;

final class Styles {

    public static final TrueTypeFont font = TrueTypeFont.defaultFont;

    public static final Color
        COLOR_ERROR    = Colors.fromRGB32(0xee2222),
        COLOR_MODIFIED = Colors.fromRGB32(0x9f5a00);

    public static ResourceLocation texture(String path) {
        return new ResourceLocation("lambdalib2", "textures/vis/" + path + ".png");
    }

    public static ResourceLocation elemTexture(String path) {
        return texture("elements/" + path);
    }

    public static ResourceLocation buttonTexture(String path) {
        return texture("buttons/" + path);
    }

    public static TextBox newTextBox() {
        return new TextBox(new FontOption());
    }

    public static TextBox newTextBox(FontOption option) {
        TextBox ret = new TextBox(option);
        ret.font = font;
        return ret;
    }

    public static Widget newButton(float x, float y, float w, float h, String text) {
        Widget ret = new Widget(x, y, w, h);
        ret.addComponent(monoTint(0.25f, 0.35f, false));

        TextBox textBox = newTextBox(new FontOption(8, FontAlign.CENTER)).setContent(text);
        ret.addComponent(textBox);

        return ret;
    }

    public static Tint monoTint(float colorIdle, float colorHover, boolean affectTex) {
        Tint ret = new Tint();
        ret.idleColor = Colors.fromFloatMono(colorIdle);
        ret.hoverColor = Colors.fromFloatMono(colorHover);
        ret.affectTexture = affectTex;
        return ret;
    }

    private Styles() {}
}
