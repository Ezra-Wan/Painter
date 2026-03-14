// PaintInfo.java
package com.SouthernWall_404.Painter.API.Capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;

/**
 * 添加至区块
 * 记录位置、朝向和uuid（注：注释中的uuid已过时，实际用BlockPos作键）
 * 面向对象编程
 */
public class PaintInfo implements INBTSerializable<CompoundTag> {

    //========需要持久化的数据========
    private Map<BlockPos, Paint> paints = new HashMap<>();
    //========不需要持久化的数据========
    private boolean isChanged=false;
    //========构造方法========



    public PaintInfo() {
    }

    public PaintInfo(Map<BlockPos, Paint> paints) {
        this.paints = paints;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag paintsTag = new CompoundTag();

        for (Map.Entry<BlockPos, Paint> entry : paints.entrySet()) {
            // 将 BlockPos 转换为 long 作为键（用字符串表示）
            String posKey = Long.toString(entry.getKey().asLong());
            // 每个 Paint 序列化为其自身的 CompoundTag
            CompoundTag paintTag = entry.getValue().serializeNBT(provider);
            paintsTag.put(posKey, paintTag);
        }

        tag.put("paints", paintsTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        paints.clear();

        CompoundTag paintsTag = compoundTag.getCompound("paints");
        for (String key : paintsTag.getAllKeys()) {
            // 从字符串解析出 BlockPos
            long posLong = Long.parseLong(key);
            BlockPos pos = BlockPos.of(posLong);

            // 反序列化 Paint 对象
            CompoundTag paintTag = paintsTag.getCompound(key);
            Paint paint = new Paint();
            paint.deserializeNBT(provider, paintTag);

            paints.put(pos, paint);
        }
    }

    //========方法类========



    public Map<BlockPos, Paint> getPaints() {
        return paints;
    }

    public void addPaint(BlockPos pos, Paint paint) {
        paints.put(pos, paint);
    }

    public void addPaint(BlockPos blockPos, Direction direction, ResourceLocation key) {

        addPaint(blockPos, new Paint(direction, key));
    }

    public void removePaint(BlockPos blockPos) {
        paints.remove(blockPos);
    }

    public Paint getPaint(BlockPos blockPos) {
        return paints.getOrDefault(blockPos, new Paint(Direction.SOUTH, ResourceLocation.parse("minecraft:stone")));
        // 或者返回 null 并让调用方处理，此处返回一个默认 Paint 避免空指针
    }
}