package com.SouthernWall_404.Painter.API.Tool;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.Quad.Quad;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.RenderHelper;
import com.SouthernWall_404.Painter.API.Tool.Wall.Edge;
import com.SouthernWall_404.Painter.API.Tool.Wall.Squad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectedZone {

    //TODO 可以考虑把Contains优化掉

    private List<BlockPos> contains = new ArrayList<>();
    private Direction face;

    private List<Squad> squads=new ArrayList<>();
    private Squad cacheSquad;

    //========渲染缓存========
    private List<Edge> cachedEdges=new ArrayList<>();
    private Map<BlockPos,Quad> cachedQuads=new HashMap<>();

    public SelectedZone(){

    }

    public Map<BlockPos, Quad> getCachedQuads() {
        return cachedQuads;
    }

    public Direction getFace() {
        return face;
    }

    public List<BlockPos> getContains() {
        return contains;
    }
    private final static float offset=0.011f;
    public void addAPosition(BlockPos pos) {
        if(!contains.contains(pos)){
            float[][] positions= RenderHelper.getSimpleQuadVertex(face);
            contains.add(pos);
            cachedQuads.put(
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

    //TODO 可能需要修改
    public void removeAPosition(BlockPos pos)
    {
        if(contains.contains(pos)) {
            contains.remove(pos);
        }
    }

    public boolean isSelecting()
    {
        if(!contains.isEmpty()&&face!=null)
        {
            return true;
        }
        return false;
    }

    public List<Edge> getCachedEdges() {
        return cachedEdges;
    }

    public boolean isInSurface(BlockPos pos, Direction direction)
    {
        return direction==face&& contains.contains(pos);
    }

    public void update()
    {
        for(Squad squad:squads)
        {
            List<BlockPos> squadContains=squad.getContians();

            squadContains.forEach(
                    (blockpos)->{
                        if(!contains.contains(blockpos))
                        {
                            addAPosition(blockpos);
                        }
                    }
            );


        }


        //生成缓存边
        cachedEdges.clear();
        for(Squad squad:squads)
        {
            cachedEdges.addAll(squad.getEdges(contains));
        }

        cachedEdges.isEmpty();

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
        return contains.contains(pos);
    }

    public void clear()
    {
//        A=null;
        face=null;
        contains.clear();
        squads.clear();
        cacheSquad=null;
        cachedEdges.clear();
        cachedQuads.clear();


    }
}
