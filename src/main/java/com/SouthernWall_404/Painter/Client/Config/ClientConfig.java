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


    public static final String LINE_COLOR="line_color";
    public static final String QUAD_COLOR="quad_color";

    public static void register(ModContainer container)
    {
        ConfigAPI.register(container,
                Painter.MODID,
                ConfigInfo.builder()
                        .type(net.neoforged.fml.config.ModConfig.Type.CLIENT)
                        .comment("测试用")
                        .defineBool("test",true)
                        // 使用Integer.MIN_VALUE到Integer.MAX_VALUE以支持完整的ARGB颜色范围
                        // 0xFFFFFFFF 在Java中表示为 -1
                        .defineInt(QUAD_COLOR, 0x4cebe5d1, Integer.MIN_VALUE, Integer.MAX_VALUE)
                        .defineInt(LINE_COLOR, 0xccebe5d1, Integer.MIN_VALUE, Integer.MAX_VALUE)
                        .build());
    }
}
