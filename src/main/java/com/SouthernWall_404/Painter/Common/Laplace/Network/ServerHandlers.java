package com.SouthernWall_404.Painter.Common.Laplace.Network;

import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkRegister;
import com.SouthernWall_404.LaplaceAPI.xNetwork.Packet.C2S.ClientRequestPacket;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Imply.SlabBlockPaint;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintAttachmentHelper;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintSyncHelper;
import com.SouthernWall_404.Painter.Common.Event.ServerTick;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public class ServerHandlers {
    public static final ResourceLocation RENDER_UPDATE_PACKET = ResourceLocation.fromNamespaceAndPath(Painter.MODID, "render_update");

    public static void register() {
        NetworkRegister.registerClientRequestHandler(RENDER_UPDATE_PACKET, ((packet, context) -> renderUpdateHandler(packet, context)));
    }


    public static void renderUpdateHandler(ClientRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CompoundTag tag = packet.data();
            Level level= context.player().level();

            ListTag rendersTag = tag.getList("renders", 10);
            rendersTag.forEach(render -> {

                if(render instanceof CompoundTag renderTag){
                    if(renderTag.contains("pos"))
                    {
                        CompoundTag posPack = (CompoundTag) renderTag.get("pos");
                        BlockPos pos = new BlockPos(posPack.getInt("x"), posPack.getInt("y"), posPack.getInt("z"));

                        if(renderTag.contains("render"))
                        {

                            AbstractPaint paint=level.getChunkAt(pos).getData(ModAttachments.PAINT_INFO).getPaints().get( pos);
                            paint.deserializeNBT(level.registryAccess(),(CompoundTag) renderTag.get("render"));

                            PaintSyncHelper.syncChunkToAll(level,new ChunkPos(pos));
                        }
                    }
                }
            });


        });
    }
}
