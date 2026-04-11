package com.SouthernWall_404.LaplaceAPI.Network.API;

import com.SouthernWall_404.LaplaceAPI.Network.ICompoundSerializer;
import com.SouthernWall_404.LaplaceAPI.Network.ModChannels;
import com.SouthernWall_404.LaplaceAPI.Network.Packet.S2C.AttachmentPacket;
import com.SouthernWall_404.LaplaceAPI.Network.Packet.S2C.DefaultPacks.AttachmentDefaultHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class Sync {

    public static void syncLevelAttachmentToAll(Level level, AttachmentType attachmentType)
    {
        if(!level.isClientSide)
        {
            PlayerList playerList =level.getServer().getPlayerList();
            if(playerList==null)throw new NullPointerException("Empty Player List");

            List<ServerPlayer> players=playerList.getPlayers();
            for(ServerPlayer player:players)
            {
                if(player.level()==level)
                {
                    syncLevelAttachment(level,attachmentType,player);

                }
            }


        }else {
            throw new IllegalStateException("Cannot Sync To All in Dist.Client");
        }
    }

    public static void syncLevelAttachment(Level level, AttachmentType attachmentType, Player player)
    {
        if(level.isClientSide)//C2S,未编写
        {

        }
        else//S2C
        {
            var attachment =level.getData(attachmentType);


            ResourceLocation attachmentId= NeoForgeRegistries.ATTACHMENT_TYPES.getKey(attachmentType);
            if(attachmentId==null)throw new IllegalStateException("Unknown Attachment:"+attachmentType);


            if(attachment instanceof ICompoundSerializer serializer)
            {
                CompoundTag modPack=serializer.serializeNBT(level.registryAccess());

                if(modPack==null)
                {
                    return;
                }
                ModChannels.sendToClient(new AttachmentPacket(modPack,new BlockPos(0,0,0),AttachmentDefaultHandlers.LEVEL,attachmentId),player);

            }else
            {

                throw new IllegalArgumentException("Unserializable Attachment:"+attachmentId);
            }
        }

    }
}
