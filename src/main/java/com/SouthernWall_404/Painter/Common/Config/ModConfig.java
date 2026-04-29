package com.SouthernWall_404.Painter.Common.Config;

import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.ConfigAPI;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.ConfigInfo;
import com.SouthernWall_404.Painter.Client.Config.ClientConfig;
import com.SouthernWall_404.Painter.Painter;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Painter模组配置管理类
 * 使用Laplace API的ConfigAPI进行配置注册
 */
public class ModConfig {


    public static void register(ModContainer container)
    {
        ConfigAPI.register(container,Painter.MODID,ConfigInfo.builder()
                .type(net.neoforged.fml.config.ModConfig.Type.COMMON)
                        .comment("测试一下")
                        .defineBool("test",true)
                .build());
        ClientConfig.register(container);
    }
}
