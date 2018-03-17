package cn.ll2test.item;

import cn.ll2test.TestMod;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class TestItems {

    public static ItemTest test1 = new ItemTest("test1"),
        test2 = new ItemTest("test2"),
        test3 = new ItemTest("test3");

    public static void init() {
        ForgeRegistries.ITEMS.register(test1);
        ForgeRegistries.ITEMS.register(test2);
        ForgeRegistries.ITEMS.register(test3);

        OreDictionary.registerOre("tests", test2);
        OreDictionary.registerOre("tests", test3);

        TestMod.recipes.map("tt1", test1);
        TestMod.recipes.map("tt2", test2);
        TestMod.recipes.map("tt3", test3);
    }

}
