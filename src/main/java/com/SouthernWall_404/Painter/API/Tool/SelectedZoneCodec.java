package com.SouthernWall_404.Painter.API.Tool;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class SelectedZoneCodec {
    public static final Codec<SelectedZone> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BlockPos.CODEC.fieldOf("A").forGetter(SelectedZone::getA),
            BlockPos.CODEC.fieldOf("B").forGetter(SelectedZone::getB),
            Direction.CODEC.fieldOf("face").forGetter(SelectedZone::getFace)
        ).apply(instance, SelectedZone::new)
    );
}