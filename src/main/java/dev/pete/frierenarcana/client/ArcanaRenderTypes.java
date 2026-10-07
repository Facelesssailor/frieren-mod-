package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;

public final class ArcanaRenderTypes extends RenderType {
    public static final RenderType MAGIC = create(
        "frieren_arcana_magic",
        DefaultVertexFormat.POSITION_COLOR,
        Mode.QUADS,
        262144,
        false,
        true,
        CompositeState.builder()
            .setShaderState(new ShaderStateShard(ArcanaShaders::energy))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setCullState(NO_CULL)
            .setWriteMaskState(COLOR_WRITE)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .createCompositeState(false)
    );
    public static final RenderType REFRACTION = create(
        "frieren_arcana_refraction",
        DefaultVertexFormat.POSITION_COLOR,
        Mode.QUADS,
        65536,
        false,
        true,
        CompositeState.builder()
            .setShaderState(new ShaderStateShard(ArcanaRefraction::shader))
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setCullState(NO_CULL)
            .setWriteMaskState(COLOR_WRITE)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .createCompositeState(false)
    );

    private ArcanaRenderTypes(String name, VertexFormat format, Mode mode, int size, boolean crumbling, boolean sorted, Runnable setup, Runnable clear) {
        super(name, format, mode, size, crumbling, sorted, setup, clear);
    }
}
