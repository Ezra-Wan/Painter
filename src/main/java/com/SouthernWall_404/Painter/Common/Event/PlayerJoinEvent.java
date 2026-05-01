package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkSync;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintChunkInfo;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintSyncHelper;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

public class PlayerJoinEvent {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        Player player=event.getEntity();
        Level level=player.level();
        if(!level.isClientSide) {
            NetworkSync.syncLevelAttachment(level,ModAttachments.PAINT_CHUNK_INFO.get(),player);

//            PaintChunkInfo paintChunkInfo=level.getData(ModAttachments.PAINT_CHUNK_INFO);
//
//            Sync.syncLevelAttachment(level,ModAttachments.PAINT_CHUNK_INFO.get(),player);
//            for(ChunkPos pos:paintChunkInfo.getPaintPoses())
//            {
//                PaintUtil.syncToClient(pos,player);//TODO 退出清缓存
//            }
        }
    }


    @SubscribeEvent
    public static void ChunkEvent(ChunkEvent.Load event)
    {
        if (!(event.getLevel() instanceof ClientLevel level)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        Minecraft mc=Minecraft.getInstance();

        ChunkPos pos=event.getChunk().getPos();

        PaintChunkInfo chunkInfo = level.getData(ModAttachments.PAINT_CHUNK_INFO);
        if (!chunkInfo.getPaintPosesNearby(mc.player.getOnPos()).contains(pos)) return;

        PaintInfo paintInfo = chunk.getData(ModAttachments.PAINT_INFO);

        if (paintInfo.getPaints().isEmpty()) {
            // 需要向服务器请求
            PaintSyncHelper.sync(pos, mc.player);
        }
    }


}
