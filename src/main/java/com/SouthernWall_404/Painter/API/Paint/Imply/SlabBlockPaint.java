package com.SouthernWall_404.Painter.API.Paint.Imply;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.PaintContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

public class SlabBlockPaint extends AbstractPaint {




    //========需要持久化的数据========
    private Map<Integer,SlabType> slabTypes=new HashMap<>();//用于标定


    private SlabType slabType;


    public SlabBlockPaint(BlockPos blockPos) {
        this(blockPos,SlabType.TOP);
    }

    public SlabBlockPaint(BlockPos blockPos,SlabType slabType)
    {

        super(blockPos,PaintContent.SLAB_BLOCK);
        this.slabType=slabType;

    }

    @Override
    public boolean hasNullInDirection(Direction f) {

        if(slabType==SlabType.TOP&&f==Direction.DOWN) return true;
        if(slabType==SlabType.BOTTOM&&f==Direction.UP)return true;
        return false;
    }

    @Override
    public void cycleTextureUV(Direction direction) {

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
    }

    //TODO 需要处理无方向面的渲染bug，需要注意，null面似乎是以当前方块为基准计算的
//
//    private BakedQuad createSlabQuad(BakedQuad quad, Direction direction, int tintIndex) {
//        // 剔除不应该渲染的面
//        SlabType uvSlabType = slabTypes.getOrDefault(getFlag(direction), slabType);
//        TextureAtlasSprite sprite = quad.getSprite();
//        int[] original = quad.getVertices();
//        int[] newVertices = original.clone();
//
//        float v0 = sprite.getV0();
//        float v1 = sprite.getV1();
//        float vMid = (v0 + v1) / 2f;
//
//        float u0=sprite.getU0();
//        float u1=sprite.getU1();
//        float uMid=(u0+u1)/2f;
//
//        float[][] originUV=new float[4][2];
//
//
//        for (int i = 0; i < 4; i++) {
//            int offset = i * 8;
//
//            // 读取原始数据
//            float x = Float.intBitsToFloat(original[offset]);
//            float y = Float.intBitsToFloat(original[offset + 1]);
//            float z = Float.intBitsToFloat(original[offset + 2]);
//            float u = Float.intBitsToFloat(original[offset + 4]);
//            float v = Float.intBitsToFloat(original[offset + 5]);
//
//            // 调整 Y 坐标（半砖高度裁剪）
//            if (slabType == SlabType.TOP && y < 0.5f) y = 0.5f;
//            if (slabType == SlabType.BOTTOM && y > 0.5f) y = 0.5f;
//
//
//            originUV[i][0]=u;
//            originUV[i][1]=v;
//            // 调整 V 坐标（仅水平方向的面）
//
//        int packedNormal = packNormal(direction.getNormal().getX(),
//                direction.getNormal().getY(),
//                direction.getNormal().getZ());
//            // 写回新顶点（保留颜色、光照、法线）
//            newVertices[offset]     = Float.floatToRawIntBits(x);
//            newVertices[offset + 1] = Float.floatToRawIntBits(y);
//            newVertices[offset + 2] = Float.floatToRawIntBits(z);
//            newVertices[offset + 3] = -1;   // 白色
//
//            newVertices[offset + 6] = 0;    // 光照（渲染时填充）
//            newVertices[offset + 7] = packedNormal;
//        }
//
//
//        //从底面的uv情况获取具体调整值
//
//        float leftDownU=originUV[1][0];
//        float rightDownU=originUV[2][0];
//
//        boolean isV=leftDownU==rightDownU?false:true;
//
//        for(int i=0;i<4;i++)
//        {
//
//            int offset = i * 8;
//            float u=originUV[i][0];
//            float v=originUV[i][1];
//
//
//            if (direction.getAxis().isHorizontal()) {
//                if(isV)
//                {
//                    switch (uvSlabType) {
//                        case TOP -> v = v0 + (v - v0) * 0.5f;
//                        case BOTTOM -> v = vMid + (v - v0)*0.5f;
//                        case DOUBLE -> {} // 不变
//                    }
//                }else
//                {
//                    switch (uvSlabType) {
//                        case TOP -> u = u0 + (u - u0) * 0.5f;
//                        case BOTTOM -> u = uMid + (u - u0)*0.5f;
//                        case DOUBLE -> {} // 不变
//                    }
//                }
//
//            }
//
//            newVertices[offset + 4] = Float.floatToRawIntBits(u);
//            newVertices[offset + 5] = Float.floatToRawIntBits(v);
//
//        }
//
//        return new BakedQuad(newVertices, tintIndex, direction, sprite, true);
//    }
//
//
//    // 辅助方法：将法线向量打包为 int（与 DefaultVertexFormat 一致）
//    private static int packNormal(float x, float y, float z) {
//        int nx = (int) (x * 127);
//        int ny = (int) (y * 127);
//        int nz = (int) (z * 127);
//        return (nx & 0xFF) | ((ny & 0xFF) << 8) | ((nz & 0xFF) << 16);
//    }

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