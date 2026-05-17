package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;

/**
 * 应当在这里处理与方块相关的逻辑，属于ListOverlayPaper的导出
 */
public class BlockWallPaper extends ListOverlayWallpaper{
    public BlockWallPaper(BlockState material,Direction face) {
        super(getQuads(material,face));
    }

    public static List<IWallpaper> getQuads(BlockState material,Direction face)
    {
        List<IWallpaper> overlays=new ArrayList<>();
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null) {
            List<BakedQuad> quads = getQuadsForDirection(material, face);
            quads.forEach(quad -> overlays.add(SpriteWallpaper.builder(quad).build()));
        }
        return overlays;
    }

    public static List<BakedQuad> getQuadsForDirection(BlockState state, Direction face) {

        BakedModel model = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
        RandomSource random = RandomSource.create();
        RenderType renderType = RenderUtil.getRenderType(state);
        return model.getQuads(state, face, random, ModelData.EMPTY, renderType);
    }
}
