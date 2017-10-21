package cn.ll2test.common;

import com.google.common.base.Strings;

import java.io.File;
import java.lang.reflect.Field;

public class OfflineTestUtils {

    public static void hackNatives() {
        String paths = System.getProperty("java.library.path");
        String nativesDir = "C:/Users/WeAth/.gradle/caches/minecraft/net/minecraft/natives/1.12.2";
        if (Strings.isNullOrEmpty(paths)) {
            paths = nativesDir;
        } else {
            paths = paths + File.pathSeparator + nativesDir;
        }

        System.setProperty("java.library.path", paths);

        try {
            Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
            sysPathsField.setAccessible(true);
            sysPathsField.set((Object)null, (Object)null);
        } catch (Throwable var3) {
            ;
        }

    }

}
