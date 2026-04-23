package com.SouthernWall_404.Painter.API.Paint.Util;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;

public final class PaintAttachmentHelper {
    private PaintAttachmentHelper() {}

    @Nullable
    public static PaintInfo getPaintInfo(Level level, BlockPos pos) {
        LevelChunk chunk = level.getChunkAt(pos);
        return chunk.getData(ModAttachments.PAINT_INFO);
    }

    @Nullable
    public static AbstractRender<?, ?> getRender(Level level, BlockPos pos) {
        PaintInfo info = getPaintInfo(level, pos);
        return info == null ? null : info.getRenders().get(pos);
    }

    public static void putRender(Level level, BlockPos pos, AbstractRender<?, ?> render) {
        PaintInfo info = getPaintInfo(level, pos);
        if (info != null) {
            info.putRender(level, pos, render);
            level.getChunkAt(pos).setUnsaved(true);
        }
    }
}