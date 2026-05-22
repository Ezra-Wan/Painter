package com.SouthernWall_404.Painter.API.Paint.Util.Paint;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Imply.SimpleBlockPaint;
import com.SouthernWall_404.Painter.API.Paint.Imply.SlabBlockPaint;
import com.SouthernWall_404.Painter.API.Wallpaper.BlockWallPaper;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PaintOperationHelper {

    public static void thinPaint(Level level, BlockPos blockPos, Direction direction)
    {
        thinPaint(level,Set.of(blockPos),direction);
    }
    public static void thinPaint(Level level,  Set<BlockPos> blockPoses, Direction direction)
    {
        blockPoses.forEach(blockPos ->{
            PaintInfo paintInfo = level.getChunkAt(blockPos).getData(ModAttachments.PAINT_INFO);
            paintInfo.removePaintMaterial(level,blockPos, direction);
        } );}

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

        Map<BlockPos,AbstractPaint> paints=paintInfo.getPaints();
        if(paints.containsKey(blockPos))
        {

            AbstractPaint paint=paints.get(blockPos);

            paint.paint(direction,new BlockWallPaper(material,direction));

            paintInfo.putPaints(level,blockPos, paint);



        }else {

            BlockState origin=level.getBlockState(blockPos);//获取原本方块

            if(!PaintValidHelper.isPaintable(origin,level,blockPos))//检查是否是可以渲染的类型
            {
                return;//不是则不处理
            }
            if(origin.getBlock() instanceof SlabBlock)
            {
                SlabType slabType=origin.getValue(SlabBlock.TYPE);
                AbstractPaint paint=new SlabBlockPaint(blockPos,slabType);
                paint.paint(direction, new BlockWallPaper(material,direction));
                paintInfo.putPaints(level,blockPos, paint);
            }
            if(origin.isCollisionShapeFullBlock(level,blockPos))
            {
                AbstractPaint paint=new SimpleBlockPaint(blockPos);
                paint.paint(direction, new BlockWallPaper(material,direction));
                paintInfo.putPaints(level,blockPos, paint);
            }
            //        AbstractRender render = new SimpleBlockPaint();//默认普通方块
        }
    }


    @OnlyIn(Dist.CLIENT)
    public static void cycleTextureUV(BlockPos blockPos, Direction direction) {

        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
            Level level = mc.level;
            if(level!= null)
            {
                LevelChunk chunk = level.getChunkAt(blockPos);
                PaintInfo paintInfo = chunk.getData(ModAttachments.PAINT_INFO);
//                paintInfo.cycleTextureUV(level,blockPos, direction);
            }
        }
    }
}