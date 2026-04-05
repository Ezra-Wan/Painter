package com.SouthernWall_404.Painter.API.Tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayList;
import java.util.List;

public class SelectedZone {

    private List<BlockPos> selected = new ArrayList<>();
    private Direction face;


    //待移除
    private BlockPos A;


    public SelectedZone(){

    }


    public Direction getFace() {
        return face;
    }

    public List<BlockPos> getSelected() {
        return selected;
    }

    public void addAPosition(BlockPos pos) {
        if(!selected.contains(pos)) selected.add(pos);
        //TODO:添加对渲染可见性的检验
    }

    public void removeAPosition(BlockPos pos)
    {
        if(selected.contains(pos))selected.remove(pos);
    }

    public boolean isSelecting()
    {
        if(!selected.isEmpty()&&face!=null)
        {
            return true;
        }
        return false;
    }

    public boolean isInSurface(BlockPos pos,Direction direction)
    {
        return direction==face&&selected.contains(pos);
    }

    public String isValid(BlockPos b)
    {
        if(this.face==null||this.A==null)
        {
            return ToolContent.EMPTY_POSA;
        }
        Direction.Axis axis=face.getAxis();
        if(this.A.get(axis)==b.get(axis))//若为同层
        {
            return ToolContent.PASS;
        }else {//不为同层
            return ToolContent.NOT_IN_SURFACE;
        }
    }

    public void setA(BlockPos a, Direction face)
    {
        this.A=a;
        this.face=face;

        //TODO:添加对已有选区的清除
    }
    public String setB(BlockPos b) {
        String result = isValid(b);
        if (!ToolContent.PASS.equals(result)) {
            return result;
        }

        // 法向量（轴向单位向量）
        Vec3i n = face.getNormal();
        // 获取两个垂直于 n 的基向量
        Vec3i u = getPerpendicularU(n);
        Vec3i v = getPerpendicularV(n);

        // 平面上的恒定坐标值（即 A 在法向量上的投影）
        int constCoord = dot(A, n);

        // A 和 b 在 (u, v) 坐标系下的坐标
        int a_u = dot(A, u);
        int a_v = dot(A, v);
        int b_u = dot(b, u);
        int b_v = dot(b, v);

        int minU = Math.min(a_u, b_u);
        int maxU = Math.max(a_u, b_u);
        int minV = Math.min(a_v, b_v);
        int maxV = Math.max(a_v, b_v);

        // 遍历矩形区域
        for (int du = minU; du <= maxU; du++) {
            for (int dv = minV; dv <= maxV; dv++) {
                BlockPos pos = new BlockPos(
                        constCoord * n.getX() + du * u.getX() + dv * v.getX(),
                        constCoord * n.getY() + du * u.getY() + dv * v.getY(),
                        constCoord * n.getZ() + du * u.getZ() + dv * v.getZ()
                );
                addAPosition(pos);
            }
        }

        return ToolContent.PASS;
    }

    // 点积
    private int dot(BlockPos p, Vec3i vec) {
        return p.getX() * vec.getX() + p.getY() * vec.getY() + p.getZ() * vec.getZ();
    }

    // 根据法向量 n 返回第一个垂直基向量
    private Vec3i getPerpendicularU(Vec3i n) {
        if (n.getX() != 0) return new Vec3i(0, 1, 0);
        if (n.getY() != 0) return new Vec3i(1, 0, 0);
        return new Vec3i(1, 0, 0); // n.getZ() != 0
    }

    // 第二个垂直基向量（保证与 n 和 u 都垂直）
    private Vec3i getPerpendicularV(Vec3i n) {
        if (n.getX() != 0) return new Vec3i(0, 0, 1);
        if (n.getY() != 0) return new Vec3i(0, 0, 1);
        return new Vec3i(0, 1, 0); // n.getZ() != 0
    }

    public boolean contains(BlockPos pos)
    {
        return selected.contains(pos);
    }

    public void clear()
    {
        A=null;
        face=null;
        selected.clear();


    }
}
