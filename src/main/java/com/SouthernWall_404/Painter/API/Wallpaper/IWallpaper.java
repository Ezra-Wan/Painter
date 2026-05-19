package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.List;

/**
 * 壁纸类的通用接口，承担创建渲染面的职能
 */
public interface IWallpaper extends INBTSerializable<CompoundTag> {

    public String getType();//种类

    public List<BakedQuad> createQuad(BakedQuad originQuad);//建立渲染面的通用方法

    @OnlyIn(Dist.CLIENT)
    public void render(AbstractPaint paint, Direction direction, PoseStack poseStack, VertexConsumer buffer);

    @OnlyIn(Dist.CLIENT)
    public void refresh();
}
