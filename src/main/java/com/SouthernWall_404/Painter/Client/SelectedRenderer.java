package com.SouthernWall_404.Painter.Client;

import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.Line.Line;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.Line.LineRenderType;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.RenderHelper;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.Vector3f;
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
import net.minecraft.client.renderer.block.model.BakedQuad;
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

@EventBusSubscriber(modid = Painter.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class SelectedRenderer {

    private static final BlockPos START = new BlockPos(0, 128, 0);
    private static final BlockPos END = new BlockPos(1, 128, 0); // 从(0,128,0)到(10,128,0)的线


    //TODO 添加新的渲染方法
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
        Direction face=selectedZone.getFace();

        // 2. 获取相机位置用于坐标转换
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();




        // 起点
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();




        var buffer = bufferSource.getBuffer(CustomRenderTypes.PURE_COLOR);
        var poseStack = event.getPoseStack();

        Line line=Line.builder(new Vector3f(0,-31,0),new Vector3f(0,-28,0))
                .setColor(0xccebe5d1)
                .setWidth(0.1f)
                .build();
        line.render(camPos,bufferSource.getBuffer(LineRenderType.PURE_COLOR_SOLID),poseStack);
        bufferSource.endBatch(LineRenderType.PURE_COLOR_SOLID);


        for(BlockPos pos:poses)
        {
            if(!PaintUtil.isPaintable(level,pos))
            {
                continue;
            }


            //            renderFace(pos,face,camPos,buffer,poseStack);

//            poseStack.pushPose();
//            // 4. 获取VertexConsumer并开始渲染线条
//
//            // 3. 设置PoseStack，将世界坐标转换为相机相对坐标
//
//            poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
//            Matrix4f matrix = poseStack.last().pose();
//
//
//
//            Vec3i normal=Direction.NORTH.getNormal();
//            buffer.addVertex(matrix, 0, 0, -0.001f)
//                    .setColor(235, 229, 209, 64)
//                    .setNormal(normal.getX(),normal.getY(),normal.getZ());
////                    .setUv(0,0).setUv2(0,0);
//            // 终点
//            buffer.addVertex(matrix, 0, 1, -0.001f)
//                    .setColor(235, 229, 209, 64)
//                    .setNormal(normal.getX(),normal.getY(),normal.getZ());
////                    .setUv(0,1)
////                    .setUv2(0,1);
//            buffer.addVertex(matrix, 1, 1, -0.001f)
//                    .setColor(235, 229, 209, 64)
//                    .setNormal(normal.getX(),normal.getY(),normal.getZ());
////                    .setUv(1,0).setUv2(1,0);
//
//
//            buffer.addVertex(matrix, 0, 0, -0.001f)
//                    .setColor(235, 229, 209, 64)
//                    .setNormal(normal.getX(),normal.getY(),normal.getZ());
////                    .setUv(0,1).setUv2(0,1);
//            buffer.addVertex(matrix, 1, 1, -0.001f)
//                    .setColor(235, 229, 209, 64)
//                    .setNormal(normal.getX(),normal.getY(),normal.getZ());
////                    .setUv(1,0).setUv2(1,0);
////            buffer.addVertex(matrix, 1, 0, 0)
////                    .setColor(235, 229, 209, 128)
////                    .setNormal(normal.getX(),normal.getY(),normal.getZ());
//            buffer.addVertex(matrix, 1, 0, -0.001f)
//                    .setColor(235, 229, 209, 64).setNormal(normal.getX(),normal.getY(),normal.getZ());
//
//            // 5. 提交渲染
//
//            poseStack.popPose();
        }



        List<Edge> edges=selectedZone.getCachedEdges();

        VertexConsumer lineBuffer=bufferSource.getBuffer(RenderType.LINES);

        edges.forEach((edge)->{

            renderEdge(edge,face,camPos,lineBuffer,poseStack);

        });
        bufferSource.endBatch(CustomRenderTypes.PURE_COLOR);

        bufferSource.endBatch(RenderType.LINES);

    }

    public static void renderEdge(Edge edge, Direction face, Vec3 camPos, VertexConsumer buffer, PoseStack poseStack)
    {
        poseStack.pushPose();
        // 4. 获取VertexConsumer并开始渲染线条

        // 3. 设置PoseStack，将世界坐标转换为相机相对坐标

        poseStack.translate(edge.A.x - camPos.x, edge.A. y- camPos.y, edge.A.z - camPos.z);
        Matrix4f matrix = poseStack.last().pose();

        edge.render(poseStack, buffer,  face);


        poseStack.popPose();
    }
    public static void renderFace(BlockPos pos, Direction face, Vec3 camPos, VertexConsumer buffer, PoseStack poseStack)
    {
        poseStack.pushPose();
        // 4. 获取VertexConsumer并开始渲染线条

        // 3. 设置PoseStack，将世界坐标转换为相机相对坐标

        poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
        Matrix4f matrix = poseStack.last().pose();

        vertexDeal(matrix,face,buffer);


        poseStack.popPose();
    }




    //TODO 他妈的忘做渲染移除了
    //TODO 还有，性能有待优化
    private static void vertexDeal(Matrix4f matrix4f,Direction face, VertexConsumer buffer) {
//        boolean isTopSlab = origin.getValue(SlabBlock.TYPE) == SlabType.TOP;


        int color=0x19ebe5d1;
        float offset=0.011f;

        if(face==null)return;

        Vec3i normal=face.getNormal();
        float[][] positions= RenderHelper.getSimpleQuadVertex(face);

//        switch (face) {
//            case DOWN -> positions =new float[][]{
//                    {0, yMin, 1}, {0, yMin, 0}, {1, yMin, 0}, {1, yMin, 1}
//            };
//            case UP -> positions = new float[][]{
//                    {0, yMax, 0}, {0, yMax, 1}, {1, yMax, 1}, {1, yMax, 0}
//            };
//            case NORTH -> positions = new float[][]{
//                    {1, yMax, 0},{1, yMin, 0},{0, yMin, 0},{0, yMax, 0}
//            };
//            case SOUTH -> positions = new float[][]{
//                    {0, yMax, 1}, {0, yMin, 1}, {1, yMin, 1}, {1, yMax, 1}
//            };
//            case WEST -> positions = new float[][]{
//                    {0, yMax, 0}, {0, yMin, 0}, {0, yMin, 1}, {0, yMax, 1}
//
//            };
//            case EAST -> positions = new float[][]{
//                    {1, yMax, 1}, {1, yMin, 1}, {1, yMin, 0}, {1, yMax, 0}
//            };
//            default -> throw new IllegalArgumentException("Invalid direction: " + face);
//        }



        buffer.addVertex(matrix4f,positions[0][0]+normal.getX()*offset,positions[0][1]+normal.getY()*offset,positions[0][2]+normal.getZ()*offset)
                .setNormal(normal.getX(),normal.getY(),normal.getZ())
                .setColor(color);
        buffer.addVertex(matrix4f,positions[3][0]+normal.getX()*offset,positions[3][1]+normal.getY()*offset,positions[3][2]+normal.getZ()*offset)
                .setNormal(normal.getX(),normal.getY(),normal.getZ())
                .setColor(color);
        buffer.addVertex(matrix4f,positions[2][0]+normal.getX()*offset,positions[2][1]+normal.getY()*offset,positions[2][2]+normal.getZ()*offset)
                .setNormal(normal.getX(),normal.getY(),normal.getZ())
                .setColor(color);


        buffer.addVertex(matrix4f,positions[0][0]+normal.getX()*offset,positions[0][1]+normal.getY()*offset,positions[0][2]+normal.getZ()*offset)
                .setNormal(normal.getX(),normal.getY(),normal.getZ())
                .setColor(color);
        buffer.addVertex(matrix4f,positions[1][0]+normal.getX()*offset,positions[1][1]+normal.getY()*offset,positions[1][2]+normal.getZ()*offset)
                .setNormal(normal.getX(),normal.getY(),normal.getZ())
                .setColor(color);
        buffer.addVertex(matrix4f,positions[2][0]+normal.getX()*offset,positions[2][1]+normal.getY()*offset,positions[2][2]+normal.getZ()*offset)
                .setNormal(normal.getX(),normal.getY(),normal.getZ())
                .setColor(color);
    }
}