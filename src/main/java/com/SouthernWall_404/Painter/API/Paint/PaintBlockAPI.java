package com.SouthernWall_404.Painter.API.Paint;

import com.SouthernWall_404.Painter.Common.Init.ModBlock;
import com.SouthernWall_404.Painter.Common.Network.ModChannels;
import com.SouthernWall_404.Painter.Common.Network.S2C.RenderS2CPacket;
import com.SouthernWall_404.Painter.Common.World.Block.PaintBlock;
import com.SouthernWall_404.Painter.Common.World.BlockEntity.PaintBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PaintBlockAPI {


    public static void placeARender(Level level, BlockPos blockPos,Player player)
    {
        BlockState origin=level.getBlockState(blockPos);
        if(origin.getBlock() instanceof PaintBlock)
        {
            return;
        }
        
        AbstractRender<?> render=new SimpleBlockPaint(origin);

        BlockState renderBlock=ModBlock.RENDER_BEDROCK.get().defaultBlockState();
        level.setBlock(blockPos,renderBlock,3);

        BlockEntity blockEntity=level.getBlockEntity(blockPos);
        if(blockEntity instanceof PaintBlockEntity paintBlockEntity)
        {
            paintBlockEntity.init(render);
        }

    }

    public static void syncToClient(BlockEntity blockEntity, Player player)
    {
        ModChannels.sendToClient(new RenderS2CPacket(blockEntity.getUpdateTag(null),blockEntity.getBlockPos()),(ServerPlayer)player);
    }
}