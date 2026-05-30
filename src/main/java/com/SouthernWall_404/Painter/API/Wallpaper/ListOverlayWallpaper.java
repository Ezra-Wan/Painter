package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.LaplaceAPI.Math37.Vector2f;
import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuad.VerticesInfo;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

/**
 * 应当允许存储多个Wallpaper，包括自身，即自引用
 */

/**
 * 注意：此处会重写大部分原有的缓存处理方法，ListOverlayWallpaper 只缓存ao 可见性
 */
public class ListOverlayWallpaper extends AbstractWallpaper {

    public static final String TYPE = "List";


    // ========数据========
    protected List<IWallpaper> overlays;

    @Override
    public void cycleTextureUV(BakedQuad originQuad, AbstractPaint paint) {
        overlays.forEach(wallpaper->wallpaper.cycleTextureUV(originQuad,paint));
    }

    //========构造方法========
    public ListOverlayWallpaper(List<IWallpaper> overlays) {
        super(0,null);
        this.overlays=overlays;
    }

    /**
     * 从NBT建立
     * @param provider
     * @param tag
     */
    public ListOverlayWallpaper(HolderLookup.Provider provider,CompoundTag tag){
        this(List.of());
        deserializeNBT(provider,tag);
    }

    /**
     * 从Quads建立
     * @param quads
     * @return
     */
    public static ListOverlayWallpaper ofQuads(List<BakedQuad> quads){

        List<IWallpaper> overlays=new ArrayList<>();
        quads.forEach(quad->overlays.add(new SpriteWallpaper( quad)));

        return new ListOverlayWallpaper(overlays);

    }
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

    //========基础方法========
    @Override
    public void setUVOffset(Vector2f uvOffset, AbstractPaint paint) {
        overlays.forEach(wallpaper->wallpaper.setUVOffset(uvOffset, paint));
    }
    @Override
    public String getType() {
        return TYPE;
    }
    public List<IWallpaper> getOverlays() {
        return overlays;
    }

    public void setOverlays(List<IWallpaper> overlays) {
        this.overlays = overlays;
        refresh();
    }

    //========渲染方法========

    /**
     * 抛空，此处不做Texture处理
     * @param originQuad
     * @return
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    protected List<VerticesInfo> createTexture(BakedQuad originQuad) {
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


}
