// AbstractPaint.java
package com.SouthernWall_404.Painter.API.Paint.API;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticeInfo;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.ModelRender;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuadRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.DataResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.*;

/**
 * Quad Vertice顺序 留档备用
 * 顺序：左上，左下，右下，右上
 */
public abstract class AbstractPaint extends AbstractRender<List<BakedQuad>, Direction> {

//    protected static final int FLAG_NONE = -1;
    protected static final int NORTH = 1, SOUTH = 2, WEST = 4, EAST = 8, UP = 16, DOWN = 32;
    protected static final int NULL_NORTH = -1, NULL_SOUTH = -2, NULL_WEST = -4, NULL_EAST = -8, NULL_UP = -16, NULL_DOWN = -32;

    //========不需要持久化的数据========
    public static float[] shape = new float[ModelRender.DIRECTIONS.length * 2];
    private static BitSet shapeFlags = new BitSet(3);
    private int tick = 0;
    private Map<Integer, ModelRender.AmbientOcclusionFace> aoFaces = new HashMap<>();

    protected Map<Integer,Boolean> visibles =new HashMap<>();

//    protected Map<Integer,Vector3f> normals=new HashMap<>();
    //========需要持久化的数据========
    protected Map<Integer, BlockState> materials = new HashMap<>();

    //========构造方法========
    public AbstractPaint(BlockPos blockPos,String type) {
        super(blockPos,type);
        update();
    }


    //========内部方法========
    @OnlyIn(Dist.CLIENT)
    @Override
    protected void update() {
        super.update();
        if(FMLEnvironment.dist!=Dist.CLIENT)return;
        if (Minecraft.getInstance() == null) {
            return;
        }
        createQuads();
        refreshVisible();
        refreshAO(blockPos);

    }

    @Override
    protected void registerFlag(Direction direction, int flag) {
        super.registerFlag(direction, flag);

        normals.put(flag,new Vector3f(direction));
        normals.put(-flag,new Vector3f(direction));

    }

    public void cycleTextureUV(Direction direction) {
        // 预留
    }

