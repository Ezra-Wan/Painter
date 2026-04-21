package com.SouthernWall_404.Painter.API.Paint.Imply;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.PaintContent;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

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


    public SimpleBlockPaint() {
        super( PaintContent.SIMPLE_BLOCK);
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
        for(Map.Entry<Integer,BlockState> entry:materials.entrySet())
        {
            int flag=entry.getKey();
            BlockState material=entry.getValue();

            Direction direction=getDirection(flag);

            List<BakedQuad> quads = getQuadsForDirection(material,direction);

            objects.put(flag, quads);

        }
    }
}