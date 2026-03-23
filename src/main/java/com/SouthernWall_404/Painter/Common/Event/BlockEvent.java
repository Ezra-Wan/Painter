package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.Painter.API.Paint.Util.PaintBlockUtil;
import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.World.Item.ChulkItem;
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
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class BlockEvent {

    @SubscribeEvent
    public static void onBlockClicked(PlayerInteractEvent.RightClickBlock event)
    {
//        if (event.getLevel().isClientSide) return; // 只服务端执行
        Level level=event.getLevel();
        BlockPos blockPos=event.getPos();


        if(!isPaintable(level,blockPos))return;


        Player player=event.getEntity();
        ItemStack itemStack=player.getItemInHand(InteractionHand.MAIN_HAND);
        Item item=itemStack.getItem().asItem();
        if(item instanceof PaintItem)
        {
            event.setCanceled(true);
            SelectedZone selectedZone=player.getData(ModAttachments.SELECTED_ZONE);
            if (selectedZone!=null&&selectedZone.isSelecting())
            {
                if(!selectedZone.isInSurface(blockPos,event.getFace()))//如果不在平面范围内
                {
                    return;//不做处理
                }
                else//在平面范围内
                {
                    if (!selectedZone.contains(blockPos))//却不在圈定范围内
                    {
                        return;//判定为超出选区，不做处理
                    }
                }
            }

            Item itemToPaint=player.getItemInHand(InteractionHand.OFF_HAND).getItem();
            if(itemToPaint instanceof BlockItem blockItem)
            {
                Block block=blockItem.getBlock();

                PaintBlockUtil.placeARender(level,blockPos,player,event.getFace());


            }

        }

        else if (item instanceof ChulkItem chulkItem)
        {
            SelectedZone selectedZone=player.getData(ModAttachments.SELECTED_ZONE);
            if(selectedZone!=null)
            {
                if(player.isShiftKeyDown())
                {
                    selectedZone.clear();

                    //TODO：这里加清空信息提示

                    event.setCanceled(true);
                    return;
                }

                if(selectedZone.setB(blockPos))//如果设置成功
                {
                    //TODO:这里添加一个信息显示
                }
            }

            event.setCanceled(true);
        }
    }


    @SubscribeEvent
    public static void onBLockLeftClicked(PlayerInteractEvent.LeftClickBlock event)
    {
        Player player=event.getEntity();
        Item item=player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        if(item instanceof ChulkItem chulk)
        {
            BlockPos pos=event.getPos();

            SelectedZone selectedZone=player.getData(ModAttachments.SELECTED_ZONE);

            if(selectedZone!=null)
            {

                if(player.isShiftKeyDown())
                {
                    selectedZone.clear();

                    //TODO：这里加清空信息提示

                    event.setCanceled(true);
                    return;
                }

                selectedZone.setA(pos,event.getFace());
            }


            event.setCanceled(true);
        }

    }

    public static boolean isPaintable(Level level,BlockPos blockPos)
    {

//        BlockState blockState=level.getBlockState(blockPos);
//        if(blockState.isCollisionShapeFullBlock(level, blockPos))
//        {
//            return true;
//        }
//
//        return false;
        return true;
    }

}
