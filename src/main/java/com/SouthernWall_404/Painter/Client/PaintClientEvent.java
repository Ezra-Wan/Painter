package com.SouthernWall_404.Painter.Client;

import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintChunkInfo;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;


@EventBusSubscriber(modid = Painter.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)

public class PaintClientEvent {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
            Level level=mc.level;
            if(level==null)return;

            PaintChunkInfo chunkInfo=level.getData(ModAttachments.PAINT_CHUNK_INFO.get());
            chunkInfo.getPaintPoses().forEach(chunkPos->{
                LevelChunk chunk=level.getChunk(chunkPos.x,chunkPos.z);
                PaintInfo paintInfo=chunk.getData(ModAttachments.PAINT_INFO);
                paintInfo.tick(chunkPos);
            });

        }
    }
}