    public void cycleTextureDir(Direction direction) {
        BlockState material = getMaterial(direction);
        if (material.hasProperty(TrapDoorBlock.HALF) && material.getOptionalValue(TrapDoorBlock.OPEN).orElse(false))
            paint(direction, material.cycle(TrapDoorBlock.HALF));
        else if (material.hasProperty(BlockStateProperties.FACING))
            paint(direction, material.cycle(BlockStateProperties.FACING));
        else if (material.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            paint(direction, material.setValue(BlockStateProperties.HORIZONTAL_FACING,
                    material.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()));
        else if (material.hasProperty(BlockStateProperties.AXIS))
            paint(direction, material.cycle(BlockStateProperties.AXIS));
        else if (material.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
            paint(direction, material.cycle(BlockStateProperties.HORIZONTAL_AXIS));
        else if (material.hasProperty(BlockStateProperties.LIT))
            paint(direction, material.cycle(BlockStateProperties.LIT));
        super.update();
    }

    public List<BakedQuad> getQuadsForDirection(BlockState state, int flag) {
        Direction direction;

        if(flag<0)
        {
            direction=null;
        }else direction=getDirection(flag);

        BakedModel model = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
        RandomSource random = RandomSource.create();
        RenderType renderType = RenderUtil.getRenderType(state);
        return model.getQuads(state, direction, random, ModelData.EMPTY, renderType);
    }

    public Direction getDirection(int flag) {
        if(flag<0)flag=-flag;
        for (Map.Entry<Direction, Integer> entry : flags.entrySet()) {
            if (entry.getValue() == flag) {
                return entry.getKey();
            }
        }
        return Direction.NORTH;
    }

    @OnlyIn(Dist.CLIENT)
    public void refreshVisible() {
        Level level = Minecraft.getInstance().level;
        flags.forEach((direction, flag) -> {
            boolean shouldRender = RenderUtil.shouldRenderFace(blockPos, origin, direction);
            visibles.put(flag, shouldRender);
            visibles.put(-flag,shouldRender);
        });
    }
    @OnlyIn(Dist.CLIENT)
    public void refreshAO(BlockPos blockPos) {
        Level level = Minecraft.getInstance().level;
        if (level==null)return;
        for (Map.Entry<Integer, BlockState> entry : materials.entrySet()) {
            int flag = entry.getKey();
            BlockState state = materials.get(flag);
            Direction direction = getDirection(flag);

            ModelRender.AmbientOcclusionFace aoFace = new ModelRender.AmbientOcclusionFace();
            aoFace.calculate(level, state, blockPos.relative(direction), direction, shape, shapeFlags, true);
            aoFaces.put(flag, aoFace);
        }
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

    private static int[][] UV_ORDER=new int[][]{
            {0,1},
            {1,1},
            {1,0},
            {0,0}
    };

    private static int[]test=new int[]{0,3,2,1};
    public List<BakedQuad> createQuad(int flag, BlockState material)
    {
        Direction direction = getDirection(flag);
        List<BakedQuad> originQuads = getQuadsForDirection(origin, flag);
        List<BakedQuad> materialQuads = getQuadsForDirection(material, getFlag(direction));

        if (originQuads == null || originQuads.isEmpty() || materialQuads == null || materialQuads.isEmpty()) {
            return List.of();
        }

        // 为了简单，假设每个方向只有一个 quad（多数方块模型如此）；
        // 如果有多个，你可以按索引一一对应，或统一使用第一个 originQuad 的形状。
        BakedQuad originQuad = originQuads.get(0);
        VerticesInfo originVertices = new VerticesInfo(originQuad);

        List<BakedQuad> newQuads = new ArrayList<>();

        for (BakedQuad materialQuad : materialQuads) {
            VerticesInfo materialVertices = new VerticesInfo(materialQuad);

            VerticeInfo materialLeftDown=materialVertices.LeftDown();
            float startU=materialLeftDown.u;
            float startV=materialLeftDown.v;
            float uOffset=originVertices.getULength();
            float vOffset=originVertices.getVLength();


            // 逐顶点混合数据
            VerticeInfo[] mixedVertices = new VerticeInfo[4];

            for (int i = 0; i < 4; i++) {
                VerticeInfo originVert = originVertices.vertices.get(test[i]);
                VerticeInfo materialVert = materialVertices.vertices.get(test[i]);

                mixedVertices[i] = VerticeInfo.builder()
                        .position(originVert.position)          // 几何位置来自原始方块
                        .color(materialVert.alpha, materialVert.red, materialVert.green, materialVert.blue) // 颜色来自原始方块
                        .uv(startU+uOffset*UV_ORDER[i][0],startV+vOffset*UV_ORDER[i][1])     // ★ 纹理坐标来自材质方块 ★
                        .light(originVert.light)                // 光照值保持原始方块（或可根据需要混合）
                        .normal(originVert.normal)              // 法线保持原始方块
                        .build();
            }
            //TODO 改Laplace
            //TODO 改uv旋转

            VerticesInfo newQuadVertices = new VerticesInfo(List.of(mixedVertices));
            int[] newVertexArray = newQuadVertices.vertices();

            // 使用 materialQuad 的元数据创建新的 BakedQuad
            BakedQuad newQuad = new BakedQuad(
                    newVertexArray,
                    materialQuad.getTintIndex(),
                    originQuad.getDirection(),
                    materialQuad.getSprite(),
                    true
            );

            newQuads.add(newQuad);
        }

        return newQuads;
    }

    public void createQuads() {
        materials.forEach((flag, material) -> {


            objects.put(flag, createQuad(flag,material));   // 存入结果，objects 应为 Map<Integer, List<BakedQuad>>
        });
    }

    private static final double offset = 0.01; //TODO 考虑添加配置项
    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockPos blockPos, PoseStack poseStack, int packedLight, int packedOverlay, float partialTick, VertexConsumer buffer) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        for (Map.Entry<Integer, List<BakedQuad>> entry : objects.entrySet()) {
            List<BakedQuad> quads = entry.getValue();
            int flag = entry.getKey();
            Vector3f normal=normals.get(flag);

            BlockState material = materials.get(flag);
            Direction direction = getDirection(flag);


            if (!visibles.getOrDefault(flag,false))continue;

            Vec3 vec3 = new Vec3(blockPos.getX() + offset * normal.getX(),
                    blockPos.getY() + offset * normal.getY(),//TODO 记得删测试
                    blockPos.getZ() + offset * normal.getZ());
            for (BakedQuad quad : quads) {
                if (aoFaces.containsKey(flag)) {
                    BakedQuadRender.renderInOfferredAO(quad, material, vec3, poseStack, buffer, aoFaces.get(flag));
                } else {
                    refreshAO(blockPos);
                    if (aoFaces.containsKey(flag))
                        BakedQuadRender.renderInOfferredAO(quad, material, vec3, poseStack, buffer, aoFaces.get(flag));
                }
            }
        }
    }

    public final void paint(Direction f, BlockState blockState) {

        putMaterial(f,blockState);
        update();
    }

    public void putMaterial(Direction f, BlockState blockState)
    {
        int flag = getFlag(f);

        if(hasNullInDirection(f))
        {
            materials.put(-flag,blockState);
        }else
        {
            materials.put(flag, blockState);
        }
    }

    public abstract boolean hasNullInDirection(Direction f);


    public BlockState getMaterial(Direction f) {
        int flag = getFlag(f);
        return materials.get(flag);
    }

    private int hasBlockInPaint(BlockState toCheck) {
        for (Map.Entry<Integer, BlockState> entry : materials.entrySet()) {
            BlockState blockState = entry.getValue();
            if (blockState != null && blockState.getBlock() == toCheck.getBlock()) {
                return entry.getKey();
            }
        }
        return -1;
    }
    //========覆盖序列化方法，处理 materials========
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);
        // 序列化 materials
        ListTag materialsList = new ListTag();
        for (Map.Entry<Integer, BlockState> entry : materials.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("flag", entry.getKey());
            DataResult<Tag> stateResult = BlockState.CODEC.encode(entry.getValue(),
                    provider.createSerializationContext(NbtOps.INSTANCE), new CompoundTag());
            entryTag.put("state", stateResult.getOrThrow());
            materialsList.add(entryTag);
        }
        tag.put("materials", materialsList);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        super.deserializeNBT(provider, tag);
        this.materials.clear();
        ListTag materialsList = tag.getList("materials", Tag.TAG_COMPOUND);
        for (int i = 0; i < materialsList.size(); i++) {
            CompoundTag entryTag = materialsList.getCompound(i);
            int flag = entryTag.getInt("flag");
            Tag stateTag = entryTag.get("state");
            DataResult<BlockState> stateResult = BlockState.CODEC.parse(
                    provider.createSerializationContext(NbtOps.INSTANCE), stateTag);
            BlockState state = stateResult.getOrThrow();
            materials.put(flag, state);
        }

        createQuads();
//        if(FMLEnvironment.dist==Dist.CLIENT) update();
    }
}