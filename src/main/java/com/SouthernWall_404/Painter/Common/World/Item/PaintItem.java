package com.SouthernWall_404.Painter.Common.World.Item;

import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintUtil;
import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.API.Tool.ToolContent;
import com.SouthernWall_404.Painter.Common.Content.ComponentContent;
import com.SouthernWall_404.Painter.Common.Event.BlockRelativeEvent;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class PaintItem extends BlockInteractItem {

    public PaintItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.addAll(ComponentContent.getTooltip(ComponentContent.BRUSH, ComponentContent.BRUSH_ROW, Style.EMPTY.withColor(ChatFormatting.GRAY)));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public void dealRightClick(PlayerInteractEvent.RightClickBlock event,boolean isInMainHand) {
        // 原 BlockEvent 中处理 PaintItem 右键的逻辑
        if(handCheck(event,isInMainHand))return;
        event.setCanceled(true);
        var level = event.getLevel();
        var blockPos = event.getPos();
        var player = event.getEntity();

        // 检查选区
        SelectedZone selectedZone = player.getData(ModAttachments.SELECTED_ZONE);
        if (selectedZone != null && selectedZone.isSelecting()) {
            if (!selectedZone.isInSurface(blockPos, event.getFace())) {
                player.displayClientMessage(
                        ToolContent.getMessage(ToolContent.parentString(ToolContent.FAILED_TO_PAINT, BlockRelativeEvent.OUT_OF_RANGE), Style.EMPTY.withColor(ChatFormatting.RED)),
                        true
                );
                return;
            }
        }
        // 执行涂色
        PaintUtil.dealBrushClick(level, blockPos, player, event.getFace(),isInMainHand);
        player.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public void dealLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        // 原 BlockEvent 中处理 PaintItem 左键的逻辑（清除选区）
        var player = event.getEntity();
        SelectedZone selectedZone = player.getData(ModAttachments.SELECTED_ZONE);
        if (selectedZone != null) {
            selectedZone.clear();
            player.displayClientMessage(ToolContent.getMessage(ToolContent.CLEAR, Style.EMPTY.withColor(ChatFormatting.YELLOW)), true);
        }
        event.setCanceled(true);
    }
}