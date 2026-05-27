package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.Laplace.VerticesHelper;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuadRender;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.ModelRender;
import com.SouthernWall_404.LaplaceAPI.UlrichToolBox.Blocks.BlockClientUtil;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

/**
 * 定义壁纸的渲染职能
 * 初始的内容
 * 需要处理渲染、
 */
public abstract class AbstractWallpaper implements IWallpaper{
    private final float[] shape = new float[ModelRender.DIRECTIONS.length * 2];
    private final BitSet shapeFlags = new BitSet(3);
    protected List<BakedQuad> quads=new ArrayList<>();//建立的Quad
    protected ModelRender.AmbientOcclusionFace aoFace=null;//AO缓存
    protected short isVisible=-1;//可见性,负数为非法
    
    // 通用属性
    protected int tintIndex;
    protected ResourceLocation altasKey;

    public AbstractWallpaper(int tintIndex, ResourceLocation altasKey) {
        this.tintIndex = tintIndex;
        this.altasKey = altasKey;
    }

    /**
     * 用于以填充方式创建Quad
     * @param originInfo
     * @param texture
     * @return
     */
    public VerticesInfo doStuff(VerticesInfo originInfo,VerticesInfo texture) {
        float xScale=originInfo.getXLength()/texture.getXLength();
        float yScale=originInfo.getYLength()/texture.getYLength();

        texture.scaleUV(xScale,yScale);
        return texture;
    }

    /**
     * 用于最终建立Quad
     * @param originQuad
     * @return
     */
    @Override
    public List<BakedQuad> createQuad(BakedQuad originQuad) {

        List<BakedQuad> quads=new ArrayList<>();
        List<VerticesInfo> textures= createTexture(originQuad);//获取纹理信息
        VerticesInfo originInfo=VerticesInfo.of(originQuad);


        Minecraft mc=Minecraft.getInstance();
        if(mc!=null){
            ModelManager modelManager = Minecraft.getInstance().getModelManager();
            TextureAtlas blocksAtlas = modelManager.getAtlas(TextureAtlas.LOCATION_BLOCKS);
            TextureAtlasSprite sprite=blocksAtlas.getSprite(altasKey);

            textures.forEach(texture -> {//遍历纹理信息
//                texture=texture.stuffedBy(originInfo);//填充
                texture= VerticesHelper.offset(VerticesHelper.StuffedTo(texture,originInfo),0f,0f);

                BakedQuad quad=new BakedQuad(texture.vertices(), tintIndex,originQuad.getDirection(),sprite,true);//创建Quad

                quads.add(quad);
            });
        }


        return quads;
    }

    /**
     * 用于建立原始纹理，不考虑原有方块情况
     * @param originQuads
     * @return
     */
    public abstract List<VerticesInfo> createTexture(BakedQuad originQuads);

    //TODO 考虑将getType放在这里
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
                    quads=createQuad(BlockClientUtil.getQuadsForDirection(paint.getOrigin(),paint.translateFace(direction)).getFirst());//TODO 不是很标准的编程
                }

                if(isVisible>0){//正数为可见
                    quads.forEach(quad -> BakedQuadRender.renderInOfferredAO(quad, paint.getOrigin(),paint.getRenderVec().get(paint.getFlag(direction)), poseStack, buffer, aoFace));
                }
            }
        }

    }

    /**
     * 获取 tintIndex
     */
    public int getTintIndex() {
        return tintIndex;
    }

    /**
     * 设置 tintIndex
     */
    public void setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    /**
     * 获取 altasKey
     */
    public ResourceLocation getAltasKey() {
        return altasKey;
    }

    /**
     * 设置 altasKey
     */
    public void setAltasKey(ResourceLocation altasKey) {
        this.altasKey = altasKey;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        
        // 序列化 tintIndex
        tag.putInt("tintIndex", tintIndex);
        
        // 序列化 altasKey
        if (altasKey != null) {
            tag.put("altasKey", ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, altasKey).result().orElse(null));
        }
        
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        // 反序列化 tintIndex
        if (compoundTag.contains("tintIndex")) {
            tintIndex = compoundTag.getInt("tintIndex");
        }
        
        // 反序列化 altasKey
        if (compoundTag.contains("altasKey")) {
            altasKey = ResourceLocation.CODEC.parse(
                NbtOps.INSTANCE,
                compoundTag.get("altasKey")
            ).result().orElse(null);
        }
    }

}
