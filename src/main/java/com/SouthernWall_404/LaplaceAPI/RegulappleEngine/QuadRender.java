package com.SouthernWall_404.LaplaceAPI.RegulappleEngine;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3;
import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.Quad.Quad;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public class QuadRender {

    public static void renderQuad(Quad quad, Vector3f renderPos, Vec3 camPos, PoseStack poseStack, VertexConsumer buffer){

        poseStack.pushPose();
        poseStack.translate(renderPos.getX()-camPos.x,renderPos.getY()-camPos.y,renderPos.getZ()-camPos.z);
        Matrix4f matrix4f=poseStack.last().pose();
        poseStack.popPose();

        quad.dynamicRender(matrix4f,buffer);
    }
}
