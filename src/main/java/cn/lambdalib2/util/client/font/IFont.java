/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib2.util.client.font;

import cn.lambdalib2.util.ColorUtils;
import cn.lambdalib2.util.client.font.Fragmentor.IFontSizeProvider;
import cn.lambdalib2.s11n.SerializeType;
import org.lwjgl.util.Color;

import java.util.List;

/**
 * A generic font interface.
 */
public interface IFont {

    enum FontAlign {
        LEFT(0), CENTER(0.5), RIGHT(1);

        public final double lenOffset;

        FontAlign(double _lenOffset) {
            lenOffset = _lenOffset;
        }
    }

    class Extent {
        public int linesDrawn;
        public double width;
        public double height;

        public Extent(int _lines, double _width, double _height) {
            linesDrawn = _lines;
            width = _width;
            height = _height;
        }
    }

    @SerializeType
    class FontOption {

        public double fontSize;
        public FontAlign align;
        public Color color;

        public FontOption() {
            this(10);
        }

        public FontOption(double _fontsz) {
            this(_fontsz, FontAlign.LEFT);
        }

        public FontOption(double _fontsz, Color _color) {
            this(_fontsz, FontAlign.LEFT, _color);
        }

        public FontOption(double _fontsz, int hex) {
            this(_fontsz, ColorUtils.fromRGBA32(hex));
        }

        public FontOption(double _fontsz, FontAlign _align) {
            this(_fontsz, _align, ColorUtils.white());
        }

        public FontOption(double _fontsz, FontAlign _align, Color _color) {
            fontSize = _fontsz;
            align = _align;
            color = _color;
        }

        public FontOption(double _fontsz, FontAlign _align, int hex) {
            this(_fontsz, _align, ColorUtils.fromRGBA32(hex));
        }

        @Override
        public FontOption clone() {
            FontOption ret = new FontOption();
            ret.fontSize = fontSize;
            ret.align = align;
            return ret;
        }

    }

    /**
     * Draws the string at the given position with given font option in one line. <br>
     *
     * The string is assumed to not include line-seperate characters. (\n or \r). Violating this yields undefined
     *     behaviour.
     */
    void draw(String str, double x, double y, FontOption option);

    /**
     * Get the width of given character when drawed with given FontOption.
     */
    double getCharWidth(int chr, FontOption option);

    /**
     * Get the text width that will be drawn if calls the {@link IFont#draw}.
     */
    double getTextWidth(String str, FontOption option);

    /**
     * Draws a line-seperated string at the given position.
     */
    default void drawSeperated(String str, final double x, double y, double limit, FontOption option) {
        List<String> lines = Fragmentor.toMultiline(str, provider(option), limit);
        for (int i = 0; i < lines.size(); ++i) {
            draw(lines.get(i), x, y + i * option.fontSize, option);
        }
    }

    /**
     * Simulates the {@link IFont#drawSeperated} and return the extent drawn.
     * @return A {@link Extent} describing the drawn area
     */
    default Extent drawSeperated_Sim(String str, double limit, FontOption option) {
        List<String> lines = Fragmentor.toMultiline(str, provider(option), limit);
        return new Extent(lines.size(), lines.size() == 1 ? getTextWidth(lines.get(0), option) : limit
            , lines.size() * option.fontSize);
    }

    default IFontSizeProvider provider(FontOption option) {
        return new IFontSizeProvider() {
            @Override
            public double getCharWidth(int chr) {
                return IFont.this.getCharWidth(chr, option);
            }

            @Override
            public double getTextWidth(String str) {
                return IFont.this.getTextWidth(str, option);
            }
        };
    }

}
