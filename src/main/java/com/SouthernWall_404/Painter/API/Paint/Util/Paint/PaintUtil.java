package com.SouthernWall_404.Painter.API.Paint.Util.Paint;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Map;

public class PaintUtil {



    public static void dealBrushClick(Level level, BlockPos blockPos, Player player, Direction direction, boolean isInMainHand) {


        LevelChunk chunk=level.getChunkAt(blockPos);
        Map<BlockPos,AbstractPaint> paints=PaintAttachmentHelper.getPaintInfo(chunk).getPaints();//获取本区块render


        AbstractPaint paint=paints.get(blockPos);

        if (paint!=null)//如果已经存在渲染
        {
            if(isInMainHand)//如果刷子/油漆桶在主手
            {
                ItemStack handItemStack = player.getItemInHand(InteractionHand.OFF_HAND);//获取副手物品


                if (handItemStack.isEmpty()) {//副手为空
                    PaintOperationHelper.cycleTextureDir(paint, direction);//旋转
                } else {
                    if (handItemStack.getItem() instanceof BlockItem blockItem) {//副手为方块
                        Block block = blockItem.getBlock();//获取方块

                        if (paint.getMaterial(direction).getBlock() == block) {//若为相同方块
                            PaintOperationHelper.cycleTextureDir( paint, direction);//旋转
                        } else {//不为相同方块
                            PaintOperationHelper.paint(level, blockPos, player, direction);//喷涂
                        }
                    }
                }

            }else//如果刷子/油漆桶在副手
            {
                if (PaintValidHelper.hasPaint(paints,blockPos))//如果已经存在渲染
                    PaintOperationHelper.cycleTextureUV(paint, direction);//旋转
            }
        } else {//如果不存在渲染
            PaintOperationHelper.paint(level, blockPos, player, direction);//喷涂
        }
        chunk.setUnsaved(true);

    }
}