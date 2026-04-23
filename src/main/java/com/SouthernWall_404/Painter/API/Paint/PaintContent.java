package com.SouthernWall_404.Painter.API.Paint;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Imply.SimpleBlockPaint;
import com.SouthernWall_404.Painter.API.Paint.Imply.SlabBlockPaint;
import com.mojang.datafixers.types.Func;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PaintContent {

    public static final String SIMPLE_BLOCK="simple_block";
    public static final String SLAB_BLOCK="slab_block";

    private static Map<String, Function<BlockPos,AbstractRender>> RENDERS=new HashMap<>();

    /**
     * 建立从NBT获取渲染种类的方法
     * @param nbt
     * @return
     */
    public static Function<BlockPos,AbstractRender> getRender(CompoundTag nbt)
    {
        String type = nbt.getString("type");
        if (type.isEmpty()) {
            type = SIMPLE_BLOCK; // 默认值
        }
        return getRender(type);
    }

    public static Function<BlockPos,AbstractRender> getRender(String type)
    {
        return RENDERS.getOrDefault(type,(pos -> new SimpleBlockPaint(pos)));
    }

    static {
        RENDERS.put(SIMPLE_BLOCK,(pos -> new SimpleBlockPaint(pos)));
        RENDERS.put(SLAB_BLOCK,(pos -> new SlabBlockPaint(pos)));
    }


}
