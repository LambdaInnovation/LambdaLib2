/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib2.cgui.component;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import cn.lambdalib2.cgui.*;
import cn.lambdalib2.s11n.SerializeIncluded;
import cn.lambdalib2.util.GameTimer;
import cn.lambdalib2.util.client.font.Fonts;
import cn.lambdalib2.util.client.font.IFont;
import cn.lambdalib2.util.client.font.IFont.FontOption;
import cn.lambdalib2.util.client.font.TrueTypeFont;
import cn.lambdalib2.cgui.component.Transform.HeightAlign;
import net.minecraft.util.text.translation.I18n;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cn.lambdalib2.cgui.event.FrameEvent;
import cn.lambdalib2.cgui.event.GuiEvent;
import cn.lambdalib2.cgui.event.KeyEvent;
import cn.lambdalib2.cgui.event.LeftClickEvent;
import net.minecraft.util.ChatAllowedCharacters;

import javax.vecmath.Vector2d;

/**
 * Textbox displays text on the widget area, it might also be edited. TextBox is designed to handle single-line texts.
 * @author WeAthFolD
 */
public class TextBox extends Component {
    
    /**
     * Fired each time the TextBox's content is being edited.
     */
    public static class ChangeContentEvent implements GuiEvent {}
    
    /**
     * Fired each time the TextBox's input is confirmed. (a.k.a. User presses enter)
     */
    public static class ConfirmInputEvent implements GuiEvent {}
    
    public String content = "";

    @SerializeIncluded
    public IFont font = Fonts.getDefault();

    public FontOption option;

    public HeightAlign heightAlign = HeightAlign.CENTER;

    /**
     * Only activated when doesn't allow edit. If activated, The display string will be
     *  <code>StatCollector.translateToLocal(content).</code>
     */
    public boolean localized = false;

    /**
     * Whether the editing is enabled.
     */
    public boolean allowEdit = false;

    /**
     * Whether this textBox doesn't draw chars that are out of bounds.
     */
    public boolean emit = true;

    public boolean doesEcho = false;
    public char echoChar = '*';
    
    public double zLevel = 0;

    public double xOffset, yOffset;

    private int caretPos = 0;

    private int displayOffset = 0;

    public TextBox() {
        this(new FontOption());
    }

    public TextBox(FontOption _option) {
        super("TextBox");
        this.option = _option;
    }

    {
        // Draws the content
        listen(FrameEvent.class, (w, e) -> {
            validate();

            final Vector2d origin = origin();
            final double widthLimit = w.transform.width - xOffset;

            final String processed = processedContent().substring(displayOffset);

            final int localCaret = caretPos - displayOffset; // ∈[0, processed.length]

            double acc = 0.0;
            int i = processed.length();
            if (emit) {
                for (i = 0; i < processed.length() && acc < widthLimit; ++i) {
                    acc += font.getCharWidth(processed.codePointAt(i), option);
                }
            }

            final String display = processed.substring(0, i);

            GL11.glPushMatrix();
            GL11.glTranslated(0, 0, zLevel);

            font.draw(display, origin.x, origin.y, option);

            if (w.isFocused() && allowEdit && GameTimer.getAbsTime() % 2 < 1) {
                font.draw("|", origin.x + sumLength(display, 0, localCaret), origin.y - 1, option);
            }

            GL11.glPopMatrix();
        });

        // Handles input
        listen(KeyEvent.class, (__, evt) -> {
            if (!allowEdit) {
                return;
            }

            final char input = evt.inputChar;
            final int keyCode = evt.keyCode;

            if (keyCode == Keyboard.KEY_RIGHT) {
                caretPos = Math.min(content.length(), caretPos + 1);
                checkCaretRegion();
            } else if (keyCode == Keyboard.KEY_LEFT) {
                caretPos = Math.max(0, caretPos - 1);
                if (caretPos < displayOffset) {
                    displayOffset = caretPos;
                }
            } else if (keyCode == Keyboard.KEY_V && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                setContent(content.substring(0, caretPos) + getClipboardContent() + content.substring(caretPos));
                validate();

                widget.post(new ChangeContentEvent());
            } else if (keyCode == Keyboard.KEY_C && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
                saveClipboardContent();
            } else if (keyCode == Keyboard.KEY_BACK) {
                if (caretPos != 0) {
                    content = content.substring(0, caretPos - 1) + content.substring(caretPos);
                    --caretPos;
                    if (displayOffset != 0) {
                        --displayOffset;
                    }
                    widget.post(new ChangeContentEvent());

                    checkCaretRegion();
                    validate();
                }
            } else if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                widget.post(new ConfirmInputEvent());
            } else if (keyCode == Keyboard.KEY_DELETE) {
                content = "";
                widget.post(new ChangeContentEvent());

                validate();
            } else if (ChatAllowedCharacters.isAllowedCharacter(input)) {
                content = content.substring(0, caretPos) + input + content.substring(caretPos);
                caretPos = Math.min(content.length(), caretPos + 1);
                widget.post(new ChangeContentEvent());

                checkCaretRegion();
            }
        });

