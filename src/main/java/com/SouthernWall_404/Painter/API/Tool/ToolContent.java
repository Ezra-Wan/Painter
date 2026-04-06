package com.SouthernWall_404.Painter.API.Tool;

import com.SouthernWall_404.Painter.Painter;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;


public class ToolContent {

    public static String SELECT_POSA ="select_a";
    public static String SELECT_POSB ="select_b";
    public static String CLEAR ="clear";

    public static String FAILED_TO_PAINT ="failed_to_paint";

    //========结果处理========
    public static String PASS="pass";
    public static String EMPTY_POSA="missing_a";
    public static String EMPTY_POSB="missing_a";
    public static String EMPTY_FACE="missing_face";

    public static String NOT_IN_SURFACE="not_in_surface";


    public static Component getMessage(String type)
    {
        return getMessage(type,Style.EMPTY);
    }
    public static Component getMessage(String type, Style style){
        return Component.translatable(Painter.MODID+".message."+type).withStyle(style);
    }

    public static String getMessagePath(String type)
    {
        String path=Painter.MODID+".message."+type;
        return path;
    }

    public static String parentString(String parent,String child)
    {
        return parent+"."+child;
    }

    public static Component getFaceTranslation(Direction direction)
    {
        return Component.translatable(Painter.MODID+".direction."+direction.getSerializedName());
    }
}
