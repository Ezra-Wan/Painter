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
        SelectedZone zone = player.getData(ModAttachments.SELECTED_ZONE_FOR_SWAP);//获取选区情况

        if (zone == null) return;

        // 判断当前是设置A点还是B点
        if (!zone.isCreating()) {
            // 第一次右键：设置A点
            zone.setA(event.getPos(), event.getFace());
            player.displayClientMessage(Component.translatable(
                            ToolContent.getMessagePath(ToolContent.SELECT_POSA),
                            event.getPos().getX(),
                            event.getPos().getY(),
                            event.getPos().getZ(),
                            ToolContent.getFaceTranslation(event.getFace())
                    ).withStyle(style -> style.withColor(ChatFormatting.YELLOW)),
                    true);
        } else {
            // 第二次右键：设置B点并形成选区
            zone.setB(event.getPos());

            //TODO 记得加反馈信息
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
        SelectedZone zone = player.getData(ModAttachments.SELECTED_ZONE_FOR_SWAP);

        // 左键清除选区
        zone.clear();
        player.displayClientMessage(ToolContent.getMessage(ToolContent.CLEAR, Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
        event.setCanceled(true);
    }
}