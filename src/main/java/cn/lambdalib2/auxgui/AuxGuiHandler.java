package cn.lambdalib2.auxgui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cn.lambdalib2.util.GameTimer;
import cn.lambdalib2.util.RenderUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.opengl.GL14;

/**
 * @author WeathFolD
 *
 */
@SideOnly(Side.CLIENT)
public class AuxGuiHandler {

    private static AuxGuiHandler instance = new AuxGuiHandler();
    
    private AuxGuiHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private static boolean iterating;
    private static List<AuxGui> auxGuiList = new LinkedList<>();
    private static List<AuxGui> toAddList = new ArrayList<>();
    
    public static void register(AuxGui gui) {
        if(!iterating)
            doAdd(gui);
        else
            toAddList.add(gui);
    }

    public static List<AuxGui> active() {
        return ImmutableList.copyOf(auxGuiList);
    }

    public static boolean hasForegroundGui() {
        return auxGuiList.stream().anyMatch(gui -> !gui.disposed && gui.foreground);
    }

    private static void doAdd(AuxGui gui) {
        auxGuiList.add(gui);
        MinecraftForge.EVENT_BUS.post(new OpenAuxGuiEvent(gui));
        gui.onEnable();
    }
    
    private static void startIterating() {
        iterating = true;
    }
    
    private static void endIterating() {
        iterating = false;
    }
    
    @SubscribeEvent
    public void drawHudEvent(RenderGameOverlayEvent event) {
        if(event.getType() == ElementType.EXPERIENCE) {
            doRender(event);
        }
    }

    private void doRender(RenderGameOverlayEvent event) {
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderUtils.pushTextureState();

        Iterator<AuxGui> iter = auxGuiList.iterator();
        startIterating();
        while(iter.hasNext()) {
            AuxGui gui = iter.next();
            if(!gui.disposed) {
                if(!gui.lastFrameActive)
                    gui.lastActivateTime = GameTimer.getTime();
                gui.draw(event.getResolution());
                gui.lastFrameActive = true;
            }
        }
        endIterating();

        RenderUtils.popTextureState();
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDepthMask(true);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }
    
    @SubscribeEvent
    public void clientTick(ClientTickEvent event) {
        if(!Minecraft.getMinecraft().isGamePaused()) {
            for(AuxGui gui : toAddList)
                doAdd(gui);
            toAddList.clear();
            
            Iterator<AuxGui> iter = auxGuiList.iterator();
            startIterating();
            while(iter.hasNext()) {
                AuxGui gui = iter.next();
                
                if(gui.disposed) {
                    gui.onDisposed();
                    gui.lastFrameActive = false;
                    iter.remove();
                } else if(gui.requireTicking) {
                    if(!gui.lastFrameActive)
                        gui.lastActivateTime = GameTimer.getTime();
                    gui.onTick();
                    gui.lastFrameActive = true;
                }
            }
            endIterating();
        }
    }
    
    @SubscribeEvent
    public void disconnected(ClientDisconnectionFromServerEvent event) {
        startIterating();
        Iterator<AuxGui> iter = auxGuiList.iterator();
        while(iter.hasNext()) {
            AuxGui gui = iter.next();
            if(!gui.consistent) {
                gui.onDisposed();
                iter.remove();
            }
        }
        endIterating();
    }
    

}
