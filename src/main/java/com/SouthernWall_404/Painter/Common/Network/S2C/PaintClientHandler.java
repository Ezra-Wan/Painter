package com.SouthernWall_404.Painter.Common.Network.S2C;

import com.SouthernWall_404.Painter.API.Capability.PaintInfo;
import com.SouthernWall_404.Painter.API.Capability.PaintUtil;
import com.SouthernWall_404.Painter.Common.World.BlockEntity.RenderBedRockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PaintClientHandler {

    public static void handlePaintSync(final PaintS2CPacket packet, final IPayloadContext context)
    {
        context.enqueueWork(()->{
            ChunkPos pos=packet.pos();
            Level level=Minecraft.getInstance().player.level();


            PaintInfo paintInfo= PaintUtil.getPaints(level,pos);

            paintInfo.deserializeNBT(null,packet.modPack());
        });
    }

    public static void handleRenderSync(final RenderS2CPacket packet,final IPayloadContext context)
    {
        context.enqueueWork(()->{
            BlockPos pos=packet.pos();
            Level level=Minecraft.getInstance().level;

            BlockEntity entity=level.getBlockEntity(pos);
            if(entity!=null&&entity instanceof RenderBedRockEntity renderEntity)
            {
                renderEntity.fromNBT(packet.modPack());
            }
        });
    }
}
