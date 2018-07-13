package cn.lambdalib2.vis.editor;

import cn.lambdalib2.LambdaLib2;
import net.minecraftforge.common.config.Configuration;

import java.util.Objects;
import java.util.Optional;


class VisConfig {

    private static final String
        KEY_WORKDIR = "working_dirs",
        KEY_CURDIR = "curr_working_dir";

    private static final String[] EMPTY_STR_ARR = new String[0];

    private static final Configuration config = Objects.requireNonNull(LambdaLib2.config);

    public static String[] getWorkDirs() {
        return config.getStringList(KEY_WORKDIR, "vis", EMPTY_STR_ARR, "Working dirs");
    }

    public static void setWorkDirs(String[] result) {
        config.get("vis", KEY_WORKDIR, EMPTY_STR_ARR).set(result);
    }

    public static Optional<String> getCurrentDir() {
        return Optional.ofNullable(config.getString("vis", KEY_CURDIR, null, "Current dir"));
    }

    public static void setCurrentDir(String dir) {
        config.get("vis", KEY_CURDIR, dir).set(dir);
    }

    private VisConfig() {}

}
