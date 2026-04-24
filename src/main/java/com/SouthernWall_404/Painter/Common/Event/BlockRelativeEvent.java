package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkSync;
import com.SouthernWall_404.LaplaceAPI.xNetwork.Packet.S2C.ClientHandler;
import com.SouthernWall_404.Painter.API.Paint.API.AbstractPaint;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintAttachmentHelper;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintValidHelper;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.Network.ClientHandlers;
import com.SouthernWall_404.Painter.Common.World.Item.BlockInteractItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashSet;
import java.util.Set;

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
            paintInfo.removeRender(level, event.getPlayer(), pos);
        }

    }

    @SubscribeEvent
    public static void onBlockUpdate(BlockEvent.NeighborNotifyEvent event)//纯客户端事件
    {
        Set<BlockPos> posesToRefresh=new HashSet<>();

        BlockPos origin=event.getPos();
        // 添加以 origin 为中心的 3x3x3 区域内的所有方块
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    posesToRefresh.add(origin.offset(dx, dy, dz));
                }
            }
        }

        //TODO 将可能出现AO更新、面剔除更新的位置加入
        //对于
        NetworkSync.sendBlockSetToPlayers(event.getLevel().getServer().getPlayerList().getPlayers(),posesToRefresh, ClientHandlers.AO_FRESH_PACKET);
//        Set<>

    }


}