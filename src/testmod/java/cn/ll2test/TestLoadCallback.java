package cn.ll2test;

import cn.lambdalib2.registry.RegistryCallback;
import cn.lambdalib2.registry.StateEventCallback;
import cn.lambdalib2.util.Debug;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class TestLoadCallback {

    @StateEventCallback
    public static void testPreInit(FMLPreInitializationEvent ev) {
        System.out.println("TestPreInit called! " + ev);
    }

    @RegistryCallback
    public static void registerItems(RegistryEvent<Item> ev) {
        System.out.println("RegistryEvent<Item> callback");
    }

    @StateEventCallback
    public static void testInit(FMLInitializationEvent ev) {
        Debug.log("Init called! " + ev);
    }

}
