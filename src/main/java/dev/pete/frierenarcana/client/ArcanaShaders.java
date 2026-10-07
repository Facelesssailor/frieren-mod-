package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.pete.frierenarcana.FrierenArcana;
import java.io.IOException;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
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
public final class ArcanaShaders {
    private static ShaderInstance energy;

    public static ShaderInstance energy() {
        return energy == null ? GameRenderer.getPositionColorShader() : energy;
    }

    @SubscribeEvent
    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(
            new ShaderInstance(event.getResourceProvider(), FrierenArcana.id("arcana_energy"), DefaultVertexFormat.POSITION_COLOR), shader -> energy = shader
        );
    }

    public static void prepare(double seconds, Vec3 camera) {
        if (energy != null) {
            energy.safeGetUniform("ArcanaTime").set((float)seconds);
            energy.safeGetUniform("CameraOffset").set((float)camera.x, (float)camera.y, (float)camera.z);
        }
    }
}
