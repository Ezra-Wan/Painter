package com.SouthernWall_404.Painter.API.Paint;

import com.SouthernWall_404.Painter.Common.Init.ModBlock;
import com.SouthernWall_404.Painter.Common.World.BlockEntity.PaintBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.IShearable;
import org.joml.Vector2d;

import java.util.*;

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

                BlockState state=getPaintedBlock(direction).defaultBlockState();
                VertexConsumer testconsumer = bufferSource.getBuffer(RenderUtil.getRenderType(state));
                for (BakedQuad quad : quads) {
                    modRenderer.calculateShape(level, state, pos, quad.getVertices(), quad.getDirection(), shape, shapeFlags);


                    aoFace.calculate(level, state, pos, quad.getDirection(), shape, shapeFlags, quad.isShade());


                    modRenderer.putQuadData(level, state, pos,testconsumer, poseStack.last(), quad,
                            aoFace.brightness[0], aoFace.brightness[1], aoFace.brightness[2], aoFace.brightness[3],
                            aoFace.lightmap[0], aoFace.lightmap[1], aoFace.lightmap[2], aoFace.lightmap[3],
                            packedOverlay);
                }

            }
            else
            {
                VertexConsumer consumer = bufferSource.getBuffer(renderType);
                random.setSeed(seed);
                quads= model.getQuads(origin, direction, random, ModelData.EMPTY, renderType);

                for (BakedQuad quad : quads) {
                    modRenderer.calculateShape(level, origin, pos, quad.getVertices(), quad.getDirection(), shape, shapeFlags);


                    aoFace.calculate(level, origin, pos, quad.getDirection(), shape, shapeFlags, quad.isShade());


                    modRenderer.putQuadData(level, origin, pos, consumer, poseStack.last(), quad,
                            aoFace.brightness[0], aoFace.brightness[1], aoFace.brightness[2], aoFace.brightness[3],
                            aoFace.lightmap[0], aoFace.lightmap[1], aoFace.lightmap[2], aoFace.lightmap[3],
                            packedOverlay);
                }

                // 渲染无方向的原版面（如粒子面）
                random.setSeed(seed);
                List<BakedQuad> generalQuads = model.getQuads(origin, null, random, ModelData.EMPTY, renderType);
                if (!generalQuads.isEmpty()) {
                    for (BakedQuad quad : generalQuads) {
                        modRenderer.calculateShape(level, origin, pos, quad.getVertices(), quad.getDirection(), shape, shapeFlags);
                        aoFace.calculate(level, origin, pos, quad.getDirection(), shape, shapeFlags, quad.isShade());
                        modRenderer.putQuadData(level, origin, pos, consumer, poseStack.last(), quad,
                                aoFace.brightness[0], aoFace.brightness[1], aoFace.brightness[2], aoFace.brightness[3],
                                aoFace.lightmap[0], aoFace.lightmap[1], aoFace.lightmap[2], aoFace.lightmap[3],
                                packedOverlay);
                    }
                }
            }
            if (quads.isEmpty()) continue;




        }



