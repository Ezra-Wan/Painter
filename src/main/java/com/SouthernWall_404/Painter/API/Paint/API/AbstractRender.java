package com.SouthernWall_404.Painter.API.Paint.API;

import com.SouthernWall_404.Painter.Client.PaintRender;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
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


    //========构造方法========

    public AbstractRender(String type) {
        this.type=type;
        initFlags();

        update();

    }

    @Override
    public String getType() {
        return type;
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
    protected void update(){
        PaintRender.redraw();
    }//TODO 考虑尝试，实现新式的自动uv替换

    //========业务方法========




    public void putMaterial(F f, BlockState blockState)
    {
        int hasBlockFlag=hasBlockInPaint(blockState);
        if(hasBlockFlag!=-1)
        {
            blockState= materials.get(hasBlockFlag);
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
////        ResourceLocation handlerKey= RenderUtil.getBlockKey(block);
////
////        putRenderObject(flag,handlerKey);
//
//        putRenderBlock(object,block.defaultBlockState());
//
//        update();
//    }
//
//    public void putRenderObject(int flag, ResourceLocation handlerKey) {
//        paintPaths.put(flag,handlerKey);
//
//        update();
//    }

    public int getFlag(F object) {
        return flags.getOrDefault(object,-1);
    }
// AbstractRender.java 片段

    // ========== 序列化 F 类型的抽象方法 ==========
    protected abstract void serializeF(F f, CompoundTag tag, String key);

    protected abstract F deserializeF(CompoundTag tag, String key);

    // ========== NBT 序列化 ==========

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type);

//        // 序列化 origin (使用 CODEC)
//        DataResult<Tag> originResult = BlockState.CODEC.encode(this.origin, provider.createSerializationContext(NbtOps.INSTANCE), new CompoundTag());
//        tag.put("origin", originResult.getOrThrow());

        // 序列化 materials (Map<Integer, BlockState>)
        ListTag materialsList = new ListTag();
        for (Map.Entry<Integer, BlockState> entry : materials.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putInt("flag", entry.getKey());
            // 使用 CODEC 序列化 BlockState
            DataResult<Tag> stateResult = BlockState.CODEC.encode(entry.getValue(), provider.createSerializationContext(NbtOps.INSTANCE), new CompoundTag());
            entryTag.put("state", stateResult.getOrThrow());
            materialsList.add(entryTag);
        }
        tag.put("materials", materialsList);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.type = tag.getString("type");

//        // 反序列化 origin
//        Tag originTag = tag.get("origin");
//        DataResult<BlockState> originResult = BlockState.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), originTag);
//        this.origin = originResult.getOrThrow();

        // 反序列化 materials
        this.materials.clear();
        ListTag materialsList = tag.getList("materials", Tag.TAG_COMPOUND);
        for (int i = 0; i < materialsList.size(); i++) {
            CompoundTag entryTag = materialsList.getCompound(i);
            int flag = entryTag.getInt("flag");
            Tag stateTag = entryTag.get("state");
            DataResult<BlockState> stateResult = BlockState.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), stateTag);
            BlockState state = stateResult.getOrThrow();
            materials.put(flag, state);
        }

        // 重建 flags (由子类 initFlags 定义)
        update();
    }
}