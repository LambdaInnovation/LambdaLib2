package cn.lambdalib2.registry.mc.gui;

import cn.lambdalib2.registry.RegistryContext;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.util.Debug;
import cn.lambdalib2.util.ReflectionUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegGuiHandler {
}


class RegGuiHandlerImpl {

    private static Map<Object, ModGuiHandler> modHandlers = new HashMap();

    private static void regHandler(Object mod, GuiHandlerBase handler) {
        ModGuiHandler modHandler = modHandlers.get(mod);
        if (modHandler == null) {
            modHandler = new ModGuiHandler();
            modHandlers.put(mod, modHandler);
            NetworkRegistry.INSTANCE.registerGuiHandler(mod, modHandler);
        }
        int id = modHandler.addHandler(handler.getHandler());
        handler.register(mod, id);
    }

    protected static void register(Object mod, GuiHandlerBase value, RegGuiHandler anno, String field) throws Exception {
        regHandler(mod, value);
    }

    @StateEventCallback
    @SuppressWarnings("unchecked")
    private static void init(FMLInitializationEvent ev) {
        ReflectionUtils.getRawObjects(RegGuiHandler.class.getCanonicalName(), true).forEach(it -> {
            try {
                Class<?> clz = Class.forName(it.getClassName(), true, Loader.instance().getModClassLoader());
                RegGuiHandler anno = clz.getAnnotation(RegGuiHandler.class);
                Object mod = RegistryContext.getModForPackage(clz.getCanonicalName());
                Field field = clz.getDeclaredField(it.getObjectName());
                GuiHandlerBase handlerBase = (GuiHandlerBase) field.get(null);
                register(mod, handlerBase, anno, it.getObjectName());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


//    private static Map<Object, Integer> counterMap = new HashMap<>();

    private static class ModGuiHandler implements IGuiHandler {

        private List<IGuiHandler> subHandlers = new ArrayList();

        public int addHandler(IGuiHandler handler) {
            subHandlers.add(handler);
            return subHandlers.size() - 1;
        }

        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            if (ID >= subHandlers.size()) {
                Debug.error("Invalid GUI id on server.");
                return null;
            }
            return subHandlers.get(ID).getServerGuiElement(0, player, world, x, y, z);
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            if (ID >= subHandlers.size()) {
                Debug.error("Invalid GUI id on client.");
                return null;
            }
            return subHandlers.get(ID).getClientGuiElement(0, player, world, x, y, z);
        }

    }
}
