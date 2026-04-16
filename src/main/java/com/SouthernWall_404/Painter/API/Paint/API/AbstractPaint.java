package com.SouthernWall_404.Painter.API.Paint.API;

import com.SouthernWall_404.Painter.API.Paint.ModModelRender;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuadRender;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.BitSet;
import java.util.List;
import java.util.Map;


/**
 * Quad Vertice顺序 留档备用
 * 顺序：左上，左下，右下，右上
 */

public abstract class AbstractPaint extends AbstractRender<List<BakedQuad>,Direction>{


    //========构造方法=========
    public AbstractPaint(String type) {
        super(type);
    }

    //========内部方法========
    @OnlyIn(Dist.CLIENT)
    @Override
    protected void update() {

        if(Minecraft.getInstance()==null)
        {
            return;
        }

        createQuads();

        super.update();
    }
    public void cycleTextureUV(Direction direction)
    {

    }

    public void cycleTextureDir(Direction direction)
    {
        BlockState material=getMaterial(direction);

        if (material.hasProperty(TrapDoorBlock.HALF) && material.getOptionalValue(TrapDoorBlock.OPEN)
                .orElse(false))
            setMaterial(direction,material.cycle(TrapDoorBlock.HALF));
        else if (material.hasProperty(BlockStateProperties.FACING))
            setMaterial(direction,material.cycle(BlockStateProperties.FACING));
        else if (material.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
            setMaterial(direction,material.setValue(BlockStateProperties.HORIZONTAL_FACING,
                    material.getValue(BlockStateProperties.HORIZONTAL_FACING)
                            .getClockWise()));
        else if (material.hasProperty(BlockStateProperties.AXIS))
            setMaterial(direction,material.cycle(BlockStateProperties.AXIS));
        else if (material.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
            setMaterial(direction,material.cycle(BlockStateProperties.HORIZONTAL_AXIS));
        else if (material.hasProperty(BlockStateProperties.LIT))
            setMaterial(direction,material.cycle(BlockStateProperties.LIT));

        super.update();
    }

    public List<BakedQuad> getQuadsForDirection(BlockState state, Direction direction) {
        BakedModel model = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
        RandomSource random = RandomSource.create();
        RenderType renderType = RenderUtil.getRenderType(state);
        return model.getQuads(state, direction, random, ModelData.EMPTY, renderType);
    }

    protected void renderQuadsWithAO(Level level, BlockState state, BlockPos pos, List<BakedQuad> quads,
                                   VertexConsumer consumer, ModModelRender modRenderer,
                                   float[] shape, BitSet shapeFlags, ModModelRender.AmbientOcclusionFace aoFace,
                                   PoseStack poseStack, int packedOverlay) {
        if(state==null)
        {
            return;
        }
        for (BakedQuad quad : quads) {
            modRenderer.calculateShape(level, state, pos, quad.getVertices(), quad.getDirection(), shape, shapeFlags);

            //TODO 这里有没有可能将光照数据直接移植过来？考虑本质上是覆盖层
            aoFace.calculate(level, state, pos, quad.getDirection(), shape, shapeFlags, quad.isShade());
            modRenderer.putQuadData(level, state, pos, consumer, poseStack.last(), quad,
                    aoFace.brightness[0], aoFace.brightness[1], aoFace.brightness[2], aoFace.brightness[3],
                    aoFace.lightmap[0], aoFace.lightmap[1], aoFace.lightmap[2], aoFace.lightmap[3],
                    packedOverlay);
        }
    }

    /**
     * 
     * @param flag
     * @return
     */
    public Direction getDirection(int flag)
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



    @Override
    protected void serializeF(Direction direction, CompoundTag tag, String key) {
        tag.putString(key, direction.getName());
    }

    @Override
    protected Direction deserializeF(CompoundTag tag, String key) {
        return Direction.byName(tag.getString(key));
    }

    public abstract void createQuads();


    @Override
    public void render(BlockPos blockPos, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int packedLight, int packedOverlay, float partialTick) {

        for(Map.Entry<Integer,List<BakedQuad>> entry:objects.entrySet())//遍历所有缓存的quad
        {
            List<BakedQuad> quads=entry.getValue();
            int flag=entry.getKey();
            double offset=0.001;

            BlockState state=materials.get(flag);


            Direction direction=getDirection(flag);
            Vec3i normal=direction.getNormal();


            Vec3 vec3=new Vec3(blockPos.getX()+offset*normal.getX(),blockPos.getY()+offset*normal.getY(),blockPos.getZ()+offset*normal.getZ());
            for(BakedQuad quad:quads)
            {

                BakedQuadRender.renderWithAO(quad,state,vec3,poseStack,RenderType.CUTOUT,bufferSource,new ModModelRender.AmbientOcclusionFace());
            }

        }
        bufferSource.endBatch(RenderType.CUTOUT);

    }


    //========业务方法========


    public static TextureAtlasSprite getTexture(ResourceLocation key)
    {
        return  Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(key);
    }



}
