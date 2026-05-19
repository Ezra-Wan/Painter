package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector3f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuadRender;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.ModelRender;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.Configs;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintValidHelper;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.SouthernWall_404.Painter.Client.Config.ClientConfig;
import com.SouthernWall_404.Painter.Painter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

/**
 * 定义壁纸的渲染职能
 */
public abstract class AbstractWallpaper implements IWallpaper{
    private final float[] shape = new float[ModelRender.DIRECTIONS.length * 2];
    private final BitSet shapeFlags = new BitSet(3);
    protected List<BakedQuad> quads=new ArrayList<>();//建立的Quad
    protected ModelRender.AmbientOcclusionFace aoFace=null;//AO缓存
    protected short isVisible=-1;//可见性,负数为非法

    @OnlyIn(Dist.CLIENT)
    public void refreshAO(AbstractPaint paint,Direction direction){
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null){
            Level level=mc.level;
            if(level!= null)
            {
                BlockPos blockPos=paint.getBlockPos();
                if(!paint.hasNullInDirection( direction))blockPos=blockPos.relative(direction);//进行非dir面偏移
                if(aoFace==null)aoFace=new ModelRender.AmbientOcclusionFace();

                aoFace.calculate(level,paint.getOrigin(),blockPos, direction, shape, shapeFlags, true);
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void refreshVisibles(AbstractPaint paint,Direction direction){
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null){
            isVisible= RenderUtil.shouldRenderFace(paint.getBlockPos(),paint.getOrigin(),direction)?(short)1:(short)0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void refresh(){
        aoFace=null;
        isVisible=-1;
        quads.clear();
    }

    @OnlyIn(Dist.CLIENT)
    public void render(AbstractPaint paint,Direction direction, PoseStack poseStack, VertexConsumer buffer)
    {
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
            Level level=mc.level;
            if(level!=null)
            {
                if(aoFace==null)refreshAO(paint,direction);
                if(isVisible<0)refreshVisibles(paint,direction);
                if(quads.isEmpty()){
                    quads=createQuad(RenderUtil.getQuadsForDirection(paint.getOrigin(),paint.translateFace(direction)).getFirst());//TODO 不是很标准的编程
                }

                if(isVisible>0){//正数为可见
                    quads.forEach(quad -> BakedQuadRender.renderInOfferredAO(quad, paint.getOrigin(),paint.getRenderVec().get(paint.getFlag(direction)), poseStack, buffer, aoFace));
                }
            }
        }

    }


}
