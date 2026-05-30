package com.SouthernWall_404.Painter.Client.Event;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintSyncHelper;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * 用于在客户端执行队列化操作
 */
@EventBusSubscriber(modid = Painter.MODID, bus = EventBusSubscriber.Bus.GAME,value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ClientTick {

    public static Set<AbstractRender> toUpdate=new HashSet<>();
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {

        if(Minecraft.getInstance()!=null&&Minecraft.getInstance().level!=null)
        {
            //执行render更新需求
            Set<AbstractRender> toUpdateSnapshot;
            synchronized (toUpdate) {
                toUpdateSnapshot = new HashSet<>(toUpdate);//创建副本
                toUpdate.clear();//创建副本并清空原有，以免冲突
            }

            PaintSyncHelper.syncRenders(toUpdateSnapshot);//批量同步所有待处理的render
        }


        return;

    }
    public static synchronized void updateRender(AbstractRender render)
    {
        toUpdate.add(render);
    }


}
