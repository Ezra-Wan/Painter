package com.SouthernWall_404.Painter.API.Tool.Wall.Filters;

import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintValidHelper;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.SouthernWall_404.Painter.API.Tool.Wall.IFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EmptyFilter implements IFilter {

    public EmptyFilter() {
    }

    @Override
    public boolean check(BlockPos pos, Player player, Direction face) {

        Level level=player.level();
        BlockState blockState=level.getBlockState(pos);

        if(!PaintValidHelper.isPaintable(blockState,level,pos))
        {
            return false;
        }

        if(!RenderUtil.shouldRenderFace(level,pos,blockState,face))
        {
            return false;
        }
        return true;//不论如何，什么影响都没有
    }
}
