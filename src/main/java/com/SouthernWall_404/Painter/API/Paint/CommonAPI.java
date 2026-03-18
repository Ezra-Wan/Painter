package com.SouthernWall_404.Painter.API.Paint;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class CommonAPI {

    public static Vec3 rotate90(Vec3 vec, Direction.Axis axis) {
        switch (axis) {
            case X:
                return new Vec3(vec.x, -vec.z, vec.y);
            case Y:
                return new Vec3(vec.z, vec.y, -vec.x);
            case Z:
                return new Vec3(-vec.y, vec.x, vec.z);
            default:
                throw new IllegalArgumentException("Unknown axis: " + axis);
        }
    }
    public static Vec3 buildVec2(int x,int y, Direction.Axis axis) {
        switch (axis) {
            case X:
                // 将 Vec2 视为 YZ 平面上的点 (y, z)，绕 X 轴旋转
                return new Vec3(0, y, x);
            case Y:
                // 将 Vec2 视为 XZ 平面上的点 (x, z)，绕 Y 轴旋转
                return new Vec3(x, 0, y);
            case Z:
                // 将 Vec2 视为 XY 平面上的点 (x, y)，绕 Z 轴旋转
                return new Vec3(x, y, 0);
            default:
                throw new IllegalArgumentException("Unknown axis: " + axis);
        }
    }
}
