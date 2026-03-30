package com.SouthernWall_404.Painter.API.Paint.API;

import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRender<T,F extends Object> implements IRender<T> {

    //========不需要持久化的数据========
    protected Map<F,Integer> flags=new HashMap<>();//识别码定义系统
    protected Map<Integer,T> objects=new HashMap<>();//渲染内容缓存
    protected String type;
    //========需要持久化的数据========

    protected Map<Integer, BlockState> paintStates = new HashMap<>();//TODO：准备换成BlockState渲染
    protected Map<Integer, ResourceLocation> paintPaths = new HashMap<>();//渲染内容路径，便于保存
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



    @Deprecated
    @Override
    public void putRenderBlock(Object object, Block block) {
        int flag=getFlag(object);

        ResourceLocation key= RenderUtil.getBlockKey(block);

        putRenderObject(flag,key);
    }

    @Override
    public void putRenderObject(int flag, ResourceLocation key) {
        paintPaths.put(flag,key);

        update();
    }

    @Override
    public int getFlag(Object object) {
        return flags.getOrDefault(object,-1);
    }
// AbstractRender.java 片段

// AbstractRender.java 片段

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

        // 序列化 paintPaths
        CompoundTag pathsTag = new CompoundTag();
        for (Map.Entry<Integer, ResourceLocation> entry : paintPaths.entrySet()) {
            int flag = entry.getKey();
            ResourceLocation location = entry.getValue();
            // 将 ResourceLocation 编码为 String（也可使用 CODEC，但字符串已足够）
            pathsTag.putString(String.valueOf(flag), location.toString());
        }
        tag.put("paint_paths", pathsTag);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        // 反序列化 origin
        if (compoundTag.contains("origin")) {
            Tag originTag = compoundTag.get("origin");
            DataResult<BlockState> result = BlockState.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), originTag);
            result.resultOrPartial(error -> {
                // 可在此记录日志，例如：LOGGER.error("Failed to decode origin: {}", error);
            }).ifPresent(state -> this.origin = state);
        }

        // 反序列化 paintPaths
        paintPaths.clear();
        if (compoundTag.contains("paint_paths", CompoundTag.TAG_COMPOUND)) {
            CompoundTag pathsTag = compoundTag.getCompound("paint_paths");
            for (String key : pathsTag.getAllKeys()) {
                try {
                    int flag = Integer.parseInt(key);
                    String pathStr = pathsTag.getString(key);
                    ResourceLocation location = ResourceLocation.tryParse(pathStr);
                    if (location != null) {
                        paintPaths.put(flag, location);
                    }
                } catch (NumberFormatException e) {
                    // 忽略非整数键（正常情况下不应出现）
                }
            }
        }

        // 重建缓存
        update();
    }
}