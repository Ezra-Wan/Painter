package com.SouthernWall_404.Painter.API.Paint.API;

import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRender<T,F extends Object> implements IRender<T> {

    //========不需要持久化的数据========
    protected Map<F,Integer> flags=new HashMap<>();//识别码定义系统
    protected Map<Integer, T> objects = new HashMap<>();//渲染内容缓存
    protected String type;
    //========需要持久化的数据========

    protected Map<Integer, BlockState> materials = new HashMap<>();

    protected Map<Block,F> placeRecord=new HashMap<>();//每类material的默认放置方向
    protected BlockState origin;

    //========构造方法========

    public AbstractRender(BlockState origin,String type) {
        this.type=type;
        initFlags();
        init(origin);

        update();

    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void init(BlockState origin) {
        this.origin=origin;


        update();
    }

    public abstract void initFlags();

    //========内部方法========

    protected void registerFlag(F f,int flag)
    {
        if(flags.containsValue(flag))
        {
            return;
        }//防止重复注册
        else {
            flags.put(f,flag);
        }
    }


    @OnlyIn(Dist.CLIENT)
    protected abstract void update();

    //========业务方法========


    @Override
    public BlockState getOrigin() {
        return this.origin;
    }


    public void putRenderBlock(F f,BlockState blockState)
    {
        int hasBlockFlag=hasBlockInPaint(blockState);
        if(hasBlockFlag!=-1)
        {
            blockState= materials.get(hasBlockFlag);
        }
        else {
            placeRecord.put(blockState.getBlock(),f);//存储此类方块的初始放置方向
        }
        setMaterial(f,blockState);


    }

    public void setMaterial(F f,BlockState blockState)
    {
        int flag=getFlag(f);
        materials.put(flag,blockState);
        update();
    }

    public BlockState getMaterial(F f)
    {
        int flag=getFlag(f);

        return materials.getOrDefault(flag, Blocks.AIR.defaultBlockState());
    }

    /**
     *
     * @param toCheck
     * @return 检查到的已有的BlockState的对应flag
     */
    private int hasBlockInPaint(BlockState toCheck)
    {

        for(Map.Entry<Integer,BlockState> entry: materials.entrySet())
        {
            int flag=entry.getKey();
            BlockState blockState=entry.getValue();

            if(blockState!=null)
            {
                if(blockState.getBlock()==toCheck.getBlock())
                {
                    return flag;
                }
            }

        }

        return -1;
    }

//    @Deprecated
//    @Override
//    public void putRenderBlock(F object, Block block) {
////        int flag=getFlag(object);
////
////        ResourceLocation key= RenderUtil.getBlockKey(block);
////
////        putRenderObject(flag,key);
//
//        putRenderBlock(object,block.defaultBlockState());
//
//        update();
//    }
//
//    public void putRenderObject(int flag, ResourceLocation key) {
//        paintPaths.put(flag,key);
//
//        update();
//    }

    public int getFlag(F object) {
        return flags.getOrDefault(object,-1);
    }
// AbstractRender.java 片段

// AbstractRender.java 片段=
    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        // 序列化 origin BlockState
        if (origin != null) {
            DataResult<Tag> result = BlockState.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), origin);
            result.resultOrPartial(error -> {
                // 可在此记录日志，例如：LOGGER.error("Failed to encode origin: {}", error);
            }).ifPresent(originTag -> tag.put("origin", originTag));
        }


        // ===== 新增：序列化 paintStates =====
        CompoundTag statesTag = new CompoundTag();
        for (Map.Entry<Integer, BlockState> entry : materials.entrySet()) {
            int flag = entry.getKey();
            BlockState state = entry.getValue();
            if (state != null) {
                DataResult<Tag> result = BlockState.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), state);
                result.resultOrPartial(error -> {
                    // 可在此记录日志
                }).ifPresent(stateTag -> statesTag.put(String.valueOf(flag), stateTag));
            }
        }
        tag.put("paint_states", statesTag);
        // ===== 新增结束 =====

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        // 反序列化 origin
        if (compoundTag.contains("origin")) {
            Tag originTag = compoundTag.get("origin");
            DataResult<BlockState> result = BlockState.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), originTag);
            result.resultOrPartial(error -> {
                // 可在此记录日志
            }).ifPresent(state -> this.origin = state);
        }

        // ===== 新增：反序列化 paintStates =====
        materials.clear();
        if (compoundTag.contains("paint_states", CompoundTag.TAG_COMPOUND)) {
            CompoundTag statesTag = compoundTag.getCompound("paint_states");
            for (String key : statesTag.getAllKeys()) {
                try {
                    int flag = Integer.parseInt(key);
                    Tag stateTag = statesTag.get(key);
                    DataResult<BlockState> result = BlockState.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), stateTag);
                    result.resultOrPartial(error -> {
                        // 可在此记录日志
                    }).ifPresent(state -> materials.put(flag, state));
                } catch (NumberFormatException e) {
                    // 忽略非整数键
                }
            }
        }
        // ===== 新增结束 =====

        // 重建缓存
        update();
    }
}