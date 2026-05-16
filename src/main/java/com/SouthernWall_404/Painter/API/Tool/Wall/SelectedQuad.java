package com.SouthernWall_404.Painter.API.Tool.Wall;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.OutLine.Line;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.Configs;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Util.CommonUtil;
import com.SouthernWall_404.Painter.Client.Config.ClientConfig;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

public class SelectedQuad{

    public BlockPos A;
    public BlockPos B;
    public Direction face;

    public SelectedQuad(BlockPos A, BlockPos B, Direction  face){
        this.A = A;
        this.face = face;
        this.B=B;
        if(isValid()){
            return;
        }
        else throw new IllegalArgumentException("Invalid SelectedQuad");
    }

    /**
     * 用于检查当前SelectedQuad是否合法
     * 两个BlockPos应当在face向量定义的同一平面
     * 两个BlockPos均应当存在
     */
    public boolean isValid(){
        return isSameSurface(A,B,face);
    }

    public static boolean isSameSurface(BlockPos A,BlockPos B,Direction face){
        if (A == null || B == null || face == null) {
            return false;
        }

        Direction.Axis axis = face.getAxis();
        // 根据 face 方向检查两个点是否在同一平面
        return A.get( axis)== B.get(axis);
    }

    /**
     * 计算并返回最小坐标点
     * @return 包含最小X、Y、Z坐标的BlockPos
     */
    public BlockPos getMinPos() {
        return new BlockPos(
            Math.min(A.getX(), B.getX()),
            Math.min(A.getY(), B.getY()),
            Math.min(A.getZ(), B.getZ())
        );
    }

    /**
     * 计算并返回最大坐标点
     * @return 包含最大X、Y、Z坐标的BlockPos
     */
    public BlockPos getMaxPos() {
        return new BlockPos(
            Math.max(A.getX(), B.getX()),
            Math.max(A.getY(), B.getY()),
            Math.max(A.getZ(), B.getZ())
        );
    }

    //========业务方法=========

    /**
     * 用于获取选中的方块
     * @return 选中区域内所有方块位置的集合
     */
    public Set<BlockPos> getSelectedPos(){
        Set<BlockPos> result = new HashSet<>();
            
        if (!isValid()) {
            return result;
        }
            
        // 计算两个点的最小和最大坐标
        BlockPos minPos = getMinPos();
        BlockPos maxPos = getMaxPos();
        
        int minX = minPos.getX();
        int maxX = maxPos.getX();
        int minY = minPos.getY();
        int maxY = maxPos.getY();
        int minZ = minPos.getZ();
        int maxZ = maxPos.getZ();
            
        // 根据 face 方向，只遍历对应平面上的方块
        switch (face) {
            case NORTH, SOUTH -> {
                // Z 坐标固定，遍历 X 和 Y
                int fixedZ = A.getZ();
                for (int x = minX; x <= maxX; x++) {
                    for (int y = minY; y <= maxY; y++) {
                        result.add(new BlockPos(x, y, fixedZ));
                    }
                }
            }
            case EAST, WEST -> {
                // X 坐标固定，遍历 Y 和 Z
                int fixedX = A.getX();
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        result.add(new BlockPos(fixedX, y, z));
                    }
                }
            }
            case UP, DOWN -> {
                // Y 坐标固定，遍历 X 和 Z
                int fixedY = A.getY();
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        result.add(new BlockPos(x, fixedY, z));
                    }
                }
            }
        }
            
        return result;
    }
}
