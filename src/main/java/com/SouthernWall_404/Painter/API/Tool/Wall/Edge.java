package com.SouthernWall_404.Painter.API.Tool.Wall;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class Edge {

    public Vec3 A;
    public Vec3 B;

    public Edge(Vec3 a, Vec3 b) {
        A = a;
        B = b;
    }

    public Vec3 getNormal()
    {
        return new Vec3(A.x-B.x,A.y-B.y,A.z-B.z);
    }
    /**
     * 渲染此边的线框（一条直线）。
     * 注意：调用前需要确保 PoseStack 已变换到合适的局部坐标（例如 A 点相对于相机的位置）。
     * @param poseStack     当前渲染姿态（应已包含平移/旋转/缩放）
     * @param buffer        VertexConsumer (通常来自 RenderType.LINES)
     * @param r,g,b,a       颜色与透明度 (0~1 或 0~255 取决于 buffer 的实现)
     * @param face          用于计算偏移方向的面（可选，用于避免深度冲突）
     */
    public void render(PoseStack poseStack, VertexConsumer buffer,
                       float r, float g, float b, float a, Direction face) {
        Matrix4f matrix = poseStack.last().pose();
        Vec3i normal = face.getNormal();
        float offset = 0.002f; // 微小偏移，避免与方块表面深度冲突

        // 起点 A 在世界中的坐标已经通过外部的 poseStack.translate 处理，
        // 这里 A 和 B 应该被视为相对于当前局部原点的偏移量。
        // 由于外部已经将 poseStack 平移到 edge.A 处，所以 A 的局部坐标是 (0,0,0)，B 的局部坐标是 (B.x - A.x, ...)
        float localBX = (float)(B.x - A.x);
        float localBY = (float)(B.y - A.y);
        float localBZ = (float)(B.z - A.z);

        // 应用法线偏移（使线条稍微突出于方块表面）
        float offX = normal.getX() * offset;
        float offY = normal.getY() * offset;
        float offZ = normal.getZ() * offset;

        // 起点 (0,0,0) + 偏移
        buffer.addVertex(matrix, offX, offY, offZ)
                .setColor(r, g, b, a)
                .setNormal(normal.getX(), normal.getY(), normal.getZ());
        // 终点 (localBX, localBY, localBZ) + 偏移
        buffer.addVertex(matrix, localBX + offX, localBY + offY, localBZ + offZ)
                .setColor(r, g, b, a)
                .setNormal(normal.getX(), normal.getY(), normal.getZ());
    }
}
