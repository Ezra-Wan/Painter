package com.SouthernWall_404.Painter;

import com.SouthernWall_404.LaplaceAPI.VertinCore.Frame.CommonRegisterFrame;
import com.SouthernWall_404.Painter.Common.Config.ModConfig;
import com.SouthernWall_404.Painter.Common.Event.BlockRelativeEvent;
import com.SouthernWall_404.Painter.Common.Event.PlayerJoinEvent;
import com.SouthernWall_404.Painter.Common.Init.*;
import com.SouthernWall_404.Painter.Common.Network.ClientHandlers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Painter.MODID)
public class Painter {

    public static final String MODID="painter";

    public  static final CommonRegisterFrame frame=new CommonRegisterFrame(MODID);

    public Painter(IEventBus modEventBus, ModContainer container)
    {
//        ModBlock.BLOCKS.register(modEventBus);
//        ModBlockEntities.BLOCK_ENTITY_TYPE.register(modEventBus);

//        ModItems.ITEMS.register(modEventBus);
//        ModCreativeModeTab.CREATIVE_TABS.register(modEventBus);
//        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        frame.register(modEventBus);

        // 初始化配置系统
        ModConfig.register(container);

        if(FMLEnvironment.dist== Dist.CLIENT)ClientHandlers.register();

        NeoForge.EVENT_BUS.register(BlockRelativeEvent.class);
        NeoForge.EVENT_BUS.register(PlayerJoinEvent.class);

    }
}
