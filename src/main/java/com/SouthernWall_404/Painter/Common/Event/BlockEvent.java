package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.Painter.API.Paint.RenderBlockAPI;
import com.SouthernWall_404.Painter.Common.World.Item.PaintItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class BlockEvent {

    @SubscribeEvent
    public static void onBlockClicked(PlayerInteractEvent.RightClickBlock event)
    {
        Level level=event.getLevel();
        BlockPos blockPos=event.getPos();

        Player player=event.getEntity();
        ItemStack itemStack=player.getItemInHand(InteractionHand.MAIN_HAND);
        Item item=itemStack.getItem().asItem();
        if(item instanceof PaintItem item1)
        {
            RenderBlockAPI.onFirstPaint(level,blockPos);
        }
    }


}
