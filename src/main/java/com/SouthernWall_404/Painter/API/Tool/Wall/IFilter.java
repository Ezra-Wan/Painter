package com.SouthernWall_404.Painter.API.Tool.Wall;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

public interface IFilter {

    public boolean check(BlockPos pos, Player player, Direction face);

}
