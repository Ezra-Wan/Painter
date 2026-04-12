package com.SouthernWall_404.Painter;

import com.SouthernWall_404.Painter.Common.Event.BlockRelativeEvent;
import com.SouthernWall_404.Painter.Common.Event.PlayerJoinEvent;
import com.SouthernWall_404.Painter.Common.Init.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Painter.MODID)
public class Painter {

    public static final String MODID="painter";

    public Painter(IEventBus modEventBus)
    {
//        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModBlock.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPE.register(modEventBus);

        ModItems.ITEMS.register(modEventBus);
        ModCreativeModeTab.CREATIVE_TABS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        NeoForge.EVENT_BUS.register(BlockRelativeEvent.class);
        NeoForge.EVENT_BUS.register(PlayerJoinEvent.class);

    }
}
