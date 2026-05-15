package com.SouthernWall_404.Painter.API.Tool.Wall;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Set;
import java.util.HashSet;

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
        int minX = Math.min(A.getX(), B.getX());
        int maxX = Math.max(A.getX(), B.getX());
        int minY = Math.min(A.getY(), B.getY());
        int maxY = Math.max(A.getY(), B.getY());
        int minZ = Math.min(A.getZ(), B.getZ());
        int maxZ = Math.max(A.getZ(), B.getZ());
            
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
