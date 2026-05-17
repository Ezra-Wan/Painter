package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticeInfo;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 需要做拆分
 * 这里只负责某个单独材质的处理，不处理叠加
 */
public class SpriteWallpaperForSwap implements IWallpaper {

    private float[][] uvs = new float[4][2];//顺序：左上，左下，右下，右上
    private int[] color = new int[4];
    private boolean isTinted;
    private TextureAtlasSprite sprite;

    private static final int NORTH = 1, SOUTH = 2, WEST = 4, EAST = 8, UP = 16, DOWN = 32;
    private Map<Direction, Integer> flags = new HashMap<>();

    private SpriteWallpaperForSwap(float[][] uvs, int[] color, boolean isTinted, TextureAtlasSprite sprite) {
        this.uvs = uvs;
        this.color = color;
        this.isTinted = isTinted;
        this.sprite = sprite;
        initFlags();
    }
    private void initFlags() {
        flags.put(Direction.NORTH, NORTH);
        flags.put(Direction.SOUTH, SOUTH);
        flags.put(Direction.WEST, WEST);
        flags.put(Direction.EAST, EAST);
        flags.put(Direction.UP, UP);
        flags.put(Direction.DOWN, DOWN);
    }
    public static Builder builder(float[][] uvs, int[] color, boolean isTinted, TextureAtlasSprite sprite) {
        return new Builder(uvs, color, isTinted, sprite);
    }

    public static Builder builder(BakedQuad quad)
    {
        return new Builder(quad);
    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public List<BakedQuad> createQuad(int flag, BlockState origin, boolean isTinted, TextureAtlasSprite sprite) {

        Direction direction=getDirection(flag);
        List<BakedQuad> originQuads = getQuadsForDirection(origin, flag);
        BakedQuad originQuad=originQuads.getFirst();
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
        BakedQuad quad=new BakedQuad(mixedInfo.vertices(), 0,direction,sprite,true);//TODO 这里的着色有待处理


        return List.of(quad);
    }

    public List<BakedQuad> getQuadsForDirection(BlockState state, int flag) {
        Direction direction;

        if(flag<0)
        {
            direction=null;
        }else direction=getDirection(flag);

        BakedModel model = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
        RandomSource random = RandomSource.create();
        RenderType renderType = RenderUtil.getRenderType(state);
        return model.getQuads(state, direction, random, ModelData.EMPTY, renderType);
    }
    public Direction getDirection(int flag) {
        if(flag<0)flag=-flag;
        for (Map.Entry<Direction, Integer> entry : flags.entrySet()) {
            if (entry.getValue() == flag) {
                return entry.getKey();
            }
        }
        return Direction.NORTH;
    }
    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {

    }

    public static class Builder{
        private float[][] uvs=new float[4][2];//顺序：左上，左下，右下，右上
        private int[] color=new int[4];
        private boolean isTinted;
        private TextureAtlasSprite sprite;

        public Builder(float[][] uvs, int[] color, boolean isTinted,TextureAtlasSprite sprite) {
            this.uvs = uvs;
            this.color = color;
            this.isTinted = isTinted;
            this.sprite=sprite;
        }

        public Builder(BakedQuad quad)
        {

            this.isTinted=quad.isTinted();
            this.sprite=quad.getSprite();
            VerticesInfo verticesInfo=new VerticesInfo(quad);

            this.color=new int[]{
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

        public SpriteWallpaperForSwap build()
        {
            return new SpriteWallpaperForSwap(uvs,color,isTinted,sprite);
        }
    }


}
