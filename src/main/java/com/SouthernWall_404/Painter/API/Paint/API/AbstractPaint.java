// AbstractPaint.java
package com.SouthernWall_404.Painter.API.Paint.API;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
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

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Quad Vertice顺序 留档备用
 * 顺序：左上，左下，右下，右上
 */
public abstract class AbstractPaint extends AbstractRender<List<BakedQuad>, Direction> {

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
    }

    public void cycleTextureUV(Direction direction) {
        // 预留
    }

    public void cycleTextureDir(Direction direction) {
        BlockState material = getMaterial(direction);
        if (material.hasProperty(TrapDoorBlock.HALF) && material.getOptionalValue(TrapDoorBlock.OPEN).orElse(false))
            setMaterial(direction, material.cycle(TrapDoorBlock.HALF));
        else if (material.hasProperty(BlockStateProperties.FACING))
            setMaterial(direction, material.cycle(BlockStateProperties.FACING));
        else if (material.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            setMaterial(direction, material.setValue(BlockStateProperties.HORIZONTAL_FACING,
                    material.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise()));
        else if (material.hasProperty(BlockStateProperties.AXIS))
            setMaterial(direction, material.cycle(BlockStateProperties.AXIS));
        else if (material.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
            setMaterial(direction, material.cycle(BlockStateProperties.HORIZONTAL_AXIS));
        else if (material.hasProperty(BlockStateProperties.LIT))
            setMaterial(direction, material.cycle(BlockStateProperties.LIT));
        super.update();
    }

    public List<BakedQuad> getQuadsForDirection(BlockState state, Direction direction) {
        BakedModel model = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
        RandomSource random = RandomSource.create();
        RenderType renderType = RenderUtil.getRenderType(state);
        return model.getQuads(state, direction, random, ModelData.EMPTY, renderType);
    }

    public Direction getDirection(int flag) {
        for (Map.Entry<Direction, Integer> entry : flags.entrySet()) {
            if (entry.getValue() == flag) {
                return entry.getKey();
            }
        }
        return Direction.NORTH;
    }

    public abstract void createQuads();

    @OnlyIn(Dist.CLIENT)
    public void refreshVisible()
    {
        Level level=Minecraft.getInstance().level;
        flags.forEach((direction,flag)->{
            boolean shouldRender=RenderUtil.shouldRenderFace(blockPos, origin, direction);
            visibles.put(flag,shouldRender);

        });
    }

    @OnlyIn(Dist.CLIENT)
    public void refreshAO(BlockPos blockPos) {
        Level level = Minecraft.getInstance().level;
        for (Map.Entry<Integer, BlockState> entry : materials.entrySet()) {
            int flag = entry.getKey();
            BlockState state = materials.get(flag);
            Direction direction = getDirection(flag);
            ModelRender.AmbientOcclusionFace aoFace = new ModelRender.AmbientOcclusionFace();
            aoFace.calculate(level, state, blockPos.relative(direction), direction, shape, shapeFlags, true);
            aoFaces.put(flag, aoFace);
        }
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

    public void setMaterial(Direction f, BlockState blockState) {
        int flag = getFlag(f);
        materials.put(flag, blockState);
        update();
    }

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