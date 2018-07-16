package cn.lambdalib2.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {

    /**
     * Get all the methods for a class, including those that are private or protedted in parent class.
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

}
