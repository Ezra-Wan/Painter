package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticeInfo;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintSyncHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

/**
 * 需要做拆分
 * 这里只负责某个单独材质的处理，不处理叠加
 */
public class SpriteWallpaper extends AbstractWallpaper {

    public static final String TYPE="sprite";

    //========属性=========
    private int tintIndex;
    private ResourceLocation altasKey;
    private float uOffset=0;//左下角点u方向偏移量
    private float vOffset=0;//左下角点v方向偏移量

    private VerticesInfo texture;



    SpriteWallpaper(VerticesInfo texture,ResourceLocation atlasKey, int tintIndex) {
        this.texture=texture;
        this.tintIndex = tintIndex;
        this.altasKey=atlasKey;
    }

    SpriteWallpaper(HolderLookup.Provider provider,CompoundTag tag){
        this(VerticesInfo.fromNBT(provider,tag.getCompound("texture")),null,-1);
        deserializeNBT(provider,tag);
    }

    public static Builder builder(BakedQuad quad)
    {
        return new Builder(quad);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public void setUVOffset(float u, float v)
    {
        uOffset = u;
        vOffset = v;
        refresh();

        //TODO 记得做错误处理
    }
    @Override
    public void setUVOffset(BlockPos blockPos,Direction direction,float uOffset, float vOffset) {
        this.uOffset = uOffset;
        this.vOffset = vOffset;
        refresh();
        if (Minecraft.getInstance()!=null&&Minecraft.getInstance().level.isClientSide)
        {
            PaintSyncHelper.syncPaintUV(blockPos,direction,new float[]{uOffset,vOffset});
        }

        //TODO UV处理需要再修复以下
        //TODO Block和这里的职能略有不清，需要明确
        //TODO 似乎有循环调用的错误
    }

    public void cycleTextureUV(BakedQuad originQuad, Direction direction, BlockPos blockPos){

        //获取步长
        VerticesInfo originVertices =VerticesInfo.of(originQuad);
        float stepU = originVertices.getXLength();
        float stepV = originVertices.getYLength();

        /**
         * TODO 进行步进
         *  先沿u行进，如果截止后沿v步进重启
         *  若v截止，且u截止，则归零
         *  - 截止条件：当前值在经过stepU补正（也即quad右上点）刚好到达1时停止
         *  - 处理：
         *   - 若小于1，则加上stepU继续步进
         *   - 若大于1，则归位到1-stepU，下一次截止
         */
        
        // 计算右上角的UV值（当前偏移 + 步长）
        float nextU = uOffset + stepU;
        float nextV = vOffset + stepV;
        
        // 判断U方向是否截止
        boolean uReachedEnd = (nextU >= 1.0f);
        // 判断V方向是否截止
        boolean vReachedEnd = (nextV >= 1.0f);

        float uToSet=0;
        float vToSet=0;

        if (!uReachedEnd) {
            // U方向未截止，继续步进U
            uToSet = nextU;
        } else {
            // U方向已截止，重置U并步进V
            uToSet = 0.0f;

            if (!vReachedEnd) {
                // V方向未截止，步进V
                vToSet = nextV;
            } else {
                // V方向也已截止，全部归零
                vToSet = 0.0f;
            }
        }
        setUVOffset(blockPos, direction,uToSet, vToSet);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<BakedQuad> createQuad(BakedQuad originQuad) {

        List<BakedQuad> quads=new ArrayList<>();
            Minecraft mc=Minecraft.getInstance();
            if(mc!=null)
            {
                VerticesInfo originInfo=VerticesInfo.of(originQuad);
                //TODO 记得改laplace命名

                ModelManager modelManager = Minecraft.getInstance().getModelManager();
                TextureAtlas blocksAtlas = modelManager.getAtlas(TextureAtlas.LOCATION_BLOCKS);
                TextureAtlasSprite sprite=blocksAtlas.getSprite(altasKey);

                //uv处理
                VerticesInfo mixedInfo=
                        originInfo.copy()
                                .stuffedBy( texture)
                                .color(texture.getColors());

                BakedQuad quad=new BakedQuad(mixedInfo.vertices(), tintIndex,originQuad.getDirection(),sprite,true);
                quads.add(quad);
        }
        return quads;
    }
    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        tag.put("texture", texture.serializeNBT(provider));
        // 序列化 tintIndex
        tag.putInt("tintIndex", tintIndex);

        tag.putFloat("u_offset", uOffset);
        tag.putFloat("v_offset", vOffset);

        tag.put("altasKey", ResourceLocation.CODEC.encodeStart(NbtOps.INSTANCE, altasKey).result().orElse(null));
        
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {

        if(compoundTag.contains("u_offset"))uOffset=compoundTag.getFloat("u_offset");
        if(compoundTag.contains("v_offset"))vOffset=compoundTag.getFloat("v_offset");

        if(compoundTag.contains("texture"))texture=VerticesInfo.fromNBT(provider,compoundTag.getCompound("texture"));

        if(compoundTag.contains("tintIndex")) tintIndex = compoundTag.getInt("tintIndex");
        if(compoundTag.contains("altasKey"))altasKey=ResourceLocation.CODEC.parse(
            NbtOps.INSTANCE,
            compoundTag.get("altasKey")
        ).result().orElse(null);


    }

    public static class Builder{
        private int tintIndex;
        private VerticesInfo texture;
        private ResourceLocation altasKey;

        public Builder(VerticesInfo texture, ResourceLocation altasKey,int tintIndex) {

            this.texture = texture;
            this.altasKey = altasKey;
            this.tintIndex = tintIndex;
        }

        public Builder(BakedQuad quad)
        {

            this.tintIndex =quad.getTintIndex();
            texture=VerticesInfo.of(quad);
            this.altasKey= quad.getSprite().contents().name();
        }

//        public Builder(BakedQuad quad,float u,float v)
//        {
//
//            this.tintIndex =quad.getTintIndex();
//            VerticesInfo verticesInfo=VerticesInfo.of(quad);
//            this.altasKey= quad.getSprite().contents().name();
//
//            this.color=new int[]{//TODO 记得优化
//                    FastColor.ARGB32.color(verticesInfo.LeftUp().alpha,verticesInfo.LeftUp().red,verticesInfo.LeftUp().green,verticesInfo.LeftUp().blue),
//                    FastColor.ARGB32.color(verticesInfo.LeftDown().alpha,verticesInfo.LeftDown().red,verticesInfo.LeftDown().green,verticesInfo.LeftDown().blue),
//                    FastColor.ARGB32.color(verticesInfo.RightDown().alpha,verticesInfo.RightDown().red,verticesInfo.RightDown().green,verticesInfo.RightDown().blue),
//                    FastColor.ARGB32.color(verticesInfo.RightUp().alpha,verticesInfo.RightUp().red,verticesInfo.RightUp().green,verticesInfo.RightUp().blue)
//            };
//            this.uvs=new float[][]{
//                    {verticesInfo.LeftUp().u(), verticesInfo.LeftUp().v()},
//                    {verticesInfo.LeftDown().u(), verticesInfo.LeftDown().v()},
//                    {verticesInfo.RightDown().u(), verticesInfo.RightDown().v()},
//                    {verticesInfo.RightUp().u(), verticesInfo.RightUp().v()}
//            };
//
//        }

        public SpriteWallpaper build()
        {
            return new SpriteWallpaper(texture,altasKey, tintIndex);
        }
    }
}
