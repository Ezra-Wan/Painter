package com.SouthernWall_404.Painter.API.Paint.Util.Paint;

import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkSync;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.Laplace.Network.ClientHandlers;
import com.SouthernWall_404.Painter.Common.Laplace.Network.ServerHandlers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Set;

public class PaintSyncHelper {

    public static void sync(ChunkPos pos, Player player) {
        Level level = player.level();
        NetworkSync.syncChunkAttachment(level, pos,ModAttachments.PAINT_INFO.get(), player);
    }

    /**
     * 仅在服务端执行，禁止在服务端执行
     * 用于将当前更新向所有玩家执行
     */
    public static void syncChunkToAll(Level level, ChunkPos pos)
    {
        NetworkSync.syncChunkAttachmentToAll(level, pos,level.players(),ModAttachments.PAINT_INFO.get());
    }


    @OnlyIn(Dist.CLIENT)
    public static void syncRenders(List<AbstractRender> renders){

        CompoundTag tag=new CompoundTag();
        ListTag renderList=new ListTag();
        Minecraft mc=Minecraft.getInstance();
        if(mc!=null){

            renders.forEach(render -> {
                CompoundTag renderTag=new CompoundTag();

                CompoundTag pos=new CompoundTag();
                pos.putInt("x",render.getBlockPos().getX());
                pos.putInt("y",render.getBlockPos().getY());
                pos.putInt("z",render.getBlockPos().getZ());
                renderTag.put("pos",pos);

                renderTag.put("render",render.serializeNBT(mc.level.registryAccess()));

                renderList.add(renderTag);
            });
        }

        tag.put("renders",renderList);


        NetworkSync.requireToServer(ServerHandlers.RENDER_UPDATE_PACKET,tag);
    }

    /**
     * 同步UV偏移数据到服务端
     * @param blockPos 方块位置
     * @param direction 方向
     * @param uv UV偏移数组，长度为2，[0]为U，[1]为V
     */
    public static void syncPaintUV(BlockPos blockPos, Direction direction, float[] uv) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", blockPos.getX());
        tag.putInt("y", blockPos.getY());
        tag.putInt("z", blockPos.getZ());
        tag.putString("direction", direction.getName());
        tag.putFloat("u", uv[0]);
        tag.putFloat("v", uv[1]);


        NetworkSync.requireToServer(ServerHandlers.CYCLE_TEXTURE_UV_PACKET,tag);
    }

    public static void syncRefresh(Level level, Set<ChunkPos> poses)
    {
        CompoundTag tag = new CompoundTag();
        ListTag chunkList = new ListTag();

        for (ChunkPos pos : poses) {
            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putInt("x", pos.x);
            chunkTag.putInt("z", pos.z);
            chunkList.add(chunkTag);
        }

        tag.put("chunks", chunkList);


        //TODO 以后记得改用level.players
        level.players().forEach(player ->
                NetworkSync.sendNoticeToPlayer(tag,player,ClientHandlers.AO_FRESH_PACKET));
    }
}
