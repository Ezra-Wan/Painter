package com.SouthernWall_404.Painter.Common.Data;


import com.SouthernWall_404.Painter.Painter;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLangGen extends LanguageProvider {

    public ModLangGen(PackOutput output,String locale){super(output, Painter.MODID,locale);}

    //重写这个方法，这个方法里添加对应物品的信息
    @Override
    protected void addTranslations() {
        //物品
        //直接调用add方法，传入物品和物品对应的名字即可
    }
}
