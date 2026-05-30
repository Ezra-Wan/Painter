package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuadRender;
import com.SouthernWall_404.LaplaceAPI.UlrichToolBox.Blocks.BlockClientUtil;
import com.SouthernWall_404.LaplaceAPI.UlrichToolBox.Blocks.BlockUtil;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintOperationHelper;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
import com.SouthernWall_404.Painter.API.Paint.Util.Wallpaper.WallpaperBlockHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.List;

/**
 * 应当在这里处理与方块相关的逻辑，属于ListOverlayPaper的导出
 *
 * 对于方块纹理的专用wallpaper
 */
public class BlockWallPaper extends ListOverlayWallpaper{
    public static final String TYPE = "block";

    //========数据========
    private BlockState material;
    private Direction face;

    //========构造方法=========
    public BlockWallPaper(BlockState material,Direction face) {
        super(WallpaperBlockHelper.getOverlays(material,face));
        this.material = material;
        this.face = face;
    }

    public BlockWallPaper(HolderLookup.Provider provider, CompoundTag tag) {
        super(List.of());
        deserializeNBT(provider, tag);
    }



    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        tag.putString("type", TYPE);

        if (material != null) {
            Tag stateTag = BlockState.CODEC.encodeStart(
                    provider.createSerializationContext(NbtOps.INSTANCE),
                    material
            ).getOrThrow();
            tag.put("blockState", stateTag);
        }

        if (face != null) {
            tag.putInt("face", face.get3DDataValue());
        }

        tag.put("list", super.serializeNBT(provider));

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        if (compoundTag.contains("blockState")) {
            Tag stateTag = compoundTag.get("blockState");
            material = BlockState.CODEC.parse(
                    provider.createSerializationContext(NbtOps.INSTANCE),
                    stateTag
            ).getOrThrow();
        }

        if (compoundTag.contains("face")) {
            int faceValue = compoundTag.getInt("face");
            face = Direction.from3DDataValue(faceValue);
        }

        if(compoundTag.contains("list")){
            super.deserializeNBT(provider, compoundTag.getCompound("list"));
        }
    }

    //=========基本方法========

    /**
     * 默认的材质修改方法，带有渲染更新
     * @param material
     */
    public void setMaterial(BlockState material) {
        this.material = material;

        setOverlays(WallpaperBlockHelper.getOverlays(material,face));

    }
    @Override
    public String getType() {
        return TYPE;
    }
    //========业务方法========

    /**
     * 修改方块内容
     */
    public void cycleTextureDir() {
        if (material != null) {
            setMaterial(BlockUtil.cycleInDirection( material));
        }
    }

    /**
     * 使用方块进行渲染，以免出现着色问题
     */
    @Override
    protected void renderQuad(AbstractPaint paint, Direction direction, PoseStack poseStack, VertexConsumer buffer) {
        quads.forEach(quad -> BakedQuadRender.renderInOfferredAO(quad, material,paint.getRenderVec().get(paint.getFlag(direction)), poseStack, buffer, aoFace));
    }
}
