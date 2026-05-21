package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticeInfo;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 需要做拆分
 * 这里只负责某个单独材质的处理，不处理叠加
 */
public class SpriteWallpaper extends AbstractWallpaper {

    public static final String TYPE="sprite";

    //========属性=========
    private float[][] uvs = new float[4][2];//顺序：左上，左下，右下，右上
    private int[] color = new int[4];
    private int tintIndex;
    private ResourceLocation altasKey;
    private float uOffset=0;//左下角点u方向偏移量
    private float vOffset=0;//左下角点v方向偏移量



    SpriteWallpaper(float[][] uvs, int[] color, int tintIndex, ResourceLocation altasKey) {
        this.uvs = uvs;
        this.color = color;
        this.tintIndex = tintIndex;
        this.altasKey = altasKey;
    }

    SpriteWallpaper(HolderLookup.Provider provider,CompoundTag tag){
        this(new float[4][2], new int[4], -1, null);
        deserializeNBT(provider,tag);
    }

    public static Builder builder(float[][] uvs, int[] color, int tintIndex, ResourceLocation altasKey) {
        return new Builder(uvs, color, tintIndex, altasKey);
    }

    public static Builder builder(BakedQuad quad)
    {
        return new Builder(quad);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public void cycleTextureUV(BakedQuad originQuad){

        //获取步长
        VerticesInfo originVertices = new VerticesInfo(originQuad);
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
        
        if (!uReachedEnd) {
            // U方向未截止，继续步进U
            uOffset = nextU;
        } else {
            // U方向已截止，重置U并步进V
            uOffset = 0.0f;
            
            if (!vReachedEnd) {
                // V方向未截止，步进V
                vOffset = nextV;
            } else {
                // V方向也已截止，全部归零
                vOffset = 0.0f;
            }
        }

        refresh();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<BakedQuad> createQuad(BakedQuad originQuad) {

        List<BakedQuad> quads=new ArrayList<>();
            Minecraft mc=Minecraft.getInstance();
            if(mc!=null)
            {
                VerticesInfo originInfo=new VerticesInfo(originQuad);
                //TODO 记得改laplace命名

                ModelManager modelManager = Minecraft.getInstance().getModelManager();
                TextureAtlas blocksAtlas = modelManager.getAtlas(TextureAtlas.LOCATION_BLOCKS);
                TextureAtlasSprite sprite=blocksAtlas.getSprite(altasKey);


                //建立基础普通方块面
                VerticeInfo[] mixedVertices=new VerticeInfo[4];
                for(int i=0;i<4;i++)
                {
                    VerticeInfo originVert=originInfo.vertices.get(i);//TODO 记得添加可变方法
                    VerticeInfo mixedVert= VerticeInfo.builder()
                            .uv(uvs[i][0],uvs[i][1])
                            .normal(originVert.normal)
                            .color(
                                    FastColor.ABGR32.alpha(color[i]),
                                    FastColor.ABGR32.blue(color[i]),
                                    FastColor.ABGR32.green(color[i]),
                                    FastColor.ABGR32.red(color[i])
                            )
                            .position(originVert.position)
                            .light(originVert.light)
                            .build();
                    mixedVertices[i]=mixedVert;

                }

                //uv处理
                VerticesInfo mixedInfo=VerticesInfo.of(mixedVertices);
                mixedInfo.implyUV(originInfo);//进行Uv长度变换
                mixedInfo.implyUVOffest(uOffset,vOffset);
                BakedQuad quad=new BakedQuad(mixedInfo.vertices(), tintIndex,originQuad.getDirection(),sprite,true);
                quads.add(quad);
        }





        return quads;
    }
    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        
        // 序列化 UV 坐标数组
        ListTag uvsList = new ListTag();
        for (int i = 0; i < 4; i++) {
            ListTag uvPair = new ListTag();
            uvPair.add(FloatTag.valueOf(uvs[i][0]));
            uvPair.add(FloatTag.valueOf(uvs[i][1]));
            uvsList.add(uvPair);
        }
        tag.put("uvs", uvsList);
        
        // 序列化颜色数组
        ListTag colorList = new ListTag();
        for (int i = 0; i < 4; i++) {
            colorList.add(IntTag.valueOf(color[i]));
        }
        tag.put("color", colorList);
        
        // 序列化 tintIndex
        tag.putInt("tintIndex", tintIndex);
        
        // 序列化纹理资源位置
        if (altasKey != null) {
            tag.putString("altasKey", altasKey.toString());
        }
        
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        // 反序列化 UV 坐标数组
        ListTag uvsList = compoundTag.getList("uvs", net.minecraft.nbt.Tag.TAG_LIST);
        for (int i = 0; i < 4 && i < uvsList.size(); i++) {
            ListTag uvPair = uvsList.getList(i);
            if (uvPair.size() >= 2) {
                uvs[i][0] = uvPair.getFloat(0);
                uvs[i][1] = uvPair.getFloat(1);
            }
        }
        
        // 反序列化颜色数组
        ListTag colorList = compoundTag.getList("color", net.minecraft.nbt.Tag.TAG_INT);
        for (int i = 0; i < 4 && i < colorList.size(); i++) {
            color[i] = colorList.getInt(i);
        }
        
        // 反序列化 tintIndex
        tintIndex = compoundTag.getInt("tintIndex");
        
        // 反序列化纹理资源位置
        if (compoundTag.contains("altasKey", net.minecraft.nbt.Tag.TAG_STRING)) {
            String atlasKeyStr = compoundTag.getString("altasKey");
            altasKey = ResourceLocation.parse(atlasKeyStr);
        }
    }

    public static class Builder{
        private float[][] uvs=new float[4][2];//顺序：左上，左下，右下，右上
        private int[] color=new int[4];
        private int tintIndex;
        private ResourceLocation altasKey;

        public Builder(float[][] uvs, int[] color, int tintIndex, ResourceLocation altasKey ) {
            this.uvs = uvs;
            this.color = color;
            this.tintIndex = tintIndex;
            this.altasKey=altasKey;
        }

        public Builder(BakedQuad quad)
        {

            this.tintIndex =quad.getTintIndex();
            VerticesInfo verticesInfo=new VerticesInfo(quad);
            this.altasKey= quad.getSprite().contents().name();

            this.color=new int[]{//TODO 记得优化
                    FastColor.ARGB32.color(verticesInfo.LeftUp().alpha,verticesInfo.LeftUp().red,verticesInfo.LeftUp().green,verticesInfo.LeftUp().blue),
                    FastColor.ARGB32.color(verticesInfo.LeftDown().alpha,verticesInfo.LeftDown().red,verticesInfo.LeftDown().green,verticesInfo.LeftDown().blue),
                    FastColor.ARGB32.color(verticesInfo.RightDown().alpha,verticesInfo.RightDown().red,verticesInfo.RightDown().green,verticesInfo.RightDown().blue),
                    FastColor.ARGB32.color(verticesInfo.RightUp().alpha,verticesInfo.RightUp().red,verticesInfo.RightUp().green,verticesInfo.RightUp().blue)
            };
            this.uvs=new float[][]{
                    {verticesInfo.LeftUp().u, verticesInfo.LeftUp().v},
                    {verticesInfo.LeftDown().u, verticesInfo.LeftDown().v},
                    {verticesInfo.RightDown().u, verticesInfo.RightDown().v},//TODo 这里似乎出现了解码错误
                    {verticesInfo.RightUp().u, verticesInfo.RightUp().v}
            };

        }

        public Builder(BakedQuad quad,float u,float v)
        {

            this.tintIndex =quad.getTintIndex();
            VerticesInfo verticesInfo=new VerticesInfo(quad);
            this.altasKey= quad.getSprite().contents().name();

            this.color=new int[]{//TODO 记得优化
                    FastColor.ARGB32.color(verticesInfo.LeftUp().alpha,verticesInfo.LeftUp().red,verticesInfo.LeftUp().green,verticesInfo.LeftUp().blue),
                    FastColor.ARGB32.color(verticesInfo.LeftDown().alpha,verticesInfo.LeftDown().red,verticesInfo.LeftDown().green,verticesInfo.LeftDown().blue),
                    FastColor.ARGB32.color(verticesInfo.RightDown().alpha,verticesInfo.RightDown().red,verticesInfo.RightDown().green,verticesInfo.RightDown().blue),
                    FastColor.ARGB32.color(verticesInfo.RightUp().alpha,verticesInfo.RightUp().red,verticesInfo.RightUp().green,verticesInfo.RightUp().blue)
            };
            this.uvs=new float[][]{
                    {verticesInfo.LeftUp().u, verticesInfo.LeftUp().v},
                    {verticesInfo.LeftDown().u, verticesInfo.LeftDown().v},
                    {verticesInfo.RightDown().u, verticesInfo.RightDown().v},
                    {verticesInfo.RightUp().u, verticesInfo.RightUp().v}
            };

        }

        public SpriteWallpaper build()
        {
            return new SpriteWallpaper(uvs,color, tintIndex,altasKey);
        }
    }
}
