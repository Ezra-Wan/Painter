package com.SouthernWall_404.LaplaceAPI.Network.Register;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public class PacketRegister<T extends CustomPacketPayload> {

    public final CustomPacketPayload.Type<T> TYPE;
    public final StreamCodec<ByteBuf, T> STREAM_CODEC;
    public final IPayloadHandler<T> HANDLER;

    public PacketRegister(CustomPacketPayload.Type<T> TYPE, StreamCodec<ByteBuf, T> STREAM_CODEC, IPayloadHandler<T> HANDLER) {
        this.TYPE = TYPE;
        this.STREAM_CODEC = STREAM_CODEC;
        this.HANDLER = HANDLER;
    }
}
