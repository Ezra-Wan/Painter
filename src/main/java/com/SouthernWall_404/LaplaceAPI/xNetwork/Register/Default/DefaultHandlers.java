package com.SouthernWall_404.LaplaceAPI.xNetwork.Register.Default;

import com.SouthernWall_404.LaplaceAPI.xNetwork.Packet.S2C.DefaultPacks.AttachmentDefaultHandlers;
import com.SouthernWall_404.LaplaceAPI.xNetwork.Register.PacketRegisters;

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
