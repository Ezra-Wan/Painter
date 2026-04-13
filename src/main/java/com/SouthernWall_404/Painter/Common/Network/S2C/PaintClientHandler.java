package com.SouthernWall_404.Painter.Common.Network.S2C;

import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.Client.PaintRender;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PaintClientHandler {

    public static void handlePaintSync(final ChunkS2CPacket packet, final IPayloadContext context)
    {
        context.enqueueWork(()->{
            ChunkPos pos=packet.pos();
            Level level= Minecraft.getInstance().level;
            LevelChunk chunk=level.getChunk(pos.x,pos.z);

            PaintInfo paintInfo=chunk.getData(ModAttachments.PAINT_INFO);

            paintInfo.deserializeNBT(level.registryAccess(),packet.modPack());

            chunk.setData(ModAttachments.PAINT_INFO,paintInfo);

            PaintRender.redraw();
//            if (Minecraft.getInstance().levelRenderer != null) {
//                // 标记整个区块需要重新渲染
//                Minecraft.getInstance().levelRenderer.setBlocksDirty(
//                        pos.getMinBlockX(), 0, pos.getMinBlockZ(),
//                        pos.getMaxBlockX(), level.getMaxBuildHeight(), pos.getMaxBlockZ()
//                );
//            }
//            if (Minecraft.getInstance().levelRenderer != null) {
//                // 标记该区块需要重新渲染（简单方式：标记周围区域脏）
//                Minecraft.getInstance().levelRenderer.setBlocksDirty(pos.getMinBlockX(), 0, pos.getMinBlockZ(),
//                        pos.getMaxBlockX(), level.getMaxBuildHeight(), pos.getMaxBlockZ());
//            }
        });
    }

    public static void handleRenderSync(final RenderS2CPacket packet,final IPayloadContext context)
    {
//        context.enqueueWork(()->{
//            BlockPos pos=packet.pos();
//            Level level=Minecraft.getInstance().level;
//
//            BlockEntity entity=level.getBlockEntity(pos);
//            if(entity!=null&&entity instanceof PaintBlockEntity renderEntity)
//            {
//                renderEntity.fromNBT(packet.modPack());
//            }
//        });
    }
}
