package com.SouthernWall_404.Painter.API.Paint.Util;

import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.Network.ClientRequestPack;
import com.SouthernWall_404.Painter.Common.Network.ModChannels;
import com.SouthernWall_404.Painter.Common.Network.S2C.ChunkS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class PaintSyncHelper {


    public static void RequireSync(ChunkPos pos)
    {
        ModChannels.sendToServer(new ClientRequestPack(1,pos));
    }
    public static void syncToClient(ChunkPos pos, Player player) {
        Level level = player.level();
        LevelChunk chunk = level.getChunk(pos.x,pos.z);
        PaintInfo paintInfo = chunk.getData(ModAttachments.PAINT_INFO);
        CompoundTag modPack=paintInfo.serializeNBT(player.level().registryAccess());

        ModChannels.sendToClient(new ChunkS2CPacket(modPack,pos),(ServerPlayer) player);
    }
}
