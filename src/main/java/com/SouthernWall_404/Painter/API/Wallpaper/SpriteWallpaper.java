package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.Laplace.VerticesHelper;
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

    //========数据=========
    private VerticesInfo texture;


    //========构造方法========
    SpriteWallpaper(VerticesInfo texture,ResourceLocation atlasKey, int tintIndex) {
        super(tintIndex, atlasKey);
        this.texture=texture;

    }

    /**
     * 用于从NBT建立
     * @param provider
     * @param tag
     */
    SpriteWallpaper(HolderLookup.Provider provider,CompoundTag tag){
        this(VerticesInfo.fromNBT(provider,tag.getCompound("texture")),null,-1);
        deserializeNBT(provider,tag);
    }
    /**
     * 用于从BakedQuad建立
     * @param quad
     */
    public SpriteWallpaper(BakedQuad quad) {
        this(VerticesInfo.of(quad.getVertices()),quad.getSprite().contents().name(),quad.getTintIndex());
    }

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
    //========基本方法========
    @Override
    public String getType() {
        return TYPE;
    }

    public void setTexture(VerticesInfo texture) {
        this.texture = texture;
    }

    public VerticesInfo getTexture() {
        return texture;
    }


    //========业务方法========

    /**
     * 用于自动旋转uv
     * @param originQuad
     * @param paint
     */
    @OnlyIn(Dist.CLIENT)
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

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected List<VerticesInfo> createTexture(BakedQuad originQuad) {

        List<VerticesInfo> textures=new ArrayList<>();
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null){

            texture= VerticesHelper.localize(texture,altasKey);

            VerticesInfo originInfo=VerticesInfo.of(originQuad);
            textures.add(
                    originInfo.copy()
                            .color(texture.getColors())
                            .texture(texture));

        }


        return textures;
    }

}