//// ----- 渲染自定义 Quad -----
//        if (!objects.isEmpty()) {
//            for (BakedQuad quad : customQuads) {
//                Direction direction = quad.getDirection();
//                // 可选：检查该面是否应被渲染（相邻方块遮挡）
//                BlockPos neighborPos = pos.relative(direction);
//                if (!RenderUtil.shouldRenderFace(origin, level, pos, direction, neighborPos)) continue;
//
//                modRenderer.calculateShape(level, origin, pos, quad.getVertices(), direction, shape, shapeFlags);
//                aoFace.calculate(level, origin, pos, direction, shape, shapeFlags, quad.isShade());
//                modRenderer.putQuadData(level, origin, pos, consumer, poseStack.last(), quad,
//                        aoFace.brightness[0], aoFace.brightness[1], aoFace.brightness[2], aoFace.brightness[3],
//                        aoFace.lightmap[0], aoFace.lightmap[1], aoFace.lightmap[2], aoFace.lightmap[3],
//                        packedOverlay);
//            }
//        }
        poseStack.popPose();
    }

    // 示例：构造自定义 Quad 的方法（你需要根据需求实现）
    private List<BakedQuad> buildCustomQuads(Level level, BlockPos pos, BlockState state) {

        List<BakedQuad> quads = new ArrayList<>();
        // 遍历所有方向，为每个方向生成一个 quad（如果对应 flag 有纹理）
        for (Direction direction : Direction.values()) {
            int flag = getFlag(direction); // 假设你已将 Direction 注册为 flag key

            List<BakedQuad> quad=objects.get(flag);
            if(quad==null)continue;


            quads.addAll(quad);
//            TextureAtlasSprite sprite = objects.get(flag);
//            if (sprite == null) continue; // 没有自定义纹理则跳过
//
//            // 构建该方向的 quad
//            BakedQuad quad = createQuad(sprite, direction);
//            quads.add(quad);
        }

        // 也可以根据其他逻辑生成自定义 quad，例如使用特定的 flag 值
        // int customFlag = getFlag("someKey");
        // TextureAtlasSprite customSprite = objects.get(customFlag);
        // if (customSprite != null) { ... }

        return quads;
    }
    private BakedQuad createQuad(TextureAtlasSprite sprite, Direction direction) {
        // 1. 标准顺序的四个角坐标（左下、右下、右上、左上）
        float[][] positions = new float[4][3];
        switch (direction) {
            case DOWN:
                positions[0] = new float[]{0, 0, 0}; // 左下
                positions[1] = new float[]{1, 0, 0}; // 右下
                positions[2] = new float[]{1, 0, 1}; // 右上
                positions[3] = new float[]{0, 0, 1}; // 左上
                break;
            case UP:
                positions[0] = new float[]{0, 1, 1}; // 左下
                positions[1] = new float[]{1, 1, 1}; // 右下
                positions[2] = new float[]{1, 1, 0}; // 右上
                positions[3] = new float[]{0, 1, 0}; // 左上
                break;
            case NORTH:
                // 修正：左下应为 (0,0,0)，右下 (1,0,0)，右上 (1,1,0)，左上 (0,1,0)
                positions[0] = new float[]{1, 0, 0}; // 左下
                positions[1] = new float[]{0, 0, 0}; // 右下
                positions[2] = new float[]{0, 1, 0}; // 右上
                positions[3] = new float[]{1, 1, 0}; // 左上
                break;
            case SOUTH:
                positions[0] = new float[]{0, 0, 1}; // 左下
                positions[1] = new float[]{1, 0, 1}; // 右下
                positions[2] = new float[]{1, 1, 1}; // 右上
                positions[3] = new float[]{0, 1, 1}; // 左上
                break;
            case WEST:
                // 注意：WEST 面（法向 -X），左下应为 (0,0,0)? 实际上 WEST 面位于 x=0 平面，左下应该是 (0,0,0)（y最小z最小），右下 (0,0,1)（y最小z最大），右上 (0,1,1)，左上 (0,1,0)
                positions[0] = new float[]{0, 0, 0};
                positions[1] = new float[]{0, 0, 1};
                positions[2] = new float[]{0, 1, 1};
                positions[3] = new float[]{0, 1, 0};
                break;
            case EAST:
                // EAST 面（法向 +X），位于 x=1 平面，左下应该是 (1,0,1)（y最小z最大），右下 (1,0,0)（y最小z最小），右上 (1,1,0)，左上 (1,1,1)
                positions[0] = new float[]{1, 0, 1};
                positions[1] = new float[]{1, 0, 0};
                positions[2] = new float[]{1, 1, 0};
                positions[3] = new float[]{1, 1, 1};
                break;
            default:
                return null;
        }

        // 2. 标准 UV 映射：左下(U0,V1), 右下(U1,V1), 右上(U1,V0), 左上(U0,V0)
        float[] u = new float[4];
        float[] v = new float[4];
        u[0] = sprite.getU0(); v[0] = sprite.getV1();
        u[1] = sprite.getU1(); v[1] = sprite.getV1();
        u[2] = sprite.getU1(); v[2] = sprite.getV0();
        u[3] = sprite.getU0(); v[3] = sprite.getV0();

        // 3. 获取重映射表（标准索引 → 实际顶点索引）
        int[] remap = getRemapForDirection(direction);

        // 4. 按重映射表填充最终顶点数据
        int[] vertexData = new int[32];
        for (int stdIdx = 0; stdIdx < 4; stdIdx++) {
            int targetIdx = remap[stdIdx];
            int offset = targetIdx * 8;
            vertexData[offset + 0] = Float.floatToRawIntBits(positions[stdIdx][0]);
            vertexData[offset + 1] = Float.floatToRawIntBits(positions[stdIdx][1]);
            vertexData[offset + 2] = Float.floatToRawIntBits(positions[stdIdx][2]);
            vertexData[offset + 3] = -1; // 颜色白色
            vertexData[offset + 4] = Float.floatToRawIntBits(u[stdIdx]);
            vertexData[offset + 5] = Float.floatToRawIntBits(v[stdIdx]);
            vertexData[offset + 6] = 0;   // 光照占位
            vertexData[offset + 7] = 0;   // 法线占位
        }

        return new BakedQuad(vertexData, -1, direction, sprite, true);
    }

    private int[] getRemapForDirection(Direction dir) {
        switch (dir) {
            case DOWN:
            case SOUTH:
                return new int[]{0, 1, 2, 3};
            case UP:
                return new int[]{2, 3, 0, 1};
            case NORTH:
                return new int[]{3, 0, 1, 2};
            case WEST:
                return new int[]{ 0, 1, 2, 3};
            case EAST:
                return new int[]{3, 0, 1, 2};
            default:
                return new int[]{1, 2, 3, 0};
        }
    }

    private static void renderQuadList(PoseStack.Pose pose, VertexConsumer consumer, float red, float green, float blue, List<BakedQuad> quads, int packedLight, int packedOverlay) {
        for(BakedQuad bakedquad : quads) {
            float f;
            float f1;
            float f2;
//            if (bakedquad.isTinted()) {
                f = red;
                f1 = green;
                f2 = blue;
//            } else {
//                f = 1.0F;
//                f1 = 1.0F;
//                f2 = 1.0F;
//            }


            consumer.putBulkData(pose, bakedquad, f, f1, f2, 1.0F, packedLight, packedOverlay);
        }

    }
    //
