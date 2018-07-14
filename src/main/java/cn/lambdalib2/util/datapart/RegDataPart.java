/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib2.util.datapart;


import net.minecraftforge.fml.relauncher.Side;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegDataPart {

    /**
     * @return The type that this DataPart applies on. Also applies for all subclasses.
     */
    Class value();

    /**
     * @return At what sides this DataPart should be constructed
     */
    Side[] side() default { Side.CLIENT, Side.SERVER };

    /**
     * @return Whether this DataPart should be lazily constructed.
     */
    boolean lazy() default false;
    
}
