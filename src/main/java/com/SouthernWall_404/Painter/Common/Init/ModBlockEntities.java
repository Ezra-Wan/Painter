package com.SouthernWall_404.Painter.Common.Init;

import com.SouthernWall_404.Painter.Common.World.BlockEntity.PaintBlockEntity;
import com.SouthernWall_404.Painter.Painter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE=DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Painter.MODID);

    // 示例注册代码
    public static final Supplier<BlockEntityType<PaintBlockEntity>> RENDER_BEDROCK_ENTITY =
            BLOCK_ENTITY_TYPE.register("store_block_entity", () ->
                    BlockEntityType.Builder.of((pos, state) -> new PaintBlockEntity(pos, state), ModBlock.RENDER_BEDROCK.get())
                            .build(null));
}
