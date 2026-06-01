package com.SouthernWall_404.Painter.API.Paint.Util.Paint;

import com.SouthernWall_404.LaplaceAPI.UlrichToolBox.Blocks.BlockUtil;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Tool.Wall.IFilter;
import com.SouthernWall_404.Painter.Common.Event.ServerTick;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.Laplace.Network.ServerHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.*;

public class PaintUtil {



    public static void dealBrushClick(Level level, BlockPos blockPos, Player player, Direction direction, boolean isInMainHand) {
        LevelChunk chunk = level.getChunkAt(blockPos);
        PaintInfo paintInfo = chunk.getData(ModAttachments.PAINT_INFO);
        Map<BlockPos, AbstractPaint> paints =paintInfo.getPaints();
        AbstractPaint paint = paints.get(blockPos);

        if (!isInMainHand) {//当副手持刷子

            PaintOperationHelper.cycleTextureUV(level,blockPos, direction);
        }
        else {//当主手持刷子
            BlockState origin = level.getBlockState(blockPos);
            BlockState toPaint = null;

            //从副手获取方块物品，构造放置状态
            ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);
            if (offHandItem.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                toPaint = BlockUtil.createBlockStateToPlace(block, blockPos, direction, level, player, offHandItem, InteractionHand.OFF_HAND);

                if (paint != null) {//如果点击存有paint
                    BlockState existingMat = paint.getMaterial(direction);//获取当前面的喷涂
                    if (existingMat != null && existingMat.getBlock() == toPaint.getBlock()) {//如果当面有喷涂，且与手中方块相同
                        toPaint = BlockUtil.cycleInDirection(existingMat);//旋转

                    }
                    //如果当面没有喷涂，或与手中方块不同
                    //则正常喷涂
                }
                //如果点击处不存在paint
                //则正常喷涂
            } else {// 副手不是方块物品，则尝试利用已有的 paint 材质进行旋转
                if (paint != null) {//如果存在paint
                    paint.cycleTextureDir( direction);

                    ServerTick.update(new ChunkPos(blockPos));

                    chunk.setUnsaved(true);
                }
            }

            if (toPaint == null) return;


            // 合法性检查
            if (!PaintValidHelper.isValidPaintOperation(toPaint, level, blockPos, origin)) {
                return;
            }

            // 执行单点喷涂
            PaintOperationHelper.paint(level, blockPos, direction, toPaint);
        }
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
                                toPaint=BlockUtil.cycleInDirection(oldMeterial);//喷涂类设定为点击处喷涂的旋转后状态
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
                        toPaint=BlockUtil.cycleInDirection(oldMeterial);//喷涂类设定为点击处喷涂的旋转后状态
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
//                PaintUtil.dealBrushClick(level,current,player,face,isInMainHand);//执行喷涂
                PaintOperationHelper.paint(level,current,face,toPaint);
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