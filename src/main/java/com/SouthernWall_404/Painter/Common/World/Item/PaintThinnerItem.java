package com.SouthernWall_404.Painter.Common.World.Item;

import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintOperationHelper;
import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.Common.Content.ComponentContent;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class PaintThinnerItem extends BlockInteractItem{

    public PaintThinnerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.addAll(ComponentContent.getTooltip(ComponentContent.THINNER, ComponentContent.THINNER_ROW, Style.EMPTY.withColor(ChatFormatting.GRAY)));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public void dealLeftClick(PlayerInteractEvent.LeftClickBlock event) {

    }

    @Override
    public void dealRightClick(PlayerInteractEvent.RightClickBlock event, boolean isInMainHand) {

        if(handCheck(event,isInMainHand))return;//防止副手故障

        if(isInMainHand)event.setCanceled(true);//位于主手，则正常运行
        else return;//位于副手，停止

        Player player = event.getEntity();
        SelectedZone zone = player.getData(ModAttachments.SELECTED_ZONE);
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        if (zone.isSelecting()) {
            // 执行大面积移除操作
            PaintOperationHelper.thinPaint(level, zone.getPositions(), event.getFace());
        } else {
            // 执行单点移除操作
            PaintOperationHelper.thinPaint(level, pos,event.getFace());
        }
    }
}
