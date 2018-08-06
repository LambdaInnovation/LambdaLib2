/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib2.registry.mc.gui;

//import cn.lambdalib.annoreg.base.RegistrationInstance;
//import cn.lambdalib.annoreg.core.LoadStage;
//import cn.lambdalib.annoreg.core.RegistryTypeDecl;
//import cpw.mods.fml.relauncher.Side;
//import cpw.mods.fml.relauncher.SideOnly;

import cn.lambdalib2.auxgui.AuxGui;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.util.ReflectionUtils;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.List;

/**
 * AuxGui register annotation.
 * @author WeathFolD
 */

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@SideOnly(Side.CLIENT)
public @interface RegAuxGui {}

@SideOnly(Side.CLIENT)
class RegAuxGuiImpl {

//    public RegAuxGui() {
//        super(RegAuxGui.class, "AuxGui");
//        this.setLoadStage(LoadStage.INIT);
//    }

    @StateEventCallback
    @SuppressWarnings("unchecked")
    private static void init(FMLInitializationEvent ev) {
//        List<Class<?>> types = ReflectionUtils.getClasses(RegAuxGui.class);
//
//        types.forEach(type -> {
//            AuxGui.register((AuxGui)type);
//        });

        ReflectionUtils.getRawObjects(RegAuxGui.class.getCanonicalName(), true).forEach(it -> {
            try {
                Class<?> clz = Class.forName(it.getClassName(), true, Loader.instance().getModClassLoader());
                RegAuxGui anno = clz.getAnnotation(RegAuxGui.class);
                Field field = clz.getDeclaredField(it.getObjectName());
                AuxGui handlerBase = (AuxGui) field.get(null);
                //fixme: how to register class here?
                AuxGui.register(handlerBase);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

//    @Override
//    protected void register(AuxGui obj, RegAuxGui anno) throws Exception {
//        AuxGui.register(obj);
//    }

}
