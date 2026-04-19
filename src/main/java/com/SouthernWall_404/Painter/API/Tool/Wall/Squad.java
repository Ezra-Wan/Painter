package com.SouthernWall_404.Painter.API.Tool.Wall;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.Painter.API.Paint.Util.CommonUtil;
import com.SouthernWall_404.Painter.API.Tool.ToolContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Squad {

    public BlockPos A;
    public BlockPos B;
    public boolean isActive;
    public Direction face;

    private List<Edge> edges=new ArrayList<>();

    private BlockPos minPos;   // 最小坐标点
    private BlockPos maxPos;   // 最大坐标点

    private int minX;
    private int maxX;
    private int minY;
    private int maxY;
    private int minZ;
    private int maxZ;

    public Squad(BlockPos a,Direction face)
    {
        this(a,a,face);
    }

    public Squad(BlockPos a, BlockPos b, Direction face) {
        this(a, b, face, true);
    }

    public Squad(BlockPos a, BlockPos b, Direction face, boolean isActive) {
        A = a;
        B = b;
        this.isActive = isActive;
        this.face = face;

        // 计算最大最小坐标并存储为 BlockPos
        int minX = Math.min(a.getX(), b.getX());
        int maxX = Math.max(a.getX(), b.getX());
        int minY = Math.min(a.getY(), b.getY());
        int maxY = Math.max(a.getY(), b.getY());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxZ = Math.max(a.getZ(), b.getZ());

        minPos = new BlockPos(minX, minY, minZ);
        maxPos = new BlockPos(maxX, maxY, maxZ);
    }

    /**
     * 判断指定坐标是否位于当前矩形区域内（包含边界）
     */
    public boolean contains(BlockPos pos) {
        if (pos == null) return false;
        return pos.getX() >= minPos.getX() && pos.getX() <= maxPos.getX() &&
                pos.getY() >= minPos.getY() && pos.getY() <= maxPos.getY() &&
                pos.getZ() >= minPos.getZ() && pos.getZ() <= maxPos.getZ();
    }

    // 可选：提供 getter 方法以便外部获取边界
    public BlockPos getMinPos() { return minPos; }
    public BlockPos getMaxPos() { return maxPos; }

    public Direction getFace() {
        return face;
    }

    public void setA(BlockPos a) {
        A = a;
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
    public String setB(BlockPos b) {

        String result=isValid(b);

        if(!ToolContent.PASS.equals(result))
        {
            return result;
        }
        B = b;

        // 计算最大最小坐标
        minX = Math.min(A.getX(), b.getX());
        maxX = Math.max(A.getX(), b.getX());
        minY = Math.min(A.getY(), b.getY());
        maxY = Math.max(A.getY(), b.getY());
        minZ = Math.min(A.getZ(), b.getZ());
        maxZ = Math.max(A.getZ(), b.getZ());

        minPos = new BlockPos(minX, minY, minZ);
        maxPos = new BlockPos(maxX, maxY, maxZ);

        return ToolContent.PASS;
    }


    public List<BlockPos> getContians() {


        String valid = isValid();
        if (!ToolContent.PASS.equals(valid)) {
            return new ArrayList<>();
        }

        List<BlockPos> result = new ArrayList<>();
        Direction.Axis fixedAxis = face.getAxis();
        int fixedCoord = A.get(fixedAxis); // A 和 B 在此轴上相等

        // 确定两个变化轴
        Direction.Axis axis1, axis2;
        switch (fixedAxis) {
            case X:
                axis1 = Direction.Axis.Y;
                axis2 = Direction.Axis.Z;
                break;
            case Y:
                axis1 = Direction.Axis.X;
                axis2 = Direction.Axis.Z;
                break;
            case Z:
                axis1 = Direction.Axis.X;
                axis2 = Direction.Axis.Y;
                break;
            default:
                return result;
        }

        // 获取变化轴上的范围
        int min1 = Math.min(A.get(axis1), B.get(axis1));
        int max1 = Math.max(A.get(axis1), B.get(axis1));
        int min2 = Math.min(A.get(axis2), B.get(axis2));
        int max2 = Math.max(A.get(axis2), B.get(axis2));

        // 遍历所有整数坐标
        for (int i = min1; i <= max1; i++) {
            for (int j = min2; j <= max2; j++) {
                BlockPos pos;
                switch (fixedAxis) {
                    case X:
                        pos = new BlockPos(fixedCoord, i, j);
                        break;
                    case Y:
                        pos = new BlockPos(i, fixedCoord, j);
                        break;
                    case Z:
                        pos = new BlockPos(i, j, fixedCoord);
                        break;
                    default:
                        continue;
                }
                result.add(pos);
            }
        }
        return result;
    }

    public String isValid()
    {
        if(this.A==null)
        {
            return ToolContent.EMPTY_POSA;
        }
        if(this.B==null)
        {
            return ToolContent.EMPTY_POSB;
        }
        if(this.face==null)
        {
            return ToolContent.EMPTY_FACE;
        }
        return ToolContent.PASS;
    }

    /**
     * 结合face,获取某一方向上的所有Edge
     * @param edgeDirection
     * @return
     */
    public List<Edge> getDirectionEdge(List<BlockPos> cover,Direction edgeDirection)
    {

        List<Edge> result=new ArrayList<>();

        if(edgeDirection.getAxis()==face.getAxis())
        {
            throw new IllegalArgumentException("Invalid EdgeDirection");
        }

        int facePos=minPos.get(face.getAxis());//Squad定位坐标
        int edgePos;//Edge定位坐标
        int startPos;//Edge纵向方向起始点定位坐标
        int endPos;//Edge纵向方向终止坐标


        //获取edge定位坐标
        Direction.Axis edgeDirectionAxis=edgeDirection.getAxis();
        if(CommonUtil.isPositiveAxis(edgeDirection))
        {
            edgePos=maxPos.get(edgeDirectionAxis);//正向轴，则给最大值
        }else {
            edgePos=minPos.get(edgeDirectionAxis);//负向轴，则给最小值
        }

        List<Direction> posDirections=CommonUtil.getOtherDirection(face,edgeDirection);
        Direction posDirection=posDirections.get(0);

        //处理edge纵向方向坐标情况
        Direction.Axis posAxis=posDirection.getAxis();
        startPos=minPos.get(posAxis);
        endPos=maxPos.get(posAxis);

        BlockPos start=new BlockPos(0,0,0).relative(face.getAxis(),facePos).relative(edgeDirectionAxis,edgePos).relative(posAxis,startPos);

        for(int i=startPos;i<=endPos;i++)
        {
            //建立当前所在的BlockPos
            BlockPos current=new BlockPos(0,0,0);
            current=current.relative(face.getAxis(),facePos);
            current=current.relative(edgeDirection.getAxis(),edgePos);
            current=current.relative(posAxis,i);

            BlockPos relative=current.relative(edgeDirection);//获取跨边缘的另一个方块的情况

            if(cover.contains(relative)!=cover.contains(current))//如果只有一个被包含，即可以判定为边框的情况
            {
                //则其为边内

                if(i==endPos)//如果抵达终点
                {
                    BlockPos displayStart=start;
                    BlockPos displayEnd=current.relative(posDirection);
                    if(CommonUtil.isPositiveAxis(edgeDirection))
                    {
                        displayStart=displayStart.relative(edgeDirection);
                        displayEnd=displayEnd.relative(edgeDirection);
                    }//轴定位正轴补偿

                    if(CommonUtil.isPositiveAxis(face))
                    {
                        displayStart=displayStart.relative(face);
                        displayEnd=displayEnd.relative(face);
                    }//面定位正轴补偿

                    Edge edge=new Edge(
                            new Vector3f(displayStart),
                            new Vector3f(displayEnd)
                    );
                    result.add(edge);
                }

                continue;
            }
            else//如果均包含或均不包含，即非边框的情况
            {
                //则需要考虑建立Edge
                if(startPos!=i)//如果与目前最近的出发点不重叠
                {
                    BlockPos displayStart=start;
                    BlockPos displayEnd=current;
                    if(CommonUtil.isPositiveAxis(edgeDirection))
                    {
                        displayStart=displayStart.relative(edgeDirection);
                        displayEnd=displayEnd.relative(edgeDirection);

                    }
                    if(CommonUtil.isPositiveAxis(face))
                    {
                        displayStart=displayStart.relative(face);
                        displayEnd=displayEnd.relative(face);
                    }
                    Edge edge=new Edge(
                            new Vector3f(displayStart),
                            new Vector3f(displayEnd)
                    );
                    result.add(edge);
                }

                startPos=i+1;//startPos前移
                start=new BlockPos(0,0,0).relative(face.getAxis(),facePos).relative(edgeDirectionAxis,edgePos).relative(posAxis,startPos);

            }

        }
        return result;
    }




    /**
     *
     * @param cover
     * @return
     */
    public List<Edge> getEdges(List<BlockPos> cover)
    {
        List<Edge> result=new ArrayList<>();

        List<Direction> directions=CommonUtil.getTanDir(face);
        for(Direction direction:directions)
        {
            List<Edge> directionEdges=getDirectionEdge(cover,direction);
            result.addAll(directionEdges);
        }
        return result;
    }
}