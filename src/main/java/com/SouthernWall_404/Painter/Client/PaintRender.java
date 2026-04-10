package com.SouthernWall_404.Painter.Client;

import com.SouthernWall_404.Painter.Common.World.BlockEntity.PaintBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class PaintRender implements BlockEntityRenderer<PaintBlockEntity> {

    BlockPos renderPos;


    public PaintRender(BlockEntityRendererProvider.Context context) {
    }


    @Override
    public int getViewDistance() {

        return Minecraft.getInstance().options.renderDistance().get()*16;
    }
    @Override
    public void render(PaintBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        if(blockEntity.getRender()==null)
        {
            return;
        }
//        blockEntity.getRender().render(blockEntity, blockEntity.getBlockPos(),poseStack, bufferSource, packedLight, packedOverlay, partialTick, );
    }

        // 注意：上面的 for 循环方式在较新版本中可能已改变，你需要根据你的 NeoForge 版本调整。
        // 一个更简单但可能不完全兼容所有方块的方式是直接使用：
        // dispatcher.renderBatched(originState, blockEntity.getBlockPos(), level, poseStack, bufferSource, packedLight, packedOverlay, ModelData.EMPTY, null);
        // 但这需要在批处理环境中调用，不一定适合 TER。

//        this.renderPos=blockEntity.getBlockPos();
//        PaintInfo paintInfo= PaintUtil.getPaints(Minecraft.getInstance().level,renderPos);
//
//        Map<BlockPos, Paint> paints=paintInfo.getPaints();
//
//        for(Map.Entry<BlockPos,Paint> entry:paints.entrySet())
//        {
//            BlockPos paintPos=entry.getKey();
//            Paint paint=entry.getValue();
//
//            Direction direction=paint.getDirection();
//            Block block=paint.getBlock();
//
//            renderPaint(paintPos,direction,block,poseStack,bufferSource);
//        }

    @Override
    public AABB getRenderBoundingBox(PaintBlockEntity blockEntity) {

        return new net.minecraft.world.phys.AABB(
                blockEntity.getBlockPos().getX() - 1000,
                blockEntity.getBlockPos().getY() - 1000,
                blockEntity.getBlockPos().getZ() - 1000,
                blockEntity.getBlockPos().getX() + 1000,
                blockEntity.getBlockPos().getY() + 1000,
                blockEntity.getBlockPos().getZ() + 1000
        );
    }
}