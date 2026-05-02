package com.SouthernWall_404.Painter.API.Paint.Attachment;


import com.SouthernWall_404.LaplaceAPI.VertinCore.IAttachment;
import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkSync;
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

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PaintInfo implements IAttachment {

    //========不需要持久化的数据========
    private int tick=0;
    //========需要持久化的数据=========
    private final Map<BlockPos, AbstractPaint> paints = new HashMap<>();

    // 提供无参构造，供附件自动创建
    public PaintInfo() {}

    /**
     * 获取原始 AbstractPaint 映射（类型安全）
     */
    public Map<BlockPos, AbstractPaint> getPaints() {
        return paints;
    }

    /**
     * 获取 AbstractRender 视图（兼容旧代码）
     */
    public Map<BlockPos, AbstractRender<?, ?>> getRenders() {
        // 复制一份，避免外部修改原 Map，同时满足泛型要求
        Map<BlockPos, AbstractRender<?, ?>> copy = new HashMap<>();
        for (Map.Entry<BlockPos, AbstractPaint> entry : paints.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    public void putPaints(Level level, BlockPos pos, AbstractPaint paint) {
        paints.put(pos, paint);
        PaintChunkInfo chunkInfo=level.getData(ModAttachments.PAINT_CHUNK_INFO);
        chunkInfo.addChunk(level.getChunkAt(pos).getPos());

    }

    public void removeRender(BlockPos pos) {

        paints.remove(pos);

        if(Minecraft.getInstance()!=null)
        {
            PaintRender.setChanged();
        }
    }

    /**
     * 时域分布光照更新算法
     * 将区块中所有 AbstractPaint 的光照更新在时域中进行分布式计算，以降低每帧渲染压力
     * 目前作为技术储备
     * TODO 考虑配置项启用
     * @param pos
     */
    public void TDDAO(ChunkPos pos) {
        int minX = pos.getMinBlockX();   // pos.x * 16
        int minZ = pos.getMinBlockZ();   // pos.z * 16
        int maxX = pos.getMaxBlockX();   // minX + 15
        int maxZ = pos.getMaxBlockZ();

        int amount = 2;          // 4x4 分块
        int step = 16 / amount;  // 每个分块边长 4

        if (Minecraft.getInstance() != null) {
            int sectionX = tick / amount;
            int sectionZ = tick % amount;

            int startX = minX + sectionX * step;
            int startZ = minZ + sectionZ * step;
            int endX = (sectionX == amount - 1) ? maxX : startX + step - 1;
            int endZ = (sectionZ == amount - 1) ? maxZ : startZ + step - 1;

            paints.forEach((blockPos, render) -> {
                if (render instanceof AbstractPaint paint) {
                    int x = blockPos.getX();
                    int z = blockPos.getZ();
                    if (x >= startX && x <= endX && z >= startZ && z <= endZ) {
                        paint.refreshAO(blockPos);
                        paint.refreshVisible();
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

        if(paints.isEmpty()){//如果
            level.getData(ModAttachments.PAINT_CHUNK_INFO).removeChunk(new ChunkPos(pos));
        }

        PaintSyncHelper.sync(level.getChunkAt(pos).getPos(),player);

    }


    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag result = new CompoundTag();
        ListTag rendersList = new ListTag();

        for (Map.Entry<BlockPos, AbstractPaint> entry : paints.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            BlockPos pos = entry.getKey();
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", pos.getX());
            posTag.putInt("y", pos.getY());
            posTag.putInt("z", pos.getZ());
            entryTag.put("pos", posTag);

            AbstractPaint paint = entry.getValue();
            entryTag.putString("type", paint.getType());
            entryTag.put("data", paint.serializeNBT(provider));
            rendersList.add(entryTag);
        }
        result.put("renders", rendersList);
        return result;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        paints.clear();
        ListTag rendersList = tag.getList("renders", Tag.TAG_COMPOUND);

        for (int i = 0; i < rendersList.size(); i++) {
            CompoundTag entryTag = rendersList.getCompound(i);
            CompoundTag posTag = entryTag.getCompound("pos");
            BlockPos pos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            String type = entryTag.getString("type");
            CompoundTag data = entryTag.getCompound("data");

            AbstractPaint paint = createPaintByType(type, provider, data,pos);
            if (paint != null) {
                paints.put(pos, paint);
            }
        }
    }

    /**
     * 根据类型创建 AbstractPaint 实例，并反序列化数据
     */
    @Nullable
    private AbstractPaint createPaintByType(String type, HolderLookup.Provider provider, CompoundTag data,BlockPos pos) {
        // 通过工厂创建实例（假设工厂能返回 AbstractPaint 子类）
        Function<BlockPos,AbstractRender> function= PaintContent.getRender(type);
        AbstractRender render=function.apply(pos);
        if (!(render instanceof AbstractPaint paint)) {
            // 类型不匹配，记录错误并返回 null
            return null;
        }
        paint.deserializeNBT(provider, data);
        return paint;
    }
}