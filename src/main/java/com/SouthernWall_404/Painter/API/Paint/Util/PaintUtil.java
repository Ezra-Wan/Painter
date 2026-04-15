package com.SouthernWall_404.Painter.API.Paint.Util;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Imply.SimpleBlockPaint;
import com.SouthernWall_404.Painter.API.Paint.Imply.SlabBlockPaint;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.Network.ClientRequestPack;
import com.SouthernWall_404.Painter.Common.Network.ModChannels;
import com.SouthernWall_404.Painter.Common.Network.S2C.ChunkS2CPacket;
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
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class PaintUtil {


    public static Map<BlockPos,AbstractRender<?,?>> getRenders(Level level,BlockPos blockPos)
    {
        LevelChunk chunk=level.getChunkAt(blockPos);

        PaintInfo paintInfo=chunk.getData(ModAttachments.PAINT_INFO);

        return paintInfo.getRenders();
    }
    public static boolean hasPaint(Level level,BlockPos blockPos)
    {

        if(getRenders(level,blockPos).containsKey(blockPos))return true;

        return false;

    }

    public static boolean hasPaint(Map<BlockPos,AbstractRender<?,?>> renders,BlockPos blockPos)
    {

        if(renders.containsKey(blockPos))return true;

        return false;

    }

    public static void dealWithPaintClick(Level level, BlockPos blockPos, Player player, Direction direction,boolean isInMainHand) {

        Map<BlockPos,AbstractRender<?,?>> renders=getRenders(level,blockPos);//获取本区块render
        if(isInMainHand)//如果刷子/油漆桶在主手
        {
            ItemStack handItemStack = player.getItemInHand(InteractionHand.OFF_HAND);//获取副手物品



            if (hasPaint(renders,blockPos))//如果已经存在渲染
            {
                if (handItemStack.isEmpty()) {//副手为空
                    cycleDir( blockPos, renders, direction);//旋转
                } else {
                    if (handItemStack.getItem() instanceof BlockItem blockItem) {//副手为方块
                        Block block = blockItem.getBlock();//获取方块
                        AbstractRender render = renders.get(blockPos);//获取已有render

                        if (render.getMaterial(direction).getBlock() == block) {//若为相同方块
                            cycleDir( blockPos, renders, direction);//旋转
                        } else {//不为相同方块
                            paint(level, blockPos, player, direction);//喷涂
                        }
                    }
                }
            } else {//如果不存在渲染
                paint(level, blockPos, player, direction);//喷涂

            }
        }else//如果刷子/油漆桶在副手
        {
            if (hasPaint(renders,blockPos))//如果已经存在渲染
            cycleUV( blockPos, renders, direction);//旋转
        }

    }

    public static void cycleUV(BlockPos blockPos, Map<BlockPos,AbstractRender<?,?>> renders, Direction direction){
        AbstractRender render = renders.get(blockPos);
        if (render instanceof AbstractPaint paint) {

            if(paint instanceof SlabBlockPaint slabBlock)
            {
                slabBlock.cycleTextureUV(direction);
            }
            else paint.cycleTextureUV(direction);
        }
    }

    public static void cycleDir(BlockPos blockPos, Map<BlockPos,AbstractRender<?,?>> renders, Direction direction) {

        AbstractRender render = renders.get(blockPos);
        if (render instanceof AbstractPaint paint) {

            if(paint instanceof SlabBlockPaint slabBlock)
            {
                slabBlock.cycleTextureDir(direction);
            }
            else paint.cycleTextureDir(direction);
        }


    }

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

            if(!isPaintable(origin,level,blockPos))//检查是否是可以渲染的类型
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