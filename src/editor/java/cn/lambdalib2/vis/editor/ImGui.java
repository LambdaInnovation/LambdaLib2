package cn.lambdalib2.vis.editor;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

public class ImGui {
    static {
        try {
            URL res = ImGui.class.getResource("/imgui_64.dll");
            File f = Paths.get(res.toURI()).toFile();
            System.load(f.getAbsolutePath());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // Windows
    public static native boolean Begin(String name, int flags);

    public static native void End();

    // Widgets: Text
    public static native void Text(String s);

}
