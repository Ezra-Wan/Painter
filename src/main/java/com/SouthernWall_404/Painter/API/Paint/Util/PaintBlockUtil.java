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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class PaintBlockUtil {

    public static void paint(Level level, BlockPos blockPos, Player player, Direction direction)
    {
        ItemStack itemStack=player.getItemInHand(InteractionHand.OFF_HAND);//添加副手的方块
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
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