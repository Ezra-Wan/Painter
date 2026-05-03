package com.SouthernWall_404.Painter.Client;


import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintChunkInfo;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Painter;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.*;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Painter.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)

public class PaintRender {

    private static Map<BlockPos, AbstractRender<?, ?>> renders = new HashMap<>();//总渲染内容缓存
    private static boolean changed = false;
    private static Minecraft mc = Minecraft.getInstance();

    public static void setChanged() {
        changed =true;
    }

    public static void done()
    {
        changed =false;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        ModelBlockRenderer.enableCaching();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        Frustum frustum = event.getFrustum();
        if (frustum == null) {
            return; // 视锥体不可用时不渲染
        }

        if(changed)redraw();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());
        for (Map.Entry<BlockPos, AbstractRender<?, ?>> entry : renders.entrySet()) {
            BlockPos pos = entry.getKey();
            AbstractRender render = entry.getValue();


            // 视锥剔除：检查方块位置的包围盒是否可见
            AABB aabb = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            if (!frustum.isVisible(aabb)) {
                continue; // 不可见，跳过渲染
            }


            Level level = mc.level;
            int packedOverLay = calculatePackedLight(level, pos);
            render.render(pos, event.getPoseStack(), packedOverLay, 0, event.getRenderTick(), buffer);
        }

        bufferSource.endBatch(RenderType.cutout());

    }

    // 辅助方法：获取方块位置的光照值（合并天空光与方块光）
    private static int calculatePackedLight(Level level, BlockPos pos) {

        if (level == null) return 0;
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);
        return (skyLight << 20) | (blockLight << 4);  // 标准打包方式
    }

    //TODO 仍需优化
    //TODO 解决远距渲染的bug
    //TODO 解决半透明面光影不兼容的问题

    public static void redraw()
    {

        Minecraft mc=Minecraft.getInstance();
        Level level=mc.level;
        Player player=mc.player;

        if(level==null)return;
        PaintChunkInfo chunkInfo=level.getData(ModAttachments.PAINT_CHUNK_INFO);
        renders=chunkInfo.getRenderNearby(player.getOnPos());
        renders.forEach((pos,render)->
                render.update());

        done();
    }

    public static synchronized void addChunk(ChunkPos pos)
    {
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
            Level level=mc.level;
            PaintChunkInfo chunkInfo=level.getData(ModAttachments.PAINT_CHUNK_INFO);
            if(chunkInfo.getPaintPoses().contains(pos))
            {
                LevelChunk chunk=level.getChunk(pos.x,pos.z);
                PaintInfo paintInfo=chunk.getData(ModAttachments.PAINT_INFO);
                renders.putAll(paintInfo.getRenders());
            }
        }
    }

    public static synchronized void addRender(BlockPos pos, AbstractRender<?, ?> render)
    {
        renders.put(pos,render);
    }

    public static synchronized void removeChunk(ChunkPos pos)
    {
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
            Level level=mc.level;
            PaintChunkInfo chunkInfo=level.getData(ModAttachments.PAINT_CHUNK_INFO);
            if(chunkInfo.getPaintPoses().contains(pos))
            {
                LevelChunk chunk=level.getChunk(pos.x,pos.z);
                PaintInfo paintInfo=chunk.getData(ModAttachments.PAINT_INFO);

                paintInfo.getRenders().forEach((pos1,render)->
                        renders.remove(pos1));
            }
        }
    }

    public static synchronized void removeRender(BlockPos pos)
    {
        renders.remove(pos);
    }

    public static synchronized void clear()
    {
        renders.clear();
    }
}
