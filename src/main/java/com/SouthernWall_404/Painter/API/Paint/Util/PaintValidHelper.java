package com.SouthernWall_404.Painter.API.Paint.Util;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintAttachmentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class PaintValidHelper {

    public static boolean hasPaint(Level level,BlockPos blockPos)
    {

        if(PaintAttachmentHelper.getPaintInfo(level,blockPos).getRenders().containsKey(blockPos))return true;

        return false;

    }

    public static boolean hasPaint(Map<BlockPos, AbstractRender<?,?>> renders, BlockPos blockPos)
    {

        if(renders.containsKey(blockPos))return true;

        return false;

    }

    public static boolean isPaintable(Level level, BlockPos blockPos)
    {
        BlockState origin=level.getBlockState(blockPos);
        return isPaintable(origin,level,blockPos);
    }
    public static boolean isPaintable(BlockState origin,Level level,BlockPos blockPos)
    {

        if (origin.isCollisionShapeFullBlock(level,blockPos))
        {
            return true;//完整方块可行
        }
        if(origin.getBlock() instanceof SlabBlock)
        {
            return true;//半砖可行
        }

        return false;
    }
}
