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

        // 判断当前是否已有A点缓存
        System.out.println("[DEBUG] zone.isCreating() = " + zone.isCreating());
        if (!zone.isCreating()) {
            // === 没有A点：设置A点 ===
            System.out.println("[DEBUG] 进入设置A点分支");
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
            // === 已有A点：尝试设置B点 ===
            System.out.println("[DEBUG] 进入设置B点分支");
            boolean result = zone.setB(event.getPos());
            System.out.println("[DEBUG] setB() 返回结果: " + result);
            if(result) {
                // B点正确：成功建立选区，setB内部已自动清除缓存
                player.displayClientMessage(
                    Component.translatable("painter.message.select.success")
                        .withStyle(ChatFormatting.GREEN),
                    true
                );
            } else {
                // B点错误：两点不在同一平面，A点缓存未被清除
                System.out.println("[DEBUG] 进入B点错误分支");
                
                // 第一步：无论是否按Shift，都先显示红色错误提示
                System.out.println("[DEBUG] 显示红色错误提示");
                player.displayClientMessage(
                    Component.translatable("painter.message.select_b.not_in_surface")
                        .withStyle(ChatFormatting.RED),
                    true
                );
                
                // 第二步：根据Shift键状态决定下一步操作
                System.out.println("[DEBUG] Shift按键状态: " + player.isShiftKeyDown());
                if (player.isShiftKeyDown()) {
                    // 按Shift：保留A点，用户可以继续右键尝试其他B点
                    System.out.println("[DEBUG] 按Shift，保留A点");
                    // 不需要额外操作，A点已保留
                } else {
                    // 不按Shift：重置A点，用当前点击位置作为新的A点
                    System.out.println("[DEBUG] 不按Shift，重置A点");
                    zone.setA(event.getPos(), event.getFace());
                    player.displayClientMessage(Component.translatable(
                                    ToolContent.getMessagePath(ToolContent.SELECT_POSA),
                                    event.getPos().getX(),
                                    event.getPos().getY(),
                                    event.getPos().getZ(),
                                    ToolContent.getFaceTranslation(event.getFace())
                            ).withStyle(style -> style.withColor(ChatFormatting.YELLOW)),
                            true);
                }
            }
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