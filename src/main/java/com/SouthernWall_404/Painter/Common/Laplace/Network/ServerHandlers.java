package com.SouthernWall_404.Painter.Common.Laplace.Network;

import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkRegister;
import com.SouthernWall_404.LaplaceAPI.xNetwork.Packet.C2S.ClientRequestPacket;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Imply.SlabBlockPaint;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintAttachmentHelper;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintSyncHelper;
import com.SouthernWall_404.Painter.Common.Event.ServerTick;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerHandlers {

    public static final ResourceLocation CYCLE_TEXTURE_UV_PACKET = ResourceLocation.fromNamespaceAndPath(Painter.MODID, "cycle_texture_uv");
    public static final ResourceLocation BUCKET_CONFIG_SYNC_PACKET = ResourceLocation.fromNamespaceAndPath(Painter.MODID, "bucket_config_sync");

    public static void register() {
        NetworkRegister.registerClientRequestHandler(CYCLE_TEXTURE_UV_PACKET, ((packet, context) -> cycleTextureUVHandler( packet, context)));
    }

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

            // 直接设置UV偏移值（服务端不执行计算，只存储）
            paint.setUVOffset(direction, u, v);
            
//            // 标记区块需要保存
//            LevelChunk chunk = level.getChunkAt(blockPos);
//            chunk.setUnsaved(true);

            ServerTick.update(new ChunkPos(blockPos));
            // 同步到所有加载该区块的玩家
//            PaintSyncHelper.syncChunkToAll(level,chunk.getPos());
        });
    }
}
