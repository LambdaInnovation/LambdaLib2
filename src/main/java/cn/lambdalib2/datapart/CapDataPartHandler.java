package cn.lambdalib2.datapart;

import cn.lambdalib2.registry.StateEventCallback;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Paindar on 17/10/19.
 */
public class CapDataPartHandler
{
    @CapabilityInject(IDataPart.class)
    static Capability<IDataPart> DATA_PART_CAPABILITY = null;

    static IStorage<IDataPart> storage = new IStorage<IDataPart>() {
        @Override
        public NBTBase writeNBT(Capability<IDataPart> capability, IDataPart instance, EnumFacing side)
        {
            NBTTagCompound nbt=new NBTTagCompound();
            instance.writeNBT(nbt);
            return nbt;
        }

        @Override
        public void readNBT(Capability<IDataPart> capability, IDataPart instance, EnumFacing side, NBTBase base)
        {
            if (! (instance instanceof EntityData))
                throw new RuntimeException("IDataPart instance does not implement EntityData");
            instance.readNBT((NBTTagCompound) base);
        }
    };

    @SubscribeEvent
    public void onAttachCapabilitiesEntity(AttachCapabilitiesEvent event)
    {
        if (event.getObject() instanceof EntityLivingBase)
        {
            ICapabilitySerializable<NBTTagCompound> provider = new CapDataPartDispatcher();
            event.addCapability(new ResourceLocation("lambda_lib"),provider);
        }
    }

    @StateEventCallback
    public static void register(FMLPreInitializationEvent event){
        MinecraftForge.EVENT_BUS.register(new CapDataPartHandler());
        CapabilityManager.INSTANCE.register(IDataPart.class, storage, EntityData::new);
    }
}
