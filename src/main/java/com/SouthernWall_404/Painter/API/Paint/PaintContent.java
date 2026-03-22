package com.SouthernWall_404.Painter.API.Paint;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Imply.SimpleBlockPaint;
import com.SouthernWall_404.Painter.API.Paint.Imply.SlabBlockPaint;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PaintContent {

    public static final String SIMPLE_BLOCK="simple_block";
    public static final String SLAB_BLOCK="slab_block";

    private static Map<String, Function<BlockState,AbstractRender>> RENDERS=new HashMap<>();

    public static Function<BlockState, AbstractRender> getRender(String type)
    {
        return RENDERS.getOrDefault(type,(blockState -> new SimpleBlockPaint(blockState)));
    }

    static {
        RENDERS.put(SIMPLE_BLOCK,(blockState -> new SimpleBlockPaint(blockState)));
        RENDERS.put(SLAB_BLOCK,(blockState -> new SlabBlockPaint(blockState)));
    }


}
