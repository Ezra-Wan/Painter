package com.SouthernWall_404.Painter.Common.Network.S2C;

import com.SouthernWall_404.Painter.Painter;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ChunkS2CPacket(CompoundTag modPack, ChunkPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ChunkS2CPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Painter.MODID, "paint_sync_s2c"));

    // 使用 ChunkPos 自带的流编解码器，简洁可靠
    public static final StreamCodec<ByteBuf, ChunkS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG, ChunkS2CPacket::modPack,
                    ByteBufCodecs.INT, packet -> packet.pos().x,
                    ByteBufCodecs.INT, packet -> packet.pos().z,
                    (modTag, x, z) -> new ChunkS2CPacket(modTag, new ChunkPos(x, z))
            );

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 客户端处理器：接收服务器发送的数据并更新客户端的 Capability
     */
    @OnlyIn(Dist.CLIENT)
    public static void handle(final ChunkS2CPacket payload, final IPayloadContext context) {
        // 委托给实际处理类
        PaintClientHandler.handlePaintSync(payload, context);
    }
}