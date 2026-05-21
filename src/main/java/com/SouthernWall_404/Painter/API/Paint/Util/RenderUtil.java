package com.SouthernWall_404.Painter.API.Paint.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.List;


@OnlyIn(Dist.CLIENT)
public class RenderUtil {

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
        // 计算相邻方块位置
        BlockPos neighborPos = pos.relative(face);
        // 调用原版标准面渲染判定逻辑
        boolean shouldRender=Block.shouldRenderFace(state, level, pos, face, neighborPos);
        return shouldRender;
    }
}

