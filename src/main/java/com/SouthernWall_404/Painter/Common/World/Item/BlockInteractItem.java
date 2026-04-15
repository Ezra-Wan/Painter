package com.SouthernWall_404.Painter.Common.World.Item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;

public abstract class BlockInteractItem extends Item {

    public BlockInteractItem(Properties properties) {
        super(properties);
    }

    //处理右键事件
    public abstract void dealLeftClick(PlayerInteractEvent.LeftClickBlock event);

    public abstract void dealRightClick(PlayerInteractEvent.RightClickBlock event,boolean isInMainHand);


    public boolean handCheck(PlayerInteractEvent.RightClickBlock event,boolean isInMainHand)
    {
        if(event.getHand()==InteractionHand.MAIN_HAND&&!isInMainHand)
        {
            return false;
        }
        if(event.getHand()==InteractionHand.OFF_HAND&&isInMainHand)
        {
            return false;
        }

        return true;
    }

    /**
     * 从玩家手中的主手物品中获取 BlockInteractItem 实例（如果物品是此类的实例）
     * @param player 玩家对象
     * @return BlockInteractItem 实例，如果手中物品不是此类型则返回 null
     */
    @Nullable
    public static BlockInteractItem getFromMainHand(Player player) {
        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        Item item = mainHandItem.getItem();
        if (item instanceof BlockInteractItem blockInteractItem) {
            return blockInteractItem;
        }
        return null;
    }

    @Nullable
    public static BlockInteractItem getFromOffHand(Player player) {
        ItemStack mainHandItem = player.getItemInHand(InteractionHand.OFF_HAND);
        Item item = mainHandItem.getItem();
        if (item instanceof BlockInteractItem blockInteractItem) {
            return blockInteractItem;
        }
        return null;
    }


}
