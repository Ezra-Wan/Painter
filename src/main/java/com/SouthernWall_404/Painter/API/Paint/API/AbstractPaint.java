// AbstractPaint.java
package com.SouthernWall_404.Painter.API.Paint.API;

// logging removed: previously used for debug logging
import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticeInfo;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.ModelRender;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.Configs;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuadRender;
import com.SouthernWall_404.Painter.Client.Config.ClientConfig;
import com.SouthernWall_404.Painter.Client.PaintRender;
import com.SouthernWall_404.Painter.Painter;
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
// removed: java.util.Arrays import

/**
 * Quad Vertice顺序 留档备用
 * 顺序：左上，左下，右下，右上
 */
public abstract class AbstractPaint extends AbstractRender<List<BakedQuad>, Direction> {
    //LOGGER removed

    protected static final int NORTH = 1, SOUTH = 2, WEST = 4, EAST = 8, UP = 16, DOWN = 32;



    //========不需要持久化的数据========
    public static float[] shape = new float[ModelRender.DIRECTIONS.length * 2];
    private static BitSet shapeFlags = new BitSet(3);
    private Map<Integer, ModelRender.AmbientOcclusionFace> aoFaces = new HashMap<>();
    protected Map<Integer,Boolean> visibles =new HashMap<>();
    //========需要持久化的数据========
    protected Map<Integer, BlockState> materials = new HashMap<>();
    protected Map<Integer,float[]> uvOffsets =new HashMap<>();

    //========构造方法========
    public AbstractPaint(BlockPos blockPos,String type) {
        super(blockPos,type);

        registerUVs();
    }

    @Override
    protected void registerFlag(Direction direction, int flag) {
        super.registerFlag(direction, flag);

        normals.put(flag,new Vector3f(direction));
        normals.put(-flag,new Vector3f(direction));

    }

    private void registerUVs()
    {
        if(uvOffsets==null)
        {
            uvOffsets=new HashMap<>();
        }

        flags.forEach((direction,flag)->{
            uvOffsets.put(flag,new float[]{0,0});
            uvOffsets.put(-flag,new float[]{0,0});
        });
    }

    /**
     * 循环切换指定方向面的纹理UV偏移位置
     * 使用origin面的xLength和yLength作为直接步长
     * 逻辑：先尝试增加U，如果U超出边界(1.0)，则尝试增加V；
     * 如果V也超出边界，则重置为(0,0)
     */
    public void cycleTextureUV(Direction direction) {
        int flag = getFlag(direction);
        // 获取指定方向的原始四边形以读取其UV空间尺寸
        List<BakedQuad> originQuads = getQuadsForDirection(origin, flag);
        if (originQuads == null || originQuads.isEmpty()) {
            return;
        }
        BakedQuad originQuad = originQuads.get(0);
        VerticesInfo originVertices = new VerticesInfo(originQuad);
        float stepU = originVertices.getXLength();
        float stepV = originVertices.getYLength();
        // 如果长度为非正值，使用合理的默认值
        if (stepU <= 0f) stepU = 0f;
        if (stepV <= 0f) stepV = 0f;

        // 获取当前UV偏移量
        float[] current = uvOffsets.get(flag);
        if (current == null || current.length < 2) {
            current = new float[]{0f, 0f};
        }

        float currentU = current[0];
        float currentV = current[1];

        // 先尝试增加stepU
        float newU = currentU + stepU;
        float newV = currentV;

        // 如果U超出边界(1.0)，重置U并尝试增加stepV
        if (newU > 1.0f-stepU) {
            newU = 0f;
            newV = currentV + stepV;

            // 如果V也超出边界，将两者都重置为(0,0)
            if (newV > 1.0f-stepV) {
                newV = 0f;
            }
        }

        setUVOffset(direction, newU, newV);
//        uvOffsets.put(flag, new float[]{newU, newV});
        refresh();
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
        refresh();
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
        if(level==null) {
            visibles .clear();
            return;
        };
        flags.forEach((direction, flag) -> {
            boolean shouldRender = RenderUtil.shouldRenderFace(blockPos, origin, direction);
            visibles.put(flag, shouldRender);
            visibles.put(-flag,shouldRender);
        });
    }
    @OnlyIn(Dist.CLIENT)
    public void refreshAO(BlockPos blockPos) {
        Level level = Minecraft.getInstance().level;
        if (level==null){
            aoFaces.clear();
            return;
        }
        for (Map.Entry<Integer, BlockState> entry : materials.entrySet()) {
            int flag = entry.getKey();
            BlockState state = materials.get(flag);
            Direction direction = getDirection(flag);

            ModelRender.AmbientOcclusionFace aoFace = new ModelRender.AmbientOcclusionFace();
            aoFace.calculate(level, state, blockPos.relative(direction), direction, shape, shapeFlags, true);
            aoFaces.put(flag, aoFace);
        }
    }

