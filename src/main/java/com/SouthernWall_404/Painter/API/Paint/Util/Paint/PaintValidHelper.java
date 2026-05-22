package com.SouthernWall_404.Painter.API.Paint.Util.Paint;

import com.SouthernWall_404.LaplaceAPI.UlrichToolBox.Blocks.BlockUtil;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.Common.World.Item.ChulkItem;
import com.SouthernWall_404.Painter.Common.World.Item.PaintBucketItem;
import com.SouthernWall_404.Painter.Common.World.Item.PaintItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class PaintValidHelper {

    public static boolean hasPaint(Level level,BlockPos blockPos)
    {

        if(PaintAttachmentHelper.getPaintInfo(level,blockPos).getPaints().containsKey(blockPos))return true;

        return false;

    }

    public static boolean hasPaint(Map<BlockPos, AbstractPaint> renders, BlockPos blockPos)
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

        if (BlockUtil.isFullBlock(level,blockPos,origin))
        {
            return true;//完整方块可行
        }
        if(origin.getBlock() instanceof SlabBlock)
        {
            return true;//半砖可行
        }

        return false;
    }

    /**
     * 用于检验是否为合法的喷涂行动
     * @param blockToPaint
     * @param level
     * @param blockPos
     * @param blockToBePainted
     * @return
     */
    public static boolean isValidPaintOperation(BlockState blockToPaint,Level level,BlockPos blockPos,BlockState blockToBePainted)
    {
        if(BlockUtil.isFullBlock(level,blockPos,blockToPaint))
        {
            if(isPaintable(blockToBePainted,level,blockPos))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否应该渲染选中区域
     * @param itemStack 要检查的物品堆
     * @return 如果物品是 PaintItem、ChulkItem 或 PaintBucketItem 则返回 true
     */
    public static boolean shouldRenderSelected(ItemStack itemStack) {
        Item item=itemStack.getItem();
        return item instanceof PaintItem ||
                item instanceof ChulkItem ||
                item instanceof PaintBucketItem;
    }
}
