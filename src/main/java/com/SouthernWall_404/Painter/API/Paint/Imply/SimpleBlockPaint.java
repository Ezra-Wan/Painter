package com.SouthernWall_404.Painter.API.Paint.Imply;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.ModModelRender;
import com.SouthernWall_404.Painter.API.Paint.PaintContent;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.BitSet;
import java.util.List;

public class SimpleBlockPaint extends AbstractPaint {

    //======== Flag 定义，与 Direction 绑定 ========
    private static final int NORTH = 1;
    private static final int SOUTH = 2;
    private static final int WEST  = 4;
    private static final int EAST  = 8;
    private static final int UP    = 16;
    private static final int DOWN  = 32;

    public SimpleBlockPaint()
    {
        this(Blocks.AIR.defaultBlockState());
    }

    public SimpleBlockPaint(BlockState origin) {
        super(origin, PaintContent.SIMPLE_BLOCK);
    }

    @Override
    public void initFlags() {
        registerFlag(Direction.NORTH, NORTH);
        registerFlag(Direction.SOUTH, SOUTH);
        registerFlag(Direction.WEST,  WEST);
        registerFlag(Direction.EAST,  EAST);
        registerFlag(Direction.UP,    UP);
        registerFlag(Direction.DOWN,  DOWN);
    }
    @Override
    public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
//        update();
        if (origin == null) return;

        Level level = blockEntity.getLevel();
        if (level == null) return;
        BlockPos pos = blockEntity.getBlockPos();

        // 对Origin的处理
        BakedModel model = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(origin);
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        RandomSource random = RandomSource.create();
        long seed = origin.getSeed(pos);
        RenderType renderType = RenderUtil.getRenderType(origin);



        // 创建自定义渲染器
        ModModelRender modRenderer = new ModModelRender(blockColors);

        Vec3 offset = origin.getOffset(level, pos);
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);

        // 准备 AO 计算所需的共享数组
        float[] shape = new float[ModModelRender.DIRECTIONS.length * 2];
        BitSet shapeFlags = new BitSet(3);
        ModModelRender.AmbientOcclusionFace aoFace = new ModModelRender.AmbientOcclusionFace();


        // 渲染每个方向的原版面（跳过有自定义的方向）
        for (Direction direction : Direction.values()) {
            List<BakedQuad> quads;

            int flag=getFlag(direction);
            BlockPos neighborPos = pos.relative(direction);
            if (!RenderUtil.shouldRenderFace(origin, level, pos, direction, neighborPos)) continue;

            if (objects.containsKey(flag)) {
                quads=objects.get(flag);

                BlockState state= materials.get(flag);
                VertexConsumer consumer = bufferSource.getBuffer(RenderUtil.getRenderType(state));

                renderQuadsWithAO(level,state,pos,quads,consumer,modRenderer,shape,shapeFlags,aoFace,poseStack,packedOverlay);

            }
            else
            {
                VertexConsumer consumer = bufferSource.getBuffer(renderType);
                random.setSeed(seed);
                quads= model.getQuads(origin, direction, random, ModelData.EMPTY, renderType);

                renderQuadsWithAO(level,origin,pos,quads,consumer,modRenderer,shape,shapeFlags,aoFace,poseStack,packedOverlay);
                // 渲染无方向的原版面（如粒子面）
                random.setSeed(seed);
                List<BakedQuad> generalQuads = model.getQuads(origin, null, random, ModelData.EMPTY, renderType);
                if (!generalQuads.isEmpty()) {

                    renderQuadsWithAO(level,origin,pos,quads,consumer,modRenderer,shape,shapeFlags,aoFace,poseStack,packedOverlay);
                }
            }
            if (quads.isEmpty()) continue;

        }
        poseStack.popPose();
    }
}