package com.SouthernWall_404.Painter.API.Tool;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.Quad.Quad;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.RenderHelper;
import com.SouthernWall_404.Painter.API.Tool.Wall.Edge;
import com.SouthernWall_404.Painter.API.Tool.Wall.Squad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.*;

public class SelectedZone {

    private Direction face;

    private Set<Squad> squads=new HashSet<>();
    private Squad cacheSquad;

    //========渲染缓存========
    private Set<Edge> edges =new HashSet<>();
    private Map<BlockPos,Quad> quads =new HashMap<>();

    public SelectedZone(){

    }

    public Map<BlockPos, Quad> getQuads() {
        return quads;
    }

    public Direction getFace() {
        return face;
    }
    private final static float offset=0.02f;
    public void addAPosition(BlockPos pos) {
        if(!quads.containsKey(pos)){
            float[][] positions= RenderHelper.getSimpleQuadVertex(face);
            quads.put(
                    pos,
                    Quad.builder()
                            .setColor(0x4cebe5d1)//TODO考虑允许配置项
                            .addVertex(new Vector3f(positions[0]).add(new Vector3f(face),offset))
                            .addVertex(new Vector3f(positions[1]).add(new Vector3f(face),offset))
                            .addVertex(new Vector3f(positions[2]).add(new Vector3f(face),offset))
                            .addVertex(new Vector3f(positions[3]).add(new Vector3f(face),offset))
                            .build()
            );
        }
    }

    public boolean isSelecting()
    {
        if(!quads.isEmpty()&&face!=null)
        {
            return true;
        }
        return false;
    }


    public Set<Edge> getEdges() {
        return edges;
    }

    public boolean isInSurface(BlockPos pos, Direction direction)
    {
        return direction==face&& quads.containsKey(pos);
    }

    public void update()
    {
        edges.clear();
        squads.forEach((squad -> {
            List<BlockPos> squadContains=squad.getContians();

            squadContains.forEach(
                    (blockpos)->{
                        addAPosition(blockpos);//将位置缓存
                    }
            );
        }));

        squads.forEach((squad)->{
            edges.addAll(squad.getEdges(quads));//更新边框
        });

    }
    public void addSquad(Squad squad)
    {
        squads.add(squad);

        update();
    }
    public void setA(BlockPos a, Direction face)
    {
        if(this.face==null)
        {
            this.face=face;
        }
        else {
            if(!this.squads.isEmpty()&&face!=this.getFace())
            {
                return;
            }
        }

        cacheSquad=new Squad(a,face);
    }
    public String setB(BlockPos b) {

        if(cacheSquad==null)
        {
            return ToolContent.EMPTY_POSA;
        }
        String result=cacheSquad.setB(b);

        if(result.equals(ToolContent.PASS))
        {
            addSquad(cacheSquad);

            cacheSquad=null;

            return result;
        }
        return result;
    }

    public boolean contains(BlockPos pos)
    {
        return quads.containsKey(pos);
    }

    public void clear()
    {
//        A=null;
        face=null;
        squads.clear();
        cacheSquad=null;
        edges.clear();
        quads.clear();


    }
}
