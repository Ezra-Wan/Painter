package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.Painter.Painter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

/**
 * 应当允许存储多个Wallpaper，包括自身，即自引用
 */
public class ListOverlayWallpaper implements IWallpaper {

    private List<IWallpaper> overlays;
    public static final String TYPE = "List";

    public ListOverlayWallpaper(List<IWallpaper> overlays) {
        this.overlays=overlays;
    }

    public ListOverlayWallpaper(HolderLookup.Provider provider,CompoundTag tag){
        this(List.of());
        deserializeNBT(provider,tag);
    }

    public static ListOverlayWallpaper ofQuads(List<BakedQuad> quads){

        List<IWallpaper> overlays=new ArrayList<>();
        quads.forEach(quad->overlays.add(SpriteWallpaper.builder(quad).build()));

        return new ListOverlayWallpaper(overlays);

    }

    @Override
    public String getType() {
        return TYPE;
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
        CompoundTag tag = new CompoundTag();
        
        // 保存类型标识
        tag.putString("type", TYPE);
        
        // 序列化 overlays 列表
        net.minecraft.nbt.ListTag overlaysList = new net.minecraft.nbt.ListTag();
        for (IWallpaper wallpaper : overlays) {
            CompoundTag wallpaperTag = wallpaper.serializeNBT(provider);
            // 确保每个 wallpaper 都有 type 字段
            if (!wallpaperTag.contains("type")) {
                wallpaperTag.putString("type", wallpaper.getType());
            }
            overlaysList.add(wallpaperTag);
        }
        tag.put("overlays", overlaysList);
        
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        // 反序列化 overlays 列表
        net.minecraft.nbt.ListTag overlaysList = compoundTag.getList("overlays", net.minecraft.nbt.Tag.TAG_COMPOUND);
        
        List<IWallpaper> deserializedOverlays = new ArrayList<>();
        for (int i = 0; i < overlaysList.size(); i++) {
            CompoundTag wallpaperTag = overlaysList.getCompound(i);
            String type = wallpaperTag.getString("type");
            
            // 使用 Wallpapers 注册表反序列化
            IWallpaper wallpaper = Wallpapers.create(type,provider, wallpaperTag);
            if (wallpaper != null) {
                deserializedOverlays.add(wallpaper);
            }
        }
        
        this.overlays = deserializedOverlays;
    }

}
