package com.SouthernWall_404.Painter.Common.Init;

import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintChunkInfo;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.Painter;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Painter.MODID);

//    public static final DeferredRegister<AttachmentType<SelectedZone>> SELECTED_ZONE =
//        ATTACHMENT_TYPES.register("selected_zone",
//            () -> AttachmentType.builder(() -> new SelectedZone()) // 提供默认值
//                .serialize(SelectedZoneCodec.CODEC)               // 持久化支持
//                .copyOnDeath()                                    // 死亡时保留
//                .build()
//        );
public static final Supplier<AttachmentType<PaintInfo>> PAINT_INFO =
        ATTACHMENT_TYPES.register("paint_info",
                () -> AttachmentType.serializable(()->new PaintInfo())
                        .build());
public static final Supplier<AttachmentType<PaintChunkInfo>>PAINT_CHUNK_INFO=
        ATTACHMENT_TYPES.register("paint_chunk_info",
                ()->AttachmentType.serializable(()->new PaintChunkInfo())
                        .build());
    public static final Supplier<AttachmentType<SelectedZone>> SELECTED_ZONE =
            ATTACHMENT_TYPES.register(Painter.MODID+".selected_zone",
                    () -> AttachmentType.builder(() ->
                            new SelectedZone())
                            .build()
    );
}