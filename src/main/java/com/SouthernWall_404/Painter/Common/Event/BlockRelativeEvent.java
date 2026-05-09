package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintValidHelper;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.World.Item.BlockInteractItem;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;


@EventBusSubscriber(modid = Painter.MODID, bus = EventBusSubscriber.Bus.GAME)
public class BlockRelativeEvent {

    //========放置失败的原因注册字段========
    public static final String OUT_OF_RANGE = "out_of_range";

    //新区块的数据获取有待优化
    @SubscribeEvent
    public static void onBlockRightClicked(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        // 获取主手中的交互物品
        BlockInteractItem mainHand = BlockInteractItem.getFromMainHand(player);
        if (mainHand != null) {
            // 调用物品自身的右键处理方法
            mainHand.dealRightClick(event,true);
        }

        BlockInteractItem offHand = BlockInteractItem.getFromOffHand(player);
        if (offHand != null) {
            // 调用物品自身的右键处理方法
            offHand.dealRightClick(event,false);
        }
    }

    @SubscribeEvent
    public static void onBlockLeftClicked(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        // 获取主手中的交互物品
        BlockInteractItem interactItem = BlockInteractItem.getFromMainHand(player);
        if (interactItem != null) {
            // 调用物品自身的左键处理方法
            interactItem.dealLeftClick(event);
        }
    }
    @SubscribeEvent
    public static void onBlockBroke(BlockEvent.BreakEvent event)
    {
        BlockPos pos=event.getPos();
        Level level=event.getPlayer().level();

        if(PaintValidHelper.hasPaint(level,pos))
        {
            LevelChunk chunk=level.getChunkAt(pos);
            PaintInfo paintInfo=chunk.getData(ModAttachments.PAINT_INFO);

            if(!level.isClientSide) paintInfo.removeRender(level, pos);
        }
    }


    @SubscribeEvent
    public static void onBlockUpdate(BlockEvent.NeighborNotifyEvent event) {
        // 仅在服务端处理（避免客户端发送网络包）
        if (event.getLevel().isClientSide()) return;

        BlockPos originPos = event.getPos();
        Level level = (Level) event.getLevel();
        
        // 收集周围15格内所有涉及的区块
        int radius = 15;

        for(int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                ChunkPos chunkPos = new ChunkPos(originPos.offset(x * radius, 0, z * radius));
                // 只有当区块包含粉刷数据时才加入刷新队列
                if (level.getData(ModAttachments.LEVEL_PAINT_INFO).contains(chunkPos)) {
                    ServerTick.refreshAO(chunkPos);
                }
            }
        }
    }
}