package com.SouthernWall_404.Painter.API.Wallpaper;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Wallpapers {

    private  static Map<String, BiFunction<HolderLookup.Provider,CompoundTag, IWallpaper>> wallpapers=new HashMap<>();
    public static void register(String type, BiFunction<HolderLookup.Provider,CompoundTag, IWallpaper> wallpaper){
        wallpapers.put(type, wallpaper);
    }

    public static IWallpaper create(String type,HolderLookup.Provider provider, CompoundTag tag){
        return wallpapers.get(type).apply(provider,tag);
    }

    static {
        register(SpriteWallpaper.TYPE, SpriteWallpaper::new);
        register(ListOverlayWallpaper.TYPE, ListOverlayWallpaper::new);
    }

}
