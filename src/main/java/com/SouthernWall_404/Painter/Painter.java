package com.SouthernWall_404.Painter;

import com.SouthernWall_404.LaplaceAPI.VertinCore.Frame.CommonRegisterFrame;
import com.SouthernWall_404.Painter.Common.Config.ModConfig;
import com.SouthernWall_404.Painter.Common.Event.BlockRelativeEvent;
import com.SouthernWall_404.Painter.Common.Event.PlayerJoinEvent;
import com.SouthernWall_404.Painter.Common.Event.ServerTick;
import com.SouthernWall_404.Painter.Common.Laplace.Network.ClientHandlers;
import com.SouthernWall_404.Painter.Common.Laplace.Network.ServerHandlers;
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

        frame.register(modEventBus);

        // 初始化配置系统
        ModConfig.register(container);

        // 注册网络处理器
        if(FMLEnvironment.dist== Dist.CLIENT)ClientHandlers.register();
        ServerHandlers.register();

    }
}
