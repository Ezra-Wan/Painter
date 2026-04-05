package com.SouthernWall_404.Painter.Client;

import com.SouthernWall_404.Painter.API.Paint.Util.PaintBlockUtil;
import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.List;

@EventBusSubscriber(modid = Painter.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class LineRenderer {

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


        SelectedZone selectedZone=player.getData(ModAttachments.SELECTED_ZONE);
        List<BlockPos> poses=selectedZone.getSelected();

        // 2. 获取相机位置用于坐标转换
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();




        // 起点
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        var buffer = bufferSource.getBuffer(CustomRenderTypes.PURE_COLOR);
        for(BlockPos pos:poses)
        {
            if(!PaintBlockUtil.isPaintable(level,pos))
            {
                continue;
            }
            var poseStack = event.getPoseStack();
            poseStack.pushPose();
            // 4. 获取VertexConsumer并开始渲染线条

            // 3. 设置PoseStack，将世界坐标转换为相机相对坐标

            poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
            Matrix4f matrix = poseStack.last().pose();



            Vec3i normal=Direction.NORTH.getNormal();
            buffer.addVertex(matrix, 0, 0, -0.001f)
                    .setColor(235, 229, 209, 64)
                    .setNormal(normal.getX(),normal.getY(),normal.getZ());
//                    .setUv(0,0).setUv2(0,0);
            // 终点
            buffer.addVertex(matrix, 0, 1, -0.001f)
                    .setColor(235, 229, 209, 64)
                    .setNormal(normal.getX(),normal.getY(),normal.getZ());
//                    .setUv(0,1)
//                    .setUv2(0,1);
            buffer.addVertex(matrix, 1, 1, -0.001f)
                    .setColor(235, 229, 209, 64)
                    .setNormal(normal.getX(),normal.getY(),normal.getZ());
//                    .setUv(1,0).setUv2(1,0);


            buffer.addVertex(matrix, 0, 0, -0.001f)
                    .setColor(235, 229, 209, 64)
                    .setNormal(normal.getX(),normal.getY(),normal.getZ());
//                    .setUv(0,1).setUv2(0,1);
            buffer.addVertex(matrix, 1, 1, -0.001f)
                    .setColor(235, 229, 209, 64)
                    .setNormal(normal.getX(),normal.getY(),normal.getZ());
//                    .setUv(1,0).setUv2(1,0);
//            buffer.addVertex(matrix, 1, 0, 0)
//                    .setColor(235, 229, 209, 128)
//                    .setNormal(normal.getX(),normal.getY(),normal.getZ());
            buffer.addVertex(matrix, 1, 0, -0.001f)
                    .setColor(235, 229, 209, 64).setNormal(normal.getX(),normal.getY(),normal.getZ());

            // 5. 提交渲染

            poseStack.popPose();
        }
        bufferSource.endBatch(CustomRenderTypes.PURE_COLOR);

    }
}