package com.SouthernWall_404.Painter.API.Wallpaper;

import com.SouthernWall_404.LaplaceAPI.RegulappleEngine.BakedQuadRender;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Util.RenderUtil;
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
import net.minecraft.world.level.block.state.BlockState;
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
    
    private BlockState blockState;
    private Direction face;

    public BlockWallPaper(BlockState material,Direction face) {
        super(getOverlays(material,face));
        this.blockState = material;
        this.face = face;
    }

    public BlockWallPaper(HolderLookup.Provider provider, CompoundTag tag) {
        super(List.of());
        deserializeNBT(provider, tag);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(AbstractPaint paint,Direction direction, PoseStack poseStack, VertexConsumer buffer)
    {
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null)
        {
            Level level=mc.level;
            if(level!=null)
            {
                if(aoFace==null)refreshAO(paint,direction);
                if(isVisible<0)refreshVisibles(paint,direction);
                if(quads.isEmpty()){
                    quads=createQuad(RenderUtil.getQuadsForDirection(blockState,paint.translateFace(direction)).getFirst());//TODO 不是很标准的编程
                }

                if(isVisible>0){//正数为可见
                    quads.forEach(quad -> BakedQuadRender.renderInOfferredAO(quad, blockState,paint.getRenderVec().get(paint.getFlag(direction)), poseStack, buffer, aoFace));
                }
            }
        }

    }


    //TODO 添加对于方块的特殊调整
    // createQuad需要修改
    // render需要添加对于颜色的处理

    public static List<IWallpaper> getOverlays(BlockState material, Direction face)
    {
        List<IWallpaper> overlays=new ArrayList<>();
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null) {
            List<BakedQuad> quads = getQuadsForDirection(material, face);
            quads.forEach(quad -> overlays.add(SpriteWallpaper.builder(quad).build()));
        }
        return overlays;
    }

    public static List<BakedQuad> getQuadsForDirection(BlockState state, Direction face) {

        BakedModel model = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(state);
        RandomSource random = RandomSource.create();
        RenderType renderType = RenderUtil.getRenderType(state);
        return model.getQuads(state, face, random, ModelData.EMPTY, renderType);
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        
        tag.putString("type", TYPE);
        
        if (blockState != null) {
            Tag stateTag = BlockState.CODEC.encodeStart(
                provider.createSerializationContext(NbtOps.INSTANCE),
                blockState
            ).getOrThrow();
            tag.put("blockState", stateTag);
        }
        
        if (face != null) {
            tag.putInt("face", face.get3DDataValue());
        }

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        if (compoundTag.contains("blockState")) {
            Tag stateTag = compoundTag.get("blockState");
            blockState = BlockState.CODEC.parse(
                provider.createSerializationContext(NbtOps.INSTANCE),
                stateTag
            ).getOrThrow();
        }
        
        if (compoundTag.contains("face")) {
            int faceValue = compoundTag.getInt("face");
            face = Direction.from3DDataValue(faceValue);
        }

        if (blockState != null && face != null) {
            this.overlays = getOverlays(blockState, face);
        }
    }
}
