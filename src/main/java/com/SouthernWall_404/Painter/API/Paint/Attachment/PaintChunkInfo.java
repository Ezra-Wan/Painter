package com.SouthernWall_404.Painter.API.Paint.Attachment;

import com.SouthernWall_404.LaplaceAPI.Network.API.Sync;
import com.SouthernWall_404.LaplaceAPI.Network.ICompoundSerializer;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PaintChunkInfo implements ICompoundSerializer {

    //========需要持久化的数据========
    private Set<ChunkPos> paintPoses=new HashSet<>();//记录存有Paint的LevelChunk

    //========不需要持久化的数据========
    private boolean isChanged=false;//记录是否有更新，有则触发全局更新


    public void tick(Level level)
    {
        if(level.isClientSide)
        {
            return;
        }
        if(isChanged)
        {
            Sync.syncLevelAttachmentToAll(level, ModAttachments.PAINT_CHUNK_INFO.get());
            done();
        }
    }


    public Set<ChunkPos> getPaintPoses() {
        return Collections.unmodifiableSet(paintPoses);
    }

    public void setChanged() {
        isChanged = true;
    }

    public void done()
    {
        isChanged=false;
    }

    public void addChunk(ChunkPos pos)
    {
        if(!paintPoses.contains(pos))
        {
            paintPoses.add(pos);

            setChanged();
        }
    }

    public void removeChunk(ChunkPos pos) {
        if (paintPoses.contains(pos)){
            paintPoses.remove(pos);
        }

        setChanged();
    }


    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        // 将 ChunkPos 集合转换为 long 数组 (使用 ChunkPos.toLong())
        long[] chunks = paintPoses.stream().mapToLong(ChunkPos::toLong).toArray();
        nbt.putLongArray("chunks", chunks);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        // 清空现有数据，准备加载持久化数据
        paintPoses.clear();
        // 获取 long 数组，若不存在则返回空数组
        long[] chunks = compoundTag.getLongArray("chunks");
        for (long chunkLong : chunks) {
            paintPoses.add(new ChunkPos(chunkLong));
        }
        // 反序列化后不需要标记 changed，因为数据已与磁盘一致
        done();
    }
}
