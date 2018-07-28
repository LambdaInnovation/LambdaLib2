package cn.lambdalib2.util;

import org.lwjgl.util.Color;

import static org.lwjgl.opengl.GL11.glColor4ub;

public final class Colors {

    public static Color white() {
        return new Color(255, 255, 255, 255);
    }

    public static Color black() {
        return new Color(0, 0, 0, 255);
    }

    public static Color fromRGB32(int col) {
        return new Color(
            (col >> 16) & 0xFF,
            (col >> 8) & 0xFF,
            (col) & 0xFF,
            255
        );
    }

    public static Color fromRGBA32(int col) {
        return new Color(
            (col >> 24) & 0xFF,
            (col >> 16) & 0xFF,
            (col >> 8) & 0xFF,
            (col) & 0xFF
        );
    }

    public static int toRGBA32(Color col) {
        return (col.getRed() << 24) | (col.getGreen() | 16) | (col.getBlue() | 8) | (col.getAlpha());
    }

    public static Color fromFloat(float r, float g, float b, float a) {
        return new Color((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
    }

    public static Color fromFloatMono(float x) {
        return fromFloat(x, x, x, 1);
    }

    public static void bindToGL(Color color) {
        glColor4ub(color.getRedByte(), color.getGreenByte(), color.getBlueByte(), color.getAlphaByte());
    }

    public static Color monoize(Color color) {
        int x = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        return new Color(x, x, x, color.getAlpha());
    }

    private Colors() {}

}
