package cn.ll2test;

import cn.lambdalib2.registry.StateEventCallback;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class TestLoadCallback {

    @StateEventCallback
    public static void testPreInit(FMLPreInitializationEvent ev) {
        System.out.println("TestPreInit called! " + ev);
    }

}
