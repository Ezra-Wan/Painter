package com.SouthernWall_404.Painter.Client;


import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintChunkInfo;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.ModModelRender;
import com.SouthernWall_404.Painter.API.Paint.Util.CommonUtil;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Painter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.*;

@EventBusSubscriber(modid = Painter.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)

public class PaintRender {

    private static Map<BlockPos,AbstractRender<?,?>> renders=new HashMap<>();
    private static boolean isFirstRender=true;
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        ModelBlockRenderer.enableCaching();
        MultiBufferSource.BufferSource bufferSource=Minecraft.getInstance().renderBuffers().bufferSource();




        //TODO这里以后记得处理一下世界退出处理
        if (renders.isEmpty())
        {
            if(isFirstRender)redraw();
//            System.out.println("Client's Renders Empty");
        }
        for(Map.Entry<BlockPos,AbstractRender<?,?>> entry:renders.entrySet())
        {
            BlockPos pos=entry.getKey();
            AbstractRender render=entry.getValue();
            render.render(pos,event.getPoseStack(),bufferSource,0,0,event.getRenderTick());
        }
//
//        // 1. 获取 Block 的默认状态 (如果你需要特定状态，可以传入相应的 BlockState)
//        BlockState state = Blocks.IRON_BLOCK.defaultBlockState();
//
//        // 2. 获取 ModelManager 并得到这个 BlockState 对应的 BakedModel
//        //    注意：这段代码必须在客户端执行，因为 ModelManager 只在客户端存在。
//        BakedModel model =
//                Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
//        RandomSource random = RandomSource.create();
//        List<BakedQuad> testQuad=model.getQuads(state, Direction.NORTH,random, ModelData.EMPTY, RenderType.CUTOUT);
//
//        MultiBufferSource.BufferSource bufferSource=Minecraft.getInstance().renderBuffers().bufferSource();
//
//        ModModelRender.AmbientOcclusionFace aoFace = new ModModelRender.AmbientOcclusionFace();
//
//        PoseStack poseStack=event.getPoseStack();
//        BlockPos pos=new BlockPos(0,128,0);
//        BakedQuad quad=testQuad.get(0);
//
//        BakedQuadRender.renderWithAO(quad,state,pos,poseStack,RenderType.CUTOUT,bufferSource,aoFace);
//
//        bufferSource.endBatch(RenderType.CUTOUT);

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

    /**
     * 遍历玩家周围radius范围，获取所有的Render
     * @return
     */
    private static Map<BlockPos,AbstractRender<?,?>> getRenderNearby()
    {

        Map<BlockPos,AbstractRender<?,?>> result=new HashMap<>();

        Minecraft mc=Minecraft.getInstance();
        Player player=mc.player;
        Level level=mc.level;
        Vec3 vec3=player.position();
        BlockPos origin= CommonUtil.vec32Pos(vec3);

        int radius=Minecraft.getInstance().options.getEffectiveRenderDistance()*16;


        LevelChunk originChunk=level.getChunkAt(origin);

        ChunkPos originChunkPos=originChunk.getPos();

        for(int i=-radius;i<=radius;i++)

        {
            for(int j=-radius;j<=radius;j++)

            {
                ChunkPos currentChunkPos=new ChunkPos(originChunkPos.x+i,originChunkPos.z+j);
                LevelChunk chunk=level.getChunk(currentChunkPos.x,currentChunkPos.z);
                PaintInfo paintInfo=chunk.getData(ModAttachments.PAINT_INFO);

                result.putAll(paintInfo.getRenders());

            }

        }
        return result;
    }
}
