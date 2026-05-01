package com.SouthernWall_404.Painter.API.Paint.Util.Paint;

import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkSync;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.Client.PaintRender;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class PaintSyncHelper {

    public static void syncToClient(ChunkPos pos, Player player) {
        Level level = player.level();
        LevelChunk chunk = level.getChunk(pos.x,pos.z);
        PaintInfo paintInfo = chunk.getData(ModAttachments.PAINT_INFO);
        CompoundTag modPack=paintInfo.serializeNBT(player.level().registryAccess());
        NetworkSync.syncChunkAttachment(level, pos,ModAttachments.PAINT_INFO.get(), player);
//        PaintRender.redraw();//TODO 考虑放到渲染管线中进行帧末渲染
//        ModChannels.sendToClient(new ChunkS2CPacket(modPack,pos),(ServerPlayer) player);
    }
}
