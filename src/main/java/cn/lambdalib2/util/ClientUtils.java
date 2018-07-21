package cn.lambdalib2.util;

import cn.lambdalib2.auxgui.AuxGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client-Side judgement helper and other stuffs.
 * @author WeAthFolD
 */
@SideOnly(Side.CLIENT)
public class ClientUtils {
    
    /**
     * @return whether the player is playing the client game and isn't opening any GUI.
     */
    public static boolean isPlayerInGame() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        return player != null && Minecraft.getMinecraft().currentScreen == null && !AuxGuiHandler.hasForegroundGui();
    }

    public static boolean isInWorld() {
        return Minecraft.getMinecraft().player != null;
    }
    
    public static boolean isPlayerPlaying() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.player != null && !mc.isGamePaused();
    }
    
    /**
     * Quick alias for playing static sound
     * @param src
     * @param pitch
     */
    public static void playSound(ResourceLocation src, float pitch) {
        Minecraft.getMinecraft().getSoundHandler().playSound(
            PositionedSoundRecord.getMasterRecord(new SoundEvent(src), pitch));
    }

}
