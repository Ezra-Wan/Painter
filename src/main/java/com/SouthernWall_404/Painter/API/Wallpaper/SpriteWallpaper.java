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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpriteWallpaper implements IWallpaper{

    private static final int NORTH = 1, SOUTH = 2, WEST = 4, EAST = 8, UP = 16, DOWN = 32;
    private Map<Direction, Integer> flags = new HashMap<>();

    private List<VerticesInfo> materialInfo;

    public SpriteWallpaper(List<VerticesInfo> materialInfo) {
        this.materialInfo = materialInfo;
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

    @Override
    public String getType() {
        return "";
    }

    @Override
    public List<BakedQuad> createQuad(int flag, BlockState origin, boolean isTinted, TextureAtlasSprite sprite) {

        List<BakedQuad> mixedQuads = new ArrayList<>();
        List<BakedQuad> originQuads = getQuadsForDirection(origin, flag);

        VerticesInfo originInfo = new VerticesInfo(originQuads.getFirst());

        materialInfo.forEach(materialInfo -> {
            VerticeInfo[] mixedVerts=new VerticeInfo[4];
            for(int i=0;i<4;i++)
            {
                VerticeInfo originVert=originInfo.vertices.get(i);
                VerticeInfo materialVert=materialInfo.vertices.get(i);
                VerticeInfo mixedVert= VerticeInfo
                        .builder()
                        .position(originVert.position)
                        .color(materialVert.alpha, materialVert.red, materialVert.green, materialVert.blue)
                        .light(originVert.light)
                        .normal(originVert.normal)
                        .uv(materialVert.u, materialVert.v)
                        .build();
                mixedVerts[i]=mixedVert;
            }
            VerticesInfo mixedInfo = VerticesInfo.of(mixedVerts);
            mixedInfo.implyUV(originInfo);
            // 使用 materialQuad 的元数据创建新的 BakedQuad
            mixedQuads.add(new BakedQuad(mixedInfo.vertices(), 0, Direction.NORTH, sprite, true));//TODO 这里以后要更新
        });


        return mixedQuads;
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
}
