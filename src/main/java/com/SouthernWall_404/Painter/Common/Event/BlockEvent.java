package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.Painter.API.Paint.PaintBlockAPI;
import com.SouthernWall_404.Painter.Common.World.Item.PaintItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class BlockEvent {

    @SubscribeEvent
    public static void onBlockClicked(PlayerInteractEvent.RightClickBlock event)
    {
//        if (event.getLevel().isClientSide) return; // 只服务端执行
        Level level=event.getLevel();
        BlockPos blockPos=event.getPos();

        Player player=event.getEntity();
        ItemStack itemStack=player.getItemInHand(InteractionHand.MAIN_HAND);
        Item item=itemStack.getItem().asItem();
        if(item instanceof PaintItem)
        {
            Item itemToPaint=player.getItemInHand(InteractionHand.OFF_HAND).getItem();
            if(itemToPaint instanceof BlockItem blockItem)
            {
                Block block=blockItem.getBlock();
//                PaintUtil.addPaint(level,blockPos,new Paint(Direction.SOUTH,block));
//                RenderBlockAPI.onPaint(level,blockPos);
                PaintBlockAPI.placeARender(level,blockPos,player,event.getFace());

                event.setCanceled(true);
            }


        }
    }


}
