package com.SouthernWall_404.LaplaceAPI.Network.Packet.S2C;

import com.SouthernWall_404.LaplaceAPI.Network.Register.PacketRegisters;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;


public class ClientHandler {

    public static void dealWithStaticAttachmentPacket(final AttachmentPacket packet, final IPayloadContext context)
    {
        context.enqueueWork(()->{
           ResourceLocation key =packet.handlerKey();

           IPayloadHandler<AttachmentPacket> handler= PacketRegisters.STATIC_ATTACHMENT_HANDLER.get(key);
           if(handler==null)
           {
               throw new NullPointerException("Unregistered handler method referrence:"+key);
           }
            handler.handle(packet,context);
        });
    }
}
