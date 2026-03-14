package com.SouthernWall_404.Painter.Common.World.BlockEntity;

import com.SouthernWall_404.Painter.Common.Init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RenderBedRockEntity extends BlockEntity {

    public RenderBedRockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public RenderBedRockEntity(BlockPos blockPos, BlockState blockState) {
        this(ModBlockEntities.RENDER_BEDROCK_ENTITY.get(),blockPos,blockState);
    }
}
