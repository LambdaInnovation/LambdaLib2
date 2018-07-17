package cn.lambdalib2.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {

    /**
     * Get all the methods for a class, including those that are private or protected in parent class.
     * All the methods are made accessible.
     */
    public static List<Method> getAllAccessibleMethods(Class cls) {
        List<Method> ret = new ArrayList<>();

        while (cls != null) {
            for (Method m : cls.getDeclaredMethods()) {
                m.setAccessible(true);
                ret.add(m);
            }
            cls = cls.getSuperclass();
        }

        return ret;
    }

    public static Method getObfMethod(Class<?> cl, String methodName, String obfName, Class... parameterTypes) {
        Method m = null;
        try {
            try {
                m = cl.getDeclaredMethod(methodName, parameterTypes);
            } catch (Exception ignored) {
            }

            if (m == null)
                m = cl.getDeclaredMethod(obfName, parameterTypes);

            m.setAccessible(true);
            return m;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getObfFieldInstance(Class cl, Object instance, String normName, String obfName) {
        Field f = getObfField(cl, normName, obfName);
        try {
            return (T) f.get(instance);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T getFieldInstance(Class cl, Object instance, String name) {
        return getObfFieldInstance(cl, instance, name, name);
    }

    /**
     * Get a class field (both in workspace and in builds) by its deobf name and obf name.
     */
    public static Field getObfField(Class cl, String normName, String obfName) {
        Field f = null;
        try {
            try {
                f = cl.getDeclaredField(normName);
            } catch (Exception ignored) {}

            if (f == null) {
                f = cl.getDeclaredField(obfName);
            }
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
