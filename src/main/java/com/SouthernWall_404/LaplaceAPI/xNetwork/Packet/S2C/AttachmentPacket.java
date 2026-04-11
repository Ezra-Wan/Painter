package com.SouthernWall_404.LaplaceAPI.xNetwork.Packet.S2C;

import com.SouthernWall_404.LaplaceAPI.Laplace;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record AttachmentPacket(@NotNull CompoundTag modPack, BlockPos pos, ResourceLocation handlerKey, ResourceLocation attachmentId) implements CustomPacketPayload {

    public static final Type<AttachmentPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Laplace.MODID, "attachment_s2c"));

    // 使用 ChunkPos 自带的流编解码器，简洁可靠
    public static final StreamCodec<ByteBuf, AttachmentPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG, AttachmentPacket::modPack,
                    ByteBufCodecs.INT, packet -> packet.pos().getX(),
                    ByteBufCodecs.INT, packet -> packet.pos().getY(),
                    ByteBufCodecs.INT, packet -> packet.pos().getZ(),
                    ByteBufCodecs.STRING_UTF8,packet -> packet.handlerKey().toString(),
                    ByteBufCodecs.STRING_UTF8,packet -> packet.attachmentId().toString(),

                    (modTag, x, y,z,handlerKey,attachmentId) ->
                            new AttachmentPacket(
                                    modTag,
                                    new BlockPos(x,y,z),
                                    ResourceLocation.read(handlerKey).getOrThrow(),
                                    ResourceLocation.read(attachmentId).getOrThrow()
                            )
            );

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}