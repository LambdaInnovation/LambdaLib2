package cn.ll2test;

import cn.lambdalib2.crafting.RecipeRegistry;
import cn.lambdalib2.registry.RegistryMod;
import cn.ll2test.client.render.TESRStrangeCube;
import cn.ll2test.item.TestItems;
import cn.ll2test.tileentity.TileEntityStrangeCube;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@RegistryMod(resourceDomain = "ll2test")
@Mod(modid = "ll2test", version="0.1")
public class TestMod {

    public static CreativeTabs cct = new CreativeTabs("LLTestMod") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.BONE);
        }
    };

    public static RecipeRegistry recipes = new RecipeRegistry();

    static {
        new RegistrationHelper();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Initialized LambdaLib2 TestMod");
        TestItems.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        recipes.addRecipeFromResourceLocation(new ResourceLocation("ll2test:recipes/default.recipe"));
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    public void clientInit(FMLInitializationEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStrangeCube.class, new TESRStrangeCube());
        MinecraftForge.EVENT_BUS.register(new TestDebugDraw());
    }

}
