package com.SouthernWall_404.Painter.Client.Config;

import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.ConfigAPI;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.ConfigInfo;
import com.SouthernWall_404.Painter.Painter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Painter客户端配置管理类
 * 使用Laplace API的ConfigAPI进行配置注册
 */
public class ClientConfig {

    public static void register(ModContainer container)
    {
        ConfigAPI.register(container,
                Painter.MODID,
                ConfigInfo.builder()
                        .type(net.neoforged.fml.config.ModConfig.Type.CLIENT)
                        .comment("测试用")
                        .defineBool("test",true)
                        .build());
    }
}
