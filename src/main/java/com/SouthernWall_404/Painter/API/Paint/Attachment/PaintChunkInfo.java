package com.SouthernWall_404.Painter.API.Paint.Attachment;

import com.SouthernWall_404.LaplaceAPI.VertinCore.IAttachment;
import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkSync;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.Client.PaintRender;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

public class PaintChunkInfo implements IAttachment {

    //========需要持久化的数据========
    private Set<ChunkPos> paintPoses=new HashSet<>();//记录存有Paint的LevelChunk

    //========不需要持久化的数据========
    private boolean isChanged=false;//记录是否有更新，有则触发全局更新


    //TODO 需要应用起来
    /**
     * 统一进度
     * @param level
     */
    public void tick(Level level)
    {
        if(level.isClientSide)
        {
            return;
        }
        if(isChanged)
        {
//            NetworkSync.syncLevelAttachmentToAll(level,level.players(),ModAttachments.PAINT_CHUNK_INFO.get());//TODO 以后记得修好
            done();
        }
    }

    public Map<BlockPos, AbstractRender<?,?>>  getRenderNearby(BlockPos blockPos)
    {
        Map<BlockPos, AbstractRender<?,?>> result=new HashMap<>();
        Minecraft mc=Minecraft.getInstance();
        if(mc ==null)return new HashMap<>();
        ChunkPos playerPos=new ChunkPos(blockPos);

        Level level=mc.level;

        int viewDistance=mc.options.simulationDistance().get()+1;

        paintPoses.forEach(pos -> {
            if(pos.getChessboardDistance(playerPos)<=viewDistance)
            {

                LevelChunk chunk=level.getChunkSource().getChunkNow(pos.x,pos.z);
                if(chunk!=null)
                {
                    PaintInfo paintInfo=chunk.getData(ModAttachments.PAINT_INFO);
                    result.putAll(paintInfo.getPaints());
                }
            }
        });
        return result;
    }

    public Set<ChunkPos> getPaintPosesNearby(BlockPos pos) {
        ChunkPos playerPos=new ChunkPos(pos);
        Set<ChunkPos> chunkNearby=new HashSet<>();
        Minecraft mc=Minecraft.getInstance();
        if(mc==null)return new HashSet<>();
        int viewDistance=mc.options.getEffectiveRenderDistance();
        paintPoses.forEach((chunkpos)->{

            if(playerPos.distanceSquared(chunkpos)<=viewDistance*viewDistance)chunkNearby.add(chunkpos);//如果在视距内，入队
        });
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

            PaintRender.addChunk(pos);//TODO 可能需要检测

            setChanged();
        }
    }

    public void removeChunk(ChunkPos pos) {
        if (paintPoses.contains(pos)){
            paintPoses.remove(pos);
            PaintRender.removeChunk(pos);
        }

        setChanged();
    }

    public Set<ChunkPos> getPaintPoses() {
        return paintPoses;
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
