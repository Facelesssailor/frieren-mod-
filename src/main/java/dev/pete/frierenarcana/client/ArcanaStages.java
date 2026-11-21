package dev.pete.frierenarcana.client;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * When the mod draws its glow. Vanilla draws clouds and rain after the translucent and particle passes; the barrier,
 * the needle and the shards write no depth, so anything drawn before the clouds had the clouds' dark undersides
 * painted over it (seen in the in-game capture: grey blocks across the bands and the falling crystals). With Fancy
 * graphics the glow is therefore drawn after the weather. Fabulous graphics composites clouds and rain separately by
 * depth, so there the earlier passes are kept.
 */
final class ArcanaStages {
    private ArcanaStages() {
    }

    /** The stage for the barrier, beams, circles and shards. */
    static RenderLevelStageEvent.Stage glow() {
        return Minecraft.useShaderTransparency() ? RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS : RenderLevelStageEvent.Stage.AFTER_WEATHER;
    }

    /** The stage for the sprite effects. */
    static RenderLevelStageEvent.Stage sprites() {
        return Minecraft.useShaderTransparency() ? RenderLevelStageEvent.Stage.AFTER_PARTICLES : RenderLevelStageEvent.Stage.AFTER_WEATHER;
    }
}
