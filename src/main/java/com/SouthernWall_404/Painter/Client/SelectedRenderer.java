package com.SouthernWall_404.Painter.Client;

import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.OutLine.Line;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.OutLine.LineRenderType;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.Quad.Quad;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.QuadRender;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.RenderHelper;
import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.Painter.API.Paint.Util.PaintUtil;
import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.API.Tool.Wall.Edge;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.World.Item.ChulkItem;
import com.SouthernWall_404.Painter.Common.World.Item.PaintBucketItem;
import com.SouthernWall_404.Painter.Common.World.Item.PaintItem;
import com.SouthernWall_404.Painter.Painter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Painter.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class SelectedRenderer {

    private static final BlockPos START = new BlockPos(0, 128, 0);
    private static final BlockPos END = new BlockPos(1, 128, 0); // 从(0,128,0)到(10,128,0)的线

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 1. 选择一个合适的渲染阶段
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }


        Minecraft mc = Minecraft.getInstance();
        Player player=mc.player;
        Level level=mc.level;

        if (player == null || level == null) return;

        ItemStack itemStack=player.getItemInHand(InteractionHand.MAIN_HAND);
        if(itemStack.getItem()instanceof PaintItem||
                itemStack.getItem()instanceof ChulkItem||itemStack.getItem() instanceof PaintBucketItem)
        {

        }else return;

        SelectedZone selectedZone=player.getData(ModAttachments.SELECTED_ZONE);
        List<BlockPos> poses=selectedZone.getContains();

        // 2. 获取相机位置用于坐标转换
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();




        // 起点
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();


        for(BlockPos pos:poses)
        {
            if(!PaintUtil.isPaintable(level,pos))
            {
                continue;
            }
        }



        List<Edge> edges=selectedZone.getCachedEdges();

        VertexConsumer lineBuffer=bufferSource.getBuffer(LineRenderType.PURE_COLOR_SOLID);

        edges.forEach((edge)->{

            renderEdge(edge,camPos,lineBuffer,poseStack);

        });

        VertexConsumer faceBuffer=bufferSource.getBuffer(LineRenderType.PURE_COLOR);
        Map<BlockPos,Quad> quads=selectedZone.getCachedQuads();
        quads.forEach(
                (blockPos,quad)->{
                    renderFace(quad,camPos,blockPos,faceBuffer,poseStack);
                }
        );


        bufferSource.endBatch(LineRenderType.PURE_COLOR_SOLID);
        bufferSource.endBatch(LineRenderType.PURE_COLOR);

    }

    public static void renderEdge(Edge edge, Vec3 camPos, VertexConsumer buffer, PoseStack poseStack)
    {
        edge.render(camPos,buffer,poseStack);
    }

    public static void renderFace(Quad quad,Vec3 camPos,BlockPos pos, VertexConsumer buffer, PoseStack poseStack)
    {
        poseStack.pushPose();
        poseStack.translate(pos.getX()-camPos.x,pos.getY()-camPos.y,pos.getZ()-camPos.z);
        Matrix4f matrix4f=poseStack.last().pose();
        poseStack.popPose();

        quad.render(matrix4f,buffer);
    }

    //TODO 他妈的忘做渲染移除了
    //TODO 还有，性能有待优化
}