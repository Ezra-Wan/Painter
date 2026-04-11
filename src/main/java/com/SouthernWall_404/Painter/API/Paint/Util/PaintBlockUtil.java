package com.SouthernWall_404.Painter.API.Paint.Util;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Imply.SlabBlockPaint;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.Network.ClientRequestPack;
import com.SouthernWall_404.Painter.Common.Network.ModChannels;
import com.SouthernWall_404.Painter.Common.Network.S2C.ChunkS2CPacket;
import com.SouthernWall_404.Painter.Common.World.Block.PaintBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class PaintBlockUtil {


    public static void dealWithPaintClick(Level level, BlockPos blockPos, Player player, Direction direction) {
        BlockState origin = level.getBlockState(blockPos);

        ItemStack handItemStack = player.getItemInHand(InteractionHand.OFF_HAND);

        if (origin.getBlock() instanceof PaintBlock)//如果对象是已经是渲染方块
        {
            if (handItemStack.isEmpty()) {
                cycle(level, blockPos, player, direction);
            } else {
                if (handItemStack.getItem() instanceof BlockItem blockItem) {
                    Block block = blockItem.getBlock();
                    AbstractRender render = RenderUtil.getRender(level, blockPos);

                    if (render.getMaterial(direction).getBlock() == block) {
                        cycle(level, blockPos, player, direction);
                    } else {
                        paint(level, blockPos, player, direction);
                    }
                }
            }
        } else {
            paint(level, blockPos, player, direction);

        }
    }

    public static void cycle(Level level, BlockPos blockPos, Player player, Direction direction) {

        AbstractRender render = RenderUtil.getRender(level, blockPos);
        if (render instanceof AbstractPaint paint) {
            paint.cyclePaint(direction);
        }


    }

    public static void paint(Level level, BlockPos blockPos, Player player, Direction direction) {

        if (level.isClientSide) {
            System.out.println("Client paint");
        }
        if (!level.isClientSide){
            System.out.println("Server paint");
        }


        ItemStack itemStack = player.getItemInHand(InteractionHand.OFF_HAND);//添加副手的方块
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

    public static void paint(Level level, BlockPos blockPos, Direction direction, BlockState material) {

        LevelChunk chunk = level.getChunkAt(blockPos);
        PaintInfo paintInfo = chunk.getData(ModAttachments.PAINT_INFO);

        Map<BlockPos, AbstractRender<?, ?>> renders=paintInfo.getRenders();
        if(renders.containsKey(blockPos))
        {

            AbstractRender render=renders.get(blockPos);

            render.putMaterial(direction,material);


        }else {
            //        AbstractRender render = new SimpleBlockPaint();//默认普通方块

            AbstractRender render=new SlabBlockPaint();//TODO 测试用例，记得删
            render.putMaterial(direction, material);

            paintInfo.putRender(level,blockPos, render);
        }

        chunk.setData(ModAttachments.PAINT_INFO, paintInfo);

        chunk.setUnsaved(true);


    }

//    @Deprecated
//    public static void paint(Level level, BlockPos blockPos, Direction direction,BlockState paint)
//    {
//        BlockState origin=level.getBlockState(blockPos);
//
//        if(origin.getBlock() instanceof PaintBlock)//如果对象是已经是渲染方块
//        {
//            AbstractRender render= RenderUtil.getRender(level,blockPos);
//
//            render.putRenderBlock(direction,paint);//放置新的渲染面
//
//            BlockEntity blockEntity=level.getBlockEntity(blockPos);
//            if(blockEntity instanceof PaintBlockEntity paintBlockEntity)
//            {
//                paintBlockEntity.setRender(render);//更新
//            }
//            return;
//        }
//        //如果不是渲染方块
//        if(isPaintable(origin,level,blockPos))//只在是可粉刷方块时进行响应
//        {
//            AbstractRender render=new SimpleBlockPaint(origin);//默认普通方块
//
//            if(origin.getBlock() instanceof SlabBlock)//如果是台阶
//            {
//                render=new SlabBlockPaint(origin);//修改为台阶渲染
//            }
//
//            render.putRenderBlock(direction,paint);
//
//            BlockState renderBlock=ModBlock.RENDER_BEDROCK.get().defaultBlockState();//放置渲染方块
//            level.setBlock(blockPos,renderBlock,3);//放置
//
//            BlockEntity blockEntity=level.getBlockEntity(blockPos);
//            if(blockEntity instanceof PaintBlockEntity paintBlockEntity)
//            {
//                paintBlockEntity.init(render);//引入Render
//            }
//        }
//
//
//
//    }

    public static void RequireSync(ChunkPos pos)
    {
        ModChannels.sendToServer(new ClientRequestPack(1,pos));
    }
    public static void syncToClient(ChunkPos pos,Player player) {
            Level level = player.level();
            LevelChunk chunk = level.getChunk(pos.x,pos.z);
            PaintInfo paintInfo = chunk.getData(ModAttachments.PAINT_INFO);
            CompoundTag modPack=paintInfo.serializeNBT(player.level().registryAccess());

            ModChannels.sendToClient(new ChunkS2CPacket(modPack,pos),(ServerPlayer) player);
    }


    public static boolean isPaintable(Level level,BlockPos blockPos)
    {
        BlockState origin=level.getBlockState(blockPos);
        return isPaintable(origin,level,blockPos);
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