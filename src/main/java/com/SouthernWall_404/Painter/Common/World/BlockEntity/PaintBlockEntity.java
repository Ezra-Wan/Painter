package com.SouthernWall_404.Painter.Common.World.BlockEntity;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.PaintContent;
import com.SouthernWall_404.Painter.Common.Init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PaintBlockEntity extends BlockEntity {

    //========需要持久化的数据========
    private AbstractRender render;//渲染实例
    private RandomSource randomSource=RandomSource.create();

    //========构造方法=========
    public PaintBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public PaintBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(ModBlockEntities.RENDER_BEDROCK_ENTITY.get(), blockPos, blockState);
    }


    /**
     * 用于初始化
     * @param render
     */
    public void init(AbstractRender render)
    {
        setRender(render);
    }

    //=========业务方法=========


    public AbstractRender getRender() {
        return render;
    }

    /**
     * 用于获取本源的BlockState,即方块本身的属性
     * @return
     */
    public BlockState getOrigin()
    {
        if(render!=null)
        {
            return render.getOrigin();
        }
        else return Blocks.AIR.defaultBlockState();
    }


    public void setRender(AbstractRender render) {
        this.render = render;

        if (level != null && !level.isClientSide) {
            level.getLightEngine().checkBlock(worldPosition);
        }
        setChanged();
    }

    //========NBT方法========
    // 保存到区块
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (render != null) {
            // 保存类型，供反序列化时选择正确的子类
            tag.putString("render_type", render.getType());
            tag.put("render", render.serializeNBT(provider));
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("render_type")) {
            String type = tag.getString("render_type");
            // 通过 PaintContent 创建对应类型的实例（构造函数可传入 null，因为数据会从 tag 覆盖）
            AbstractRender newRender = PaintContent.getRender(type).apply(getOrigin());
            if (tag.contains("render")) {
                newRender.deserializeNBT(provider, tag.getCompound("render"));
            }
            this.render = newRender;
        }
    }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        if (render != null) {
            tag.putString("render_type", render.getType());
            tag.put("render", render.serializeNBT(provider));
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        if (tag.contains("render_type")) {
            String type = tag.getString("render_type");
            AbstractRender newRender = PaintContent.getRender(type).apply(null);
            if (tag.contains("render")) {
                newRender.deserializeNBT(provider, tag.getCompound("render"));
            }
            this.render = newRender;
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider provider) {
        handleUpdateTag(packet.getTag(), provider);
    }
}