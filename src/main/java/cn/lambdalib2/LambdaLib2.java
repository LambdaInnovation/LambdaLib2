package cn.lambdalib2;

import cn.lambdalib2.registry.RegistryMod;
import cn.lambdalib2.util.DebugDraw;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

@RegistryMod
@Mod(modid = LambdaLib2.MODID, version = LambdaLib2.VERSION)
public class LambdaLib2
{
    public static final String MODID = "lambdalib2";
    public static final String VERSION = "@VERSION@";

    /**
     * Whether we are in development (debug) mode.
     */
    public static final boolean DEBUG = VERSION.startsWith("@");

    public static Logger log;

    public static Configuration config;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        config = new Configuration(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        if(DEBUG) log.info("LambdaLib2 is running in development mode.");
    }

    @EventHandler
    @SideOnly(Side.CLIENT)
    public void initClient(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new DebugDraw());
    }
}
