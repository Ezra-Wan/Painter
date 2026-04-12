package com.SouthernWall_404.Painter.Common.Init;

import com.SouthernWall_404.Painter.Painter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Painter.MODID);

}
