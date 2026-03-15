package com.SouthernWall_404.Painter.API.Paint;

import com.SouthernWall_404.Painter.Common.Init.ModBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;

public class RenderBlockAPI {

    /**
     * 对于添加的位置 blockPos，先获取其所在区块，
     * 而后将区块内的（0,0）位置在 [-55, -64] 的高度上进行搜索，
     * 将最底下的基岩替换为一个铁块。
     */
    public static void onPaint(Level level, BlockPos blockPos) {
        LevelChunk chunk = level.getChunkAt(blockPos);
        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();

        BlockPos target = null;

        // 从下往上（y = -64 到 -55）搜索
        for (int y = -64; y <= -55; y++) {
            BlockPos pos = new BlockPos(startX, y, startZ);
            var blockState = level.getBlockState(pos);
            var block = blockState.getBlock();

            if (block == ModBlock.RENDER_BEDROCK.get()) {
                return;
            }

            // 如果遇到基岩，记录位置并停止搜索
            if (block == Blocks.BEDROCK) {
                target = pos;
                break;
            }
        }

        if (target != null) {
            level.setBlock(target, ModBlock.RENDER_BEDROCK.get().defaultBlockState(), 3);
        }
    }
}