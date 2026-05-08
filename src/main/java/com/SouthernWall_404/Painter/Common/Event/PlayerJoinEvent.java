package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintSyncHelper;
import com.SouthernWall_404.Painter.Client.PaintRender;
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

        // 只在客户端执行初始化逻辑
        if(level.isClientSide) {
            Minecraft mc = Minecraft.getInstance();
            if(mc == null || mc.level == null) return;

            ClientLevel clientLevel = mc.level;
            PaintRender.clear();//重绘以免出现渲染残留

            // 扫描玩家周围已加载的区块，将包含绘制数据的区块添加到渲染缓存
            int renderDistance = mc.options.getEffectiveRenderDistance();
            ChunkPos playerChunkPos = new ChunkPos(player.blockPosition());

            for(int dx = -renderDistance; dx <= renderDistance; dx++) {
                for(int dz = -renderDistance; dz <= renderDistance; dz++) {
                    int chunkX = playerChunkPos.x + dx;
                    int chunkZ = playerChunkPos.z + dz;

                    // 获取已加载的区块（如果未加载则返回null）
                    LevelChunk chunk = clientLevel.getChunkSource().getChunkNow(chunkX, chunkZ);
                    if(chunk != null) {
                        PaintInfo paintInfo = chunk.getData(ModAttachments.PAINT_INFO);
                        if(!paintInfo.getPaints().isEmpty()){
                            PaintRender.addChunk(chunk.getPos());
                        }
                    }
                }
            }
        }
    }

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
