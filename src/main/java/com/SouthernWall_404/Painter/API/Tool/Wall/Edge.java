package com.SouthernWall_404.Painter.API.Tool.Wall;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.OutLine.Line;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.Configs;
import com.SouthernWall_404.Painter.Client.Config.ClientConfig;
import com.SouthernWall_404.Painter.Painter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class Edge {


    public Line line;

    public Edge(Vector3f a, Vector3f b) {

        ModConfigSpec.ConfigValue value= Configs.getValue(Painter.MODID, ClientConfig.LINE_COLOR);
        var o=value.get();
        if(o instanceof Integer color)
        line= Line.builder(a,b)
                .setColor(color)
                .setWidth(0.05f)
                .build();
    }

    public void render(Vec3 camPos,VertexConsumer buffer,PoseStack poseStack)
    {
        line.render(camPos, buffer, poseStack);
    }
}
