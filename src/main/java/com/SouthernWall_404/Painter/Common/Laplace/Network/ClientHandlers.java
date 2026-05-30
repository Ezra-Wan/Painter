package com.SouthernWall_404.Painter.Common.Laplace.Network;

import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkRegister;
import com.SouthernWall_404.LaplaceAPI.xNetwork.Packet.S2C.BlockSetPacket;
import com.SouthernWall_404.LaplaceAPI.xNetwork.Packet.S2C.ServerNoticePacket;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintAttachmentHelper;
import com.SouthernWall_404.Painter.API.Tool.BucketSelectionConfig;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public class ClientHandlers {

    public static final ResourceLocation AO_FRESH_PACKET=ResourceLocation.fromNamespaceAndPath(Painter.MODID,"ao_refresh");

    @OnlyIn(Dist.CLIENT)
    public static void register()
    {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NetworkRegister.registerServerNoticeHandler(AO_FRESH_PACKET,((packet, context) -> aoRefreshHandler(packet,context)));
            // 注册油漆桶配置同步包处理器
            NetworkRegister.registerServerNoticeHandler(ServerHandlers.BUCKET_CONFIG_SYNC_PACKET, ((packet, context) -> bucketConfigSyncHandler(packet, context)));
        }
    }
    @OnlyIn(Dist.CLIENT)
    public static void aoRefreshHandler(ServerNoticePacket packet, IPayloadContext context) {
        // ctx.enqueueWork 保证在主线程执行
        context.enqueueWork(() -> {
            Minecraft mc=Minecraft.getInstance();
            if (mc!=null) {
                Level level=mc.level;
                if(level==null)return;
    
                // 从 CompoundTag 中反序列化 ChunkPos 列表
                ListTag chunkList = packet.data().getList("chunks", CompoundTag.TAG_COMPOUND);
    
                for (int i = 0; i < chunkList.size(); i++) {
                    CompoundTag chunkTag = chunkList.getCompound(i);
                    int x = chunkTag.getInt("x");
                    int z = chunkTag.getInt("z");
                    ChunkPos chunkPos = new ChunkPos(x, z);
                    level.getChunk(chunkPos.x, chunkPos.z).getData(ModAttachments.PAINT_INFO.get()).setChanged();
                }
            }
        });
    }
    
    /**
     * 处理油漆桶配置同步包
     * 在服务端切换配置后，同步到客户端
     */
    @OnlyIn(Dist.CLIENT)
    public static void bucketConfigSyncHandler(ServerNoticePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) return;
    
            // 从数据包中读取配置状态
            CompoundTag tag = packet.data();
            boolean requiresSelection = tag.getBoolean("requiresSelection");
    
            // 更新客户端的配置附件
            BucketSelectionConfig config = mc.player.getData(ModAttachments.BUCKET_SELECTION_CONFIG);
            config.setRequiresSelection(requiresSelection);
        });
    }
}
