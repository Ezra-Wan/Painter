package com.SouthernWall_404.Painter.Common.Network.S2C;

import com.SouthernWall_404.Painter.Painter;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
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

public record RenderS2CPacket(CompoundTag modPack, BlockPos pos) implements CustomPacketPayload {

    public static final Type<RenderS2CPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Painter.MODID, "render_sync_s2c"));

    // 使用 ChunkPos 自带的流编解码器，简洁可靠
    public static final StreamCodec<ByteBuf, RenderS2CPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG, RenderS2CPacket::modPack,
                    ByteBufCodecs.INT, packet -> packet.pos().getX(),
                    ByteBufCodecs.INT, packet -> packet.pos().getY(),
                    ByteBufCodecs.INT, packet -> packet.pos().getZ(),
                    (modTag, x, y,z) -> new RenderS2CPacket(modTag, new BlockPos(x,y,z))
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
    public static void handle(final RenderS2CPacket payload, final IPayloadContext context) {
        // 委托给实际处理类

    }
}