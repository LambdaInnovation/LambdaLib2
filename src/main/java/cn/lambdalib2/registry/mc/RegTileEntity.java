package cn.lambdalib2.registry.mc;

import cn.lambdalib2.registry.RegistryContext;
import cn.lambdalib2.registry.RegistryMod;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.util.ReflectionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegTileEntity {
}

class RegTileEntityImpl {
    @StateEventCallback
    @SuppressWarnings("unchecked")
    private static void init(FMLInitializationEvent ev) {
        ReflectionUtils.getClasses(RegTileEntity.class)
            .forEach(type -> {
                Object mod = RegistryContext.getModForPackage(type.getCanonicalName());
                GameRegistry.registerTileEntity(
                    ((Class<? extends TileEntity>) type), type.getSimpleName()
                );
            });
    }
}