package com.SouthernWall_404.LaplaceAPI.Network;

import com.SouthernWall_404.LaplaceAPI.Network.Packet.S2C.ClientHandler;
import com.SouthernWall_404.LaplaceAPI.Network.Packet.S2C.AttachmentPacket;
import com.SouthernWall_404.LaplaceAPI.Network.Register.Default.DefaultHandlers;
import com.SouthernWall_404.LaplaceAPI.Network.Register.Default.DefaultPackets;
import com.SouthernWall_404.LaplaceAPI.Network.Register.PacketRegister;
import com.SouthernWall_404.LaplaceAPI.Network.Register.PacketRegisters;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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

        registerC2S(registrar);
        registerS2C(registrar);

        registerDefault(registrar);
    }

    public static void registerDefault(PayloadRegistrar registrar)
    {
        DefaultPackets.register(registrar);
        DefaultHandlers.register();
    }

    public static void registerS2C(PayloadRegistrar registrar)
    {
        for(PacketRegister packetRegister : PacketRegisters.getS2C())
        {
            registrar.playToClient(
                    packetRegister.TYPE,
                    packetRegister.STREAM_CODEC,
                    packetRegister.HANDLER
            );
        }
    }

    public static void registerC2S(PayloadRegistrar registrar)
    {
        for(PacketRegister packetRegister : PacketRegisters.getC2S())
        {
            registrar.commonToServer(
                    packetRegister.TYPE,
                    packetRegister.STREAM_CODEC,
                    packetRegister.HANDLER
            );
        }
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