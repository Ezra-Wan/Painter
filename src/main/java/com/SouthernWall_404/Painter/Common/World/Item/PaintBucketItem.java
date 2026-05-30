package com.SouthernWall_404.Painter.Common.World.Item;

import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintUtil;
import com.SouthernWall_404.Painter.API.Tool.BucketSelectionConfig;
import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.API.Tool.Wall.Filters.EmptyFilter;
import com.SouthernWall_404.Painter.API.Tool.Wall.Filters.SelectFilter;
import com.SouthernWall_404.Painter.API.Tool.Wall.IFilter;
import com.SouthernWall_404.Painter.Common.Content.ComponentContent;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class PaintBucketItem extends BlockInteractItem {

    public PaintBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.addAll(ComponentContent.getTooltip(ComponentContent.BUCKET, ComponentContent.BUCKET_ROW, Style.EMPTY.withColor(ChatFormatting.GRAY)));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public void dealRightClick(PlayerInteractEvent.RightClickBlock event,boolean isInMainHand) {
        // 原 BlockEvent 中处理 PaintBucketItem 右键的逻辑

        if(handCheck(event,isInMainHand))return;
        Level level = event.getLevel();
        BlockPos blockPos = event.getPos();
        Player player = event.getEntity();
        
        // 获取玩家的油漆桶选区依赖配置（双端都执行，因为已经通过数据包同步）
        BucketSelectionConfig config = player.getData(ModAttachments.BUCKET_SELECTION_CONFIG);
        
        // 根据配置决定是否检查选区
        if (config.isRequiresSelection()) {
            SelectedZone selectedZone = player.getData(ModAttachments.SELECTED_ZONE_FOR_SWAP);

            // 强制要求选区存在
            if (selectedZone == null || !selectedZone.isSelecting()) {
                player.displayClientMessage(
                    Component.translatable("painter.message.bucket.requires_selection")
                        .withStyle(ChatFormatting.RED),
                    true
                );
                event.setCanceled(true);
                return;
            }
        }
        // 非依赖选区模式下，直接跳过选区检查

        List<IFilter> filters = new ArrayList<>();
        filters.add(new EmptyFilter());
        
        // 只有在需要选区模式下才添加选区过滤器
        if (config.isRequiresSelection()) {
            filters.add(new SelectFilter());
        }
        
        PaintUtil.dealBucketClick(blockPos, event.getFace(), player, level, filters,isInMainHand);

        event.setCanceled(true);
        player.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public void dealLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        // 桶的左键通常不做处理，或者可以留空
        // 如果有需要，可以在这里添加逻辑
    }
}