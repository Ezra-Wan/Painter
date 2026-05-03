// AbstractRender.java
package com.SouthernWall_404.Painter.API.Paint.API;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.Painter.Client.PaintRender;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRender<T, F extends Object> implements IRender<T> {

    //========不需要持久化的数据========
    protected Map<F, Integer> flags = new HashMap<>(); // 识别码定义系统
    protected Map<Integer, T> objects = new HashMap<>(); // 渲染内容缓存
    protected String type;
    protected BlockPos blockPos;
    protected Map<Integer,Vector3f> normals=new HashMap<>();
    protected BlockState origin;

    //========构造方法========
    public AbstractRender(BlockPos blockPos,String type) {
        this.type = type;
        this.blockPos=blockPos;

        initFlags();

    }

    @Override
    public String getType() {
        return type;
    }

    public abstract void initFlags();

    //========内部方法========
    protected void registerFlag(F f, int flag) {
        if (flags.containsValue(flag)) {
            return;
        }
        flags.put(f, flag);
    }

    @OnlyIn(Dist.CLIENT)
    public void update() {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
//            PaintRender.setChanged();
            Level level=mc.level;
            if(level!=null)
            {
                this.origin=level.getBlockState(blockPos);

            }
        }
    }

    //========业务方法========
    public int getFlag(F object) {
        return flags.getOrDefault(object, -1);
    }

    // ========== NBT 序列化（仅处理 type 和 flags 等通用部分，不再处理 materials）==========
    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type);
        // 注意：materials 已移至子类 AbstractPaint 中处理
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.type = tag.getString("type");
        // 注意：materials 已移至子类 AbstractPaint 中处理
        update();
    }
}