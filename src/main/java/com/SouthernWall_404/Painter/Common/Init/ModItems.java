package com.SouthernWall_404.Painter.Common.Init;


import com.SouthernWall_404.LaplaceAPI.VertinCore.World.Item.ItemRegisterHelper;
import com.SouthernWall_404.LaplaceAPI.VertinCore.World.Item.ItemRegistry;
import com.SouthernWall_404.Painter.Common.World.Item.ChulkItem;
import com.SouthernWall_404.Painter.Common.World.Item.PaintBucketItem;
import com.SouthernWall_404.Painter.Common.World.Item.PaintItem;
import com.SouthernWall_404.Painter.Common.World.Item.PaintThinnerItem;
import com.SouthernWall_404.Painter.Painter;
import com.google.common.collect.Sets;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashSet;
import java.util.function.Supplier;
@Mod(Painter.MODID)

public class ModItems {
    public static final DeferredItem<Item> BRUSH=
            ItemRegisterHelper.registerToCreativeTab(
                    Painter.frame,
                    ModCreativeModeTab.TAB_PAINTER,
                    ItemRegistry.builder("brush", PaintItem::new)
                            .apply(properties ->
                                properties
                                        .rarity(Rarity.UNCOMMON)
                                        .stacksTo(1)

                            )
                            .build()
            );
    public static final DeferredItem<Item> CHULK=
            ItemRegisterHelper.registerToCreativeTab(
                    Painter.frame,
                    ModCreativeModeTab.TAB_PAINTER,
                    ItemRegistry.builder("chulk",ChulkItem::new)
                            .apply(properties ->
                                    properties
                                            .rarity(Rarity.UNCOMMON)
                                            .stacksTo(1)
                            )
                            .build()
            );
    public static final DeferredItem<Item> PAINT_BUCKET=
            ItemRegisterHelper.registerToCreativeTab(
                    Painter.frame,
                    ModCreativeModeTab.TAB_PAINTER,
                    ItemRegistry.builder("paint_bucket",PaintBucketItem::new)
                            .apply(properties ->
                                    properties
                                            .rarity(Rarity.UNCOMMON)
                                            .stacksTo(1)
                            )
                            .build()
            );
    public static final DeferredItem<Item> PAINT_THINNER=
            ItemRegisterHelper.registerToCreativeTab(
                    Painter.frame,
                    ModCreativeModeTab.TAB_PAINTER,
                    ItemRegistry.builder("paint_thinner", PaintThinnerItem::new)
                            .apply(properties ->
                                    properties
                                            .rarity(Rarity.UNCOMMON)
                                            .stacksTo(1)
                            )
                            .build()
            );
}
