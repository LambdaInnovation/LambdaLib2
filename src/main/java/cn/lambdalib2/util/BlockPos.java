/**
 * Copyright (c) Lambda Innovation, 2013-2016
 * This file is part of LambdaLib modding library.
 * https://github.com/LambdaInnovation/LambdaLib
 * Licensed under MIT, see project root for more information.
 */
package cn.lambdalib2.util;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * World+xyz, representing a single block position.
 * @author WeAthFolD
 */
public class BlockPos {

    public final World world;

    public final int x, y, z;

    public BlockPos(World _world, int _x, int _y, int _z) {
        world = _world;
        x = _x;
        y = _y;
        z = _z;
    }

    public Block getBlock() {
        return world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)).getBlock();
    }

    public TileEntity getTile() {
        return world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
    }

    @Override
    public String toString() {
        return "BlockPos[" + world.provider.getDimension() + " (" + x + ", " + y + ", " + z + ")]";
    }

    @Override
    public int hashCode() {
        return x ^ y ^ z + world.provider.getDimension();
    }

    @Override
    public boolean equals(Object another) {
        if (!(another instanceof BlockPos))
            return false;
        BlockPos b = (BlockPos) another;
        return b.world.equals(world) && b.x == x && b.y == y && b.z == z;
    }

}
