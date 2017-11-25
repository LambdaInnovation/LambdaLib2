package cn.lambdalib2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
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
        FMLCommonHandler.instance().bus().register(this);
    }

    static long storedTime, timeLag;

    public static long getTime() {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT ? getTimeClient() : getTimeServer();
    }

    public static long getAbsTime() {
        return MinecraftServer.getCurrentTimeMillis();
    }

    @SideOnly(Side.CLIENT)
    private static long getTimeClient() {
        long time = Minecraft.getSystemTime();
        if(Minecraft.getMinecraft().isGamePaused()) {
            timeLag = time - storedTime;
        } else {
            storedTime = time - timeLag;
        }
        return time - timeLag;
    }

    private static long getTimeServer() {
        return MinecraftServer.getCurrentTimeMillis();
    }

    // In case GameTimer isn't queried frequently, use this to prevent huge (and incorrect) time lag.
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        getTimeClient();
    }

}
