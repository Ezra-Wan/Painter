package com.SouthernWall_404.Painter.Common.Init;

import com.SouthernWall_404.Painter.Painter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Painter.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_PAINTER = CREATIVE_TABS.register("painter_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.painter"))
                    .icon(() -> new ItemStack(Items.COMMAND_BLOCK))
                    .displayItems((parameters, output) -> ModItems.CREATIVE_TAB_ITEMS.forEach((item) -> output.accept(item.get())))
                    .build());
}