//    @Override
//    public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack,
//                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
//        // 该方法需要根据标志位调用 renderFace，暂留空
//
//        if(origin==null)
//        {
//            return;
//        }
//
//
//        for(Direction direction:Direction.values())
//        {
//            boolean shouldRender=RenderUtil.shouldRenderFace(blockEntity.getLevel(),blockEntity.getBlockPos(),origin,direction);
//            if(shouldRender)
//            {
//
//                List<BakedQuad> quads=RenderUtil.getQuads(origin,direction);
//                for(BakedQuad quad:quads)
//                {
//                    VertexConsumer consumer = bufferSource.getBuffer(RenderUtil.getRenderType(origin));
//
////                    renderQuadManually(blockEntity,quad,poseStack,bufferSource,packedLight,packedOverlay);
//                }
//            }
//        }
//    }
//
//
//    /**
//     * 从 BakedQuad 中提取信息，并使用手动渲染方式渲染该面
//     */
//    private static void renderQuadManually(BlockEntity blockEntity, BakedQuad quad,
//                                           PoseStack poseStack, MultiBufferSource bufferSource,
//                                           int packedLight, int packedOverlay) {
//        if (!(blockEntity instanceof PaintBlockEntity paintBE)) return;
//        Level level = blockEntity.getLevel();
//        if (level == null) return;
//
//        BlockPos pos = blockEntity.getBlockPos();
//        BlockState origin = paintBE.getOrigin();
//
//        // 从 quad 中提取纹理和方向
//        TextureAtlasSprite sprite = quad.getSprite();
//        Direction face = quad.getDirection(); // quad 的实际方向
//
//        // 获取染色信息
//        int tintIndex = quad.isTinted() ? quad.getTintIndex() : -1;
//
//
//
//
//
//        // 调用手动渲染方法（需要扩展 renderFace 以支持 tintIndex）
////        renderFace(blockEntity, sprite, face, tintIndex, poseStack, bufferSource, packedLight, packedOverlay);
//    }
//    /**
//     * 渲染单个面
//     * @param blockEntity   方块实体
//     * @param texture       要使用的纹理
//     * @param direction     要渲染的面方向
//     * @param poseStack     变换栈
//     * @param bufferSource  渲染缓冲区
//     */
//    private static void renderFace(BlockEntity blockEntity, TextureAtlasSprite texture, Direction direction,int tintIndex,
//                                   PoseStack poseStack, MultiBufferSource bufferSource, int partickedLight, int packedOverlay) {
//        if(blockEntity instanceof PaintBlockEntity paintBlockEntity) {
//            Level level = blockEntity.getLevel();
//            if (level == null) return;
//
//
//            BlockPos originPos = blockEntity.getBlockPos();
//            BlockPos facePos = originPos.relative(direction);
//            BlockState origin=paintBlockEntity.getOrigin();
//            // 获取阴影系数
//            float shade = level.getShade(direction, true);
//
//            // 计算颜色乘数
//            float r = shade, g = shade, b = shade;
//            if (tintIndex >= 0) {
//                BlockColors blockColors = Minecraft.getInstance().getBlockColors();
//                int color = blockColors.getColor(origin, level,originPos, tintIndex);
//                if (color != -1) {
//                    r = ((color >> 16) & 0xFF) / 255.0f * shade;
//                    g = ((color >> 8) & 0xFF) / 255.0f * shade;
//                    b = (color & 0xFF) / 255.0f * shade;
//                }
//            }
//
//
//            int worldLight=0;
//
//
////            VideoSettingsScreen
//            Options options=Minecraft.getInstance().options;
//
////            if(options.)else
//            // 获取该位置的光照值
////            int blockLight = level.getBrightness(LightLayer.BLOCK, facePos);
////            int skyLight   = level.getBrightness(LightLayer.SKY, facePos);
////
////            worldLight = (blockLight << 4) | (skyLight << 20); // 标准光照打包
//
//
//
//
//            // 法向向量
//            Vec3i normal = direction.getNormal();
//
//            // 纹理 UV 范围
//            float minU = texture.getU0();
//            float maxU = texture.getU1();
//            float minV = texture.getV0();
//            float maxV = texture.getV1();
//
//            // 根据方向确定四个顶点的局部坐标（左下、右下、右上、左上）
//            float off = 0.001f; // 微小偏移，避免深度冲突
//            float[][] vertices = new float[4][3];
//
//            switch (direction) {
//                case NORTH: // -Z
//                    vertices[0] = new float[]{1,     0,    -off};
//                    vertices[1] = new float[]{0,     0,    -off};
//                    vertices[2] = new float[]{0,     1,    -off};
//                    vertices[3] = new float[]{1,     1,    -off};
//                    break;
//                case SOUTH: // +Z
//                    vertices[0] = new float[]{0,     0,     1 + off};
//                    vertices[1] = new float[]{1,     0,     1 + off};
//                    vertices[2] = new float[]{1,     1,     1 + off};
//                    vertices[3] = new float[]{0,     1,     1 + off};
//                    break;
//                case WEST:  // -X
//                    vertices[0] = new float[]{-off,  0,     0};
//                    vertices[1] = new float[]{-off,  0,     1};
//                    vertices[2] = new float[]{-off,  1,     1};
//                    vertices[3] = new float[]{-off,  1,     0};
//                    break;
//                case EAST:  // +X
//                    vertices[0] = new float[]{1 + off, 0,     1};
//                    vertices[1] = new float[]{1 + off, 0,     0};
//                    vertices[2] = new float[]{1 + off, 1,     0};
//                    vertices[3] = new float[]{1 + off, 1,     1};
//                    break;
//                case DOWN:  // -Y
//                    vertices[0] = new float[]{0,    -off,   0};
//                    vertices[1] = new float[]{1,    -off,   0};
//                    vertices[2] = new float[]{1,    -off,   1};
//                    vertices[3] = new float[]{0,    -off,   1};
//                    break;
//                case UP:    // +Y
//                    vertices[0] = new float[]{0,     1 + off, 1};
//                    vertices[1] = new float[]{1,     1 + off, 1};
//                    vertices[2] = new float[]{1,     1 + off, 0};
//                    vertices[3] = new float[]{0,     1 + off, 0};
//                    break;
//            }
//
//            VertexConsumer consumer = bufferSource.getBuffer(RenderUtil.getRenderType(origin));
//
//
//            for (int i = 0; i < 4; i++) {
//
//                float[] local = vertices[i];
//                double worldX = originPos.getX() + local[0];
//                double worldY = originPos.getY() + local[1];
//                double worldZ = originPos.getZ() + local[2];
//
//                int vertexLight;
////                if (smoothLighting == 0) {
////                    // 平滑关闭：使用面所在方块的光照（保持原逻辑）
////                    int blockLight = level.getBrightness(LightLayer.BLOCK, facePos);
////                    int skyLight   = level.getBrightness(LightLayer.SKY, facePos);
////                    vertexLight = (blockLight << 4) | (skyLight << 20);
////                } else {
//                    // 平滑开启：顶点亮度由周围八个方块的光照平均得到
//                    vertexLight = getSmoothLight(level, new BlockPos((int) worldX,(int)worldY,(int)worldZ),direction);
////                }
//
//
//                float u, v;
//                switch (i) {
//                    case 0: // 左下
//                        u = minU;
//                        v = maxV;
//                        break;
//                    case 1: // 右下
//                        u = maxU;
//                        v = maxV;
//                        break;
//                    case 2: // 右上
//                        u = maxU;
//                        v = minV;
//                        break;
//                    case 3: // 左上
//                        u = minU;
//                        v = minV;
//                        break;
//                    default:
//                        u = minU;
//                        v = minV;
//                }
//
//                if(origin.getBlock()instanceof SnowyDirtBlock)
//                {
//                    consumer.addVertex(poseStack.last().pose(), local[0], local[1], local[2])
//                            .setColor(r, g,b, 1.0f)
//                            .setUv(u, v)
//                            .setLight(vertexLight)
//                            .setNormal(normal.getX(), normal.getY(), normal.getZ());
//                }
//                else if(origin.getBlock()instanceof IShearable)
//                {
//
//                    consumer.addVertex(poseStack.last().pose(), local[0], local[1], local[2])
//                            .setColor(r, g,b, 1.0f)
//                            .setUv(u, v)
//                            .setLight(vertexLight)
//                            .setNormal(normal.getX(), normal.getY(), normal.getZ());
//                }else{
//                    consumer.addVertex(poseStack.last().pose(), local[0], local[1], local[2])
//                            .setColor(shade, shade,shade, 1.0f)
//                            .setUv(u, v)
//                            .setLight(vertexLight)
//                            .setNormal(normal.getX(), normal.getY(), normal.getZ());
//                }
//
//            }
//        }
//    }
//
//    // 辅助方法：计算顶点处的平滑光照（八个相邻方块的平均值）
//    private static int getSmoothLight(Level level,BlockPos pos,Direction direction) {
//
//        int x=0;
//        int y=0;
//        int z=0;
//        Vec3i normal=direction.getNormal();
//        x=normal.getX()==1?0:1;
//        y=normal.getY()==1?0:1;
//        z=normal.getZ()==1?0:1;
//
//        Direction.Axis axis=direction.getAxis();
//
//        Vec3 vec3=CommonAPI.buildVec2(1,1,axis);
//
//        int sumBlock = 0;
//        int sumSky = 0;
//
//
//        for(int i=0;i<4;i++)
//        {
//            int dX=(int) Math.floor(vec3.x*0.5);
//            int dY=(int) Math.floor(vec3.y*0.5);
//            int dZ=(int) Math.floor(vec3.z*0.5);
//
//            BlockPos checkPos=new BlockPos(pos.getX()+dX,pos.getY()+dY,pos.getZ()+dZ);
//            sumBlock += level.getBrightness(LightLayer.BLOCK, checkPos);
//            sumSky   += level.getBrightness(LightLayer.SKY, checkPos);
//
//            vec3=CommonAPI.rotate90(vec3,axis);
//        }
//
////        for(int i=-1;i<=0;i++)
////        {
////            for(int j=-1;j<=0;j++)
////            {
////                for(int k=-1;j<=0;j++)
////                {
////                    BlockPos checkPos=
////                    BlockPos checkPos = new BlockPos(surface.getX()*i+pos.getX(),surface.getY()*j+pos.getY(),pos.getZ()+surface.getZ()*k);
////                    sumBlock += level.getBrightness(LightLayer.BLOCK, checkPos);
////                    sumSky   += level.getBrightness(LightLayer.SKY, checkPos);
////                }
////            }
////        }
//
//
//        // 取平均（整数除法）
//        int avgBlock = sumBlock / 4;
//        int avgSky   = sumSky / 4;
//        return (avgBlock << 4) | (avgSky << 20);
//    }
}