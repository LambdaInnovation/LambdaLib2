package cn.lambdalib2;

import cn.lambdalib2.multiblock.MsgBlockMulti;
import cn.lambdalib2.registry.RegistryMod;
import cn.lambdalib2.s11n.network.NetworkEvent;
import cn.lambdalib2.s11n.network.NetworkMessage;
import cn.lambdalib2.util.DebugDraw;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

@RegistryMod(resourceDomain = "lambdalib2")
@Mod(modid = LambdaLib2.MODID, version = LambdaLib2.VERSION)
public class LambdaLib2
{
    public static final String MODID = "lambdalib2";
    public static final String VERSION = "@LAMBDA_LIB_VERSION@";

    /**
     * Whether we are in development (debug) mode.
     */
    public static final boolean DEBUG = VERSION.startsWith("@");

    public static final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
    public static Configuration config;

    private static Logger log;

    public static Logger getLogger() {
        return log;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        config = new Configuration(event.getSuggestedConfigurationFile());

        channel.registerMessage(NetworkEvent.MessageHandler.class, NetworkEvent.Message.class, 0, Side.CLIENT);
        channel.registerMessage(NetworkEvent.MessageHandler.class, NetworkEvent.Message.class, 1, Side.SERVER);
        channel.registerMessage(NetworkMessage.Handler.class, NetworkMessage.Message.class, 2, Side.CLIENT);
        channel.registerMessage(NetworkMessage.Handler.class, NetworkMessage.Message.class, 3, Side.SERVER);
        channel.registerMessage(MsgBlockMulti.ReqHandler.class, MsgBlockMulti.Req.class, 4, Side.SERVER);
        channel.registerMessage(MsgBlockMulti.Handler.class, MsgBlockMulti.class, 5, Side.CLIENT);
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
