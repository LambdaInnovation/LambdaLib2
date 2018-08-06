/**
 * Copyright (c) Lambda Innovation, 2013-2016
 * This file is part of LambdaLib modding library.
 * https://github.com/LambdaInnovation/LambdaLib
 * Licensed under MIT, see project root for more information.
 */
package cn.lambdalib2.registry.mc.gui;

//import cpw.mods.fml.common.network.IGuiHandler;
//import cpw.mods.fml.relauncher.Side;
//import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class GuiHandlerBase {

    private Object mod;
    private int guiId;

    private boolean isClientGuiOpening;

    private IGuiHandler guiHandler = new IGuiHandler() {
        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            return getServerContainer(player, world, x, y, z);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            if (isClientGuiOpening) {
                isClientGuiOpening = false;
                return getClientGui();
            } else {
                return getClientContainer(player, world, x, y, z);
            }
        }
    };

    /*
     * API for registry part.
     */

    void register(Object mod, int guiId) {
        this.mod = mod;
        this.guiId = guiId;
    }

    IGuiHandler getHandler() {
        return guiHandler;
    }

    /*
     * API for external call.
     */

    /**
     * Open a gui container. Should be called on SERVER ONLY.
     * Side check is enforced.
     * @param player
     * @param world
     * @param x
     * @param y
     * @param z
     */
    public final void openGuiContainer(EntityPlayer player, World world, int x, int y, int z) {
        if (!world.isRemote)
            player.openGui(mod, guiId, world, x, y, z);
    }

    /**
     * Called on client side to just open a GUI on client.
     */
    @SideOnly(Side.CLIENT)
    public final void openClientGui() {
        clientGui();
    }

    /**
     * Only open a GUI on client.
     * Can be overridden if you wants other behaviors not only just opening a GUI.
     */
    @SideOnly(Side.CLIENT)
    protected void clientGui() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        isClientGuiOpening = true;
        player.openGui(mod, guiId, player.world, 0, 0, 0);
    }

    /*
     * GUI/Container generation
     */

    @SideOnly(Side.CLIENT)
    protected GuiScreen getClientGui() {
        throw new UnsupportedOperationException();
    }

    @SideOnly(Side.CLIENT)
    protected Object getClientContainer(EntityPlayer player, World world, int x, int y, int z) {
        throw new UnsupportedOperationException();
    }

    protected Object getServerContainer(EntityPlayer player, World world, int x, int y, int z) {
        throw new UnsupportedOperationException();
    }
}
