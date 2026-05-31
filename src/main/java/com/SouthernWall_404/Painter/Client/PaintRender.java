package com.SouthernWall_404.Painter.Client;


import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Painter;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashSet;
import java.util.Set;


@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Painter.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class PaintRender {

    // 只缓存包含粉刷数据的区块位置
    private static Set<ChunkPos> cachedChunks = new HashSet<>();

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }

        ModelBlockRenderer.enableCaching();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());

        Frustum frustum = event.getFrustum();
        if (frustum == null) {
            return;
        }

        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        // 创建缓存区块的快照副本以避免并发修改异常
        Set<ChunkPos> chunksSnapshot;
        synchronized (cachedChunks) {
            chunksSnapshot = new HashSet<>(cachedChunks);
        }

        // 遍历缓存的区块，调用每个 PaintInfo 的 render 方法
        chunksSnapshot.forEach(chunkPos -> {
            LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);

            // 2. 区块级别视锥剔除
            int minX = chunkPos.getMinBlockX();
            int minZ = chunkPos.getMinBlockZ();
            int maxX = chunkPos.getMaxBlockX();
            int maxZ = chunkPos.getMaxBlockZ();
            AABB chunkAABB = new AABB(minX, -64, minZ, maxX + 1, 320, maxZ + 1);

            if (!frustum.isVisible(chunkAABB)) {
                return; // 整个区块在视野外
            }

            if (chunk != null) {
                PaintInfo paintInfo = chunk.getData(ModAttachments.PAINT_INFO);
                paintInfo.render(event.getPoseStack(), buffer, frustum);
            }
        });

        bufferSource.endBatch(RenderType.cutout());
    }

    /**
     * 添加需要渲染的区块
     */
    public static void addChunk(ChunkPos pos) {
        synchronized (cachedChunks) {
            cachedChunks.add(pos);
        }
    }

    /**
     * 移除不需要渲染的区块
     */
    public static void removeChunk(ChunkPos pos) {
        synchronized (cachedChunks) {
            cachedChunks.remove(pos);
        }
    }

    /**
     * 清空所有缓存
     */
    public static void clear() {
        synchronized (cachedChunks) {
            cachedChunks.clear();
        }
    }

    /**
     * 刷新所有缓存区块的渲染数据
     */
    public static void refresh() {
        // 创建缓存区块的快照副本以避免并发修改异常
        Set<ChunkPos> chunksSnapshot;
        synchronized (cachedChunks) {
            chunksSnapshot = new HashSet<>(cachedChunks);
        }
        
        chunksSnapshot.forEach(pos -> {
            LevelChunk chunk = Minecraft.getInstance().level.getChunkSource().getChunkNow(pos.x, pos.z);
            if (chunk != null) {
                PaintInfo paintInfo = chunk.getData(ModAttachments.PAINT_INFO);
                paintInfo.refresh();
            }
        });
    }
}
