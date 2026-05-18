package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticeInfo;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
public class SpriteWallpaper implements IWallpaper {

    private float[][] uvs = new float[4][2];//顺序：左上，左下，右下，右上
    private int[] color = new int[4];
    private int tintIndex;

    private ResourceLocation altasKey;

    private SpriteWallpaper(float[][] uvs, int[] color, int tintIndex, ResourceLocation altasKey) {
        this.uvs = uvs;
        this.color = color;
        this.tintIndex = tintIndex;
        this.altasKey = altasKey;
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
        return "";
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public List<BakedQuad> createQuad(BakedQuad originQuad) {

        List<BakedQuad> quads=new ArrayList<>();
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
            ModelManager modelManager = Minecraft.getInstance().getModelManager();
            TextureAtlas blocksAtlas = modelManager.getAtlas(TextureAtlas.LOCATION_BLOCKS);
            TextureAtlasSprite sprite=blocksAtlas.getSprite(altasKey);

            VerticesInfo originInfo=new VerticesInfo(originQuad);

            VerticeInfo[] mixedVertices=new VerticeInfo[4];
            for(int i=0;i<4;i++)
            {
                VerticeInfo originVert=originInfo.vertices.get(i);
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
            VerticesInfo mixedInfo=VerticesInfo.of(mixedVertices);
            BakedQuad quad=new BakedQuad(mixedInfo.vertices(), tintIndex,originQuad.getDirection(),sprite,true);
            quads.add(quad);
        }



        return quads;
    }
    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {

        CompoundTag tag = new CompoundTag();

        return null;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
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
