package com.SouthernWall_404.Painter.Client;

import com.SouthernWall_404.Painter.Common.Init.ModBlockEntities;
import com.SouthernWall_404.Painter.Painter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

// 确保只在客户端侧执行
@EventBusSubscriber(modid = Painter.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class PainterClient {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 注册你的 BlockEntity 渲染器
        // 第一个参数是你的 BlockEntityType，第二个参数是渲染器的提供者

    }
}
