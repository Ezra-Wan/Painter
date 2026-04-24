package com.SouthernWall_404.Painter.Common.Network;

import com.SouthernWall_404.Painter.Common.Network.S2C.PaintClientHandler;
import com.SouthernWall_404.Painter.Common.Network.S2C.ChunkS2CPacket;
import com.SouthernWall_404.Painter.Common.Network.S2C.RenderS2CPacket;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Painter.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModChannels {

    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // ========== C2S 包 ==========

        registrar.commonToServer(
                ClientRequestPack.TYPE,
                ClientRequestPack.STREAM_CODEC,
                ClientRequestPack::handle
        );

//        registrar.commonToServer(
//                PaintSyncC2SPacket.TYPE,
//                PaintSyncC2SPacket.STREAM_CODEC,
//                PaintSyncC2SPacket::handle
//        );
        // ========== S2C 包 ==========

        registrar.playToClient(
                ChunkS2CPacket.TYPE,
                ChunkS2CPacket.STREAM_CODEC,
                PaintClientHandler::handlePaintSync
        );

        registrar.playToClient(
                RenderS2CPacket.TYPE,
                RenderS2CPacket.STREAM_CODEC,
                PaintClientHandler::handleRenderSync
        );
    }

    // 发送工具
    public static <MSG extends CustomPacketPayload> void sendToServer(MSG message) {
        PacketDistributor.sendToServer(message);
    }

    public static <MSG extends CustomPacketPayload> void sendToClient(MSG message, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, message);
        }
    }
}