package com.SouthernWall_404.Painter.API.Paint.API;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 基本框架类，需要以下功能：
 * - init 传入一个BlockState，用于初始化一个方块
 * - getFlag 获取统一的标识码
 * - getRenderObject T 从标识码获取渲染内容
 * - render 渲染类，传入渲染直接可用
 * @param <T>
 */
public interface IRender<T extends Object> extends INBTSerializable<CompoundTag> {

    void init(BlockState blockState);//用于初始化

    String getType();


    BlockState getOrigin();

    void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,int packedLight, int packedOverlay);//用于渲染内容

}
