package com.SouthernWall_404.Painter.API.Paint;

import com.SouthernWall_404.Painter.Common.World.BlockEntity.PaintBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.IShearable;

import java.util.List;

public class SimpleBlockPaint extends AbstractPaint {

    //======== Flag 定义，与 Direction 绑定 ========
    private static final int NORTH = 1;
    private static final int SOUTH = 2;
    private static final int WEST  = 4;
    private static final int EAST  = 8;
    private static final int UP    = 16;
    private static final int DOWN  = 32;

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
        // 该方法需要根据标志位调用 renderFace，暂留空

        if(origin==null)
        {
            return;
        }

        for(Direction direction:Direction.values())
        {


            List<BakedQuad> quads=RenderUtil.getQuads(origin,direction);
            for(BakedQuad quad:quads)
            {
                renderQuadManually(blockEntity,quad,poseStack,bufferSource,packedLight,packedOverlay);
            }
        }
    }


    /**
     * 从 BakedQuad 中提取信息，并使用手动渲染方式渲染该面
     */
    private static void renderQuadManually(BlockEntity blockEntity, BakedQuad quad,
                                           PoseStack poseStack, MultiBufferSource bufferSource,
                                           int packedLight, int packedOverlay) {
        if (!(blockEntity instanceof PaintBlockEntity paintBE)) return;
        Level level = blockEntity.getLevel();
        if (level == null) return;

        BlockPos pos = blockEntity.getBlockPos();
        BlockState origin = paintBE.getOrigin();

        // 从 quad 中提取纹理和方向
        TextureAtlasSprite sprite = quad.getSprite();
        Direction face = quad.getDirection(); // quad 的实际方向

        // 获取染色信息
        int tintIndex = quad.isTinted() ? quad.getTintIndex() : -1;

        // 调用手动渲染方法（需要扩展 renderFace 以支持 tintIndex）
        renderFace(blockEntity, sprite, face, tintIndex, poseStack, bufferSource, packedLight, packedOverlay);
    }
    /**
     * 渲染单个面
     * @param blockEntity   方块实体
     * @param texture       要使用的纹理
     * @param direction     要渲染的面方向
     * @param poseStack     变换栈
     * @param bufferSource  渲染缓冲区
     */
    private static void renderFace(BlockEntity blockEntity, TextureAtlasSprite texture, Direction direction,int tintIndex,
                                   PoseStack poseStack, MultiBufferSource bufferSource, int partickedLight, int packedOverlay) {
        if(blockEntity instanceof PaintBlockEntity paintBlockEntity) {
            Level level = blockEntity.getLevel();
            if (level == null) return;


            BlockPos originPos = blockEntity.getBlockPos();
            BlockPos facePos = originPos.relative(direction);
            BlockState origin=paintBlockEntity.getOrigin();
            // 获取阴影系数
            float shade = level.getShade(direction, true);

            // 计算颜色乘数
            float r = shade, g = shade, b = shade;
            if (tintIndex >= 0) {
                BlockColors blockColors = Minecraft.getInstance().getBlockColors();
                int color = blockColors.getColor(origin, level,originPos, tintIndex);
                if (color != -1) {
                    r = ((color >> 16) & 0xFF) / 255.0f * shade;
                    g = ((color >> 8) & 0xFF) / 255.0f * shade;
                    b = (color & 0xFF) / 255.0f * shade;
                }
            }



            // 获取该位置的光照值
            int blockLight = level.getBrightness(LightLayer.BLOCK, facePos);
            int skyLight   = level.getBrightness(LightLayer.SKY, facePos);
            int worldLight = (blockLight << 4) | (skyLight << 20); // 标准光照打包

            // 法向向量
            Vec3i normal = direction.getNormal();

            // 纹理 UV 范围
            float minU = texture.getU0();
            float maxU = texture.getU1();
            float minV = texture.getV0();
            float maxV = texture.getV1();

            // 根据方向确定四个顶点的局部坐标（左下、右下、右上、左上）
            float off = 0.001f; // 微小偏移，避免深度冲突
            float[][] vertices = new float[4][3];

            switch (direction) {
                case NORTH: // -Z
                    vertices[0] = new float[]{1,     0,    -off};
                    vertices[1] = new float[]{0,     0,    -off};
                    vertices[2] = new float[]{0,     1,    -off};
                    vertices[3] = new float[]{1,     1,    -off};
                    break;
                case SOUTH: // +Z
                    vertices[0] = new float[]{0,     0,     1 + off};
                    vertices[1] = new float[]{1,     0,     1 + off};
                    vertices[2] = new float[]{1,     1,     1 + off};
                    vertices[3] = new float[]{0,     1,     1 + off};
                    break;
                case WEST:  // -X
                    vertices[0] = new float[]{-off,  0,     0};
                    vertices[1] = new float[]{-off,  0,     1};
                    vertices[2] = new float[]{-off,  1,     1};
                    vertices[3] = new float[]{-off,  1,     0};
                    break;
                case EAST:  // +X
                    vertices[0] = new float[]{1 + off, 0,     1};
                    vertices[1] = new float[]{1 + off, 0,     0};
                    vertices[2] = new float[]{1 + off, 1,     0};
                    vertices[3] = new float[]{1 + off, 1,     1};
                    break;
                case DOWN:  // -Y
                    vertices[0] = new float[]{0,    -off,   0};
                    vertices[1] = new float[]{1,    -off,   0};
                    vertices[2] = new float[]{1,    -off,   1};
                    vertices[3] = new float[]{0,    -off,   1};
                    break;
                case UP:    // +Y
                    vertices[0] = new float[]{0,     1 + off, 1};
                    vertices[1] = new float[]{1,     1 + off, 1};
                    vertices[2] = new float[]{1,     1 + off, 0};
                    vertices[3] = new float[]{0,     1 + off, 0};
                    break;
            }

            VertexConsumer consumer = bufferSource.getBuffer(RenderUtil.getRenderType(origin));

            for (int i = 0; i < 4; i++) {
                float[] local = vertices[i];
                float u, v;
                switch (i) {
                    case 0: // 左下
                        u = minU;
                        v = maxV;
                        break;
                    case 1: // 右下
                        u = maxU;
                        v = maxV;
                        break;
                    case 2: // 右上
                        u = maxU;
                        v = minV;
                        break;
                    case 3: // 左上
                        u = minU;
                        v = minV;
                        break;
                    default:
                        u = minU;
                        v = minV;
                }

                if(origin.getBlock()instanceof SnowyDirtBlock)
                {
                    // 获取生物群系颜色乘数
//                    float r = shade, g = shade, b = shade; // 默认使用 shade 灰度
//                    BlockColors blockColors = Minecraft.getInstance().getBlockColors();
//                    if (blockColors != null) {
//                        int color = blockColors.getColor(origin, level, originPos, 0);
//                        if (color != -1) {
//                            r = ((color >> 16) & 0xFF) / 255.0f * shade;
//                            g = ((color >> 8) & 0xFF) / 255.0f * shade;
//                            b = (color & 0xFF) / 255.0f * shade;
//                        }
//                    }

                    consumer.addVertex(poseStack.last().pose(), local[0], local[1], local[2])
                            .setColor(r, g,b, 1.0f)
                            .setUv(u, v)
                            .setLight(worldLight)
                            .setNormal(normal.getX(), normal.getY(), normal.getZ());
                }
                else if(origin.getBlock()instanceof IShearable)
                {
                    // 获取生物群系颜色乘数
//                    float r = shade, g = shade, b = shade; // 默认使用 shade 灰度
//                    BlockColors blockColors = Minecraft.getInstance().getBlockColors();
//                    if (blockColors != null) {
//                        int color = blockColors.getColor(origin, level, originPos, 0);
//                        if (color != -1) {
//                            r = ((color >> 16) & 0xFF) / 255.0f * shade;
//                            g = ((color >> 8) & 0xFF) / 255.0f * shade;
//                            b = (color & 0xFF) / 255.0f * shade;
//                        }
//                    }

                    consumer.addVertex(poseStack.last().pose(), local[0], local[1], local[2])
                            .setColor(r, g,b, 1.0f)
                            .setUv(u, v)
                            .setLight(worldLight)
                            .setNormal(normal.getX(), normal.getY(), normal.getZ());
                }else{
                    consumer.addVertex(poseStack.last().pose(), local[0], local[1], local[2])
                            .setColor(shade, shade,shade, 1.0f)
                            .setUv(u, v)
                            .setLight(worldLight)
                            .setNormal(normal.getX(), normal.getY(), normal.getZ());
                }

            }
        }


    }
}