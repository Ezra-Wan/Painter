package com.SouthernWall_404.LaplaceAPI.Network.Register.Default;

import com.SouthernWall_404.LaplaceAPI.Network.Packet.S2C.AttachmentPacket;
import com.SouthernWall_404.LaplaceAPI.Network.Packet.S2C.ClientHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class DefaultPackets {

    public static void register(PayloadRegistrar registrar){
        registrar.playToClient(
                AttachmentPacket.TYPE,
                AttachmentPacket.STREAM_CODEC,
                ClientHandler::dealWithStaticAttachmentPacket
        );
    }
}
