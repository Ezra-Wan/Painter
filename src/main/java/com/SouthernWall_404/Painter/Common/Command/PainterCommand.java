package com.SouthernWall_404.Painter.Common.Command;

import com.SouthernWall_404.LaplaceAPI.VertinCore.Command.CommandAPI;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Command.CommandTree;
import com.SouthernWall_404.LaplaceAPI.xNetwork.API.NetworkSync;
import com.SouthernWall_404.Painter.API.Tool.BucketSelectionConfig;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.Laplace.Network.ServerHandlers;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.Mod;

/**
 * Painter 模组命令处理器
 * 使用 Laplace API 的 CommandAPI + CommandTree 注册命令
 */
@Mod(Painter.MODID)
public class PainterCommand {
    
    static {
        // 注册 /painter bucket_mode 命令
        CommandAPI.register(
            CommandTree.root(Painter.MODID)
                .literal("bucket_mode")
                .execute(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayer();
                    if (player == null) {
                        ctx.getSource().sendFailure(Component.literal("只有玩家可以执行此命令"));
                        return 0;
                    }
                    
                    // 获取玩家的配置附件
                    BucketSelectionConfig config = player.getData(ModAttachments.BUCKET_SELECTION_CONFIG);
                    
                    // 切换模式
                    boolean newState = config.toggle();
                    
                    System.out.println("[DEBUG PainterCommand] 服务端切换模式: requiresSelection=" + newState);
                    
                    // 同步到客户端
                    syncConfigToClient(player, newState);
                    
                    // 发送反馈消息
                    Component modeText = newState 
                        ? Component.translatable("painter.command.bucket_mode.strict").withStyle(ChatFormatting.GREEN)
                        : Component.translatable("painter.command.bucket_mode.free").withStyle(ChatFormatting.YELLOW);
                    
                    Component message = Component.translatable("painter.command.bucket_mode.toggled", modeText);
                    player.displayClientMessage(message, true);
                    
                    return 1;
                })
                .build()
        );
    }
    
    /**
     * 同步配置到客户端
     */
    private static void syncConfigToClient(ServerPlayer player, boolean requiresSelection) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("requiresSelection", requiresSelection);
        NetworkSync.sendNoticeToPlayer(tag, player, ServerHandlers.BUCKET_MODE_SYNC_PACKET);
    }
}
