package com.SouthernWall_404.LaplaceAPI.Network.Packet.S2C.DefaultPacks;

import com.SouthernWall_404.LaplaceAPI.Laplace;
import com.SouthernWall_404.LaplaceAPI.Network.ICompoundSerializer;
import com.SouthernWall_404.LaplaceAPI.Network.Packet.S2C.AttachmentPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AttachmentDefaultHandlers {

    public static final ResourceLocation LEVEL=ResourceLocation.fromNamespaceAndPath(Laplace.MODID,"level_attachment_handler");
    public static final ResourceLocation CHUNK=ResourceLocation.fromNamespaceAndPath(Laplace.MODID,"chunk_attachment_handler");
    public static final ResourceLocation BLOCK=ResourceLocation.fromNamespaceAndPath(Laplace.MODID,"block_attachment_handler");

    public static void LevelAttachmentHandler(AttachmentPacket packet, IPayloadContext context) {

        context.enqueueWork(
                () -> {
                    Level level = Minecraft.getInstance().level;
                    ResourceLocation attachmentId = packet.attachmentId();

                    if (level == null) {
                        throw new IllegalStateException("Client level not available for attachment sync");
                    }

                    AttachmentType attachmentType = NeoForgeRegistries.ATTACHMENT_TYPES.get(attachmentId);

                    if(attachmentType==null)
                    {
                        throw new NullPointerException("Unknown Attachment Type:"+attachmentId);
                    }

                    var attachment = level.getData(attachmentType);

                    if (attachment instanceof ICompoundSerializer serializer) {//如是可持久化的数据

                        CompoundTag receivedNbt = packet.modPack();

                        serializer.deserializeNBT(level.registryAccess(),receivedNbt);

                        level.setData(attachmentType,serializer);

                    }
                }
        );
    }

    public static void ChunkAttachmentHandler(AttachmentPacket packet, IPayloadContext context) {

    }

    public static void BlockAttachmentHandler(AttachmentPacket packet, IPayloadContext context) {

    }
}
