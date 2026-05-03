package com.SouthernWall_404.Painter.API.Paint.Util.Paint;

import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkSync;
import com.SouthernWall_404.LaplaceAPI.xNetwork.Packet.C2S.ClientRequestPacket;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.Laplace.Network.ServerHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class PaintSyncHelper {

    public static void sync(ChunkPos pos, Player player) {
        Level level = player.level();
        NetworkSync.syncChunkAttachment(level, pos,ModAttachments.PAINT_INFO.get(), player);
    }

    /**
     * 同步UV偏移数据到服务端
     * @param blockPos 方块位置
     * @param direction 方向
     * @param uv UV偏移数组，长度为2，[0]为U，[1]为V
     */
    public static void syncPaintUV(BlockPos blockPos, Direction direction, float[] uv) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", blockPos.getX());
        tag.putInt("y", blockPos.getY());
        tag.putInt("z", blockPos.getZ());
        tag.putString("direction", direction.getName());
        tag.putFloat("u", uv[0]);
        tag.putFloat("v", uv[1]);


        NetworkSync.requireToServer(ServerHandlers.CYCLE_TEXTURE_UV_PACKET,tag);
    }
}
