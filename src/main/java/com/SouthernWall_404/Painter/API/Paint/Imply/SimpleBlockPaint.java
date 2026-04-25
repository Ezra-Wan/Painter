package com.SouthernWall_404.Painter.API.Paint.Imply;

import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticeInfo;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.PaintContent;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimpleBlockPaint extends AbstractPaint {

    //======== Flag 定义，与 Direction 绑定 ========
    private static final int NORTH = 1;
    private static final int SOUTH = 2;
    private static final int WEST  = 4;
    private static final int EAST  = 8;
    private static final int UP    = 16;
    private static final int DOWN  = 32;


    public SimpleBlockPaint(BlockPos blockPos) {
        super(blockPos, PaintContent.SIMPLE_BLOCK);
    }

    @Override
    public Direction getDirectionForEmpty() {
        return Direction.UP;
    }

    @Override
    public void initFlags() {
        registerFlag(Direction.NORTH, NORTH);
        registerFlag(Direction.SOUTH, SOUTH);
        registerFlag(Direction.WEST,  WEST);
        registerFlag(Direction.EAST,  EAST);
        registerFlag(Direction.UP,    UP);
        registerFlag(Direction.DOWN,  DOWN);
    }

    @Override
    public void createQuads() {
        materials.forEach((flag, material) -> {
            Direction direction = getDirection(flag);
            List<BakedQuad> originQuads = getQuadsForDirection(origin, direction);
            List<BakedQuad> materialQuads = getQuadsForDirection(material, direction);

            if (originQuads == null || originQuads.isEmpty() || materialQuads == null || materialQuads.isEmpty()) {
                return;
            }

            // 为了简单，假设每个方向只有一个 quad（多数方块模型如此）；
            // 如果有多个，你可以按索引一一对应，或统一使用第一个 originQuad 的形状。
            BakedQuad originQuad = originQuads.get(0);
            VerticesInfo originVertices = new VerticesInfo(originQuad);

            List<BakedQuad> newQuads = new ArrayList<>();

            for (BakedQuad materialQuad : materialQuads) {
                VerticesInfo materialVertices = new VerticesInfo(materialQuad);

                // 逐顶点混合数据
                VerticeInfo[] mixedVertices = new VerticeInfo[4];
                for (int i = 0; i < 4; i++) {
                    VerticeInfo originVert = originVertices.vertices.get(i);
                    VerticeInfo materialVert = materialVertices.vertices.get(i);

                    mixedVertices[i] = VerticeInfo.builder()
                            .position(originVert.position)          // 几何位置来自原始方块
                            .color(materialVert.alpha, materialVert.red, materialVert.green, materialVert.blue) // 颜色来自原始方块
                            .uv(materialVert.u, materialVert.v)     // ★ 纹理坐标来自材质方块 ★
                            .light(originVert.light)                // 光照值保持原始方块（或可根据需要混合）
                            .normal(originVert.normal)              // 法线保持原始方块
                            .build();
                }

                VerticesInfo newQuadVertices = new VerticesInfo(List.of(mixedVertices));
                int[] newVertexArray = newQuadVertices.vertices();

                // 使用 materialQuad 的元数据创建新的 BakedQuad
                BakedQuad newQuad = new BakedQuad(
                        newVertexArray,
                        materialQuad.getTintIndex(),
                        materialQuad.getDirection(),
                        materialQuad.getSprite(),
                        true
                );

                newQuads.add(newQuad);
            }

            objects.put(flag, newQuads);   // 存入结果，objects 应为 Map<Integer, List<BakedQuad>>
        });

//        for (Map.Entry<Integer, BlockState> entry : materials.entrySet()) {
//            int flag = entry.getKey();
//            BlockState material = entry.getValue();
//
//            Direction direction = getDirection(flag);
//
//            List<BakedQuad> quads = getQuadsForDirection(material, direction);
//
//            objects.put(flag, quads);
//
//        }
    }

//        for (Map.Entry<Integer, BlockState> entry : materials.entrySet()) {
//            int flag = entry.getKey();
//            BlockState material = entry.getValue();
//
//            Direction direction = getDirection(flag);
//
//            List<BakedQuad> quads = getQuadsForDirection(material, direction);
//
//            objects.put(flag, quads);
//
//        }
//    }

}