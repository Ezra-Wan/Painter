package com.SouthernWall_404.Painter.API.Paint;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.List;

public class RenderUtil {

//    public static IRender addSimple(BlockState origin,)

    public static ResourceLocation getKey(Block block)
    {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        // 将方块注册名转换为纹理路径，例如 "minecraft:iron_block" -> "minecraft:block/iron_block"
        ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(
                key.getNamespace(), "block/" + key.getPath());

        return textureLocation;
    }

    public static TextureAtlasSprite getFaceFromBlock(BlockState state, Direction face)
    {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);

        RandomSource random = RandomSource.create(); // 可传入种子，通常用静态随机即可
        List<BakedQuad> quads = model.getQuads(state, face, random, ModelData.EMPTY, RenderType.solid());

        if (!quads.isEmpty()) {
            // 通常一个面可能包含多个四边形，这里取第一个作为代表
            BakedQuad quad = quads.get(0);
            return quad.getSprite(); // NeoForge 扩展方法
        }

        // 若该面无四边形，回退到粒子图标（常为默认面纹理）
        return model.getParticleIcon();
    }

    /**
     * 获取方块状态对应的渲染类型
     * @param state 方块状态
     * @return 渲染类型，如果获取失败则返回 null
     */
    public static RenderType getRenderType(BlockState state) {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

        try {
            // 获取方块模型使用的渲染类型
            var model = dispatcher.getBlockModel(state);
            var renderTypes = model.getRenderTypes(state, mc.level.random, ModelData.EMPTY);

            // 返回第一个非空的渲染类型
            if (renderTypes != null) {
                for (RenderType type : renderTypes) {
                    if (type != null) {
                        return type;
                    }
                }
            }
        } catch (Exception e) {
            // 如果获取失败，返回默认的 solid 类型
            return RenderType.solid();
        }

        return RenderType.solid();
    }


    public static List<BakedQuad> getQuads(BlockState state, @Nullable Direction direction,
                                           RandomSource random, ModelData modelData, RenderType renderType) {
        Minecraft mc = Minecraft.getInstance();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);
        return model.getQuads(state, direction, random, modelData, renderType);
    }

    /**
     * 简化版本：使用默认随机源和空的模型数据
     */
    public static List<BakedQuad> getQuads(BlockState state, @Nullable Direction direction, RenderType renderType) {
        return getQuads(state, direction, RandomSource.create(), ModelData.EMPTY, renderType);
    }

    /**
     * 获取指定方向的所有 quads（包括使用默认渲染类型）
     */
    public static List<BakedQuad> getQuads(BlockState state, @Nullable Direction direction) {
        return getQuads(state, direction, RenderUtil.getRenderType(state));
    }
}

