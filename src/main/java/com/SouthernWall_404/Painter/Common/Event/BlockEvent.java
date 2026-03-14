package com.SouthernWall_404.Painter.Common.Event;

import com.SouthernWall_404.Painter.API.Capability.Paint;
import com.SouthernWall_404.Painter.API.Capability.PaintUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class BlockEvent {

    @SubscribeEvent
    public static void onBlockClicked(PlayerInteractEvent.RightClickBlock event)
    {
        Level level=event.getLevel();
        BlockPos blockPos=event.getPos();

        PaintUtil.addPaint(level,blockPos,new Paint());
    }


}
