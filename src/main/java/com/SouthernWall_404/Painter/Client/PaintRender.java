package com.SouthernWall_404.Painter.Client;

import com.SouthernWall_404.Painter.Common.World.BlockEntity.PaintBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PaintRender implements BlockEntityRenderer<PaintBlockEntity> {

    BlockPos renderPos;


    public PaintRender(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PaintBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        if(blockEntity.getRender()==null)
        {
            return;
        }
        blockEntity.getRender().render(blockEntity,partialTick,poseStack,bufferSource,packedLight,packedOverlay);
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


    /**
     * 渲染单个涂色面
     */
    private void renderPaint(BlockPos paintPos, Direction direction, Block block,
                             PoseStack poseStack, MultiBufferSource bufferSource) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        // 获取纹理
        TextureAtlasSprite sprite = getTexture(block);

        // 计算相对于渲染原点的偏移量（相邻方块的位置）
        Vec3i normal = direction.getNormal();

        double dx = paintPos.getX() - this.renderPos.getX() + normal.getX()+normal.getX()*0.001;
        double dy = paintPos.getY() - this.renderPos.getY() + normal.getY()+normal.getY()*0.001;
        double dz = paintPos.getZ() - this.renderPos.getZ() + normal.getZ()+normal.getZ()*0.001;

        // 该面实际所在的方块位置（用于光照）
        BlockPos facePos = paintPos.relative(direction);

        // 光照值
        int blockLight = level.getBrightness(LightLayer.BLOCK, facePos);
        int skyLight = level.getBrightness(LightLayer.SKY, facePos);
        int worldLight = (blockLight << 4) | (skyLight << 20); // 标准光照打包

        // 环境光遮蔽因子
        float shade = level.getShade(direction, true);

        // 获取纹理 UV 边界
        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        poseStack.pushPose();
        poseStack.translate(dx, dy, dz);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.solid());

        // 根据方向生成四个顶点（局部坐标 + 法向 + UV）
        // 使用 right 和 up 向量构建平面，使纹理 V 始终沿 Y 轴向上
        Direction.Axis axis = direction.getAxis();
        Vec3i rightVec, upVec = new Vec3i(0, 1, 0); // 上方向固定为 Y

        // 确定水平轴（rightVec）：
        // - 南北面：水平轴为 X
        // - 东西面：水平轴为 Z
        // - 上下底：水平轴为 X，垂直轴为 Z（此处简化，上下底可根据需要调整）
        if (axis == Direction.Axis.Y) {
            // 顶/底面：使用 X 作为 U，Z 作为 V（与标准顶底纹理一致）
            // 这里为了简单，让 U 沿 X，V 沿 Z，但注意底面可能需要翻转 V，可自行扩展
            rightVec = new Vec3i(1, 0, 0);
            upVec = new Vec3i(0, 0, 1); // 对于上下底，V 沿 Z
        } else {
            // 侧面：U 沿水平轴，V 沿 Y
            if (axis == Direction.Axis.Z) {
                // 南北面：水平轴为 X
                rightVec = new Vec3i(1, 0, 0);
            } else {
                // 东西面：水平轴为 Z
                rightVec = new Vec3i(0, 0, 1);
            }
            // 如果方向为负（北/西），需要翻转 right 以保证纹理方向一致（避免镜像）
            if (direction == Direction.NORTH || direction == Direction.WEST) {
                rightVec = new Vec3i(-rightVec.getX(), -rightVec.getY(), -rightVec.getZ());
            }
        }

        // 生成四个顶点 (u,v) 从 (0,0) 到 (1,1)
        float[][] uvPoints = {
                {0, 0}, // 对应顶点顺序：左下? 这里根据实际调整
                {1, 0},
                {1, 1},
                {0, 1}
        };

        // 顶点顺序需要是逆时针（从外部看向该面），同时与 UV 对应
        // 对于侧面和顶底，标准的逆时针顺序是：左下 -> 右下 -> 右上 -> 左上
        // 对应 UV: (0,1) (1,1) (1,0) (0,0) 或 (0,0) (1,0) (1,1) (0,1)
        // 我们采用与 ExampleRender 一致的顺序：左下(0,0,0) (u=0,v=1) 开始
        // 为简化，我们直接根据 right/up 计算四个角点，并赋予正确的 UV

        // 四个角点的局部坐标（相对于面所在的方块内部）
        // 面位于该方块的边界上，因此对于侧面，平面方程为：法向轴坐标固定为0
        // 其他两个轴在 [0,1] 变化
        Vec3[] corners = new Vec3[4];
        corners[0] = new Vec3(0, 0, 0); // 我们将用 right/up 组合生成
        corners[1] = new Vec3(1, 0, 0);
        corners[2] = new Vec3(1, 1, 0);
        corners[3] = new Vec3(0, 1, 0);

        // 根据方向将 corners 的 x,y,z 映射到实际坐标
        for (int i = 0; i < 4; i++) {
            double u = (i == 0 || i == 3) ? 0 : 1; // 简化：直接用 i 对应的 u,v
            double v = (i < 2) ? 0 : 1;            // 但更准确应使用上面的 uvPoints

            // 用 right 和 up 向量计算实际位置
            double x = rightVec.getX() * u + upVec.getX() * v;
            double y = rightVec.getY() * u + upVec.getY() * v;
            double z = rightVec.getZ() * u + upVec.getZ() * v;

            // 对于侧面，还需要加上法向偏移？实际上我们在 translate 后已经位于目标方块内，
            // 而面就在该方块的边界上，所以对于侧面，法向坐标应为0（即贴着原方块的那一面）。
            // 上面用 u,v 计算时，我们假设面在法向轴上的坐标为0，因此正确。
            // 但对于顶/底面，法向轴是Y，我们需要保证该面在Y=0（底面）或Y=1（顶面）处。
            // 我们目前 translate 到了相邻方块的位置，对于顶面，相邻方块在 y+1 处，我们在那里渲染底面（Y=0）即可。
            // 所以这里直接使用 (x,y,z) 作为顶点坐标，它已经隐含了法向轴为0的条件。

            // 根据 i 获取对应的 UV
            float uTex = (i == 0 || i == 3) ? minU : maxU;
            float vTex = (i < 2) ? maxV : minV; // 注意 V 方向：原点在左上，所以左下对应 maxV

            // 提交顶点
            consumer.addVertex(poseStack.last().pose(), (float)x, (float)y, (float)z)
                    .setColor(shade, shade, shade, 1.0f)
                    .setUv(uTex, vTex)
                    .setLight(worldLight)
                    .setNormal(normal.getX(), normal.getY(), normal.getZ());
        }

        poseStack.popPose();
    }

    private TextureAtlasSprite getTexture(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        // 将方块注册名转换为纹理路径，例如 "minecraft:iron_block" -> "minecraft:block/iron_block"
        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(
                key.getNamespace(), "block/" + key.getPath());
        return Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(textureLocation);
    }


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