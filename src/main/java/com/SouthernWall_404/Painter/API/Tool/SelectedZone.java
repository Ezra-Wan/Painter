package com.SouthernWall_404.Painter.API.Tool;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.Quad.Quad;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.RenderHelper;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.Configs;
import com.SouthernWall_404.Painter.API.Tool.Wall.Edge;
import com.SouthernWall_404.Painter.API.Tool.Wall.Squad;
import com.SouthernWall_404.Painter.Client.Config.ClientConfig;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

public class SelectedZone {

    //TODO 需要移除
    //========等待弃用========
    private Direction face;



    //=========数据缓存========
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
//    private final static float offset=0.02f;
//    public void addAPosition(BlockPos pos) {
//        if(!quads.containsKey(pos)){
//            float[][] positions= RenderHelper.getSimpleQuadVertex(face);
//            // 使用ClientConfig的静态方法获取颜色值
//            ModConfigSpec.ConfigValue colorValue=Configs.getValue(Painter.MODID,ClientConfig.QUAD_COLOR);
//            ModConfigSpec.ConfigValue alphaValue=Configs.getValue(Painter.MODID,ClientConfig.QUAD_COLOR_ALPHA);
//            quads.put(
//                    pos,
//                    Quad.builder()
//                            .setColor(FastColor.ARGB32.color((int)alphaValue.get(),(int)colorValue.get()))
//                            .addVertex(new Vector3f(positions[0]).add(new Vector3f(face),offset))
//                            .addVertex(new Vector3f(positions[1]).add(new Vector3f(face),offset))
//                            .addVertex(new Vector3f(positions[2]).add(new Vector3f(face),offset))
//                            .addVertex(new Vector3f(positions[3]).add(new Vector3f(face),offset))
//                            .build()
//            );
//
//        }
//    }

    public Set<BlockPos> getPositions() {
        return quads.keySet();
    }

    public boolean isSelecting()
    {
        if(!quads.isEmpty())
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

    public boolean contains(BlockPos pos)
    {
        return quads.containsKey(pos);
    }


    public void clear()
    {
//        A=null;
        squads.clear();
        cacheSquad=null;
        edges.clear();
        quads.clear();


    }
}
