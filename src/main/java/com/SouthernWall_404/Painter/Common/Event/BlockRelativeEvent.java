package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkSync;
import com.SouthernWall_404.Painter.API.Paint.Attachment.LevelPaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintSyncHelper;
import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintValidHelper;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.Laplace.Network.ClientHandlers;
import com.SouthernWall_404.Painter.Common.World.Item.BlockInteractItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockRelativeEvent {

    //TODO 小馋猫在这
    //========放置失败的原因注册字段========
    public static final String OUT_OF_RANGE = "out_of_range";


    private static final Map<BlockPos, Integer> pendingLightRefresh = new ConcurrentHashMap<>();

    private static final Set<ChunkPos> chunkToRefresh = new HashSet<>();
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


        for(int x=-1;x<=1;x++)
        {
            for (int z=-1;z<=1;z++)
            {
                chunkToRefresh.add(new ChunkPos(originPos.offset(x*radius,0,z*radius)));
            }
        }
    }

    // 每个服务端 tick 执行一次，用于处理延迟的光照刷新

    //TODO 似乎区块更新会大量触发方块更新事件
    // 解决方案：
    // 将受光照影响的区块标记为脏
    // 在客户端进行全部的refreshAO

    @SubscribeEvent
    public static void onServerTick(LevelTickEvent.Post event) {

        if(!chunkToRefresh.isEmpty())
        {
            Level level=event.getLevel();
            if(!level.isClientSide){
                PaintSyncHelper.syncAO(level,chunkToRefresh);
                chunkToRefresh.clear();
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