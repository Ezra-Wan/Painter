package com.SouthernWall_404.Painter.Common.Data;

import com.SouthernWall_404.Painter.Painter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModelGen extends ItemModelProvider {
    // 通用模型，让其他物品继承使用
    public static final String GENERATED = "item/generated";

    // 构造函数
    public ModItemModelGen(PackOutput output, ExistingFileHelper existingFileHelper) {
        // 第二个参数要填自己的modid
        super(output, Painter.MODID, existingFileHelper);
    }

    // 重写该方法，在这个方法里面填写要生成数据的物品
    @Override
    protected void registerModels() {
        // 在里面调用自定义的生成模型的方法，传入对应参数即可
    }

    // 自定义方法，item表明我们要生成数据的物品，texture表明生成数据的贴图资源
    public void itemGenerateModel(Item item, ResourceLocation texture) {
        // 因为要继承通用模型，调用这个方法设置父模型为通用模型，返回这个类的对象调用texture方法设置贴图
        withExistingParent(itemName(item), GENERATED).texture("layer0", texture);
    }

    // 获取item的所在路径
    public String itemName(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath();
    }

    // 拼接路径寻找贴图资源
    public ResourceLocation resourceItem(String path) {
        return ResourceLocation.fromNamespaceAndPath(Painter.MODID, "item/" + path);
    }
}