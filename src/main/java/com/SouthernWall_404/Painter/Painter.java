package com.SouthernWall_404.Painter;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Painter.MODID)
public class Painter {

    public static final String MODID="painter";

    public Painter(IEventBus modEventBus)
    {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
    }
}
