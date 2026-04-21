package com.SouthernWall_404.Painter.API.Paint.Util;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
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



    @Deprecated
    public static RenderType getRenderType(BlockGetter level,BlockPos pos)
    {
        BlockState blockState=level.getBlockState(pos);
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
    public static BlockState getPaintBlockOrigin(BlockGetter level,BlockPos neighborPos)
    {
        BlockState blockState=level.getBlockState(neighborPos);
        return blockState;

    }

    @Deprecated
    public static boolean shouldRenderFace(BlockGetter level, BlockPos pos,BlockState state, Direction face ) {


        //远距剔除
        if(Minecraft.getInstance()!=null)
        {
            Minecraft mc=Minecraft.getInstance();
            int distance=mc.options.renderDistance().get()*16;
            Player player=mc.player;

            Vector3f eyePos=new Vector3f(player.getEyePosition());
            Vector3f blockPos=new Vector3f(pos);

            if(eyePos.distanceTo(blockPos)>distance)
            {
                return false;
            }

        }
        // 计算相邻方块位置
        BlockPos neighborPos = pos.relative(face);
        // 调用原版标准面渲染判定逻辑
        boolean shouldRender=Block.shouldRenderFace(state, level, pos, face, neighborPos);
        return shouldRender;
    }
}

