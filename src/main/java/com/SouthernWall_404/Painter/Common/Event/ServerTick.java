package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintSyncHelper;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 服务端Level Tick事件处理器
 * 仅在服务端的Level tick时执行，客户端不会触发
 */
@EventBusSubscriber(modid = Painter.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerTick {

    private static Set<ChunkPos> toRefreshAO = new HashSet<>();
    private static Set<ChunkPos> toUpdate=new HashSet<>();


    public static void refreshAO(ChunkPos pos)
    {
        toRefreshAO.add(pos);
    }
    /**
     * 添加需要同步的区块到待处理队列
     * 此方法可以在客户端或服务端调用，但只会在服务端生效
     * 
     * @param pos 需要同步的区块位置
     */


    public static void update(ChunkPos pos)
    {
        // 使用静态字段检查是否为客户端环境
        // 在专用服务器上 Minecraft.getInstance() 返回 null
        // 在单机模式下，通过 level.isClientSide 判断更准确
        toUpdate.add(pos);
    }

    @SubscribeEvent
    public static void onServerLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        
        /* 确保只在服务端执行
         * LevelTickEvent.Post 在服务端和客户端都会触发，
         * 必须通过 isClientSide 检查来区分
         */

        //TODO 考虑添加时域分布发包
        if(level.isClientSide)return;

        if (!level.isClientSide()) {

            if(!toUpdate.isEmpty()){
                // 批量同步所有待处理的区块
                toUpdate.forEach(pos -> PaintSyncHelper.syncChunkToAll(level, pos));

                // 清空待处理队列
                toUpdate.clear();
            }

            if(!ServerTick.toRefreshAO.isEmpty())
            {
                PaintSyncHelper.syncAO(level,toRefreshAO);
                ServerTick.toRefreshAO.clear();
            }
        }


    }
}
