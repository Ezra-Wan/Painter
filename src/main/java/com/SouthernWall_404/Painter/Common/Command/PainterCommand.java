package com.SouthernWall_404.Painter.Common.Command;

import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkSync;
import com.SouthernWall_404.Painter.API.Tool.BucketSelectionConfig;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.Laplace.Network.ServerHandlers;
import com.SouthernWall_404.Painter.Painter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Painter 模组命令处理器
 * 提供 /painter 命令及其子命令
 * 
 * @author EzraW
 * @version 1.0
 */
public class PainterCommand {

    /**
     * 注册所有 Painter 模组的命令
     * 
     * @param dispatcher 命令分发器
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // 注册 /painter 主命令
        dispatcher.register(Commands.literal("painter")
                .requires(source -> source.hasPermission(0)) // 所有玩家都可以使用
                .then(Commands.literal("toggle_bucket_selection")
                        .executes(PainterCommand::toggleBucketSelection)));
    }

    /**
     * 切换油漆桶选区依赖状态
     * 
     * @param context 命令上下文
     * @return 命令执行结果代码
     * @throws CommandSyntaxException 命令语法异常
     */
    private static int toggleBucketSelection(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        // 获取命令执行者
        ServerPlayer player = context.getSource().getPlayerOrException();
        
        // 获取玩家的配置附件
        BucketSelectionConfig config = player.getData(ModAttachments.BUCKET_SELECTION_CONFIG);
        
        // 切换状态并获取新状态
        boolean newState = config.toggleRequiresSelection();
        
        // 同步配置到客户端
        syncConfigToClient(player, newState);
        
        // 根据新状态构建反馈消息
        Component message;
        if (newState) {
            // 切换到需要选区模式
            message = Component.translatable("painter.command.bucket_selection.enabled")
                    .withStyle(ChatFormatting.GREEN);
        } else {
            // 切换到不需要选区模式
            message = Component.translatable("painter.command.bucket_selection.disabled")
                    .withStyle(ChatFormatting.YELLOW);
        }
        
        // 发送消息给玩家
        player.displayClientMessage(message, true);
        
        return 1;
    }

    /**
     * 将配置同步到客户端
     * 
     * @param player 玩家
     * @param requiresSelection 是否需要选区
     */
    private static void syncConfigToClient(ServerPlayer player, boolean requiresSelection) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("requiresSelection", requiresSelection);
        
        // 使用 Laplace API 发送通知包到客户端
        NetworkSync.sendNoticeToPlayer(tag, player, ServerHandlers.BUCKET_CONFIG_SYNC_PACKET);
    }
}
