package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector2f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuadRender;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.ModelRender;
import com.SouthernWall_404.LaplaceAPI.UlrichToolBox.Blocks.BlockClientUtil;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.SouthernWall_404.Painter.Client.Event.ClientTick;
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

    //========常量========
    private final float[] SHAPE = new float[ModelRender.DIRECTIONS.length * 2];
    private final BitSet SHAPE_FLAGS = new BitSet(3);

    //========缓存========
    protected List<BakedQuad> quads=new ArrayList<>();//建立的Quad
    protected ModelRender.AmbientOcclusionFace aoFace=null;//AO缓存
    protected short isVisible=-1;//可见性,负数为非法
    
    //========数据========
    protected int tintIndex;//着色情况
    protected ResourceLocation altasKey;//sprite贴图情况
    protected Vector2f uvOffset=new Vector2f(0f,0f);//uv偏移情况


    //========构造方法========
    public AbstractWallpaper(int tintIndex, ResourceLocation altasKey) {
        this.tintIndex = tintIndex;
        this.altasKey = altasKey;
    }


    /**
     * 反序列化
     * @param provider
     * @param compoundTag
     */
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

        // 反序列化 uvOffset
        if (compoundTag.contains("u_offset") && compoundTag.contains("v_offset")) {
            uvOffset = new Vector2f(
                    compoundTag.getFloat("u_offset"),
                    compoundTag.getFloat("v_offset")
            );
        }

        refresh();
    }

    /**
     * 序列化
     * @param provider
     * @return
     */
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        // 序列化 tintIndex
        tag.putInt("tintIndex", tintIndex);

        // 序列化 altasKey
        if (altasKey != null) {
            tag.put("altasKey", ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, altasKey).result().orElse(null));
        }

        // 添加 uvOffset
        tag.putFloat("u_offset", uvOffset.getX());
        tag.putFloat("v_offset", uvOffset.getY());

        return tag;
    }
    //=========刷新方法========

    /**
     * 用于刷新AO缓存
     * @param paint
     * @param direction
     */
    @OnlyIn(Dist.CLIENT)
    public void refreshAO(AbstractPaint paint,Direction direction){
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null){
            Level level=mc.level;
            if(level!= null)
            {
                BlockPos blockPos=paint.getBlockPos();
                if(!paint.hasNullInDirection( direction))blockPos=blockPos.relative(direction);
                if(aoFace==null)aoFace=new ModelRender.AmbientOcclusionFace();

                aoFace.calculate(level,paint.getOrigin(),blockPos, direction, SHAPE, SHAPE_FLAGS, true);
            }
        }
    }

    /**
     * 用于刷新可见性缓存
     * @param paint
     * @param direction
     */
    @OnlyIn(Dist.CLIENT)
    public void refreshVisibles(AbstractPaint paint,Direction direction){
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null){
            isVisible= RenderUtil.shouldRenderFace(paint.getBlockPos(),paint.getOrigin(),direction)?(short)1:(short)0;
        }
    }

    /**
     * 整体刷新
     */
    @OnlyIn(Dist.CLIENT)
    public void refresh(){
        aoFace=null;
        isVisible=-1;
        quads.clear();

    }
    //=========基本方法========

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

    /**
     * 获取 uvOffset
     */
    public Vector2f getUvOffset() {
        return uvOffset;
    }

    /**
     * 设置 uvOffset，并添加更新方法
     * @param uvOffset
     * @param paint
     */
    @Override
    public void setUVOffset(Vector2f uvOffset, AbstractPaint paint) {

        this.uvOffset=uvOffset;//设置uv偏移
        refresh();//刷新

        //如果在客户端
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null&&mc.level!=null){

            ClientTick.updateRender( paint);//添加当前内容到更新列表
        }
        //TODO Block和这里的职能略有不清，需要明确
        //TODO 似乎有循环调用的错误
    }

    //=========业务方法========

    /**
     * 根据当前数据，生成新的Quad
     * @param originQuad
     * @return
     */
    @Override
    public List<BakedQuad> createQuad(BakedQuad originQuad) {

        List<BakedQuad> quads=new ArrayList<>();
        List<VerticesInfo> textures= createTexture(originQuad);//根据原始quad，生成当前应有基准纹理
        VerticesInfo originInfo=VerticesInfo.of(originQuad);//原始quad信息


        //只在客户端执行
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null){
            //获取sprite内容
            ModelManager modelManager = Minecraft.getInstance().getModelManager();
            TextureAtlas blocksAtlas = modelManager.getAtlas(TextureAtlas.LOCATION_BLOCKS);
            TextureAtlasSprite sprite=blocksAtlas.getSprite(altasKey);

            //将基准纹理调用
            textures.forEach(texture -> {//遍历纹理信息
                texture=texture
                        .stuffedTo(originInfo)//填充
                        .offset(uvOffset.getX(), uvOffset.getY());//设置偏移
                BakedQuad quad=new BakedQuad(texture.vertices(), tintIndex,originQuad.getDirection(),sprite,true);//创建Quad

                quads.add(quad);
            });
        }


        return quads;
    }

    /**
     * 用于建立基准纹理
     * 即对于任意一个1*1的面，所需要填充的纹理
     * @param originQuads
     * @return
     */
    protected abstract List<VerticesInfo> createTexture(BakedQuad originQuads);


    /**
     * 通用渲染方法
     * @param paint
     * @param direction
     * @param poseStack
     * @param buffer
     */
    @OnlyIn(Dist.CLIENT)
    public void render(AbstractPaint paint,Direction direction, PoseStack poseStack, VertexConsumer buffer)
    {
        //只在客户端执行
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
            Level level=mc.level;
            if(level!=null)
            {
                //进行合法检验
                if(aoFace==null)refreshAO(paint,direction);//若ao空，则刷新
                if(isVisible<0)refreshVisibles(paint,direction);//刷新可见性
                if(quads.isEmpty()){
                    quads=createQuad(BlockClientUtil.getQuadsForDirection(paint.getOrigin(),paint.translateFace(direction)).getFirst());
                }//刷新渲染缓存

                if(isVisible>0){//正数为可见
                    renderQuad(paint,direction, poseStack, buffer);
                }
            }
        }

    }

    /**
     * 最终渲染，允许继承类修改以实现其他内容
     * @param paint
     * @param direction
     * @param poseStack
     * @param buffer
     */
    protected void renderQuad(AbstractPaint paint,Direction direction, PoseStack poseStack, VertexConsumer buffer)
    {
        quads.forEach(quad -> BakedQuadRender.renderInOfferredAO(quad, paint.getOrigin(),paint.getRenderVec().get(paint.getFlag(direction)), poseStack, buffer, aoFace));
    }
}
