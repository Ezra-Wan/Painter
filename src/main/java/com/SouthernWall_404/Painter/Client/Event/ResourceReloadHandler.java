package com.SouthernWall_404.Painter.Client.Event;

import com.SouthernWall_404.Painter.Client.PaintRender;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = Painter.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ResourceReloadHandler {

    @SubscribeEvent
    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                // 准备阶段，无需操作
                return null;
            }
            @Override
            protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profiler) {
                // 应用阶段，无需额外操作
                Minecraft mc=Minecraft.getInstance();
                if(mc!=null){
                    Level level=mc.level;
                    if(level!=null){
                        PaintRender.refresh();//刷新渲染缓存
                    }
                }
            }
        });
    }
}