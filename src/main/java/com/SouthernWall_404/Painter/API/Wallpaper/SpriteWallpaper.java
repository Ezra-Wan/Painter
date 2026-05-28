package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector2f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintSyncHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
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

    private VerticesInfo texture;



    SpriteWallpaper(VerticesInfo texture,ResourceLocation atlasKey, int tintIndex) {
        super(tintIndex, atlasKey);
        this.texture=texture;

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



    @Override
    public void cycleTextureUV(BakedQuad originQuad, AbstractPaint paint) {

        VerticesInfo originInfo=VerticesInfo.of(originQuad);

        float maxUoffset=1/originInfo.getXLength()-1;//可用最大u向偏移倍率，减1以弥补已有框长度
        float maxVoffset=1/originInfo.getYLength()-1;//可用最大v向偏移倍率

        if(maxUoffset<0||maxVoffset<0)throw new IllegalArgumentException("texture size is too small");

        float currentUoffset=this.uvOffset.getX();//当前u向偏移倍率
        float currentVoffset=this.uvOffset.getY();//当前v向偏移倍率

        //先对u进行偏移处理
        if(currentUoffset<(int)(maxUoffset))//先从整数开始处理,若还未抵达最大
        {
            currentUoffset+=1;//步进

        }else if(currentUoffset==(int)(maxUoffset)){//若抵达整数最大

            currentUoffset=0;//回归
            //开始v进行偏移处理
            if(currentVoffset<(int)(maxVoffset))//先从整数开始处理,若还未抵达最大
                currentVoffset+=1;//步进
            else if(currentVoffset==(int)(maxVoffset)){//若抵达整数最大
                currentVoffset=0;//回归
            }
        }

        setUVOffset(new Vector2f(currentUoffset,currentVoffset), paint);
        //TODO 需要添加网络处理，向服务器进行同步

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<VerticesInfo> createTexture(BakedQuad originQuad) {

        List<VerticesInfo> textures=new ArrayList<>();
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null){
            VerticesInfo originInfo=VerticesInfo.of(originQuad);
            textures.add(
                    originInfo.copy()
                            .color(texture.getColors())
                            .texture(texture));

        }


        return textures;
    }
//
//    @OnlyIn(Dist.CLIENT)
//    @Override
//    public List<BakedQuad> createQuad(BakedQuad originQuad) {
//
//        List<BakedQuad> quads=new ArrayList<>();
//            Minecraft mc=Minecraft.getInstance();
//            if(mc!=null)
//            {
//                VerticesInfo originInfo=VerticesInfo.of(originQuad);
//                //TODO 记得改laplace命名
//
//
//
//                //uv处理
//                        VerticesInfo mixedInfo=
//                        originInfo.copy()
//                                .stuffedBy( texture)
//                                .color(texture.getColors());
//
//                BakedQuad quad=new BakedQuad(mixedInfo.vertices(), tintIndex,originQuad.getDirection(),sprite,true);
//                quads.add(quad);
//        }
//        return quads;
//    }


    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        // 调用父类序列化通用字段（tintIndex, altasKey）
        CompoundTag tag = super.serializeNBT(provider);
        
        // 序列化 SpriteWallpaper 特有字段
        tag.put("texture", texture.serializeNBT(provider));
        
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        // 调用父类反序列化通用字段（tintIndex, altasKey）
        super.deserializeNBT(provider, compoundTag);
        
        // 反序列化 SpriteWallpaper 特有字段
        if(compoundTag.contains("texture")) texture = VerticesInfo.fromNBT(provider, compoundTag.getCompound("texture"));
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

        public SpriteWallpaper build()
        {
            return new SpriteWallpaper(texture,altasKey, tintIndex);
        }
    }
}
