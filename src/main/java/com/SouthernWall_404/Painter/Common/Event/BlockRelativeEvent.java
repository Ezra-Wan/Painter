package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintChunkInfo;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Util.PaintUtil;
import com.SouthernWall_404.Painter.API.Paint.Util.PaintValidHelper;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.World.Item.BlockInteractItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

public class BlockRelativeEvent {

    //========放置失败的原因注册字段========
    public static final String OUT_OF_RANGE = "out_of_range";

    //新区块的数据获取有待优化
    @SubscribeEvent
    public static void onBlockRightClicked(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        // 获取主手中的交互物品
        BlockInteractItem mainHand = BlockInteractItem.getFromMainHand(player);
        if (mainHand != null) {
            // 调用物品自身的右键处理方法
            mainHand.dealRightClick(event,true);
        }

        BlockInteractItem offHand = BlockInteractItem.getFromOffHand(player);
        if (offHand != null) {
            // 调用物品自身的右键处理方法
            offHand.dealRightClick(event,false);
        }
    }

    @SubscribeEvent
    public static void onBlockLeftClicked(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        // 获取主手中的交互物品
        BlockInteractItem interactItem = BlockInteractItem.getFromMainHand(player);
        if (interactItem != null) {
            // 调用物品自身的左键处理方法
            interactItem.dealLeftClick(event);
        }
    }

    @SubscribeEvent
    public static void onBlockBroke(BlockEvent.BreakEvent event)
    {
        BlockPos pos=event.getPos();
        Level level=event.getPlayer().level();

        if(PaintValidHelper.hasPaint(level,pos))
        {
            LevelChunk chunk=level.getChunkAt(pos);
            PaintInfo paintInfo=chunk.getData(ModAttachments.PAINT_INFO);
            paintInfo.removeRender(level, event.getPlayer(), pos);
        }

    }
}