        // Mouse caret pos selection
        listen(LeftClickEvent.class, (w, evt) -> {
            if (!allowEdit) {
                return;
            }

            final Vector2d origin = origin();
            final String display = processedContent().substring(displayOffset);
            final double rel_x = origin.x - font.getTextWidth(display, option) * option.align.lenOffset + evt.x;

            double acc = 0.0;
            int ind = 0;
            for (; acc < rel_x && ind < display.length(); ++ind) {
                acc += font.getCharWidth(display.codePointAt(ind), option);
            }

            if (ind > 0 && rel_x < acc - font.getCharWidth(display.codePointAt(ind - 1), option) * 0.5) {
                ind--;
            }

            caretPos = displayOffset + ind;
            checkCaretRegion();
        });
    }
    
    public TextBox allowEdit() {
        allowEdit = true;
        return this;
    }
    
    public TextBox setContent(String str) {
        content = str;
        return this;
    }

    public TextBox setFont(IFont font) {
        this.font = font;
        return this;
    }

    private void validate() {
        if (!allowEdit) {
            displayOffset = caretPos = 0;
            return;
        }

        if (displayOffset >= content.length() || caretPos > content.length()) {
            displayOffset = caretPos = 0;
        }
    }

    private Vector2d origin() {
        return new Vector2d(
                widget.transform.width * option.align.lenOffset + xOffset,
                Math.max(0, widget.transform.height - option.fontSize) * heightAlign.factor + yOffset
        );
    }

    private boolean shouldLocalize() {
        return !allowEdit && localized;
    }

    private void checkCaretRegion() {
        final double widthLimit = widthLimit();
        final String local = processedContent().substring(displayOffset);
        final int localCaret = caretPos - displayOffset;
        final double distance = sumLength(local, 0, localCaret);
        if (distance > widthLimit) {
            double acc = 0.0;
            int mini = 0;
            for (; mini < localCaret && distance - acc > widthLimit; ++mini) {
                acc += font.getCharWidth(local.codePointAt(mini), option);
            }
            displayOffset += mini;
        }

        if (displayOffset >= caretPos) {
            displayOffset = Math.max(0, caretPos - 1);
        }

        assert caretPos == 0 || displayOffset < caretPos;
    }

    private double widthLimit() {
        return widget.transform.width - xOffset;
    }

    private String processedContent() {
        String ret = content;
        if (shouldLocalize()) {
            ret = I18n.translateToLocal(ret);
        }
        if (doesEcho) {
            ret = StringUtils.repeat(echoChar, ret.length());
        }

        return ret;
    }

    private double sumLength(String str, int begin, int end) {
        return font.getTextWidth(str.substring(begin, end), option);
    }
    
    private String getClipboardContent() {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        if(cb.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            try {
                return (String) cb.getData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException|IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
    
    private void saveClipboardContent() {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection ss = new StringSelection(content);
        cb.setContents(ss, ss);
    }
    
    public static TextBox get(Widget w) {
        return w.getComponent("TextBox");
    }

}
