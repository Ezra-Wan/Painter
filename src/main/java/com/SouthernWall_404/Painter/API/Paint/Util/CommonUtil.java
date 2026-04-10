package com.SouthernWall_404.Painter.API.Paint.Util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class CommonUtil {


    public static BlockPos vec32Pos(Vec3 vec3)
    {
        return new BlockPos((int)Math.floor(vec3.x),(int)Math.floor(vec3.y),(int)Math.floor(vec3.z));
    }

    public static Vec3 pos2Vec3(BlockPos pos)
    {
        return new Vec3(pos.getX(),pos.getY(),pos.getZ());
    }
    /**
     * 获取direction是否是正轴
     * @param direction
     * @return true 表示正轴方向（EAST/UP/SOUTH），false 表示负轴方向（WEST/DOWN/NORTH）
     */
    public static boolean isPositiveAxis(Direction direction) {
        return direction.getStepX() == 1 || direction.getStepY() == 1 || direction.getStepZ() == 1;
    }
    /**
     * 用于获取垂直于两方向的两个Direction
     * 结果列表中第一位为正轴的Direction，第二位为负轴
     */
    public static List<Direction> getOtherDirection(Direction x, Direction b) {
        int x1 = x.getStepX(), y1 = x.getStepY(), z1 = x.getStepZ();
        int x2 = b.getStepX(), y2 = b.getStepY(), z2 = b.getStepZ();

        // 计算叉积 (右手定则：x × b)
        int cx = y1 * z2 - z1 * y2;
        int cy = z1 * x2 - x1 * z2;
        int cz = x1 * y2 - y1 * x2;

        // 叉积为零说明两个方向平行，非法
        if (cx == 0 && cy == 0 && cz == 0) {
            throw new IllegalArgumentException("The two directions are parallel, cannot determine perpendicular directions.");
        }

        // 查找叉积向量对应的 Direction
        Direction direction1 = null;
        for (Direction dir : Direction.values()) {
            if (dir.getStepX() == cx && dir.getStepY() == cy && dir.getStepZ() == cz) {
                direction1 = dir;
                break;
            }
        }

        if (direction1 == null) {
            throw new IllegalStateException("Unexpected cross product result: (" + cx + "," + cy + "," + cz + ")");
        }

        List<Direction> result = new ArrayList<>();
        // 确保第一位是正轴方向，第二位是负轴方向
        if (isPositiveAxis(direction1)) {
            result.add(direction1);               // 正轴
            result.add(direction1.getOpposite()); // 负轴
        } else {
            result.add(direction1.getOpposite()); // 正轴（原方向的相反方向）
            result.add(direction1);               // 负轴
        }
        return result;
    }
    /**
     * 按右手定则方式，获取垂直的四个方向
     * @param direction
     * @return
     */
    public static List<Direction> getTanDir(Direction direction) {
        List<Direction> result = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            // 计算点积，若为0则垂直
            int dot = direction.getStepX() * dir.getStepX()
                    + direction.getStepY() * dir.getStepY()
                    + direction.getStepZ() * dir.getStepZ();
            if (dot == 0) {
                result.add(dir);
            }
        }
        return result;
    }

    public static List<Direction.Axis> getTanAxis(Direction direction)
    {
        return getTanAxis(direction.getAxis());
    }

    /**
     * 获取垂直的轴
     * @return
     */
    public static List<Direction.Axis> getTanAxis(Direction.Axis axis) {
        List<Direction.Axis> result = new ArrayList<>();
        for (Direction.Axis a : Direction.Axis.values()) {
            if (a != axis) {
                result.add(a);
            }
        }
        return result;
    }


    /**
     * 按右手定则方式，获取垂直的法向量
     * @return
     */
    public static List<int[]> getTanNormal(int[] normal) {
        List<int[]> result = new ArrayList<>();
        // 六个标准方向对应的向量
        for (Direction dir : Direction.values()) {
            int x = dir.getStepX();
            int y = dir.getStepY();
            int z = dir.getStepZ();
            int dot = normal[0] * x + normal[1] * y + normal[2] * z;
            if (dot == 0) {
                result.add(new int[]{x, y, z});
            }
        }
        return result;
    }
}
