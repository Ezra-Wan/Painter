package com.SouthernWall_404.Painter.API.Wallpaper;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

/**
 * 应当允许存储多个Wallpaper，包括自身，即自引用
 */
public class ListOverlayWallpaper implements IWallpaper {

    private List<IWallpaper> overlays;

    public ListOverlayWallpaper(List<IWallpaper> overlays) {
        this.overlays=overlays;
    }

    public static ListOverlayWallpaper ofQuads(List<BakedQuad> quads){

        List<IWallpaper> overlays=new ArrayList<>();
        quads.forEach(quad->overlays.add(SpriteWallpaper.builder(quad).build()));

        return new ListOverlayWallpaper(overlays);

    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public List<BakedQuad> createQuad(BakedQuad originQuad) {

        List<BakedQuad> quads=new ArrayList<>();

        overlays.forEach(wallpaper->{
            quads.addAll(wallpaper.createQuad(originQuad));
        });

        return quads;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {

    }
}
