package com.SouthernWall_404.Painter.API.Paint.Util.Wallpaper;

import com.SouthernWall_404.LaplaceAPI.UlrichToolBox.Blocks.BlockClientUtil;
import com.SouthernWall_404.Painter.API.Wallpaper.IWallpaper;
import com.SouthernWall_404.Painter.API.Wallpaper.SpriteWallpaper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class WallpaperBlockHelper {

    /**
     * 获取当前方块的所有堆叠层
     * @param material
     * @param face
     * @return
     */
    @OnlyIn(Dist.CLIENT)
    public static List<IWallpaper> getOverlays(BlockState material, Direction face)
    {
        List<IWallpaper> overlays=new ArrayList<>();
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null) {
            List<BakedQuad> quads = BlockClientUtil.getQuadsForDirection(material, face);
            quads.forEach(quad -> overlays.add(new SpriteWallpaper( quad)));
        }
        return overlays;
    }
}
