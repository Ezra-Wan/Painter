package com.SouthernWall_404.Painter.API.Capability;

import com.SouthernWall_404.Painter.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class PaintUtil {

    public static PaintInfo getPaints(Level level,BlockPos pos)
    {
        LevelChunk chunk=level.getChunkAt(pos);
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

    public static void save(Level level,BlockPos pos)
    {
        LevelChunk chunk=level.getChunkAt(pos);
        chunk.setUnsaved(true);
    }
}
