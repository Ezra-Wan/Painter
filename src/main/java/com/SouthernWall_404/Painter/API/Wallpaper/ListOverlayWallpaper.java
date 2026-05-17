package com.SouthernWall_404.Painter.API.Wallpaper;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
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
        quads.forEach(quad->overlays.add(SpriteWallpaperForSwap.builder(quad).build()));

        return new ListOverlayWallpaper(overlays);

    }

    public static Builder builder(){
        return new Builder();
    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public List<BakedQuad> createQuad(int flag, BlockState origin, boolean isTinted, TextureAtlasSprite sprite) {

        List<BakedQuad> quads=new ArrayList<>();

        overlays.forEach(wallpaper->{
            quads.addAll(wallpaper.createQuad(flag,origin,isTinted,sprite));
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

    public static class Builder{

        public Builder() {
        }


    }
}
