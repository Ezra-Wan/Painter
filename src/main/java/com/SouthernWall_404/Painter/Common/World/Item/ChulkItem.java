package com.SouthernWall_404.Painter.Common.World.Item;

import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.API.Tool.ToolContent;
import com.SouthernWall_404.Painter.Common.Content.ComponentContent;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class ChulkItem extends BlockInteractItem {

    public ChulkItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.addAll(ComponentContent.getTooltip(ComponentContent.CHULK, ComponentContent.CHULK_ROW, Style.EMPTY.withColor(ChatFormatting.GRAY)));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public void dealRightClick(PlayerInteractEvent.RightClickBlock event,boolean isInMainHand) {


        event.setCanceled(true);//打断放置
        if(!handCheck(event,isInMainHand))return;//防止副手bug

        Player player = event.getEntity();
        SelectedZone selectedZone = player.getData(ModAttachments.SELECTED_ZONE);//获取选区情况
        if (selectedZone != null) {
            //清理选区
            if (player.isShiftKeyDown()) {
                selectedZone.clear();
                player.displayClientMessage(ToolContent.getMessage(ToolContent.CLEAR, Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
                return;
            }


            String result = selectedZone.setB(event.getPos());//设定B点，若不成功，则反馈
            Style style = Style.EMPTY;
            if (result == ToolContent.PASS) {
                style = style.withColor(ChatFormatting.YELLOW);
            } else {
                style = style.withColor(ChatFormatting.RED);
            }

            player.displayClientMessage(Component.translatable(
                                    ToolContent.getMessagePath(ToolContent.parentString(ToolContent.SELECT_POSB, result)),
                                    event.getPos().getX(),
                                    event.getPos().getY(),
                                    event.getPos().getZ())
                            .withStyle(style),
                    true);
        }
        player.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public void dealLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if(event.getHand()!=InteractionHand.MAIN_HAND)
        {
            return;
        }

        Player player = event.getEntity();
        SelectedZone selectedZone = player.getData(ModAttachments.SELECTED_ZONE);

        //处理选区清理
        if (selectedZone != null) {
            if (player.isShiftKeyDown()) {
                selectedZone.clear();
                player.displayClientMessage(ToolContent.getMessage(ToolContent.CLEAR, Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
                event.setCanceled(true);
                return;
            }

            selectedZone.setA(event.getPos(), event.getFace());
            player.displayClientMessage(Component.translatable(
                            ToolContent.getMessagePath(ToolContent.SELECT_POSA),
                            event.getPos().getX(),
                            event.getPos().getY(),
                            event.getPos().getZ(),
                            ToolContent.getFaceTranslation(event.getFace())
                    ).withStyle(style -> style.withColor(ChatFormatting.YELLOW)),
                    true);
        }
        event.setCanceled(true);
    }
}