package com.SouthernWall_404.Painter.Common.Laplace.Network;

import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkRegister;
import com.SouthernWall_404.LaplaceAPI.xNetwork.Packet.S2C.BlockSetPacket;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintAttachmentHelper;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Set;

public class ClientHandlers {

    public static final ResourceLocation AO_FRESH_PACKET=ResourceLocation.fromNamespaceAndPath(Painter.MODID,"ao_refresh");

    @OnlyIn(Dist.CLIENT)
    public static void register()
    {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NetworkRegister.registerHandler(AO_FRESH_PACKET,((packet, context) -> aoRefreshHandler(packet,context)));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void aoRefreshHandler(BlockSetPacket packet, IPayloadContext context) {
        // ctx.enqueueWork 保证在主线程执行
        context.enqueueWork(() -> {
            Minecraft mc=Minecraft.getInstance();
            if (mc!=null) {
                Level level=mc.level;
                if(level==null)return;

                Set<BlockPos> freshPositions=packet.positions();

                freshPositions.forEach((blockPos -> {
                    AbstractPaint paint= PaintAttachmentHelper.getPaint(level,blockPos);

                    if(paint!=null)
                    {
                        paint.refreshVisible();
                        paint.refreshAO(blockPos);
                    }
                }));
            }
        });
    }
}
