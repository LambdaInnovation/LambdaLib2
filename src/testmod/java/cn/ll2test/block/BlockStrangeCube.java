package cn.ll2test.block;

import cn.ll2test.tileentity.TileEntityStrangeCube;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;

public class BlockStrangeCube extends Block implements ITileEntityProvider {

    public BlockStrangeCube() {
        super(Material.ROCK);
        setCreativeTab(CreativeTabs.MISC);
        setRegistryName("ll2test:strange_cube");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityStrangeCube();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.INVISIBLE;
    }


}
