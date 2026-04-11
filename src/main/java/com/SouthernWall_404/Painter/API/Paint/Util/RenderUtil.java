package com.SouthernWall_404.Painter.API.Paint.Util;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Imply.SimpleBlockPaint;
import com.SouthernWall_404.Painter.Common.World.Block.PaintBlock;
import com.SouthernWall_404.Painter.Common.World.BlockEntity.PaintBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.List;

public class RenderUtil {

//    public static IRender addSimple(BlockState origin,)
private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
    Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>(2048, 0.25F) {
        protected void rehash(int newN) {
        }
    };
    object2bytelinkedopenhashmap.defaultReturnValue((byte)127);
    return object2bytelinkedopenhashmap;
});

    public static ResourceLocation getBlockKey(Block block)
    {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        // 将方块注册名转换为纹理路径，例如 "minecraft:iron_block" -> "minecraft:block/iron_block"


        return key;
    }

    public static ModelResourceLocation getModelKey(Block block)
    {
        ResourceLocation blockKey=getBlockKey(block);
        ResourceLocation modelLocation = ResourceLocation.fromNamespaceAndPath(
                blockKey.getNamespace(), "block/" + blockKey.getPath());

        ModelResourceLocation modelResourceLocation=ModelResourceLocation.inventory(blockKey);
        return modelResourceLocation;
    }

    public static TextureAtlasSprite getFaceFromBlock(BlockState state, Direction face)
    {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);

        RandomSource random = RandomSource.create(); // 可传入种子，通常用静态随机即可
        List<BakedQuad> quads = model.getQuads(state, face, random, ModelData.EMPTY, RenderType.solid());

        if (!quads.isEmpty()) {
            // 通常一个面可能包含多个四边形，这里取第一个作为代表
            BakedQuad quad = quads.get(0);
            return quad.getSprite(); // NeoForge 扩展方法
        }

        // 若该面无四边形，回退到粒子图标（常为默认面纹理）
        return model.getParticleIcon();
    }



    public static RenderType getRenderType(BlockGetter level,BlockPos pos)
    {
        BlockState blockState=level.getBlockState(pos);

        if(blockState.getBlock()instanceof PaintBlock)
        {
            blockState=getPaintBlockOrigin(level,pos);
        }

        return getRenderType(blockState);
    }

    /**
     * 获取方块状态对应的渲染类型
     * @param state 方块状态
     * @return 渲染类型，如果获取失败则返回 null
     */
    public static RenderType getRenderType(BlockState state) {

        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

            try {
                // 获取方块模型使用的渲染类型
                var model = dispatcher.getBlockModel(state);
                if(model==null){
                    return RenderType.solid();
                }
                var renderTypes = model.getRenderTypes(state,RandomSource.create(), ModelData.EMPTY);

            // 返回第一个非空的渲染类型
            if (renderTypes != null) {
                for (RenderType type : renderTypes) {
                    if (type != null) {
                        return type;
                    }
                }
            }
        } catch (Exception e) {
            // 如果获取失败，返回默认的 solid 类型
            return RenderType.solid();
        }

        return RenderType.solid();
    }


    public static List<BakedQuad> getQuads(BlockState state, @Nullable Direction direction,
                                           RandomSource random, ModelData modelData, RenderType renderType) {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);
        return model.getQuads(state, direction, random, modelData, renderType);
    }

    /**
     * 简化版本：使用默认随机源和空的模型数据
     */
    public static List<BakedQuad> getQuads(BlockState state, @Nullable Direction direction, RenderType renderType) {
        return getQuads(state, direction, RandomSource.create(), ModelData.EMPTY, renderType);
    }

    /**
     * 获取指定方向的所有 quads（包括使用默认渲染类型）
     */
    public static List<BakedQuad> getQuads(BlockState state, @Nullable Direction direction) {
        return getQuads(state, direction, RenderUtil.getRenderType(state));
    }
    @Deprecated
    public static AbstractRender getRender(Level level, BlockPos blockPos)
    {
        BlockEntity blockEntity=level.getBlockEntity(blockPos);
        AbstractRender render=new SimpleBlockPaint();

        if(blockEntity instanceof PaintBlockEntity paintBlockEntity)
        {
            render=paintBlockEntity.getRender();
        }

        return render;
    }

    @Deprecated
    public static BlockState getPaintBlockOrigin(BlockGetter level,BlockPos neighborPos)
    {
        BlockState blockState= Blocks.AIR.defaultBlockState();
        BlockEntity blockEntity=level.getBlockEntity(neighborPos);
        if(blockEntity instanceof PaintBlockEntity paintBlockEntity)
        {
            blockState =paintBlockEntity.getOrigin();
        }

        return blockState;

    }

    public static boolean shouldRenderFace(BlockGetter level, BlockPos pos,BlockState state, Direction face ) {

        BlockPos neighborPos=pos.relative(face);
        BlockState neighborState = level.getBlockState(neighborPos);
        if(neighborState.getBlock()instanceof PaintBlock)
        {
            neighborState=getPaintBlockOrigin(level,neighborPos);
        }

        // 邻居是空气 → 必须渲染
        if (neighborState.isAir()) {
            return true;
        }

        // 获取当前方块的渲染类型（用于判断是否为透明类）
        RenderType renderType = getRenderType(level,pos);
        boolean isTransparent = renderType == RenderType.cutout() ||
                renderType == RenderType.cutoutMipped() ||
                renderType == RenderType.translucent();

        // 如果邻居与当前方块类型相同，且当前方块为透明类 → 隐藏内部面
        if (isTransparent && neighborState.getBlock() == state.getBlock()) {
            return false;
        }

        // 如果邻居是不透明完整方块（使用原版 isSolid 近似判断）
        // 注意：isSolid() 对于玻璃等返回 false，所以需要结合上一步
        if (neighborState.isSolid()) {
            return false;
        }

        // 默认渲染（包括邻居为半透明、流体等情况）
        return true;
    }

    public static boolean shouldRenderFace(BlockState state, BlockGetter level, BlockPos offset, Direction face, BlockPos neighborPos) {
        BlockState blockstate = level.getBlockState(neighborPos);

        if(blockstate.getBlock()instanceof PaintBlock)blockstate=getPaintBlockOrigin(level,neighborPos);

        if (state.skipRendering(blockstate, face)) {
            return false;
        } else if (blockstate.hidesNeighborFace(level, neighborPos, state, face.getOpposite()) && state.supportsExternalFaceHiding()) {
            return false;
        } else if (blockstate.canOcclude()) {
            Block.BlockStatePairKey block$blockstatepairkey = new Block.BlockStatePairKey(state, blockstate, face);
            Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> object2bytelinkedopenhashmap = (Object2ByteLinkedOpenHashMap)OCCLUSION_CACHE.get();
            byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$blockstatepairkey);
            if (b0 != 127) {
                return b0 != 0;
            } else {
                VoxelShape voxelshape = state.getFaceOcclusionShape(level, offset, face);
                if (voxelshape.isEmpty()) {
                    return true;
                } else {
                    VoxelShape voxelshape1 = blockstate.getFaceOcclusionShape(level, neighborPos, face.getOpposite());
                    boolean flag = Shapes.joinIsNotEmpty(voxelshape, voxelshape1, BooleanOp.ONLY_FIRST);
                    if (object2bytelinkedopenhashmap.size() == 2048) {
                        object2bytelinkedopenhashmap.removeLastByte();
                    }

                    object2bytelinkedopenhashmap.putAndMoveToFirst(block$blockstatepairkey, (byte)(flag ? 1 : 0));
                    return flag;
                }
            }
        } else {
            return true;
        }
    }


    public static Block getBlockFromID(ResourceLocation key)
    {
        Block block=BuiltInRegistries.BLOCK.get(key);
        if(block==null)return Blocks.AIR;

        return block;
    }



}