    public void refresh() {
        visibles.clear();
        aoFaces.clear();
        objects.clear();
        PaintRender.setChanged();
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

    /**
     * 将uv转移，即将origin面各自的uv长度应用于material左下角
     * @param flag
     * @param material
     * @return
     */
    public List<BakedQuad> createQuad(int flag, BlockState material)
    {
        Direction direction = getDirection(flag);
        List<BakedQuad> originQuads = getQuadsForDirection(origin, flag);
        List<BakedQuad> materialQuads = getQuadsForDirection(material, Math.abs(getFlag(direction)));

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


            // 逐顶点混合数据
            VerticeInfo[] mixedVertices = new VerticeInfo[4];

            for (int i = 0; i < 4; i++) {
                VerticeInfo originVert = originVertices.vertices.get(i);
                VerticeInfo materialVert = materialVertices.vertices.get(i);

                mixedVertices[i] = VerticeInfo.builder()
                        .position(originVert.position)          // 几何位置来自原始方块
                        .color(materialVert.alpha, materialVert.red, materialVert.green, materialVert.blue) // 颜色来自原始方块
                        .uv(materialVert.u,materialVert.v)     // ★ 纹理坐标来自材质方块 ★
                        .light(originVert.light)                // 光照值保持原始方块（或可根据需要混合）
                        .normal(originVert.normal)              // 法线保持原始方块
                        .build();
            }


            VerticesInfo newQuadVertices =new VerticesInfo(List.of(mixedVertices[0],mixedVertices[1],mixedVertices[2],mixedVertices[3]));
            newQuadVertices.implyUV(originVertices);

            float[] uvOffset= this.uvOffsets.get(flag);
            newQuadVertices.implyUVOffest(uvOffset[0],uvOffset[1]);
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


            List<BakedQuad> quads=createQuad(flag,material);
            if(!quads.isEmpty())
            {
                objects.put(flag,quads);   // 存入结果，objects 应为 Map<Integer, List<BakedQuad>>
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void render(BlockPos blockPos, PoseStack poseStack, int packedLight, int packedOverlay, float partialTick, VertexConsumer buffer) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        double offset= (double)Configs.getValue(Painter.MODID, ClientConfig.PAINT_OFFSET).get();

        if (objects.isEmpty())createQuads();
        if(visibles.isEmpty())refreshVisible();
        if(aoFaces.isEmpty())refreshAO(blockPos);

        for (Map.Entry<Integer, List<BakedQuad>> entry : objects.entrySet()) {
            List<BakedQuad> quads = entry.getValue();
            int flag = entry.getKey();
            Vector3f normal=normals.get(flag);

            BlockState material = materials.get(flag);
            Direction direction = getDirection(flag);


            if (!visibles.getOrDefault(flag,false))continue;

            Vec3 vec3 = new Vec3(blockPos.getX() + offset * normal.getX(),
                    blockPos.getY() + offset * normal.getY(),
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
        refresh();
    }

    /**
     * 设置某个方向的UV偏移，供调试或扩展UI使用
     * @param direction 方向
     * @param u U分量偏移
     * @param v V分量偏移
     */
    public void setUVOffset(Direction direction, float u, float v) {
        int flag = getFlag(direction);
        float[] off = uvOffsets.get(flag);
        if (off == null || off.length < 2) {
            off = new float[]{0f, 0f};
        }
        off[0] = u;
        off[1] = v;
        uvOffsets.put(flag, off);
        refresh();
    }

    public void putMaterial(Direction f, BlockState blockState)
    {
        int flag = getFlag(f);


        materials.put(flag, blockState);

    }

    public abstract boolean hasNullInDirection(Direction f);


    public BlockState getMaterial(Direction f) {
        int flag = getFlag(f);
        return materials.get(flag);
    }

    @Override
    public int getFlag(Direction object) {
        if(hasNullInDirection(object))return -super.getFlag(object);
        return super.getFlag(object);
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

        // Persist UV offset data
        ListTag uvList = new ListTag();
        if (uvOffsets != null) {
            for (Map.Entry<Integer, float[]> e : uvOffsets.entrySet()) {
                CompoundTag uvTag = new CompoundTag();
                uvTag.putInt("flag", e.getKey());
                float[] arr = e.getValue();
                float u = (arr != null && arr.length > 0) ? arr[0] : 0f;
                float v = (arr != null && arr.length > 1) ? arr[1] : 0f;
                uvTag.putFloat("u", u);
                uvTag.putFloat("v", v);
                uvList.add(uvTag);
            }
        }
        tag.put("uvOffsets", uvList);
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

        // Restore UV offsets
        uvOffsets.clear();
        if (tag.contains("uvOffsets", Tag.TAG_LIST)) {
            ListTag uvList = tag.getList("uvOffsets", Tag.TAG_COMPOUND);
            for (int i = 0; i < uvList.size(); i++) {
                CompoundTag uvTag = uvList.getCompound(i);
                int flag = uvTag.getInt("flag");
                float u = uvTag.getFloat("u");
                float v = uvTag.getFloat("v");
                uvOffsets.put(flag, new float[]{u, v});
            }
        }
        // no debug logs
        // Ensure all direction flags have an entry
        if (uvOffsets.isEmpty()) {
            registerUVs();
        } else {
            flags.forEach((direction, f) -> {
                uvOffsets.computeIfAbsent(f, k -> new float[]{0f, 0f});
                uvOffsets.computeIfAbsent(-f, k -> new float[]{0f, 0f});
            });
            // If all loaded UV offsets are zeros, apply a small non-zero default to assist debugging
            boolean anyNonZero = uvOffsets.values().stream().anyMatch(a -> a != null && (a[0] != 0f || a[1] != 0f));
            if (!anyNonZero) {
                // Find a representative flag to modify (e.g., NORTH)
                int northFlag = getFlag(Direction.NORTH);
                uvOffsets.put(northFlag, new float[]{0.25f, 0f});
                uvOffsets.put(-northFlag, new float[]{0.25f, 0f});
                // logging removed
            }
        }
        refresh();
    }
}
