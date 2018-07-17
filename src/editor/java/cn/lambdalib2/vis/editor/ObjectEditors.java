package cn.lambdalib2.vis.editor;

import cn.lambdalib2.cgui.Widget;
import cn.lambdalib2.cgui.component.DrawTexture;
import cn.lambdalib2.cgui.component.TextBox;
import cn.lambdalib2.cgui.component.TextBox.ChangeContentEvent;
import cn.lambdalib2.cgui.component.TextBox.ConfirmInputEvent;
import cn.lambdalib2.cgui.event.GuiEvent;
import cn.lambdalib2.util.Colors;
import cn.lambdalib2.util.Debug;
import cn.lambdalib2.render.font.IFont.FontOption;
import org.lwjgl.util.Color;

public class ObjectEditors {

    static class EditEvent implements GuiEvent {}

    interface ObjectEditor {
        void updateDisplay();
    }

    abstract static class EditBox extends Widget implements ObjectEditor {

        static final Color BACK_COLOR = Colors.fromFloatMono(0.2f);

        private boolean editing = false;

        final DrawTexture drawer;

        final TextBox text;

        EditBox() {
            drawer = new DrawTexture(null, BACK_COLOR);
            addComponent(drawer);

            text = Styles.newTextBox(new FontOption(9)).allowEdit();
            addComponent(text);

            transform.setSize(35, 10);

            listen(ChangeContentEvent.class, (w, ev) -> {
                drawer.color = Styles.COLOR_MODIFIED;
                editing = true;
            });

            listen(ConfirmInputEvent.class, (w, ev) -> {
                try {
                    setValue(text.content);
                    drawer.color = BACK_COLOR;
                    updateDisplay();
                    post(new EditEvent());
                } catch (Exception ex) {
                    Debug.error(ex);
                    text.setContent("<error>");
                }
                editing = false;
            });
        }

        @Override
        public void updateDisplay() {
            try {
                text.setContent(getDisplayValue());
            } catch(Exception e) {
                Debug.error("EditBox.updateDisplay", e);
            }
        }

        @Override
        public void onAdded() {
            super.onAdded();
            updateDisplay();
        }

        abstract String getDisplayValue();

        abstract void setValue(String content);

    }

    private ObjectEditors() {}
}
