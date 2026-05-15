package com.SouthernWall_404.Painter.Client;


import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.OutLine.Line;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.OutLine.LineRenderType;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.Quad.Quad;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.RenderHelper;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.ConfigAPI;
import com.SouthernWall_404.LaplaceAPI.VertinCore.ITickable;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintValidHelper;
import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.API.Tool.SelectedZoneForSwap;
import com.SouthernWall_404.Painter.Client.Config.ClientConfig;
import com.SouthernWall_404.Painter.Common.Config.ModConfig;
import com.SouthernWall_404.Painter.Common.Event.PlayerJoinEvent;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Painter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 这里为渲染管线
 * 只缓存以下内容
 * - Map<Direction,Set<BlockPos></>></>各面的渲染内容
 * - Set<Line></>用于
 * 均为惰性更新
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Painter.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)

public class SelectedZoneRenderForSwap{


    private static boolean isChanged=false;
    //========需要线程安全的数据 =========
    private static  Set<Line> EDGES=new HashSet<>();

    public static final Map<Direction ,Quad> QUADS=new HashMap<>();

    public static void setChanged() {
        isChanged = true;
    }

    public static void done(){
        isChanged=false;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }



        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            VertexConsumer buffer = bufferSource.getBuffer(LineRenderType.PURE_COLOR);
            PoseStack poseStack = event.getPoseStack();

            Player player=mc.player;
            Level level=mc.level;

            if (player == null || level == null) return;//防止非法启用


            if(PaintValidHelper.shouldRenderSelected( player.getItemInHand(InteractionHand.MAIN_HAND))||
                    PaintValidHelper.shouldRenderSelected( player.getItemInHand(InteractionHand.OFF_HAND)))//如果主手或副手有渲染物品则进行渲染
            {

                Camera camera=mc.gameRenderer.getMainCamera();
                Vec3 camPos = camera.getPosition();


                SelectedZoneForSwap zone=player.getData(ModAttachments.SELECTED_ZONE_FOR_SWAP);

                zone.getContains().forEach((face, poses)->{

                    Quad quad=QUADS.get(face);
                    poses.forEach(pos->{
                        quad.render(pos,camPos,buffer,poseStack);
                    });
                });
                bufferSource.endBatch(LineRenderType.PURE_COLOR);
            }
        }

    }

    @SubscribeEvent
    public static void onJoinWorld(PlayerEvent.PlayerLoggedInEvent event)
    {
        int alpha= ConfigAPI.getValue(ResourceLocation.fromNamespaceAndPath(Painter.MODID,ClientConfig.QUAD_COLOR_ALPHA));
        int rgb= ConfigAPI.getValue(ResourceLocation.fromNamespaceAndPath(Painter.MODID,ClientConfig.QUAD_COLOR));
        float offset=0.02f;
        for (Direction face : Direction.values())
        {
            float[][] positions= RenderHelper.getSimpleQuadVertex(face);
            QUADS.put(face, Quad.builder()
                    .addVertex(new Vector3f(positions[0]).add(new Vector3f(face), offset))
                    .addVertex(new Vector3f(positions[1]).add(new Vector3f(face), offset))
                    .addVertex(new Vector3f(positions[2]).add(new Vector3f(face), offset))
                    .addVertex(new Vector3f(positions[3]).add(new Vector3f(face), offset))
                    .setColor(FastColor.ARGB32.color(alpha,rgb))
                    .build());
        }
    }

}
