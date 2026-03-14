package com.SouthernWall_404.Painter.Common.Network;

import com.SouthernWall_404.Painter.API.Capability.PaintUtil;
import com.SouthernWall_404.Painter.Common.Network.S2C.PaintS2CPacket;
import com.SouthernWall_404.Painter.Painter;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ClientRequestPack(int typeCode, ChunkPos pos) implements CustomPacketPayload {

    public static final Type<ClientRequestPack> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Painter.MODID, "client_request"));

    public static final StreamCodec<ByteBuf, ClientRequestPack> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    ClientRequestPack::typeCode,
                    ByteBufCodecs.INT, packet -> packet.pos().x,
                    ByteBufCodecs.INT, packet -> packet.pos().z,
                    (code, x, z) -> new ClientRequestPack(code,new ChunkPos(x,z))
            );

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 服务器端处理器
    public static void handle(final ClientRequestPack payload, final IPayloadContext context) {
        // 这里的代码运行在网络线程，enqueueWork 确保主线程执行
        context.enqueueWork(() -> {
            // 确保是服务器端
            if (context.player() instanceof ServerPlayer serverPlayer) {
                handleOnServer(payload, serverPlayer);
            }
        });
    }

    private static void handleOnServer(ClientRequestPack packet, ServerPlayer player) {
        int code = packet.typeCode;
        switch (code) {
            case 1:
                handleSync(player,packet.pos);
                break;
            default:
                break;
        }
    }

    private static void handleSync(ServerPlayer player,ChunkPos pos) {
        PaintUtil.syncToClient(pos,player);
    }

}