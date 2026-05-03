package com.SouthernWall_404.Painter.API.Paint.Util.Paint;

import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkSync;
import com.SouthernWall_404.Painter.Client.PaintRender;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class PaintSyncHelper {

    public static void sync(ChunkPos pos, Player player) {
        Level level = player.level();
        NetworkSync.syncChunkAttachment(level, pos,ModAttachments.PAINT_INFO.get(), player);

        PaintRender.addChunk(pos);
    }
}
