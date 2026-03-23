package com.SouthernWall_404.Painter.API.Tool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectedZone{

    private BlockPos A;
    private BlockPos B;
    private Direction face;

    public SelectedZone(){

    }

    public SelectedZone(BlockPos a, BlockPos b, Direction face) {
        A = a;
        B = b;
        this.face = face;
    }

    public boolean isSelecting()
    {
        if(A!=null&&B!=null&&face!=null)
        {
            return true;
        }
        return false;
    }

    public boolean isInSurface(BlockPos pos,Direction direction)
    {

        if(direction!=face)//如果不是同一朝向，不算
        {
            return false;
        }

        Direction.Axis axis=direction.getAxis();
        if(pos.get(axis)==A.get(axis))//是同一平面
        {
            if(A.distToCenterSqr(pos.getX(),pos.getY(),pos.getZ())<=30||
                    B.distToCenterSqr(pos.getX(),pos.getY(),pos.getZ())<=30||
                    contains(pos)
            )//检测范围向外延展30格
            {
                return true;
            }//超出则判定不在同一截面
        }

        return false;
    }

    public boolean isValid(BlockPos a, BlockPos b, Direction face)
    {
        if(face==null)
        {
            return false;
        }
        Direction.Axis axis=face.getAxis();
        if(a.get(axis)==b.get(axis))//若为同层
        {
            return true;
        }else {//不为同层
            return false;
        }
    }



    public BlockPos getA() {
        return A;
    }

    public BlockPos getB() {
        return B;
    }

    public Direction getFace() {
        return face;
    }

    public void setA(BlockPos a, Direction face)
    {
        this.A=a;
        this.face=face;
    }

    public boolean setB(BlockPos b)
    {
        if(isValid(this.A,b,this.face))
        {
            this.B=b;

            return true;//表示设置成功
        }
        return false;//表示设置失败
    }

    public boolean contains(BlockPos pos)
    {
        if(face!=null&&A!=null&&B!=null)
        {
            AABB aabb=new AABB(A.getX(),A.getY(),A.getZ(),B.getX(),B.getY(),B.getZ());
//            boolean contain=aabb.contains(pos.getX(),pos.getY(),pos.getZ());

            int minX = Math.min(A.getX(), B.getX());
            int minY = Math.min(A.getY(), B.getY());
            int minZ = Math.min(A.getZ(), B.getZ());
            int maxX = Math.max(A.getX(), B.getX());
            int maxY = Math.max(A.getY(), B.getY());
            int maxZ = Math.max(A.getZ(), B.getZ());

            boolean contain=
                    pos.getX()>=minX&&
                    pos.getX()<=maxX&&
                    pos.getY()>=minY&&
                    pos.getY()<=maxY&&
                    pos.getZ()>=minZ&&
                    pos.getZ()<=maxZ;

            return contain;

        }else {

            System.out.println("missing elements for!");
            return false;
        }

    }

    public void clear()
    {
        A=null;
        B=null;
        face=null;
    }
}
