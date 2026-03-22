package com.SouthernWall_404.Painter.API.Paint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
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

//    @Override
//    protected void update() {
//        // 半砖不预生成 quads，因为依赖渲染时的方向
//        objects.clear();
//    }

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

        for (Direction dir : Direction.values()) {
            int flag = getFlag(dir);
            if (!RenderUtil.shouldRenderFace(origin, level, pos, dir, pos.relative(dir))) continue;

            if (paintPaths.containsKey(flag)) {//如果存在伪装
                    Block block = RenderUtil.getBlockFromID(paintPaths.get(flag));
                    List<BakedQuad> quads= createSlabQuads(dir);

                    if (!quads.isEmpty()) {
                        VertexConsumer consumer = bufferSource.getBuffer(RenderType.cutout());
                        renderQuadsWithAO(level, block.defaultBlockState(), pos, quads, consumer, modRenderer, shape, shapeFlags, aoFace, poseStack, packedOverlay);
                    }
            } else {
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


    private List<BakedQuad> createSlabQuads(Direction direction)
    {
        int flag=getFlag(direction);
        List<BakedQuad> originQuads=objects.get(flag);
        List<BakedQuad> quads=new ArrayList<>();

        for(BakedQuad originQuad:originQuads)
        {
            TextureAtlasSprite sprite=originQuad.getSprite();

            BakedQuad quad=createSlabQuad(sprite,direction,originQuad.getTintIndex());

            quads.add(quad);
        }

        return quads;
    }

    private BakedQuad createSlabQuad(TextureAtlasSprite sprite, Direction direction,int tintIndex) {
        boolean isTopSlab = origin.getValue(SlabBlock.TYPE) == SlabType.TOP;

        SlabType slabType=origin.getValue(SlabBlock.TYPE);
        float yMin;
        float yMax;
//        float yMin = isTopSlab ? 0.5f : 0.0f;
//        float yMax = isTopSlab ? 1.0f : 0.5f;

        switch (slabType){
            case SlabType.TOP -> {
                yMin=0.5f;
                yMax=1.0f;
                break;
            }
            case SlabType.BOTTOM -> {
                yMin=0.0f;
                yMax=0.5f;
                break;
            }
            case SlabType.DOUBLE -> {
                yMin=0.0f;
                yMax=1.0f;
                break;
            }
            default -> {
                return null;
            }
        }


        float[][] positions;

        //顺序：左上，左下，右下，右上
        switch (direction) {
            case DOWN -> positions = new float[][]{
                    {0, yMin, 1}, {0, yMin, 0}, {1, yMin, 0}, {1, yMin, 1}
            };
            case UP -> positions = new float[][]{
                    {0, yMax, 0}, {0, yMax, 1}, {1, yMax, 1}, {1, yMax, 0}
            };
            case NORTH -> positions = new float[][]{
                    {1, yMax, 0},{1, yMin, 0},{0, yMin, 0},{0, yMax, 0}
            };
            case SOUTH -> positions = new float[][]{
                    {0, yMax, 1}, {0, yMin, 1}, {1, yMin, 1}, {1, yMax, 1}
            };
            case WEST -> positions = new float[][]{
                    {0, yMax, 0}, {0, yMin, 0}, {0, yMin, 1}, {0, yMax, 1}

            };
            case EAST -> positions = new float[][]{
                    {1, yMax, 1}, {1, yMin, 1}, {1, yMin, 0}, {1, yMax, 0}
            };
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        }

        // UV 与顶点顺序一致（左下、右下、右上、左上）
        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();
        if (direction.getAxis().isHorizontal()) {


            switch (slabType){
                case SlabType.TOP -> {
                    v1 = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * 0.5f; // 下半纹理
                    break;
                }
                case SlabType.BOTTOM -> {
                    v0 = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * 0.5f; // 上半纹理
                    break;
                }
                case SlabType.DOUBLE -> {
                    break;
                }
                default -> {
                    return null;
                }
            }

//            if (!isTopSlab) {
//                v0 = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * 0.5f; // 上半纹理
//            } else {
//                v1 = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * 0.5f; // 下半纹理
//            }
        }
        float[][] uvs = {
                {u0, v0}, {u0, v1}, {u1, v1}, {u1, v0}
        };

        // 打包法线
        int packedNormal = packNormal(direction.getNormal().getX(),
                direction.getNormal().getY(),
                direction.getNormal().getZ());
        int[] vertexData = new int[32];
        for (int i = 0; i < 4; i++) {
            int offset = i * 8;
            vertexData[offset]     = Float.floatToRawIntBits(positions[i][0]);
            vertexData[offset + 1] = Float.floatToRawIntBits(positions[i][1]);
            vertexData[offset + 2] = Float.floatToRawIntBits(positions[i][2]);
            vertexData[offset + 3] = -1;   // 白色
            vertexData[offset + 4] = Float.floatToRawIntBits(uvs[i][0]);
            vertexData[offset + 5] = Float.floatToRawIntBits(uvs[i][1]);
            vertexData[offset + 6] = 0;    // 光照（渲染时填充）
            vertexData[offset + 7] = packedNormal;
        }

        return new BakedQuad(vertexData,tintIndex, direction, sprite, true);
    }
    // 辅助方法：将法线向量打包为 int（与 DefaultVertexFormat 一致）
    private static int packNormal(float x, float y, float z) {
        int nx = (int) (x * 127);
        int ny = (int) (y * 127);
        int nz = (int) (z * 127);
        return (nx & 0xFF) | ((ny & 0xFF) << 8) | ((nz & 0xFF) << 16);
    }
    private List<BakedQuad> getQuadsForSlabState(BlockState state, Direction dir, Level level, BlockPos pos) {
        BakedModel model = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
        RandomSource random = RandomSource.create();
        return model.getQuads(state, dir, random, ModelData.EMPTY, RenderUtil.getRenderType(state));
    }
}