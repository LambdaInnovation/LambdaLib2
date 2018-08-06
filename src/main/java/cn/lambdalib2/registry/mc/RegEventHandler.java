/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib2.registry.mc;

//import cn.lambdalib.annoreg.base.RegistrationInstance;
//import cn.lambdalib.annoreg.core.LoadStage;
//import cn.lambdalib.annoreg.core.RegistryTypeDecl;
//import cpw.mods.fml.common.FMLCommonHandler;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.util.ReflectionUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Registers the class as a listener into either FML or Forge bus.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegEventHandler {

    public enum Bus {
        FML,
        Forge,
    }

    Bus[] value() default {Bus.FML, Bus.Forge};
}



class RegEventHandlerImpl {

    @StateEventCallback
    @SuppressWarnings("unchecked")
    private static void init(FMLInitializationEvent ev) {
        ReflectionUtils.getRawObjects(RegEventHandler.class.getCanonicalName(), true).forEach(it -> {
            try {
                Class<?> clz = Class.forName(it.getClassName(), true, Loader.instance().getModClassLoader());
//                RegEventHandler anno = clz.getAnnotation(RegEventHandler.class);
                List<ModAnnotation.EnumHolder> tholder = (List) it.getAnnotationInfo().get("value");
                Field field = clz.getDeclaredField(it.getObjectName());
                Object obj = field.get(null);
                System.out.println(obj);
                for (ModAnnotation.EnumHolder bus : tholder) {
                    register(obj, RegEventHandler.Bus.valueOf(bus.getValue()));
                }

            } catch (Exception e) {
                System.out.println(e.getStackTrace());
                throw new RuntimeException(e);
            }
        });
    }

    private static void register(Object obj, RegEventHandler.Bus bus) throws Exception {
            switch (bus) {
            case FML:
                FMLCommonHandler.instance().bus().register(obj);
                break;
            case Forge:
                MinecraftForge.EVENT_BUS.register(obj);
                break;
            default:
            }
    }
    
}
