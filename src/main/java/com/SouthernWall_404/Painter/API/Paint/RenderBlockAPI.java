package com.SouthernWall_404.Painter.API.Paint;

import com.SouthernWall_404.Painter.Common.Init.ModBlock;
import com.SouthernWall_404.Painter.Common.Network.ModChannels;
import com.SouthernWall_404.Painter.Common.Network.S2C.RenderS2CPacket;
import com.SouthernWall_404.Painter.Common.World.Block.RenderBedRock;
import com.SouthernWall_404.Painter.Common.World.BlockEntity.RenderBedRockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public class RenderBlockAPI {

    /**
     * 对于添加的位置 blockPos，先获取其所在区块，
     * 而后将区块内的（0,0）位置在 [-55, -64] 的高度上进行搜索，
     * 将最底下的基岩替换为一个铁块。
     */
    public static void onPaint(Level level, BlockPos blockPos) {

    }


    public static void placeARender(Level level, BlockPos blockPos,Player player)
    {

        BlockState origin=level.getBlockState(blockPos);

        if(origin.getBlock()instanceof RenderBedRock)
        {
            return;
        }
        BlockState render=ModBlock.RENDER_BEDROCK.get().defaultBlockState();

        level.setBlock(blockPos,render,2);

        BlockEntity entity=level.getBlockEntity(blockPos);
        if(entity instanceof RenderBedRockEntity renderEntity)
        {
            renderEntity.setCopyState(origin);
            renderEntity.setChanged();
            level.sendBlockUpdated(blockPos, render, render, 3);

//            syncToClient(renderEntity,player);
        }
    }

    public static void syncToClient(BlockEntity blockEntity, Player player)
    {
        ModChannels.sendToClient(new RenderS2CPacket(blockEntity.getUpdateTag(null),blockEntity.getBlockPos()),(ServerPlayer)player);
    }
}