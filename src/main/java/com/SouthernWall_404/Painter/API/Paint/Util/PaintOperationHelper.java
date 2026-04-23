package com.SouthernWall_404.Painter.API.Paint.Util;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Imply.SimpleBlockPaint;
import com.SouthernWall_404.Painter.API.Paint.Imply.SlabBlockPaint;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class PaintOperationHelper {


    public static void paint(Level level, BlockPos blockPos, Player player, Direction direction) {
        ItemStack itemStack = player.getItemInHand(InteractionHand.OFF_HAND);//添加副手的方块
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();

            if(!block.defaultBlockState().isCollisionShapeFullBlock(level,blockPos)){
                return;

            }
            // 构造放置上下文
            InteractionHand hand = InteractionHand.OFF_HAND;
            BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(blockPos), direction, blockPos, false);
            BlockPlaceContext context = new BlockPlaceContext(level, player, hand, itemStack, hitResult);
            BlockState paintState = block.getStateForPlacement(context);
            // 如果 getStateForPlacement 返回 null，回退到默认状态
            if (paintState == null) {
                paintState = block.defaultBlockState();
            }
            paint(level, blockPos, direction, paintState);
        }
    }


    public static void paint(Level level, BlockPos blockPos, Direction direction, BlockState material) {
        LevelChunk chunk = level.getChunkAt(blockPos);
        PaintInfo paintInfo = chunk.getData(ModAttachments.PAINT_INFO);

        Map<BlockPos, AbstractRender<?, ?>> renders=paintInfo.getRenders();
        if(renders.containsKey(blockPos))
        {

            AbstractRender render=renders.get(blockPos);

            render.putMaterial(direction,material);


        }else {

            BlockState origin=level.getBlockState(blockPos);//获取原本方块

            if(!PaintValidHelper.isPaintable(origin,level,blockPos))//检查是否是可以渲染的类型
            {
                return;//不是则不处理
            }
            if(origin.getBlock() instanceof SlabBlock)
            {
                SlabType slabType=origin.getValue(SlabBlock.TYPE);
                AbstractRender render=new SlabBlockPaint(slabType);
                render.putMaterial(direction, material);
                paintInfo.putRender(level,blockPos, render);
            }
            if(origin.isCollisionShapeFullBlock(level,blockPos))
            {
                AbstractRender render=new SimpleBlockPaint();
                render.putMaterial(direction, material);
                paintInfo.putRender(level,blockPos, render);
            }
            //        AbstractRender render = new SimpleBlockPaint();//默认普通方块
        }

        chunk.setData(ModAttachments.PAINT_INFO, paintInfo);

        chunk.setUnsaved(true);
    }
    public static void cycleTextureUV(AbstractPaint paint, Direction direction) {
        if(paint instanceof SlabBlockPaint slabBlock)//若为SlabBlockPaint
        {
            slabBlock.cycleTextureUV(direction);
        }
        else paint.cycleTextureUV(direction);
    }

    public static void cycleTextureDir(AbstractPaint paint, Direction direction) {

        if(paint instanceof SlabBlockPaint slabBlock)
        {
            slabBlock.cycleTextureDir(direction);
        }
        else paint.cycleTextureDir(direction);
    }
}