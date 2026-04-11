package com.SouthernWall_404.Painter.Common.Network;

import com.SouthernWall_404.Painter.API.Paint.API.AbstractRender;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Paint.PaintContent;
import com.SouthernWall_404.Painter.Common.Init.ModAttachments;
import com.SouthernWall_404.Painter.Painter;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record PaintSyncC2SPacket(BlockPos pos, AbstractRender<?,?> render) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PaintSyncC2SPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Painter.MODID, "paint_sync_c2s"));


    // 使用 NBT 序列化 BlockState，这是最可靠的方式
    // 正确的 StreamCodec 实现
    public static final StreamCodec<ByteBuf, PaintSyncC2SPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PaintSyncC2SPacket packet) {
            // BlockPos
            buf.writeInt(packet.pos.getX());
            buf.writeInt(packet.pos.getY());
            buf.writeInt(packet.pos.getZ());
            // 将 AbstractRender 序列化为 CompoundTag（包含 type 和 data）
            CompoundTag tag = packet.render.serializeNBT(RegistryAccess.EMPTY);
            ByteBufCodecs.COMPOUND_TAG.encode(buf, tag);
        }

        @Override
        public PaintSyncC2SPacket decode(ByteBuf buf) {
            BlockPos pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            CompoundTag tag = ByteBufCodecs.COMPOUND_TAG.decode(buf);

            // 使用 PaintContent 根据 NBT 中的 type 字段创建渲染器
            Function<BlockState, AbstractRender> factory = PaintContent.getRender(tag);
            // 创建时传入一个临时的 BlockState（反序列化时会覆盖 origin）
            AbstractRender<?,?> render = factory.apply(Blocks.AIR.defaultBlockState());
            render.deserializeNBT(RegistryAccess.EMPTY, tag);

            return new PaintSyncC2SPacket(pos, render);
        }
    };

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 服务器端处理器：接收客户端发送的数据并更新服务器的 Capability
     */
    public static void handle(final PaintSyncC2SPacket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                Level level=serverPlayer.level();

                LevelChunk chunk=level.getChunkAt(payload.pos);
                PaintInfo paintInfo=chunk.getData(ModAttachments.PAINT_INFO);

                if(payload.render!=null)
                {
                    paintInfo.putRender(level,payload.pos, payload.render());
                }

                else
                {
                    paintInfo.removeRender(payload.pos);
                }
            }
        });
    }

}
