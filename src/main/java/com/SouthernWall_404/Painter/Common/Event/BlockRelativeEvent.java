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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockRelativeEvent {

    //========放置失败的原因注册字段========
    public static final String OUT_OF_RANGE = "out_of_range";


    private static final Map<BlockPos, Integer> pendingLightRefresh = new ConcurrentHashMap<>();
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
    public static void onBlockUpdate(BlockEvent.NeighborNotifyEvent event) {
        // 仅在服务端处理（避免客户端发送网络包）
        if (event.getLevel().isClientSide()) return;

//        Map<BlockPos,Integer> posesToRefresh = new HashMap<>();
        BlockPos originPos = event.getPos();
//        Level level = (Level) event.getLevel();
//        BlockState state = level.getBlockState(originPos);
//
//        // 添加 3x3x3 区域
//        for (int dx = -1; dx <= 1; dx++) {
//            for (int dy = -1; dy <= 1; dy++) {
//                for (int dz = -1; dz <= 1; dz++) {
//                    if(PaintValidHelper.hasPaint(level,originPos.offset(dx, dy, dz)))
//                    posesToRefresh.put(originPos.offset(dx, dy, dz),1);
//                }
//            }
//        }
//
//        // 如果是光源方块（放置时），刷新大范围
//        if (isLightSource(state)) {
//            int radius = 16;
//            for (int dx = -radius; dx <= radius; dx++) {
//                for (int dy = -radius; dy <= radius; dy++) {
//                    for (int dz = -radius; dz <= radius; dz++) {
//                        if(PaintValidHelper.hasPaint(level,originPos.offset(dx, dy, dz)))
//                        posesToRefresh.put(originPos.offset(dx, dy, dz),1);
//                    }
//                }
//            }
//        }

        pendingLightRefresh.put(originPos,1);


//        NetworkSync.sendBlockSetToPlayers(
//                event.getLevel().getServer().getPlayerList().getPlayers(),
//                posesToRefresh,
//                ClientHandlers.AO_FRESH_PACKET
//        );
    }

    // 每个服务端 tick 执行一次，用于处理延迟的光照刷新
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (pendingLightRefresh.isEmpty()) return;

        // 为避免遍历中修改，使用快照
        Set<Map.Entry<BlockPos, Integer>> entries = Set.copyOf(pendingLightRefresh.entrySet());
        for (Map.Entry<BlockPos, Integer> entry : entries) {
            BlockPos pos = entry.getKey();
            int remaining = entry.getValue();
            if (remaining <= 1) {
                // 延迟结束，执行刷新
                refreshLightArea(event.getServer().overworld(), pos);
                pendingLightRefresh.remove(pos);
            } else {
                pendingLightRefresh.put(pos, remaining - 1);
            }
        }
    }

    // 刷新以 pos 为中心、半径 16 的立方体区域（与放置光源时的范围一致）
    private static void refreshLightArea(Level level, BlockPos center) {
        // 确保只在服务端执行，并且 world 必须存在
        if (level.isClientSide) return;
        int radius = 16;
        Set<BlockPos> posesToRefresh = new HashSet<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    posesToRefresh.add(center.offset(dx, dy, dz));
                }
            }
        }
        // 发送给所有在线玩家
        if (level.getServer() != null) {
            NetworkSync.sendBlockSetToPlayers(
                    level.getServer().getPlayerList().getPlayers(),
                    posesToRefresh,
                    ClientHandlers.AO_FRESH_PACKET
            );
        }
    }
    // 示例：判断是否为光源方块（需根据实际 Mod 逻辑实现）
    private static boolean isLightSource(BlockState state) {
         return state.getLightEmission() > 0;
    }

    // 示例：获取光源影响半径（需根据实际 Mod 逻辑实现）
    private static int getLightRadius(BlockState state) {
        // 可从 BlockState 中读取附件、属性或硬编码
        // return state.getValue(ModBlockProperties.LIGHT_RADIUS);
        return state.getLightEmission(); // 占位，请替换为真实半径
    }
}