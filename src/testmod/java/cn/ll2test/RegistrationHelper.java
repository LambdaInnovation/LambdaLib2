package cn.ll2test;

import cn.ll2test.block.BlockStrangeCube;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RegistrationHelper {

    BlockStrangeCube blockStrangeCube = new BlockStrangeCube();

    public RegistrationHelper() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        System.out.println("RegisterBlocks called");

        event.getRegistry().register(blockStrangeCube);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemBlock(blockStrangeCube).setRegistryName(blockStrangeCube.getRegistryName()));
    }

}
