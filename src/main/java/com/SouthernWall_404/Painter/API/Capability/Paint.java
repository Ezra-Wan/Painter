// Paint.java
package com.SouthernWall_404.Painter.API.Capability;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

public class Paint implements INBTSerializable<CompoundTag> {

    //========需要记录的数据========
    private ResourceLocation blockKey;
    private Direction direction;



    //========构造方法========


    public Paint(Direction direction, ResourceLocation blockKey) {
        this.blockKey = blockKey;
        this.direction = direction;
    }
    public Paint(Direction direction,Block block)
    {
        this(direction,BuiltInRegistries.BLOCK.getKey(block));
    }
    public Paint() {
        this(Direction.SOUTH, BuiltInRegistries.BLOCK.getKey(Blocks.STONE));
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        // 保存 ResourceLocation 为字符串
        tag.putString("blockKey", blockKey.toString());
        // 保存 Direction 为名称（如 "north"）
        tag.putString("direction", direction.getName());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        // 从字符串解析 ResourceLocation
        this.blockKey = ResourceLocation.parse(compoundTag.getString("blockKey"));
        // 从名称获取 Direction
        this.direction = Direction.byName(compoundTag.getString("direction"));
        // 若方向无效，默认 SOUTH（防御性编程）
        if (this.direction == null) {
            this.direction = Direction.SOUTH;
        }
    }

    //========Getter/Setter（可选，根据实际需要添加）========
    public ResourceLocation getBlockKey() {
        return blockKey;
    }

    public Direction getDirection() {
        return direction;
    }

    public Block getBlock()
    {
        Block block=BuiltInRegistries.BLOCK.get(blockKey);
        if(block!=null)
        {
            return block;
        }else {
            return Blocks.AIR;
        }
    }
}