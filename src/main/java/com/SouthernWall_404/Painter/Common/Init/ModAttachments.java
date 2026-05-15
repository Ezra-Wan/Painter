package com.SouthernWall_404.Painter.Common.Init;

import com.SouthernWall_404.LaplaceAPI.VertinCore.World.Attachment.AttachmentRegistryHelper;
import com.SouthernWall_404.Painter.API.Paint.Attachment.LevelPaintInfo;
import com.SouthernWall_404.Painter.API.Paint.Attachment.PaintInfo;
import com.SouthernWall_404.Painter.API.Tool.SelectedZone;
import com.SouthernWall_404.Painter.API.Tool.SelectedZoneForSwap;
import com.SouthernWall_404.Painter.Painter;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

@Mod(Painter.MODID)
public class ModAttachments {
//    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
//        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Painter.MODID);
//
//public static final Supplier<AttachmentType<PaintInfo>> PAINT_INFO =
//        ATTACHMENT_TYPES.register("paint_info",
//                () -> AttachmentType.serializable(()->new PaintInfo())
//                        .build());
//public static final Supplier<AttachmentType<PaintChunkInfo>>PAINT_CHUNK_INFO=
//        ATTACHMENT_TYPES.register("paint_chunk_info",
//                ()->AttachmentType.serializable(()->new PaintChunkInfo())
//                        .build());
//    public static final Supplier<AttachmentType<SelectedZone>> SELECTED_ZONE =
//            ATTACHMENT_TYPES.register(Painter.MODID+".selected_zone",
//                    () -> AttachmentType.builder(() ->
//                            new SelectedZone())
//                            .build()

    public static final Supplier<AttachmentType<PaintInfo>> PAINT_INFO = AttachmentRegistryHelper.register(Painter.frame,"paint_info",()->new PaintInfo());
    public static final Supplier<AttachmentType<LevelPaintInfo>> LEVEL_PAINT_INFO = AttachmentRegistryHelper.registerLevelAttachment(Painter.frame,"level_paint_info",()->new LevelPaintInfo());
    // public static final Supplier<AttachmentType<PaintChunkInfo>> PAINT_CHUNK_INFO = AttachmentRegistryHelper.register(Painter.frame,"paint_chunk_info",()->new PaintChunkInfo());
    public static final Supplier<AttachmentType<SelectedZone>> SELECTED_ZONE = AttachmentRegistryHelper.registerWithOutSerialize(Painter.frame,"selected_zone",()->new SelectedZone());
    public static final Supplier<AttachmentType<SelectedZoneForSwap>> SELECTED_ZONE_FOR_SWAP = AttachmentRegistryHelper.registerWithOutSerialize(Painter.frame,"selected_zone_for_swap",()->new SelectedZoneForSwap());

}