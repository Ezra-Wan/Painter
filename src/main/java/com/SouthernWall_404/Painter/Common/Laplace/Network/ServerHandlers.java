package com.SouthernWall_404.Painter.Common.Laplace.Network;

import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkRegister;
import com.SouthernWall_404.LaplaceAPI.xNetwork.Packet.C2S.ClientRequestPacket;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Imply.SlabBlockPaint;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintAttachmentHelper;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintSyncHelper;
import com.SouthernWall_404.Painter.Common.Event.ServerTick;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class ServerHandlers {

    public static final ResourceLocation CYCLE_TEXTURE_UV_PACKET = ResourceLocation.fromNamespaceAndPath(Painter.MODID, "cycle_texture_uv");
    public static final ResourceLocation BUCKET_CONFIG_SYNC_PACKET = ResourceLocation.fromNamespaceAndPath(Painter.MODID, "bucket_config_sync");

    public static final ResourceLocation RENDER_UPDATE_PACKET = ResourceLocation.fromNamespaceAndPath(Painter.MODID, "render_update");

    public static void register() {
        NetworkRegister.registerClientRequestHandler(CYCLE_TEXTURE_UV_PACKET, ((packet, context) -> cycleTextureUVHandler( packet, context)));
        NetworkRegister.registerClientRequestHandler(RENDER_UPDATE_PACKET, ((packet, context) -> renderUpdateHandler(packet, context)));
    }


    public static void renderUpdateHandler(ClientRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CompoundTag tag = packet.data();
            Level level= context.player().level();

            ListTag rendersTag = tag.getList("renders", 10);
            rendersTag.forEach(render -> {

                if(render instanceof CompoundTag renderTag){
                    if(renderTag.contains("pos"))
                    {
                        CompoundTag posPack = (CompoundTag) renderTag.get("pos");
                        BlockPos pos = new BlockPos(posPack.getInt("x"), posPack.getInt("y"), posPack.getInt("z"));

                        if(renderTag.contains("render"))
                        {

                            AbstractPaint paint=level.getChunkAt(pos).getData(ModAttachments.PAINT_INFO).getPaints().get( pos);
                            paint.deserializeNBT(level.registryAccess(),(CompoundTag) renderTag.get("render"));

                            PaintSyncHelper.syncChunkToAll(level,new ChunkPos(pos));
                        }
                    }
                }
            });


        });
    }

    //TODO 暂且保留作为备用
    @Deprecated
    public static void cycleTextureUVHandler(ClientRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) return;

            Level level = player.level();
            if (level.isClientSide) return;

            // 解码数据包
            var tag = packet.data();
            int x = tag.getInt("x");
            int y = tag.getInt("y");
            int z = tag.getInt("z");
            BlockPos blockPos = new BlockPos(x, y, z);

            String directionName = tag.getString("direction");
            net.minecraft.core.Direction direction = net.minecraft.core.Direction.byName(directionName);
            if (direction == null) return;

            float u = tag.getFloat("u");
            float v = tag.getFloat("v");

            // 获取Paint对象
            AbstractPaint paint = PaintAttachmentHelper.getPaint(level, blockPos);
            if (paint == null) return;
//            // 标记区块需要保存
//            LevelChunk chunk = level.getChunkAt(blockPos);
//            chunk.setUnsaved(true);

            ServerTick.update(new ChunkPos(blockPos));
            // 同步到所有加载该区块的玩家
//            PaintSyncHelper.syncChunkToAll(level,chunk.getPos());
        });
    }
}
