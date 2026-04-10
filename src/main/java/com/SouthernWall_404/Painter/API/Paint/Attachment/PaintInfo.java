package com.SouthernWall_404.Painter.API.Paint.Attachment;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.PaintContent;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PaintInfo implements INBTSerializable<CompoundTag> {

    private final Map<BlockPos, AbstractRender<?, ?>> renders = new HashMap<>();

    // 提供无参构造，供附件自动创建
    public PaintInfo() {}

    public Map<BlockPos, AbstractRender<?, ?>> getRenders() {
        return renders;
    }

    public void putRender(BlockPos pos, AbstractRender<?, ?> render) {
        renders.put(pos, render);
    }

    public AbstractRender<?, ?> removeRender(BlockPos pos) {
        return renders.remove(pos);
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag result = new CompoundTag();
        ListTag rendersList = new ListTag();

        for (Map.Entry<BlockPos, AbstractRender<?, ?>> entry : renders.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            BlockPos pos = entry.getKey();
            // 手动序列化 BlockPos
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            entryTag.put("pos", posTag);

            AbstractRender<?, ?> render = entry.getValue();
            entryTag.putString("type", render.getType());
            entryTag.put("data", render.serializeNBT(provider));
            rendersList.add(entryTag);
        }
        result.put("renders", rendersList);
        return result;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        renders.clear();
        ListTag rendersList = tag.getList("renders", Tag.TAG_COMPOUND);

        for (int i = 0; i < rendersList.size(); i++) {
            CompoundTag entryTag = rendersList.getCompound(i);
            CompoundTag posTag = entryTag.getCompound("pos");
            BlockPos pos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            String type = entryTag.getString("type");
            CompoundTag data = entryTag.getCompound("data");

            AbstractRender<?, ?> render = createRenderByType(type, provider, data);
            if (render != null) {
                renders.put(pos, render);
            }
        }
    }


    /**
     * 使用 PaintContent 工厂创建渲染器实例
     */
    private AbstractRender<?, ?> createRenderByType(String type, HolderLookup.Provider provider, CompoundTag data) {
        // 直接通过无参构造创建实例（需要子类提供无参构造）
        BlockState blockState= Blocks.AIR.defaultBlockState();
        AbstractRender<?, ?> render = PaintContent.getRender(type).apply(blockState);
        if (render == null) {
            return null;
        }
        render.deserializeNBT(provider, data);
        return render;
    }
}