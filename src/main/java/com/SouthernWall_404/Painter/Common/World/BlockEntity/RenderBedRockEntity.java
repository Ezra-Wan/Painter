package com.SouthernWall_404.Painter.Common.World.BlockEntity;

import com.SouthernWall_404.Painter.Common.Init.ModBlockEntities;
import com.SouthernWall_404.Painter.Common.World.Block.RenderBedRock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class RenderBedRockEntity extends BlockEntity {

    private BlockState copyState;
    // 用于暂存未解析的NBT，以防加载时level不可用
    private CompoundTag pendingCopyStateTag;

    public RenderBedRockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public RenderBedRockEntity(BlockPos blockPos, BlockState blockState) {
        this(ModBlockEntities.RENDER_BEDROCK_ENTITY.get(), blockPos, blockState);
    }

    public void setCopyState(BlockState copyState) {
        if (copyState != null && copyState.getBlock() instanceof RenderBedRock) {
            this.copyState = Blocks.AIR.defaultBlockState(); // 或 any safe fallback
        } else {
            this.copyState = copyState;
        }
        setChanged();
    }

    public BlockState getCopyState() {
        return copyState;
    }


    @Override
    protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries) {
        super.saveAdditional(tag,registries);
        if (copyState != null) {
            tag.put("copy_state", NbtUtils.writeBlockState(copyState));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("copy_state", CompoundTag.TAG_COMPOUND)) {
            CompoundTag stateTag = tag.getCompound("copy_state");
            if (this.level != null) {
                // level已可用，直接解析
                HolderLookup<Block> holderLookup = this.level.holderLookup(Registries.BLOCK);
                this.copyState = NbtUtils.readBlockState(holderLookup, stateTag);
            } else {
                // level尚未设置，暂存NBT，等待onLoad时处理
                this.pendingCopyStateTag = stateTag.copy();
            }
        }
    }

    public void fromNBT(CompoundTag compoundTag)
    {
        loadAdditional(compoundTag,null);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        // 如果有暂存的NBT且level现在可用，则解析copyState
        if (pendingCopyStateTag != null && this.level != null) {
            HolderLookup<Block> holderLookup = this.level.holderLookup(Registries.BLOCK);
            this.copyState = NbtUtils.readBlockState(holderLookup, pendingCopyStateTag);
            this.pendingCopyStateTag = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);  // 将 copyState 存入更新标签
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        loadAdditional(tag, registries);  // 从标签加载 copyState
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // 使用 CompoundTag 构建同步包
        return ClientboundBlockEntityDataPacket.create(this);
    }
}