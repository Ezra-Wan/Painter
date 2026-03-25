package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.Painter.API.Paint.Util.PaintBlockUtil;
import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.API.Tool.ToolContent;
import com.SouthernWall_404.Painter.API.Tool.Wall.Filters.EmptyFilter;
import com.SouthernWall_404.Painter.API.Tool.Wall.Filters.SelectFilter;
import com.SouthernWall_404.Painter.API.Tool.Wall.IFilter;
import com.SouthernWall_404.Painter.API.Tool.Wall.WallUtil;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.World.Item.ChulkItem;
import com.SouthernWall_404.Painter.Common.World.Item.PaintBucketItem;
import com.SouthernWall_404.Painter.Common.World.Item.PaintItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockEvent {

    //========放置失败的原因注册字段========
    public static String OUT_OF_RANGE="out_of_range";

    @SubscribeEvent
    public static void onBlockClicked(PlayerInteractEvent.RightClickBlock event)
    {
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
                    player.displayClientMessage(
                            ToolContent.getMessage(ToolContent.parentString(ToolContent.FAILED_TO_PAINT,OUT_OF_RANGE),Style.EMPTY.withColor(ChatFormatting.RED)),
                            true
                    );
                    return;//不做处理
                }
            }

            Item itemToPaint=player.getItemInHand(InteractionHand.OFF_HAND).getItem();
            if(itemToPaint instanceof BlockItem blockItem)
            {
                PaintBlockUtil.paint(level,blockPos,player,event.getFace());
            }

        }

        else if (item instanceof ChulkItem chulkItem) {
            SelectedZone selectedZone=player.getData(ModAttachments.SELECTED_ZONE);
            if(selectedZone!=null)
            {
                if(player.isShiftKeyDown())
                {
                    selectedZone.clear();

                    player.displayClientMessage(ToolContent.getMessage(ToolContent.CLEAR, Style.EMPTY.withColor(ChatFormatting.YELLOW)),true);

                    event.setCanceled(true);
                    return;
                }

                String result=selectedZone.setB(blockPos);
                Style style=Style.EMPTY;
                if(result==ToolContent.PASS){
                    style=style.withColor(ChatFormatting.YELLOW);
                }else {
                    style=style.withColor(ChatFormatting.RED);
                }

                player.displayClientMessage(Component.translatable(
                            ToolContent.getMessagePath(ToolContent.parentString(ToolContent.SELECT_POSB,result)),
                            blockPos.getX(),
                            blockPos.getY(),
                            blockPos.getZ()
                            ).withStyle(style),
                    true);
            }

            event.setCanceled(true);
        }

        else if (item instanceof PaintBucketItem paintBucketItem)
        {
            event.setCanceled(true);
            SelectedZone selectedZone=player.getData(ModAttachments.SELECTED_ZONE);

            if(selectedZone!=null)
            {
                List<IFilter> filters=new ArrayList<>();
                filters.add(new SelectFilter());
                if(selectedZone.isSelecting())
                {
//                    filters.add(new SelectFilter());

                }else {



                }

                Item itemToPaint=player.getItemInHand(InteractionHand.OFF_HAND).getItem();
                if(itemToPaint instanceof BlockItem blockItem)
                {
                    WallUtil.paintOnWall(blockPos,event.getFace(),player,level,blockItem.getBlock().defaultBlockState(),filters);
                }

            }
        }
    }


    @SubscribeEvent
    public static void onBLockLeftClicked(PlayerInteractEvent.LeftClickBlock event)
    {
        Player player=event.getEntity();
        Item item=player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        if(item instanceof ChulkItem chulk)
        {
            BlockPos blockPos=event.getPos();

            SelectedZone selectedZone=player.getData(ModAttachments.SELECTED_ZONE);

            if(selectedZone!=null)
            {

                if(player.isShiftKeyDown())
                {
                    selectedZone.clear();
                    player.displayClientMessage(ToolContent.getMessage(ToolContent.CLEAR, Style.EMPTY.withColor(ChatFormatting.YELLOW)),true);

                    event.setCanceled(true);
                    return;
                }

                selectedZone.setA(blockPos,event.getFace());

                player.displayClientMessage(Component.translatable(
                                ToolContent.getMessagePath(ToolContent.SELECT_POSA),
                                blockPos.getX(),
                                blockPos.getY(),
                                blockPos.getZ(),
                                ToolContent.getFaceTranslation(event.getFace())

                        ).withStyle(style -> style.withColor(ChatFormatting.YELLOW)),
                        true);
            }


            event.setCanceled(true);
        }

        if(item instanceof PaintItem)
        {
            SelectedZone selectedZone=player.getData(ModAttachments.SELECTED_ZONE);
            if(selectedZone!=null)
            {
                selectedZone.clear();
                player.displayClientMessage(ToolContent.getMessage(ToolContent.CLEAR, Style.EMPTY.withColor(ChatFormatting.YELLOW)),true);

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
