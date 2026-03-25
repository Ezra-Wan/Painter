package com.SouthernWall_404.Painter.Common.Init;


import com.SouthernWall_404.Painter.Common.World.Item.ChulkItem;
import com.SouthernWall_404.Painter.Common.World.Item.PaintBucketItem;
import com.SouthernWall_404.Painter.Common.World.Item.PaintItem;
import com.SouthernWall_404.Painter.Painter;
import com.google.common.collect.Sets;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashSet;
import java.util.function.Supplier;

public class ModItems {

    public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(Painter.MODID);
    public static LinkedHashSet<DeferredItem<Item>> CREATIVE_TAB_ITEMS = Sets.newLinkedHashSet();

    public static String TOOLTIP="tooltip."+ Painter.MODID+".";


    public static DeferredItem<Item> registerWithTab(final String name, final Supplier<Item> supplier) {
        DeferredItem<Item> item = ITEMS.register(name,supplier);
        CREATIVE_TAB_ITEMS.add(item);
        return item;
    }

    public static final DeferredItem<Item> BRUSH=registerWithTab("brush",
            ()->new PaintItem(new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .stacksTo(1)
            )
    );

    public static final DeferredItem<Item> CHULK=registerWithTab("chulk",
            ()->new ChulkItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1))
    );

    public static final DeferredItem<Item> PAINT_BUCKET=registerWithTab("paint_bucket",
            ()->new PaintBucketItem(new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1))
    );
    static {

    }


}
