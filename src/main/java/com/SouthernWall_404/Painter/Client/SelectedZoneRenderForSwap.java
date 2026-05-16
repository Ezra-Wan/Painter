package com.SouthernWall_404.Painter.Client;


import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.OutLine.Line;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.OutLine.LineRenderType;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.Quad.Quad;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.RenderHelper;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.ConfigAPI;
import com.SouthernWall_404.LaplaceAPI.VertinCore.ITickable;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Util.CommonUtil;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintValidHelper;
import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.API.Tool.SelectedZoneForSwap;
import com.SouthernWall_404.Painter.API.Tool.Wall.SelectedQuad;
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

import java.util.*;

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
    /**
     * 顶点类，包含位置和关系列表
     */
    private static class Vertex {
        Vector3f position;
        List<Direction> relations; // 存储需要检索的方向
        
        public Vertex(Vector3f position) {
            this.position = position;
            this.relations = new ArrayList<>();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Vertex)) return false;
            Vertex other = (Vertex) obj;
            return this.position.equals(other.position);
        }
        
        @Override
        public int hashCode() {
            return position.hashCode();
        }
    }
    
    /**
     * 从selected建立Edges的方法，需要检查选区轮廓，建立Lines
     *
     *                  * 思路：
     *                  * 先建立一个顶点类，包括两样内容：
     *                  * - 位置，使用Laplace的Vector3f
     *                  * - 关系，一个Direction列表，用于存储其在建立连接时应当检索的方向
     *                  * 建立一个对于下述遍历的全局顶点列表，用于统一存储顶点
     *                  * 遍历每个方向的选取位置，检查其垂直于法线方向的四个方向的临近方块，判定其是否为顶点
     *                  * 判据：
     *                  * - 如果两相邻边上均具有选区，则认为其为内凹顶点，将两相邻边的交点入队，并且将两相邻边的关系加入关系列表
     *                  * - 如果两相邻边上均无选取，认定其为外凸顶点，将两相邻边的交点入队，并且将两相邻边的关系加入关系列表
     *                  * 注意：如果入队的顶点已经存在，那么只合并关系
     *                  *
     *                  *
     *                  * 遍历完成后，对当前顶点建立连接的Line列表
     *                  * 在这一过程中，遍历每个顶点
     *                  * - 遍历其中的每个关系
     *                  *   - 对于关系所指向的方向，寻找最近的其他顶点，在两者间建立line
     *                  *   - 移除两者之间的这一关系
     */
    public static void buidEdges()
    {
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
            Player player=mc.player;
            if(player!=null)
            {
                SelectedZoneForSwap zone=player.getData(ModAttachments.SELECTED_ZONE_FOR_SWAP);
                
                // 清空旧边缘
                EDGES.clear();
                
                // 全局顶点列表
                Map<Vector3f, Vertex> globalVertices = new HashMap<>();
                
                // 遍历每个方向的选区
                zone.getContains().forEach((face, poses) -> {
                    if(poses.isEmpty()) return;
                    
                    // 获取垂直于法线方向的四个方向
                    List<Direction> tanDirsOrigin = com.SouthernWall_404.LaplaceAPI.VertinCore.Util.CommonUtil.getTanDir(face);//TODO 有空把这玩意改了，什么吊东西

                    List<Direction>tanDirs=new ArrayList<>();

                    tanDirs.add(tanDirsOrigin.get(0));
                    tanDirs.add(tanDirsOrigin.get(2));
                    tanDirs.add(tanDirsOrigin.get(1));
                    tanDirs.add(tanDirsOrigin.get(3));

                    // 遍历每个选中位置
                    for(BlockPos pos : poses) {
                        // 检查四个方向的相邻情况
                        for(int i = 0; i < tanDirs.size(); i++) {
                            Direction dir1 = tanDirs.get(i);
                            Direction dir2 = tanDirs.get((i + 1) % tanDirs.size()); // 下一个相邻方向
                            
                            BlockPos neighbor1 = pos.relative(dir1);
                            BlockPos neighbor2 = pos.relative(dir2);
                            
                            boolean hasNeighbor1 = poses.contains(neighbor1);
                            boolean hasNeighbor2 = poses.contains(neighbor2);
                            
                            // 判定是否为顶点
                            boolean isVertex = false;
                            List<Direction> vertexRelations = new ArrayList<>();
                            
                            if(hasNeighbor1 && hasNeighbor2) {
                                // 内凹顶点：两相邻边均有选区

                                if(!zone.contains(pos.relative(dir1).relative(dir2)))//附加判定，以免内部面被错误判定为内凹
                                {
                                    isVertex = true;
                                    vertexRelations.add(dir1);
                                    vertexRelations.add(dir2);
                                }

                            } else if(!hasNeighbor1 && !hasNeighbor2) {
                                // 外凸顶点：两相邻边均无选区
                                isVertex = true;
                                vertexRelations.add(dir1.getOpposite());
                                vertexRelations.add(dir2.getOpposite());
                            }
                            
                            if(isVertex) {
                                // 计算顶点位置（两相邻边的交点）
                                Vector3f vertexPos = calculateVertexPosition(pos, dir1, dir2, face);
                                
                                // 查找是否已存在该顶点
                                Vertex existingVertex = globalVertices.get(vertexPos);
                                if(existingVertex != null) {
                                    // 如果已存在，只合并关系
                                    for(Direction rel : vertexRelations) {
                                        if(!existingVertex.relations.contains(rel)) {
                                            existingVertex.relations.add(rel);
                                        }
                                    }
                                } else {
                                    // 创建新顶点
                                    Vertex newVertex = new Vertex(vertexPos);
                                    newVertex.relations.addAll(vertexRelations);
                                    globalVertices.put(vertexPos, newVertex);
                                }
                            }
                        }
                    }
                });
                
                // 遍历完成后，对每个顶点建立连接的Line列表
                for(Vertex vertex : globalVertices.values()) {
                    // 遍历其中的每个关系
                    Iterator<Direction> relationIter = vertex.relations.iterator();
                    while(relationIter.hasNext()) {
                        Direction relation = relationIter.next();
                        
                        // 对于关系所指向的方向，寻找最近的其他顶点
                        Vertex nearestVertex = findNearestVertex(vertex, relation, globalVertices);
                        
                        if(nearestVertex != null) {
                            // 在两者间建立line
                            Line line = Line.builder(vertex.position, nearestVertex.position)
                                    .setColor(FastColor.ARGB32.color(
                                            ConfigAPI.getValue(ResourceLocation.fromNamespaceAndPath(Painter.MODID, ClientConfig.LINE_COLOR_ALPHA)),
                                            ConfigAPI.getValue(ResourceLocation.fromNamespaceAndPath(Painter.MODID, ClientConfig.LINE_COLOR))
                                    ))
                                    .setWidth(0.05f)
                                    .build();
                            EDGES.add(line);
                            
                            // 移除两者之间的这一关系
                            relationIter.remove();
                            nearestVertex.relations.remove(relation.getOpposite());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 计算顶点位置（两相邻边的交点）
     */
    private static Vector3f calculateVertexPosition(BlockPos pos, Direction dir1, Direction dir2, Direction face) {
        // 顶点位于方块的角上，需要根据方向和面来计算具体位置
        Vector3f baseVec = new Vector3f(pos);

        if(CommonUtil.isPositiveAxis(face))baseVec=baseVec.add(new Vector3f(face));

        // 根据两个方向调整到角点
        if(com.SouthernWall_404.LaplaceAPI.VertinCore.Util.CommonUtil.isPositiveAxis(dir1)) {
            baseVec = baseVec.add(new Vector3f(dir1), 1.0f);
        }
        if(com.SouthernWall_404.LaplaceAPI.VertinCore.Util.CommonUtil.isPositiveAxis(dir2)) {
            baseVec = baseVec.add(new Vector3f(dir2), 1.0f);
        }
        
        return baseVec;
    }
    
    /**
     * 在指定方向上寻找最近的顶点
     */
    private static Vertex findNearestVertex(Vertex from, Direction direction, Map<Vector3f, Vertex> allVertices) {
        Vertex nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for(Vertex other : allVertices.values()) {
            if(other == from) continue;
            
            // 计算向量差
            float dx = other.position.getX() - from.position.getX();
            float dy = other.position.getY() - from.position.getY();
            float dz = other.position.getZ() - from.position.getZ();
            
            // 判断是否在同一方向线上
            if(isInDirection(dx, dy, dz, direction)) {
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if(distance < minDistance) {
                    minDistance = distance;
                    nearest = other;
                }
            }
        }
        
        return nearest;
    }
    
    /**
     * 判断向量是否在指定方向上
     */
    private static boolean isInDirection(float dx, float dy, float dz, Direction direction) {
        Vector3f dirVec = new Vector3f(direction);
        
        // 计算点积
        float dot = dx * dirVec.getX() + dy * dirVec.getY() + dz * dirVec.getZ();
        float length = (float)Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        // 如果点积为正且接近长度，则在同一方向上
        return dot > 0 && Math.abs(dot - length) < 0.001f;
    }


    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        if(isChanged){
            buidEdges();
            done();
        }



        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
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
                        quad.render(pos,camPos,bufferSource.getBuffer(LineRenderType.PURE_COLOR),poseStack);
                    });
                });
                bufferSource.endBatch(LineRenderType.PURE_COLOR);


                EDGES.forEach(line->{
                    line.render(camPos,bufferSource.getBuffer(LineRenderType.PURE_COLOR_SOLID),poseStack);
                });

                bufferSource.endBatch(LineRenderType.PURE_COLOR_SOLID);
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
