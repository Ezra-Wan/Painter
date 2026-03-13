package com.SouthernWall_404.Painter;

import com.SouthernWall_404.Painter.API.Capability.PaintInfo;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Painter
            .MODID);

    // 通过 INBTSerializable 进行序列化
    public static final Supplier<AttachmentType<PaintInfo>> PERSONALITY = ATTACHMENT_TYPES.register(
            Painter.MODID+".personality", () -> AttachmentType.serializable(() -> new PaintInfo()).build()
    );


}