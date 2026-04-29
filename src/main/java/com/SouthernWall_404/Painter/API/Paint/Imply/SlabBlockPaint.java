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

    // ======== 序列化/反序列化 ========
    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);

        // 保存 slabType
        if (slabType != null) {
            tag.putString("slab_type", slabType.name());
        }

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        // 先调用父类，恢复 origin 和 materials
        super.deserializeNBT(provider, compoundTag);
        
        // 恢复 slabType
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

        update();
    }
}