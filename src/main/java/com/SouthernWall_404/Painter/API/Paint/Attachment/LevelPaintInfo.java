package com.SouthernWall_404.Painter.API.Paint.Attachment;

import com.SouthernWall_404.LaplaceAPI.VertinCore.API.Attachmemt.AbstractLevelAttachment;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashSet;
import java.util.Set;

public class LevelPaintInfo extends AbstractLevelAttachment {

    public static final ResourceLocation TYPE_ID = ResourceLocation.fromNamespaceAndPath(Painter.MODID, "level_paint_info");
    public Set<ChunkPos> paintChunks =new HashSet<>();
    public LevelPaintInfo() {
        super(TYPE_ID);
    }

    public void setPaintChunks(Set<ChunkPos> paintChunks) {
        this.paintChunks = paintChunks;
        setChanged();
    }

    public Set<ChunkPos> getPaintChunks() {
        return paintChunks;
    }

    public void add(ChunkPos chunkPos) {
        paintChunks.add(chunkPos);
        setChanged();
    }

    public boolean contains(ChunkPos chunkPos) {
        return paintChunks.contains(chunkPos);
    }

    public void remove(ChunkPos chunkPos) {
        paintChunks.remove(chunkPos);
        setChanged();
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        
        // 序列化 paintChunks
        ListTag chunksList = new ListTag();
        for (ChunkPos chunkPos : paintChunks) {
            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putInt("x", chunkPos.x);
            chunkTag.putInt("z", chunkPos.z);
            chunksList.add(chunkTag);
        }
        tag.put("paintChunks", chunksList);
        
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        paintChunks.clear();
        
        // 反序列化 paintChunks
        if (compoundTag.contains("paintChunks", Tag.TAG_LIST)) {
            ListTag chunksList = compoundTag.getList("paintChunks", Tag.TAG_COMPOUND);
            for (int i = 0; i < chunksList.size(); i++) {
                CompoundTag chunkTag = chunksList.getCompound(i);
                int x = chunkTag.getInt("x");
                int z = chunkTag.getInt("z");
                paintChunks.add(new ChunkPos(x, z));
            }
        }
    }
}
