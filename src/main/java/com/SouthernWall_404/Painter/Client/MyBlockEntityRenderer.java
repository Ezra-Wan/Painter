package com.SouthernWall_404.Painter.Client;

import com.SouthernWall_404.Painter.Common.World.BlockEntity.RenderBedRockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.neoforged.neoforge.client.model.lighting.LightPipelineAwareModelBlockRenderer;

public class MyBlockEntityRenderer implements BlockEntityRenderer<RenderBedRockEntity> {

    private TextureAtlasSprite ironBlockTopSprite;

    public MyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        ResourceLocation ironBlockTexture = ResourceLocation.withDefaultNamespace("block/iron_block");
        this.ironBlockTopSprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ironBlockTexture);
    }

    @Override
    public void render(RenderBedRockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) return;

        // 将渲染位置移动到方块实体的南侧（+Z方向一格）
        poseStack.pushPose();
        poseStack.translate(0, 0, 1.001); // 移到相邻方块的位置

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());

        // 南侧面的四个顶点局部坐标（相对于移动后的原点），法向为 (0,0,1)
        float[][] vertices = {
                {0.0f, 0.0f, 0.0f}, // 左下
                {1.0f, 0.0f, 0.0f}, // 右下
                {1.0f, 1.0f, 0.0f}, // 右上
                {0.0f, 1.0f, 0.0f}  // 左上
        };

        // 获取纹理 UV
        float minU = ironBlockTopSprite.getU0();
        float maxU = ironBlockTopSprite.getU1();
        float minV = ironBlockTopSprite.getV0();
        float maxV = ironBlockTopSprite.getV1();

        BlockPos origin = blockEntity.getBlockPos();
        // 计算该面所在方块的位置（原方块南侧一格）



        BlockPos facePos = new BlockPos(origin.getX(), origin.getY(), origin.getZ() +1);



        float shade = level.getShade(Direction.SOUTH, true); // true 表示应被阴影遮挡

        // 获取该方块的光照值（不再进行顶点平均）
        int blockLight = level.getBrightness(LightLayer.BLOCK, facePos);
        int skyLight = level.getBrightness(LightLayer.SKY, facePos);
        int worldLight = (blockLight << 4) | (skyLight << 20); // 标准光照打


        for (int i = 0; i < 4; i++) {
            float[] local = vertices[i];


            // 选择正确的 UV 角（与顶点顺序匹配）
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

            // 提交顶点，所有顶点使用相同的光照值
            consumer.addVertex(poseStack.last().pose(), local[0], local[1], local[2])
                    .setColor(shade, shade, shade, 1.0f)
                    .setUv(u, v)
                    .setLight(worldLight)
                    .setNormal(0, 0, 1);
        }

        poseStack.popPose();
    }
}