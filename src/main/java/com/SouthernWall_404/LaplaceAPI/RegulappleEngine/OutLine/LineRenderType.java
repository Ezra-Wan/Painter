package com.SouthernWall_404.LaplaceAPI.RegulappleEngine.OutLine;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;

public class LineRenderType {

//    public RenderType liner=RenderType.create(
//            Laplace.MODID+".line",
//
//    )


        public static final RenderType PURE_COLOR = RenderType.create(
        "line",
        DefaultVertexFormat.POSITION_COLOR,       // 顶点格式：位置 + 颜色（无UV，无光照）
        VertexFormat.Mode.TRIANGLES,              // 使用三角形模式（兼容性好）
        256, false, true,
        RenderType.CompositeState.builder()
            .setShaderState(new RenderType.ShaderStateShard(GameRenderer::getPositionColorShader))
            .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)   // 支持半透明
                .setCullState(RenderType.NO_CULL)                            // 双面渲染，便于观察
                .setLightmapState(RenderType.NO_LIGHTMAP)                    // 不需要光照
                .setOverlayState(RenderType.NO_OVERLAY)
            .createCompositeState(true)
    );

    public static final RenderType PURE_COLOR_SOLID = RenderType.create(
            "line",
            DefaultVertexFormat.POSITION_COLOR,       // 顶点格式：位置 + 颜色（无UV，无光照）
            VertexFormat.Mode.TRIANGLES,              // 使用三角形模式（兼容性好）
            256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderType.ShaderStateShard(GameRenderer::getPositionColorShader))
                    .setTransparencyState(RenderType.NO_TRANSPARENCY)   // 支持半透明
                    .setCullState(RenderType.NO_CULL)                            // 双面渲染，便于观察
                    .setLightmapState(RenderType.NO_LIGHTMAP)                    // 不需要光照
                    .setOverlayState(RenderType.NO_OVERLAY)
                    .createCompositeState(true)
    );
}
