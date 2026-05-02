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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.List;


@OnlyIn(Dist.CLIENT)
public class RenderUtil {
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


    @OnlyIn(Dist.CLIENT)
    public static boolean shouldRenderFace(BlockPos pos,BlockState state, Direction face ) {
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
            return shouldRenderFace(mc.level,pos,state,face);
        }
        return false;
    }


    public static boolean shouldRenderFace(BlockGetter level, BlockPos pos,BlockState state, Direction face ) {
        //远距剔除
        if(Minecraft.getInstance()!=null)
        {
            Minecraft mc=Minecraft.getInstance();
            int distance=(mc.options.renderDistance().get()+8)*16;//TODO 这里记得改

            Vector3f camPos=new Vector3f(mc.gameRenderer.getMainCamera().getBlockPosition());//获取主视角位置
            Vector3f blockPos=new Vector3f(pos);


            float actualDistance=camPos.distanceSquaredTo(blockPos);
            if(actualDistance>distance*distance)
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

