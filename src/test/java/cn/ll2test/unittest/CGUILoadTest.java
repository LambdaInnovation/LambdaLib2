package cn.ll2test.unittest;

import cn.lambdalib2.cgui.WidgetContainer;
import cn.lambdalib2.cgui.component.DrawTexture;
import cn.lambdalib2.cgui.loader.CGUIDocument;

import java.util.Objects;

import static java.lang.System.*;

public class CGUILoadTest {

    public static void main(String[] args) {
        new CGUILoadTest().start();
    }

    private void start() {
        WidgetContainer container = CGUIDocument.read(getClass().getResourceAsStream("/cgui/terminal.xml"));

        out.println("SUCCESS: XML Load");

        DrawTexture dt = container.getWidget("back/icon").getComponent(DrawTexture.class);
        if (dt != null) {
            out.println("SUCCESS: structure");
        } else {
            out.println("FAIL: structure");
            return;
        }

        if (dt.texture.getResourcePath().endsWith("data_terminal/logo.png")) {
            out.println("SUCCESS: component data");
        } else {
            out.println("FAIL: component data");
        }
    }


}
