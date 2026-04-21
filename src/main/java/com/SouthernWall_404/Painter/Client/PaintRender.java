package com.SouthernWall_404.Painter.Client;


import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintChunkInfo;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Util.CommonUtil;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.*;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Painter.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)

public class PaintRender {

    private static Map<BlockPos,AbstractRender<?,?>> renders=new HashMap<>();//总渲染内容缓存
    private static boolean isFirstRender=true;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        ModelBlockRenderer.enableCaching();
        MultiBufferSource.BufferSource bufferSource=Minecraft.getInstance().renderBuffers().bufferSource();

        Frustum frustum =event.getFrustum();
        if (frustum == null) {
            return; // 视锥体不可用时不渲染
        }


        for(Map.Entry<BlockPos,AbstractRender<?,?>> entry:renders.entrySet())
        {
            BlockPos pos=entry.getKey();
            AbstractRender render=entry.getValue();


            // 视锥剔除：检查方块位置的包围盒是否可见
            AABB aabb = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            if (!frustum.isVisible(aabb)) {
                continue; // 不可见，跳过渲染
            }

            render.render(pos,event.getPoseStack(), 0,0,event.getRenderTick(),bufferSource.getBuffer(RenderType.cutout()));
        }

        bufferSource.endBatch(RenderType.cutout());

    }

    public static void redraw()
    {

        Minecraft mc=Minecraft.getInstance();
        Level level=mc.level;
        Player player=mc.player;

        if(level==null)return;
        PaintChunkInfo chunkInfo=level.getData(ModAttachments.PAINT_CHUNK_INFO);
        renders=chunkInfo.getRenderNearby(player.getOnPos());
    }
}
