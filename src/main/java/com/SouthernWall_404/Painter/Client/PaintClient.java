package com.SouthernWall_404.Painter.Client;


import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintSyncHelper;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber(modid = Painter.MODID, value=Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class PaintClient {


    @SubscribeEvent
    public static void ChunkEvent(ChunkEvent.Load event)
    {
        if (!(event.getLevel().isClientSide())) return;
        Minecraft mc=Minecraft.getInstance();

        ChunkPos pos=event.getChunk().getPos();

        if(!mc.level.getData(ModAttachments.LEVEL_PAINT_INFO).contains( pos))return;
        PaintSyncHelper.sync(pos, mc.player);

        PaintRender.addChunk(pos);
    }

    @SubscribeEvent
    public static void ChunkEvent(ChunkEvent.Unload event)
    {
        if(!event.getLevel().isClientSide())return;

        ChunkPos pos=event.getChunk().getPos();

        // 直接从渲染缓存中移除，不再依赖 PaintChunkInfo
        PaintRender.removeChunk(pos);
    }
}
