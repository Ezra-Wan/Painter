package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.Painter.Common.World.Item.BlockInteractItem;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class BlockEvent {

    //========放置失败的原因注册字段========
    public static final String OUT_OF_RANGE = "out_of_range";

    @SubscribeEvent
    public static void onBlockRightClicked(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        // 获取主手中的交互物品
        BlockInteractItem interactItem = BlockInteractItem.getFromMainHand(player);
        if (interactItem != null) {
            // 调用物品自身的右键处理方法
            interactItem.dealRightClick(event);
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

    // 注意：isPaintable 方法如果不再被其他地方使用，可以考虑删除或移入相关工具类
    public static boolean isPaintable(Level level, BlockPos blockPos) {
        // 原逻辑是直接返回 true，可以根据需要保留或修改
        return true;
    }
}