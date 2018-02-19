package cn.ll2test.item;

import cn.ll2test.TestMod;
import net.minecraft.item.Item;

public class ItemTest extends Item {

    public ItemTest(String name) {
        setUnlocalizedName(name);
        setCreativeTab(TestMod.cct);
        setRegistryName(name);
    }

}
