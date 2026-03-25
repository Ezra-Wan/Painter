package com.SouthernWall_404.Painter.API.Tool.Wall;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IFilter {

    public boolean check(BlockState blockState, BlockPos pos, Level level, Direction face);

}
