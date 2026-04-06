package com.SouthernWall_404.Painter.API.Tool.Wall;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class Edge {

    public Vec3 A;
    public Vec3 B;

    public Edge(Vec3 a, Vec3 b) {
        A = a;
        B = b;
    }

}
