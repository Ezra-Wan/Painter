package com.SouthernWall_404.Painter.Common.Init;

import com.SouthernWall_404.LaplaceAPI.VertinCore.World.CreativeTab.TabRegistry;
import com.SouthernWall_404.LaplaceAPI.VertinCore.World.CreativeTab.TabRegistryHelper;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Painter.MODID)
public class ModCreativeModeTab {

public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_PAINTER = TabRegistryHelper.registerTab(
        Painter.frame,
        TabRegistry.builder(
                Painter.MODID,
                "painter",
                Component.translatable("itemGroup.painter"),
                () -> new ItemStack(Items.COMMAND_BLOCK)
        ).build());
}