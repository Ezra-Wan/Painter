package com.SouthernWall_404.Painter.API.Wallpaper;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.List;

/**
 * 壁纸类的通用接口，承担创建渲染面的职能
 */
public interface IWallpaper extends INBTSerializable<CompoundTag> {

    public String getType();//种类

    public List<BakedQuad> createQuad(int flag, BlockState origin, boolean isTinted, TextureAtlasSprite sprite);//建立渲染面的通用方法
}
