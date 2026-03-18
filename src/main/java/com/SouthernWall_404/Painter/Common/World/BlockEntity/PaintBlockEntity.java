package com.SouthernWall_404.Painter.Common.World.BlockEntity;

import com.SouthernWall_404.Painter.API.Paint.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.IRender;
import com.SouthernWall_404.Painter.API.Paint.PaintContent;
import com.SouthernWall_404.Painter.Common.Init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.units.qual.A;

public class PaintBlockEntity extends BlockEntity {

    //========需要持久化的数据========
    private AbstractRender render;//渲染实例

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
    public void init(AbstractRender<?> render)
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
            tag.put("render", render.serializeNBT(provider));
        }
    }

    // 从区块加载
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("render")) {
            // 如果 render 尚未创建，需要先根据类型创建实例（可从 tag 中读取 type）
            if (render == null) {
                String type = tag.getCompound("render").getString("type");
                // 注意：创建时传入的 origin 不重要，反序列化时会覆盖
                render = PaintContent.getRender(type).apply(null);
            }
            render.deserializeNBT(provider, tag.getCompound("render"));

            if (level != null && !level.isClientSide) {
                level.getLightEngine().checkBlock(worldPosition);
            }
        }
    }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        if (render != null) {
            tag.put("render", render.serializeNBT(provider));
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        if (tag.contains("render")) {
            // 确保 render 实例存在，然后反序列化
            if (render == null) {
                // 从 tag 中读取 type 并创建对应实例
                String type = tag.getCompound("render").getString("type");
                render = PaintContent.getRender(type).apply(null);
            }
            render.deserializeNBT(provider, tag.getCompound("render"));

            if (level != null) {
                level.getLightEngine().checkBlock(worldPosition);
            }
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