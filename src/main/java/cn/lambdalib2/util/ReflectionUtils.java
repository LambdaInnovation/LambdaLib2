/**
 * Copyright (c) Lambda Innovation, 2013-2016
 * This file is part of LambdaLib modding library.
 * https://github.com/LambdaInnovation/LambdaLib
 * Licensed under MIT, see project root for more information.
 */
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
