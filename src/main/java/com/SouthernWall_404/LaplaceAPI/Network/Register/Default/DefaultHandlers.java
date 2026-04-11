package com.SouthernWall_404.LaplaceAPI.Network.Register.Default;

import com.SouthernWall_404.LaplaceAPI.Network.ModChannels;
import com.SouthernWall_404.LaplaceAPI.Network.Packet.S2C.AttachmentPacket;
import com.SouthernWall_404.LaplaceAPI.Network.Packet.S2C.DefaultPacks.AttachmentDefaultHandlers;
import com.SouthernWall_404.LaplaceAPI.Network.Register.PacketRegisters;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class DefaultHandlers {
    public static void register()
    {
        PacketRegisters.registerStaticAttachmentS2CHandler(
                AttachmentDefaultHandlers.LEVEL,
                AttachmentDefaultHandlers::LevelAttachmentHandler
        );
        PacketRegisters.registerStaticAttachmentS2CHandler(
                AttachmentDefaultHandlers.CHUNK,
                AttachmentDefaultHandlers::ChunkAttachmentHandler
        );
        PacketRegisters.registerStaticAttachmentS2CHandler(
                AttachmentDefaultHandlers.BLOCK,
                AttachmentDefaultHandlers::BlockAttachmentHandler
        );
    }
}
