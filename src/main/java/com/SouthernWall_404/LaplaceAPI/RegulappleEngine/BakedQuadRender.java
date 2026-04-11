package com.SouthernWall_404.LaplaceAPI.RegulappleEngine;

import com.SouthernWall_404.Painter.API.Paint.ModModelRender;
import com.SouthernWall_404.Painter.API.Paint.Util.CommonUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.BitSet;

/**
 * 用于渲染浮空的BakeQuad平面
 */
public class BakedQuadRender {


    public static float[] shape = new float[ModModelRender.DIRECTIONS.length * 2];

    private static BitSet shapeFlags = new BitSet(3);


    /**
     * 渲染单个Quad
     * @param quad 需要渲染的Quad
     * @param state 需要渲染的BlockState
     * @param renderVec 需要渲染的位置
     * @param poseStack 矩阵，外部传入
     * @param renderType 具体渲染类型
     * @param bufferSource VertexConsumer类来源，提供以便在外界进行渲染提交
     */
    @OnlyIn(Dist.CLIENT)
    public static void renderWithAO(BakedQuad quad, BlockState state, Vec3 renderVec, PoseStack poseStack, RenderType renderType, MultiBufferSource bufferSource, ModModelRender.AmbientOcclusionFace aoFace)
    {

        Minecraft mc=Minecraft.getInstance();
        Level level=mc.level;

        BlockColors blockColors=mc.getBlockColors();

        VertexConsumer buffer = bufferSource.getBuffer(renderType);

//        VertexConsumer buffer=mc
        Camera camera=mc.gameRenderer.getMainCamera();
        Vec3 camPos=camera.getPosition();


        poseStack.pushPose();
        poseStack.translate(renderVec.x - camPos.x, renderVec.y - camPos.y, renderVec.z - camPos.z);



        BlockPos pos=CommonUtil.vec32Pos(renderVec);

        float f;
        float f1;
        float f2;
        if (quad.isTinted()) {
            int i = blockColors.getColor(state, level, pos.relative(quad.getDirection()), quad.getTintIndex());
            f = (float)(i >> 16 & 255) / 255.0F;
            f1 = (float)(i >> 8 & 255) / 255.0F;
            f2 = (float)(i & 255) / 255.0F;
        } else {
            f = 1.0F;
            f1 = 1.0F;
            f2 = 1.0F;
        }




        aoFace.calculate(level, state, pos.relative(quad.getDirection()), quad.getDirection(), shape, shapeFlags, true);
        buffer.putBulkData(poseStack.last(),quad, aoFace.brightness, f,f1,f2,1f, aoFace.lightmap, 0,true);

        poseStack.popPose();
    }
}
