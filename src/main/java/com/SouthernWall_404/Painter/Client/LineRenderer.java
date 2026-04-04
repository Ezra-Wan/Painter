package com.SouthernWall_404.Painter.Client;

import com.SouthernWall_404.Painter.Painter;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

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
        if (mc.player == null || mc.level == null) return;

        // 2. 获取相机位置用于坐标转换
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        // 3. 设置PoseStack，将世界坐标转换为相机相对坐标
        var poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(START.getX() - camPos.x, START.getY() - camPos.y, START.getZ() - camPos.z);
        Matrix4f matrix = poseStack.last().pose();

        // 4. 获取VertexConsumer并开始渲染线条
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        var buffer = bufferSource.getBuffer(RenderType.LINES);
        // 起点

        Vec3i normal=Direction.NORTH.getNormal();
        buffer.addVertex(matrix, 0, 0, 0).setColor(255, 255, 255, 255).setNormal(normal.getX(),normal.getY(),normal.getZ());
        // 终点
        buffer.addVertex(matrix, END.getX() - START.getX(), END.getY() - START.getY(), END.getZ() - START.getZ())
                .setColor(255, 255, 255, 255).setNormal(normal.getX(),normal.getY(),normal.getZ());

        // 5. 提交渲染
        bufferSource.endBatch(RenderType.LINES);
        poseStack.popPose();
    }
}