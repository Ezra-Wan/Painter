package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.LaplaceAPI.Network.API.Sync;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintChunkInfo;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Util.CommonUtil;
import com.SouthernWall_404.Painter.API.Paint.Util.PaintBlockUtil;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.SouthernWall_404.Painter.Client.PaintRenderRebuild;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.Network.ClientRequestPack;
import com.SouthernWall_404.Painter.Common.Network.ModChannels;
import com.SouthernWall_404.Painter.Common.Network.S2C.ChunkS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerJoinEvent {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        Player player=event.getEntity();
        Level level=player.level();
        if(!level.isClientSide) {
            Sync.syncLevelAttachment(level,ModAttachments.PAINT_CHUNK_INFO.get(),player);

            PaintChunkInfo paintChunkInfo=level.getData(ModAttachments.PAINT_CHUNK_INFO);
            for(ChunkPos pos:paintChunkInfo.getPaintPoses())
            {
                PaintBlockUtil.syncToClient(pos,player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event)
    {
        Player player=event.getEntity();
        Level level=player.level();

        if(level.isClientSide)
        {
            LevelChunk chunk=level.getChunkAt(player.getOnPos());
            PaintInfo paintInfo=chunk.getData(ModAttachments.PAINT_INFO);

            if(!paintInfo.getRenders().isEmpty())
            {
                player.displayClientMessage(Component.literal("客户端检测：存在渲染"),true);
            }else {
                player.displayClientMessage(Component.literal("服务端检测：存在渲染"),true);

            }
        }
    }


    @SubscribeEvent
    public static void ChunkEvent(ChunkEvent.Load event)
    {
        if (!(event.getLevel() instanceof ClientLevel level)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        ChunkPos pos=event.getChunk().getPos();

        PaintChunkInfo chunkInfo = level.getData(ModAttachments.PAINT_CHUNK_INFO);
        if (!chunkInfo.getPaintPoses().contains(pos)) return;

        PaintInfo paintInfo = chunk.getData(ModAttachments.PAINT_INFO);

        if (paintInfo.getRenders().isEmpty()) {
            // 需要向服务器请求
            ModChannels.sendToServer(new ClientRequestPack(1, pos));
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event)
    {
        Level level=event.getLevel();
        level.getData(ModAttachments.PAINT_CHUNK_INFO).tick(level);
    }

}
