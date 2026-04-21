package com.SouthernWall_404.Painter.API.Tool.Wall;

import com.SouthernWall_404.Painter.API.Paint.Util.PaintUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class WallUtil {


    public static final String PAINT="paint";
    public static final String CYCLE="cycle";
    /**
     * 使用洪水算法，遍历四周相邻的每个垂直于face（喷涂面方向）的方块
     * 如果通过filter的check，喷涂
     * 一直到遍历到空气或距离初始点距离大于32格
     *
     * @param pos    初始喷涂位置
     * @param face   喷涂的面
     * @param level  世界
     * @param filter 过滤器，决定哪些方块可以被喷涂并继续扩散
     */

    public static void actOnWall(BlockPos pos, Direction face, Player player, Level level, IFilter filter,boolean isInMainHand) {
        actOnWall(pos,face,player,level,List.of(filter),isInMainHand);
    }

    //TODO 需要使整体喷涂的墙面呈现同一方向
    //TODO 优化油漆桶的算法和选区的算法
    public static void actOnWall(BlockPos pos, Direction face, Player player, Level level, List<IFilter> filters,boolean isInMainHand) {
        // 队列用于 BFS
        Queue<BlockPos> queue = new LinkedList<>();
        // 记录已处理过的方块，避免重复
        Set<BlockPos> visited = new HashSet<>();

        // 起始点入队
        queue.offer(pos);
        visited.add(pos);

        // 获取与 face 垂直的四个方向（即墙面内的四个方向）
        // 六个方向中，排除 face 本身和其相反方向，剩下的四个即为墙面内的方向
        List<Direction> wallDirs = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if (dir != face && dir != face.getOpposite()) {
                wallDirs.add(dir);
            }
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();

            // 检查当前方块是否在距离限制内（起始点切比雪夫距离 ≤ 32）
            if (Math.abs(current.getX() - pos.getX()) > 32 ||
                    Math.abs(current.getY() - pos.getY()) > 32 ||
                    Math.abs(current.getZ() - pos.getZ()) > 32) {
                continue; // 超出范围，不再扩散
            }

            // 获取当前方块的 BlockState
            BlockState currentState = level.getBlockState(current);
            // 检查当前方块是否符合喷涂条件


            boolean isAllowed=true;
            for(IFilter filter:filters)//遍历所有筛选项
            {
                if(!filter.check(current,player,face))//只要有一个不满足，则终止继续蔓延
                {
                    isAllowed=false;
                }
            }

            if(isAllowed)
            {
//                switch (actType){
//
//                    case "paint":
//
//                    default:
//                        break;
//                }
//                //如果所有筛选项均满足

                PaintUtil.dealWithPaintClick(level,current,player,face,isInMainHand);//执行喷涂

                // 向墙面内的四个方向扩散
                for (Direction wallDir : wallDirs) {
                    BlockPos neighbor = current.relative(wallDir);
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
                // 如果当前方块不符合条件，则不再从它向外扩散
            }

        }
    }

}