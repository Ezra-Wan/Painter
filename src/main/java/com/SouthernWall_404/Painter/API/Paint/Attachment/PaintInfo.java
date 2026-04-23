package com.SouthernWall_404.Painter.API.Paint.Attachment;


import com.SouthernWall_404.LaplaceAPI.VertinCore.IAttachment;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.PaintContent;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintSyncHelper;
import com.SouthernWall_404.Painter.Client.PaintRender;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;

public class PaintInfo implements IAttachment {

    //========不需要持久化的数据========
    private int tick=0;
    //========需要持久化的数据=========
    private final Map<BlockPos, AbstractRender<?, ?>> renders = new HashMap<>();

    // 提供无参构造，供附件自动创建
    public PaintInfo() {}

    public Map<BlockPos, AbstractRender<?, ?>> getRenders() {
        return renders;
    }

    public void putRender(Level level,BlockPos pos, AbstractRender<?, ?> render) {
        renders.put(pos, render);
        PaintChunkInfo chunkInfo=level.getData(ModAttachments.PAINT_CHUNK_INFO);
        chunkInfo.addChunk(level.getChunkAt(pos).getPos());



    }

    public void removeRender(BlockPos pos) {

        renders.remove(pos);

        if(Minecraft.getInstance()!=null)
        {
            PaintRender.redraw();
        }
    }

    public void tick(ChunkPos pos) {
        int minX = pos.getMinBlockX();   // pos.x * 16
        int minZ = pos.getMinBlockZ();   // pos.z * 16
        int maxX = pos.getMaxBlockX();   // minX + 15
        int maxZ = pos.getMaxBlockZ();

        //TODO 考虑修改分块配置项
        int amount = 2;          // 4x4 分块
        int step = 16 / amount;  // 每个分块边长 4

        if (Minecraft.getInstance() != null) {
            int sectionX = tick / amount;
            int sectionZ = tick % amount;

            int startX = minX + sectionX * step;
            int startZ = minZ + sectionZ * step;
            int endX = (sectionX == amount - 1) ? maxX : startX + step - 1;
            int endZ = (sectionZ == amount - 1) ? maxZ : startZ + step - 1;

            renders.forEach((blockPos, render) -> {
                if (render instanceof AbstractPaint paint) {
                    int x = blockPos.getX();
                    int z = blockPos.getZ();
                    if (x >= startX && x <= endX && z >= startZ && z <= endZ) {
                        paint.refreshAO(blockPos);
                    }
                }
            });
        }

        tick = (tick + 1) % (amount*amount);  // 0~15 循环
    }

    /**
     * 用于处理服务器的单端移除，执行同步
     * @param level
     * @param player
     * @param pos
     */
    public void removeRender(Level level, Player player,BlockPos pos) {

        removeRender(pos);

        PaintSyncHelper.syncToClient(level.getChunkAt(pos).getPos(),player);

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