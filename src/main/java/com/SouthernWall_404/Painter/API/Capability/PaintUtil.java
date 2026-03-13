package com.SouthernWall_404.Painter.API.Capability;

import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.Network.ModChannels;
import com.SouthernWall_404.Painter.Common.Network.S2C.PaintS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class PaintUtil {

    public static PaintInfo getPaints(Level level,BlockPos pos)
    {
        LevelChunk chunk=level.getChunkAt(pos);

        return getPaints(chunk);
    }

    public static PaintInfo getPaints(Level level, ChunkPos pos)
    {
        LevelChunk chunk=level.getChunk(pos.x,pos.z);
        return getPaints(chunk);
    }

    public static PaintInfo getPaints(LevelChunk chunk)
    {
        PaintInfo paintInfo=chunk.getData(ModAttachments.PERSONALITY.get());

        return paintInfo;
    }

    public static void addPaint(Level level,BlockPos pos, Paint paint)
    {

        PaintInfo paintInfo=getPaints(level,pos);

        paintInfo.addPaint(pos,paint);

        save(level,pos);

    }

    public static void removePaint(Level level,BlockPos pos)
    {
        PaintInfo paintInfo=getPaints(level,pos);
        paintInfo.removePaint(pos);

        save(level,pos);
    }

    public static Paint getPaint(Level level,BlockPos pos)
    {
        PaintInfo paintInfo=getPaints(level,pos);

        Paint paint=paintInfo.getPaint(pos);

        return paint;
    }

    public static void syncToClient(ChunkPos pos, Player player)
    {

        LevelChunk chunk=player.level().getChunk(pos.x,pos.z);
        PaintInfo paintInfo=getPaints(chunk);


        ModChannels.sendToClient(new PaintS2CPacket(paintInfo.serializeNBT(null),pos),player);
    }

    public static void save(Level level,BlockPos pos)
    {
        LevelChunk chunk=level.getChunkAt(pos);
        chunk.setUnsaved(true);
    }
}
