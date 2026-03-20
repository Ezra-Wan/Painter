package com.SouthernWall_404.Painter.API.Paint;

import com.SouthernWall_404.Painter.Common.World.BlockEntity.PaintBlockEntity;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.BitSet;
import java.util.List;

public class SlabBlockPaint extends AbstractPaint {
    private static final int NORTH = 1, SOUTH = 2, WEST = 4, EAST = 8, UP = 16, DOWN = 32;

    public SlabBlockPaint(BlockState origin) {
        super(origin, PaintContent.SLAB_BLOCK);
    }

    @Override
    public void initFlags() {
        registerFlag(Direction.NORTH, NORTH);
        registerFlag(Direction.SOUTH, SOUTH);
        registerFlag(Direction.WEST, WEST);
        registerFlag(Direction.EAST, EAST);
        registerFlag(Direction.UP, UP);
        registerFlag(Direction.DOWN, DOWN);
    }

    @Override
    protected void update() {
        // 半砖不预生成 quads，因为依赖渲染时的方向
        objects.clear();
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (origin == null) return;
        Level level = blockEntity.getLevel();
        if (level == null) return;
        BlockPos pos = blockEntity.getBlockPos();

        BakedModel model = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(origin);
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        RandomSource random = RandomSource.create();
        long seed = origin.getSeed(pos);
        RenderType renderType = RenderUtil.getRenderType(origin);

        ModModelRender modRenderer = new ModModelRender(blockColors);

        Vec3 offset = origin.getOffset(level, pos);
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);

        float[] shape = new float[ModModelRender.DIRECTIONS.length * 2];
        BitSet shapeFlags = new BitSet(3);
        ModModelRender.AmbientOcclusionFace aoFace = new ModModelRender.AmbientOcclusionFace();


        BakedModel halfQuad;
        for (Direction dir : Direction.values()) {
            int flag = getFlag(dir);
            if (!RenderUtil.shouldRenderFace(origin, level, pos, dir, pos.relative(dir))) continue;

            if (paintPaths.containsKey(flag)) {
                Block block = RenderUtil.getBlockFromID(paintPaths.get(flag));
                if (block instanceof SlabBlock) {
                    BlockState slabState = getSlabStateForFace(block, dir);


                    List<BakedQuad> quads = getQuadsForSlabState(slabState, dir, level, pos);//TODO:应当是这里的问题
                    if(dir==Direction.UP)
                    {
                        if(origin.getValue(SlabBlock.TYPE)==SlabType.BOTTOM)
                        {
                            quads=getQuadsForSlabState(slabState,null,level,pos);
                        }
                    }
                    if (dir==Direction.DOWN)
                    {
                        if(origin.getValue(SlabBlock.TYPE)==SlabType.TOP)
                        {
                            quads=getQuadsForSlabState(slabState,null,level,pos);
                        }
                    }

                    if (!quads.isEmpty()) {
                        VertexConsumer consumer = bufferSource.getBuffer(renderType);
                        renderQuadsWithAO(level, slabState, pos, quads, consumer, modRenderer, shape, shapeFlags, aoFace, poseStack, packedOverlay);
                    }
                }
                else {
////                    return;
//                    // 非半砖按普通方块处理（使用默认状态）
//                    BlockState state = block.defaultBlockState();
//                    List<BakedQuad> quads = getQuadsForDirection(block, dir);
//                    if (!quads.isEmpty()) {
//                        VertexConsumer consumer = bufferSource.getBuffer(RenderUtil.getRenderType(state));
//                        renderQuadsWithAO(level, state, pos, quads, consumer, modRenderer, shape, shapeFlags, aoFace, poseStack, packedOverlay);
//                    }
                }
            } else {


//                 无自定义：渲染原方块的面
                random.setSeed(seed);
                List<BakedQuad> quads = model.getQuads(origin, dir, random, ModelData.EMPTY, renderType);

                if(dir==Direction.UP)
                {
                    if(origin.getValue(SlabBlock.TYPE)==SlabType.BOTTOM)
                    {
                        quads=getQuadsForSlabState(origin,null,level,pos);
                    }
                }
                if (dir==Direction.DOWN)
                {
                    if(origin.getValue(SlabBlock.TYPE)==SlabType.TOP)
                    {
                        quads=getQuadsForSlabState(origin,null,level,pos);
                    }
                }
                if (!quads.isEmpty()) {
                    VertexConsumer consumer = bufferSource.getBuffer(renderType);
                    renderQuadsWithAO(level, origin, pos, quads, consumer, modRenderer, shape, shapeFlags, aoFace, poseStack, packedOverlay);
                }


            }
        }

        poseStack.popPose();
    }


    private BlockState getSlabStateForFace(Block block, Direction face) {
        SlabType type=origin.getValue(SlabBlock.TYPE);
        BlockState defaultState = block.defaultBlockState();
        if (defaultState.hasProperty(SlabBlock.TYPE)) {
            return defaultState.setValue(SlabBlock.TYPE, type);
        }
        return defaultState;
    }

    private List<BakedQuad> getQuadsForSlabState(BlockState state, Direction dir, Level level, BlockPos pos) {
        BakedModel model = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
        RandomSource random = RandomSource.create();
        return model.getQuads(state, dir, random, ModelData.EMPTY, RenderUtil.getRenderType(state));
    }

    private void renderQuadsWithAO(Level level, BlockState state, BlockPos pos, List<BakedQuad> quads,
                                   VertexConsumer consumer, ModModelRender modRenderer,
                                   float[] shape, BitSet shapeFlags, ModModelRender.AmbientOcclusionFace aoFace,
                                   PoseStack poseStack, int packedOverlay) {
        for (BakedQuad quad : quads) {
            modRenderer.calculateShape(level, state, pos, quad.getVertices(), quad.getDirection(), shape, shapeFlags);
            aoFace.calculate(level, state, pos, quad.getDirection(), shape, shapeFlags, quad.isShade());
            modRenderer.putQuadData(level, state, pos, consumer, poseStack.last(), quad,
                    aoFace.brightness[0], aoFace.brightness[1], aoFace.brightness[2], aoFace.brightness[3],
                    aoFace.lightmap[0], aoFace.lightmap[1], aoFace.lightmap[2], aoFace.lightmap[3],
                    packedOverlay);
        }
    }
}