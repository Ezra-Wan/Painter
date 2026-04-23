package com.SouthernWall_404.Painter.API.Paint.Util.Paint;

import com.SouthernWall_404.LaplaceAPI.VertinCore.Util.BlockUtil;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Tool.Wall.IFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class PaintUtil {



    public static void dealBrushClick(Level level, BlockPos blockPos, Player player, Direction direction, boolean isInMainHand) {


        LevelChunk chunk=level.getChunkAt(blockPos);
        Map<BlockPos,AbstractPaint> paints=PaintAttachmentHelper.getPaintInfo(chunk).getPaints();//获取本区块render


        //TODO 实现喷涂逻辑
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


    public static void dealBucketClick(BlockPos pos, Direction face, Player player, Level level, List<IFilter> filters, boolean isInMainHand)
    {

        BlockState origin=level.getBlockState(pos);
        BlockState toPaint=null;//将被喷涂的BlockState
        if(isInMainHand)//刷子在主手
        {
            ItemStack hand=player.getItemInHand(InteractionHand.OFF_HAND);

            if(hand.getItem() instanceof  BlockItem blockItem)
            {
                Block block=blockItem.getBlock();//获取手上方块
                toPaint= BlockUtil.createBlockStateToPlace(block,pos,face,level,player,hand,InteractionHand.OFF_HAND);

                if(PaintValidHelper.isValidPaintOperation(toPaint,level,pos,origin))//合法检查
                {
                    AbstractPaint paint=PaintAttachmentHelper.getPaint(level,pos);
                    if(paint!=null)//如果此处有paint
                    {
                        BlockState oldMeterial=paint.getMaterial(face);//尝试获取当前此面已有的材料
                        if(oldMeterial!=null)
                        {
                            if(oldMeterial.getBlock()==toPaint.getBlock())//如果它存在
                            {
                                toPaint=PaintOperationHelper.cycleInDirection(oldMeterial);//喷涂类设定为点击处喷涂的旋转后状态
                            }//没有原本喷涂
                        }
                    }
                }else {
                    return;
                }
            }else {//如果手上物品不为BlockItem

                AbstractPaint paint=PaintAttachmentHelper.getPaint(level,pos);
                if(paint!=null)//如果此处有paint
                {
                    BlockState oldMeterial=paint.getMaterial(face);//尝试获取当前此面已有的材料
                    if(oldMeterial!=null)//如果它存在
                    {
                        toPaint=PaintOperationHelper.cycleInDirection(oldMeterial);//喷涂类设定为点击处喷涂的旋转后状态
                    }//没有原本喷涂
                    else {
                        return;//没有动作
                    }
                }
            }
        }
        if(toPaint==null)return;

        // 队列用于 BFS
        Queue<BlockPos> queue = new LinkedList<>();
        // 记录已处理过的方块，避免重复
        Set<BlockPos> visited = new HashSet<>();

        // 起始点入队
        queue.offer(pos);
        visited.add(pos);

        // 获取与 face 垂直的四个方向（即墙面内的四个方向）
        // 六个方向中，排除 face 本身和其相反方向，剩下的四个即为墙面内的方向
        List<Direction> wallDirs = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if (dir != face && dir != face.getOpposite()) {
                wallDirs.add(dir);
            }
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();


            // 检查当前方块是否在距离限制内（起始点切比雪夫距离 ≤ 32）
            if (Math.abs(current.getX() - pos.getX()) > 32 ||
                    Math.abs(current.getY() - pos.getY()) > 32 ||
                    Math.abs(current.getZ() - pos.getZ()) > 32) {
                continue; // 超出范围，不再扩散
            }

            PaintOperationHelper.paint(level,current,face,toPaint);

            boolean isAllowed=true;
            for(IFilter filter:filters)//遍历所有筛选项
            {
                if(!filter.check(current,player,face))//只要有一个不满足，则终止继续蔓延
                {
                    isAllowed=false;
                }
            }

            if(isAllowed)
            {
                PaintUtil.dealBrushClick(level,current,player,face,isInMainHand);//执行喷涂

                // 向墙面内的四个方向扩散
                for (Direction wallDir : wallDirs) {
                    BlockPos neighbor = current.relative(wallDir);
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
                // 如果当前方块不符合条件，则不再从它向外扩散
            }

        }
    }
}