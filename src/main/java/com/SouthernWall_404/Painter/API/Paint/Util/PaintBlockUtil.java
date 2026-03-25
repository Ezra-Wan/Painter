package com.SouthernWall_404.Painter.API.Paint.Util;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Imply.SimpleBlockPaint;
import com.SouthernWall_404.Painter.API.Paint.Imply.SlabBlockPaint;
import com.SouthernWall_404.Painter.Common.Init.ModBlock;
import com.SouthernWall_404.Painter.Common.World.Block.PaintBlock;
import com.SouthernWall_404.Painter.Common.World.BlockEntity.PaintBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PaintBlockUtil {

    public static void paint(Level level, BlockPos blockPos, Player player, Direction direction)
    {
        ItemStack itemStack=player.getItemInHand(InteractionHand.OFF_HAND);//添加副手的方块
        if(itemStack.getItem() instanceof BlockItem item)
        {
            if(item instanceof BlockItem blockItem)
            {
                Block block=blockItem.getBlock();
                if(!block.defaultBlockState().isCollisionShapeFullBlock(level,blockPos))//如果不是完整方块
                {
                    return;//不允许填充
                }else {
                    paint(level,blockPos,direction,block.defaultBlockState());
                }
            }
        }
    }
    public static void paint(Level level, BlockPos blockPos, Direction direction,BlockState paint)
    {
        BlockState origin=level.getBlockState(blockPos);

        if(origin.getBlock() instanceof PaintBlock)//如果对象是已经是渲染方块
        {
            AbstractRender render= RenderUtil.getRender(level,blockPos);

            render.putRenderBlock(direction,paint.getBlock());//放置新的渲染面

            BlockEntity blockEntity=level.getBlockEntity(blockPos);
            if(blockEntity instanceof PaintBlockEntity paintBlockEntity)
            {
                paintBlockEntity.setRender(render);//更新
            }
            return;
        }
        //如果不是渲染方块
        if(isPaintable(origin,level,blockPos))//只在是可粉刷方块时进行响应
        {
            AbstractRender render=new SimpleBlockPaint(origin);//默认普通方块

            if(origin.getBlock() instanceof SlabBlock)//如果是台阶
            {
                render=new SlabBlockPaint(origin);//修改为台阶渲染
            }

            render.putRenderBlock(direction,paint.getBlock());

            BlockState renderBlock=ModBlock.RENDER_BEDROCK.get().defaultBlockState();//放置渲染方块
            level.setBlock(blockPos,renderBlock,3);//放置

            BlockEntity blockEntity=level.getBlockEntity(blockPos);
            if(blockEntity instanceof PaintBlockEntity paintBlockEntity)
            {
                paintBlockEntity.init(render);//引入Render
            }
        }



    }

    public static boolean isPaintable(BlockState origin,Level level,BlockPos blockPos)
    {
        if(origin.getBlock()instanceof PaintBlock)
        {
            return true;
        }
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