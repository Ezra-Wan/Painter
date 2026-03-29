package com.SouthernWall_404.Painter.Common.World.Item;

import com.SouthernWall_404.Painter.Common.Content.ComponentContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ChulkItem extends Item {

    public ChulkItem(Properties properties) {
        super(properties);
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.addAll(ComponentContent.getTooltip(ComponentContent.CHULK,ComponentContent.CHULK_ROW, Style.EMPTY.withColor(ChatFormatting.GRAY)));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

    }
}
