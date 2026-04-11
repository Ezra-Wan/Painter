package com.SouthernWall_404.LaplaceAPI.xNetwork.Register;

import com.SouthernWall_404.LaplaceAPI.xNetwork.Packet.S2C.AttachmentPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.*;

public class PacketRegisters {

    private static Set<PacketRegister> S2C=new HashSet<>();//S2C包
    private static Set<PacketRegister> C2S=new HashSet<>();

    public static Map<ResourceLocation,IPayloadHandler<AttachmentPacket> > STATIC_ATTACHMENT_HANDLER=new HashMap<>();


    public static Set<PacketRegister> getC2S() {
        return Collections.unmodifiableSet(C2S);
    }

    public static Set<PacketRegister> getS2C() {
        return Collections.unmodifiableSet(S2C);
    }

    public static void registerS2C(PacketRegister<?> packetRegister)
    {
        S2C.add(packetRegister);
    }

    public static void registerC2S(PacketRegister<?> packetRegister)
    {
        C2S.add(packetRegister);
    }

    public static void registerStaticAttachmentS2CHandler(ResourceLocation key,IPayloadHandler<AttachmentPacket> handler)
    {
        if(STATIC_ATTACHMENT_HANDLER.containsKey(key))throw new IllegalStateException("Repeatedly Registerred Static Attachment Method:"+key);
        STATIC_ATTACHMENT_HANDLER.put(key,handler);
    }
}
