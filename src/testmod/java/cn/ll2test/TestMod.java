package cn.ll2test;

import cn.lambdalib2.registry.RegistryMod;
import cn.ll2test.client.render.TESRStrangeCube;
import cn.ll2test.tileentity.TileEntityStrangeCube;
import net.minecraft.block.Block;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@RegistryMod
@Mod(modid = "ll2test", version="0.1")
public class TestMod {

    static {
        new RegistrationHelper();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Initialized LambdaLib2 TestMod");
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    public void clientInit(FMLInitializationEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStrangeCube.class, new TESRStrangeCube());
        MinecraftForge.EVENT_BUS.register(new TestDebugDraw());
    }

}
