package com.SouthernWall_404.Painter.Common.World.Item;

import com.SouthernWall_404.Painter.API.Paint.Util.Paint.PaintOperationHelper;
import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class PaintThinnerItem extends BlockInteractItem{

    public PaintThinnerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void dealLeftClick(PlayerInteractEvent.LeftClickBlock event) {

    }

    @Override
    public void dealRightClick(PlayerInteractEvent.RightClickBlock event, boolean isInMainHand) {
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
