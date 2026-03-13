package com.SouthernWall_404.Painter.Common.Network.S2C;

import com.SouthernWall_404.Painter.API.Capability.PaintInfo;
import com.SouthernWall_404.Painter.API.Capability.PaintUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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
}
