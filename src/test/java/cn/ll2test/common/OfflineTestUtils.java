package cn.ll2test.common;

import com.google.common.base.Strings;

import java.io.File;
import java.lang.reflect.Field;

public class OfflineTestUtils {

    public static void hackNatives() {
        String paths = System.getProperty("java.library.path");
        String nativesDir = new File(System.getProperty("user.home"), ".gradle/caches/minecraft/net/minecraft/natives/1.12.2").getAbsolutePath();
        if (Strings.isNullOrEmpty(paths)) {
            paths = nativesDir;
        } else {
            paths = paths + File.pathSeparator + nativesDir;
        }

        System.setProperty("java.library.path", paths);

        try {
            Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
            sysPathsField.setAccessible(true);
            sysPathsField.set(null, null);
        } catch (Throwable ignored) {}
    }

}
