package com.SouthernWall_404.Painter.Client.Config;

import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.ConfigAPI;
import com.SouthernWall_404.LaplaceAPI.VertinCore.Config.ConfigInfo;
import com.SouthernWall_404.Painter.Painter;
import net.neoforged.fml.ModContainer;

/**
 * Painter客户端配置管理类
 * 使用Laplace API的ConfigAPI进行配置注册
 */
public class ClientConfig {


    public static final String LINE_COLOR="line_color";
    public static final String LINE_COLOR_ALPHA="line_color_alpha";
    public static final String QUAD_COLOR="quad_color";
    public static final String QUAD_COLOR_ALPHA="quad_color_alpha";
    public static final String PAINT_OFFSET ="paint_offset";

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
                        // 四边形填充颜色 (RGB格式)
                        .push("color")
                        .comment("设置四边形填充的RGB颜色值，范围: 0x000000-0xFFFFFF")
                        .defineInt(QUAD_COLOR, 0xebe5d1, 0x00, 0xffffff)
                        // 四边形填充颜色透明度 (Alpha值, 0-255)
                        .comment("设置四边形填充的透明度，范围: 0x00(完全透明)-0xFF(完全不透明)")
                        .defineInt(QUAD_COLOR_ALPHA,0x4c,0x00,0xff)

                        // 线条颜色 (RGB格式)
                        .comment("设置线条的RGB颜色值，范围: 0x000000-0xFFFFFF")
                        .defineInt(LINE_COLOR, 0xebe5d1, 0x00, 0xffffff)
                        // 线条颜色透明度 (Alpha值, 0-255)
                        .comment("设置线条的透明度，范围: 0x00(完全透明)-0xFF(完全不透明)")
                        .defineInt(LINE_COLOR_ALPHA, 0xcc, 0x00, 0xffffff)
                        .pop()

                        .push("paint")
                        // 油漆渲染偏移量，用于避免Z-fighting现象（画面闪烁）
                        // 值越小越贴近墙面，但过小可能导致渲染问题；值越大离墙面越远，可能产生视觉间隙
                        // 范围: 0.0-Double.MAX_EXPONENT，默认值: 0.001
                        .comment("设置油漆渲染时的偏移量，用于解决Z-fighting问题，建议保持在0.001-0.01之间")
                        .defineDouble(PAINT_OFFSET,0.001,0,Double.MAX_EXPONENT)

                        .pop()
                        .build());
    }
}
