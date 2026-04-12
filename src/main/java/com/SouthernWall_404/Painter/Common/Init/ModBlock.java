package com.SouthernWall_404.Painter.Common.Init;

import com.SouthernWall_404.Painter.Painter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlock {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Painter
            .MODID);

}
