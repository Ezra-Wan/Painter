package com.SouthernWall_404.Painter.API.Paint;

import com.SouthernWall_404.Painter.Painter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public abstract class AbstractPaint extends AbstractRender<List<BakedQuad>,Direction>{


    //========构造方法=========
    public AbstractPaint(BlockState origin,String type) {
        super(origin,type);
    }

    //========内部方法========

    @Override
    protected void update() {
        for(Map.Entry<Integer,ResourceLocation> entry:paintPaths.entrySet())
        {
            int flag=entry.getKey();

            Direction direction= getDirection(flag);

            Block block=RenderUtil.getBlockFromID(paintPaths.get(flag));
            List<BakedQuad> quads = getQuadsForDirection(block, direction);

            objects.put(flag, quads);


        }
    }

    protected void renderQuadsWithAO(Level level, BlockState state, BlockPos pos, List<BakedQuad> quads,
                                   VertexConsumer consumer, ModModelRender modRenderer,
                                   float[] shape, BitSet shapeFlags, ModModelRender.AmbientOcclusionFace aoFace,
                                   PoseStack poseStack, int packedOverlay) {
        for (BakedQuad quad : quads) {
            modRenderer.calculateShape(level, state, pos, quad.getVertices(), quad.getDirection(), shape, shapeFlags);

            aoFace.calculate(level, state, pos, quad.getDirection(), shape, shapeFlags, quad.isShade());
            modRenderer.putQuadData(level, state, pos, consumer, poseStack.last(), quad,
                    aoFace.brightness[0], aoFace.brightness[1], aoFace.brightness[2], aoFace.brightness[3],
                    aoFace.lightmap[0], aoFace.lightmap[1], aoFace.lightmap[2], aoFace.lightmap[3],
                    packedOverlay);
        }
    }
    public Block getPaintedBlock(Direction direction)
    {
        int flag=getFlag(direction);

        Block block=RenderUtil.getBlockFromID(paintPaths.get(flag));

        return block;
    }

    private Direction getDirection(int flag)
    {
        for(Map.Entry<Direction,Integer> entry:flags.entrySet())
        {
            int current=entry.getValue();
            if(current==flag)
            {
                return entry.getKey();
            }


        }
        return Direction.NORTH;
    }

    public List<BakedQuad> getQuadsForDirection(Block block, Direction direction) {
        // 1. 获取 Block 的默认状态 (如果你需要特定状态，可以传入相应的 BlockState)
        BlockState state = block.defaultBlockState();

        // 2. 获取 ModelManager 并得到这个 BlockState 对应的 BakedModel
        //    注意：这段代码必须在客户端执行，因为 ModelManager 只在客户端存在。
        BakedModel model =
        Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
        // 3. 创建一个 RandomSource 实例，用于需要随机化的模型（如有些方块的多重变体）
        //    通常情况下，如果你不需要随机化，可以使用 RandomSource.create(0) 或类似方法。
        RandomSource random = RandomSource.create();

    // 4. 调用 getQuads 方法，传入我们想要的方向
    //    这样就能拿到该方向上的所有 Quad
    RenderType renderType = RenderUtil.getRenderType(state);
    List<BakedQuad> quadsForDirection = model.getQuads(state, direction, random, ModelData.EMPTY,renderType);

        return quadsForDirection;
}
//========业务方法========


public static TextureAtlasSprite getTexture(ResourceLocation key)
{
    return  Minecraft.getInstance()
            .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
            .apply(key);
}



}
