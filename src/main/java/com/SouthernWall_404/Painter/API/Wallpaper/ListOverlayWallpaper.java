package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector2f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

/**
 * 应当允许存储多个Wallpaper，包括自身，即自引用
 */
public class ListOverlayWallpaper extends AbstractWallpaper {

    protected List<IWallpaper> overlays;
    public static final String TYPE = "List";

    @Override
    public void cycleTextureUV(BakedQuad originQuad, BlockPos pos) {
        overlays.forEach(wallpaper->wallpaper.cycleTextureUV(originQuad,pos));
    }


    @Override
    public void setUVOffset(BlockPos blockPos, Vector2f uvOffset) {
        overlays.forEach(wallpaper->wallpaper.setUVOffset(blockPos, uvOffset));
    }

    public ListOverlayWallpaper(List<IWallpaper> overlays) {
        super(0,null);
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
    public List<VerticesInfo> createTexture(BakedQuad originQuad) {
        return List.of();
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
    public void refresh() {
        super.refresh();
        overlays.forEach(IWallpaper::refresh);
    }

//    @Override
//    public List<BakedQuad> createQuad(BakedQuad originQuad) {
//
//        List<BakedQuad> quads=new ArrayList<>();
//
//        overlays.forEach(wallpaper->{
//            quads.addAll(wallpaper.createQuad(originQuad));
//        });
//
//        return quads;
//    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        // 调用父类序列化通用字段（tintIndex, altasKey）
        CompoundTag tag = super.serializeNBT(provider);
        
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
        // 调用父类反序列化通用字段（tintIndex, altasKey）
        super.deserializeNBT(provider, compoundTag);
        
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
