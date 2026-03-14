package com.SouthernWall_404.Painter;

import com.SouthernWall_404.Painter.Common.Event.BlockEvent;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Common.Init.ModBlock;
import com.SouthernWall_404.Painter.Common.Init.ModBlockEntities;
import com.SouthernWall_404.Painter.Common.Network.ModChannels;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Painter.MODID)
public class Painter {

    public static final String MODID="painter";

    public Painter(IEventBus modEventBus)
    {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModBlock.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPE.register(modEventBus);

        NeoForge.EVENT_BUS.register(BlockEvent.class);
    }
}
