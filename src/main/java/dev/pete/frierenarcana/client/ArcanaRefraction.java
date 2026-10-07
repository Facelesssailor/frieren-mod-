package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.pete.frierenarcana.FrierenArcana;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT},
    bus = Bus.MOD
)
public final class ArcanaRefraction {
    private static TextureTarget snapshot;
    private static ShaderInstance shader;

    public static ShaderInstance shader() {
        return shader == null ? GameRenderer.getPositionColorShader() : shader;
    }

    @SubscribeEvent
    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(
            new ShaderInstance(event.getResourceProvider(), FrierenArcana.id("arcana_refraction"), DefaultVertexFormat.POSITION_COLOR),
            loaded -> shader = loaded
        );
    }

    public static void capture(double seconds) {
        RenderTarget source = Minecraft.getInstance().getMainRenderTarget();
        if (snapshot == null) {
            snapshot = new TextureTarget(source.width, source.height, false, Minecraft.ON_OSX);
        } else if (snapshot.width != source.width || snapshot.height != source.height) {
            snapshot.resize(source.width, source.height, Minecraft.ON_OSX);
        }

        GlStateManager._glBindFramebuffer(36008, source.frameBufferId);
        GlStateManager._glBindFramebuffer(36009, snapshot.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, source.width, source.height, 0, 0, snapshot.width, snapshot.height, 16384, 9728);
        source.bindWrite(false);
        if (shader != null) {
            shader.setSampler("SceneSampler", snapshot.getColorTextureId());
            shader.safeGetUniform("ScreenSize").set((float)source.width, (float)source.height);
            shader.safeGetUniform("ArcanaTime").set((float)seconds);
        }
    }

    public static void release() {
        if (snapshot != null) {
            snapshot.destroyBuffers();
            snapshot = null;
        }
    }
}
