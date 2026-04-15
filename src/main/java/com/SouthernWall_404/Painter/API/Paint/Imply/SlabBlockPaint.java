package com.SouthernWall_404.Painter.API.Paint.Imply;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.ModModelRender;
import com.SouthernWall_404.Painter.API.Paint.PaintContent;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.SouthernWall_404.Painter.Client.PaintRender;
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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

public class SlabBlockPaint extends AbstractPaint {
    private static final int NORTH = 1, SOUTH = 2, WEST = 4, EAST = 8, UP = 16, DOWN = 32;



    //========需要持久化的数据========
    private Map<Integer,SlabType> slabTypes=new HashMap<>();//用于标定


    private SlabType slabType;


    public SlabBlockPaint() {
        this(SlabType.TOP);
    }

    public SlabBlockPaint(SlabType slabType)
    {

        super(PaintContent.SLAB_BLOCK);

        this.slabType=slabType;
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


    //TODO 添加对面旋转的兼容
    @Override
    public void cyclePaint(Direction direction) {

        int key=getFlag(direction);
        SlabType defaultType=SlabType.TOP;
        SlabType current=slabTypes.getOrDefault(getFlag(direction),defaultType);

        switch (current){

            case TOP:
                slabTypes.put(key,SlabType.BOTTOM);
                break;
            case BOTTOM:
                slabTypes.put(key,SlabType.TOP);

                break;
            case DOUBLE:
                break;
            default:
                break;
        }

        update();
//        super.cyclePaint(direction);
    }

    @Override
    public void createQuads() {
        for(Map.Entry<Integer,BlockState> entry:materials.entrySet())
        {
            int flag=entry.getKey();
            BlockState material=entry.getValue();

            Direction direction=getDirection(flag);


            if(slabTypes.get(flag)==null) slabTypes.put(flag,SlabType.TOP);
            //            Block block= RenderUtil.getBlockFromID(paintPaths.get(flag));
            List<BakedQuad> quads = createSlabQuads(direction,material);

            objects.put(flag, quads);

        }
    }

    //TODO 添加默认上半面
    private List<BakedQuad> createSlabQuads(Direction direction,BlockState material)
    {
        List<BakedQuad> quads=RenderUtil.getQuads(material,direction);

        List<BakedQuad> result=new ArrayList<>();
        for(BakedQuad quad:quads)
        {
            BakedQuad resultQuad=createSlabQuad(quad.getSprite(),direction,quad.getTintIndex());

            result.add(resultQuad);
        }


        return result;
    }

//    @Deprecated
//    private List<BakedQuad> createSlabQuads(Direction direction)
//    {
//        int flag=getFlag(direction);
//
//
//
//        List<BakedQuad> originQuads=objects.get(flag);
//        List<BakedQuad> quads=new ArrayList<>();
//
//        for(BakedQuad originQuad:originQuads)
//        {
//            TextureAtlasSprite sprite=originQuad.getSprite();
//
//            BakedQuad quad=createSlabQuad(sprite,direction,originQuad.getTintIndex());
//
//            quads.add(quad);
//        }
//
//        return quads;
//    }

    private BakedQuad createSlabQuad(TextureAtlasSprite sprite, Direction direction,int tintIndex) {
//        boolean isTopSlab = origin.getValue(SlabBlock.TYPE) == SlabType.TOP;

        SlabType slabType=this.slabType;
        SlabType uvSlabType=slabTypes.getOrDefault(getFlag(direction),slabType);
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


            switch (uvSlabType){
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


    // ======== 序列化/反序列化 ========
    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);

        // 保存默认 slabType
        if (slabType != null) {
            tag.putString("slab_type", slabType.name());
        }
        // 保存 slabTypes
        CompoundTag slabTypesTag = new CompoundTag();
        for (Map.Entry<Integer, SlabType> entry : slabTypes.entrySet()) {
            int flag = entry.getKey();
            SlabType type = entry.getValue();
            if (type != null) {
                slabTypesTag.putString(String.valueOf(flag), type.name());
            }
        }
        tag.put("slab_types", slabTypesTag);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        // 先调用父类，恢复 origin 和 materials
        super.deserializeNBT(provider, compoundTag);
        // 1. 恢复默认 slabType
        if (compoundTag.contains("slab_type", CompoundTag.TAG_STRING)) {
            String typeName = compoundTag.getString("slab_type");
            try {
                slabType = SlabType.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                slabType = SlabType.TOP; // fallback
            }
        } else {
            slabType = SlabType.TOP; // 兼容旧数据
        }
        // 初始化 slabTypes 默认值（基于 origin 的 SlabType 属性）
        slabTypes.clear();
        SlabType defaultType = SlabType.TOP;
        for (Map.Entry<Direction, Integer> entry : flags.entrySet()) {
            slabTypes.put(entry.getValue(), defaultType);
        }

        // 从 NBT 中读取保存的 slabTypes 并覆盖
        if (compoundTag.contains("slab_types", CompoundTag.TAG_COMPOUND)) {
            CompoundTag slabTypesTag = compoundTag.getCompound("slab_types");
            for (String key : slabTypesTag.getAllKeys()) {
                try {
                    int flag = Integer.parseInt(key);
                    String typeName = slabTypesTag.getString(key);
                    SlabType type = SlabType.valueOf(typeName);
                    slabTypes.put(flag, type);
                } catch ( IllegalArgumentException e) {
                    // 忽略无效键或值
                }
            }
        }



        update();
        // 注意：这里不需要调用 update()，因为半砖的 quads 是实时生成的，无需预缓存
    }
}