package cn.lambdalib2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A simple timer wrapup to handle paused timing situations.
 * @author WeAthFolD
 */
public enum GameTimer {
    INSTANCE;

    GameTimer() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    static long storedTime, timeLag;

    static long beginTimeClient, beginTimeServer;

    public static double getTime() {
        boolean isClient = FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
        return getTime(isClient);
    }

    public static double getAbsTime() {
        return getTime(false);
    }

    private static double getTime(boolean isClient) {
        if (isClient) {
            if (beginTimeClient == 0) beginTimeClient = getRawTimeClient();
            long elapsed = getRawTimeClient() - beginTimeClient;
            return elapsed / 1000.0;
        } else {
            if (beginTimeServer == 0) beginTimeServer = getRawTimeServer();
            long elapsed = getRawTimeServer() - beginTimeServer;
            return elapsed / 1000.0;
        }
    }

    @SideOnly(Side.CLIENT)
    private static long getRawTimeClient() {
        long time = Minecraft.getSystemTime();
        if(Minecraft.getMinecraft().isGamePaused()) {
            timeLag = time - storedTime;
        } else {
            storedTime = time - timeLag;
        }
        return time - timeLag;
    }

    private static long getRawTimeServer() {
        return MinecraftServer.getCurrentTimeMillis();
    }

    // In case GameTimer isn't queried frequently, use this to prevent huge (and incorrect) time lag.
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        getRawTimeClient();
    }

}
