package $config.itemsPackageName;

/**
 * Automatically generated by LambdaLib2.xconf in $date.
 */
public class $config.itemsClassName {

#foreach ($item in $items)
    public static final $item.baseClass $item.id = new ${item.baseClass}($item.ctorArgs);
#end

    @RegistryCallback
    private static void registerItems(RegistryEvent.Register<Item> event) {
        Registry<Item> r = event.getRegistry();
    #foreach ($item in $items)
        #set($id = $item.id)
        ${id}.setRegistryName("$config.domain:$id");
        ${id}.setUnlocalizedName("$config.domain:$id");
        r.register($id);
    #end
    }

    @SideOnly(Side.CLIENT)
    @StateEventCallback
    private static void registerItemRenderers(FMLPreInitializationEvent event) {
    #foreach ($item in $items)
        ModelBakery.regItemRenderer($item.id, new ModelResourceLocation(new ResourceLocation("$config.domain:$item.id", "inventory"));
    #end
    }

}