package cn.lambdalib2;

import cn.lambdalib2.util.DebugDraw;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = LambdaLib2.MODID, version = LambdaLib2.VERSION)
public class LambdaLib2
{
    public static final String MODID = "lambdalib2";
    public static final String VERSION = "0.1";
    
    @EventHandler
    public void init(FMLInitializationEvent event) {

    }

    @EventHandler
    @SideOnly(Side.CLIENT)
    public void initClient(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new DebugDraw());
    }
}
