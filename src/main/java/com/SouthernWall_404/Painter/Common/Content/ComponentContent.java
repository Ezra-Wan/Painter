package com.SouthernWall_404.Painter.Common.Content;

import com.SouthernWall_404.Painter.Painter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

public class ComponentContent {

    //========tooltip种类定义=========

    public static String BRUSH="brush";
    public static String CHULK="chulk";
    public static String BUCKET="bucket";

    public static int BRUSH_ROW=3;
    public static int CHULK_ROW=3;
    public static int BUCKET_ROW=2;

    public static List<Component> getTooltip(String type, int row, Style style) {
        List<Component> tooltip = new ArrayList<>();
        for (int i = 1; i <=row; i++)
        {
            tooltip.add(Component.translatable(Painter.MODID+".tooltip."+type+"_"+i).withStyle(style));
        }

        return tooltip;
    }
}
