package cn.lambdalib2.util;

import cn.lambdalib2.LambdaLib2;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Generic debug utils.
 * @author WeAthFolD
 */
public class Debug {

    private static Logger logger = Objects.requireNonNull(LambdaLib2.log);

    public static void assert2(boolean expr) {
        assert2(expr, "Assersion failed");
    }

    public static void assert2(boolean expr, String message) {
        if (!expr) {
            throw new RuntimeException("Assertion failed: " + message);
        }
    }

    public static void require(boolean expr) {
        require(expr, "Requirement failed");
    }

    public static void require(boolean expr, String message) {
        if (!expr) {
            throw new RuntimeException("Requirement failed: " + message);
        }
    }

    public static <T> T assertNotNull(T obj) {
        return assertNotNull(obj, "Object is null");
    }

    public static <T> T assertNotNull(T obj, String message) {
        return Objects.requireNonNull(obj, message);
    }

    public static void log(String msg) {
        logger.info(msg);
    }

    public static void logFormat(String format, Object... params) {
        log(String.format(format, params));
    }

    public static void error(Exception ex) {
        logger.error(ex);
    }

    public static void error(String msg, Exception ex) {
        logger.error(msg, ex);
    }

    public static void error(String msg) {
        logger.error(msg);
    }

    private Debug() {}
}
