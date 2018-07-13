/**
 * Copyright (c) Lambda Innovation, 2013-2016
 * This file is part of LambdaLib modding library.
 * https://github.com/LambdaInnovation/LambdaLib
 * Licensed under MIT, see project root for more information.
 */
package cn.lambdalib2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class SideHelper {

    private static ThreadLocal<SideHelper> threadProxy = new ThreadLocal<SideHelper>() {
        @Override
        protected SideHelper initialValue() {
            Side s = FMLCommonHandler.instance().getEffectiveSide();
            if (s.isClient()) {
                return new SideHelper(getClientProxy());
            } else {
                return new SideHelper(new ServerProxy());
            }
        }

        @SideOnly(Side.CLIENT)
        private ServerProxy getClientProxy() {
            return new ClientProxy();
        }
    };

    private final ServerProxy proxy;

    private SideHelper(ServerProxy proxy) {
        this.proxy = proxy;
    }

    public static World getWorld(int dimension) {
        return threadProxy.get().proxy.getWorld(dimension);
    }

    public static Container getPlayerContainer(EntityPlayer player, int windowId) {
        return threadProxy.get().proxy.getPlayerContainer(player, windowId);
    }

    public static Side getRuntimeSide() {
        return FMLCommonHandler.instance().getEffectiveSide();
    }

    public static EntityPlayer getThePlayer() {
        return threadProxy.get().proxy.getThePlayer();
    }

    public static EntityPlayer getPlayerOnServer(String name) {
        return threadProxy.get().proxy.getPlayerOnServer(name);
    }

    public static EntityPlayer[] getPlayerList() {
        return threadProxy.get().proxy.getPlayerList();
    }

    public static boolean isClient() {
        return getRuntimeSide().isClient();
    }

    private static class ServerProxy {

        public World getWorld(int dimension) {
            return DimensionManager.getWorld(dimension);
        }

        public Container getPlayerContainer(EntityPlayer player, int windowId) {
            Container ret = player.openContainer;
            if (ret.windowId == windowId) {
                return ret;
            }
            return null;
        }

        public EntityPlayer getThePlayer() {
            return null;
        }

        public EntityPlayer getPlayerOnServer(String name) {
            return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(name);
        }

        @SuppressWarnings("unchecked")
        public EntityPlayer[] getPlayerList() {
            List<? extends EntityPlayer> list = (List<? extends EntityPlayer>) FMLCommonHandler.instance().getMinecraftServerInstance()
                    .getPlayerList().getPlayers();
            return list.toArray(new EntityPlayer[list.size()]);
        }

    }

    @SideOnly(Side.CLIENT)
    private static class ClientProxy extends ServerProxy {

        @Override
        public World getWorld(int dimension) {
            World theWorld = Minecraft.getMinecraft().world;
            if (theWorld != null && theWorld.provider.getDimension() == dimension) {
                return theWorld;
            } else {
                return null;
            }
        }

        @Override
        public Container getPlayerContainer(EntityPlayer player, int windowId) {
            Container ret = Minecraft.getMinecraft().player.openContainer;
            if (ret.windowId == windowId) {
                return ret;
            } else {
                return null;
            }
        }

        @Override
        public EntityPlayer getThePlayer() {
            return Minecraft.getMinecraft().player;
        }

        @Override
        public EntityPlayer getPlayerOnServer(String name) {
            return null;
        }

        @Override
        public EntityPlayer[] getPlayerList() {
            return new EntityPlayer[]{};
        }
    }
}
