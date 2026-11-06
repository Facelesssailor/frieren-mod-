package dev.facelesssailor.siebenkreis.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.facelesssailor.siebenkreis.SiebenkreisEntity;
import dev.facelesssailor.siebenkreis.SiebenkreisMod;
import dev.facelesssailor.siebenkreis.SiebenkreisSpell;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import static dev.facelesssailor.siebenkreis.SiebenkreisEntity.*;

/**
 * Draws the circle tower with additive, full-bright quads.
 * <pre>
 * CHARGE   layers fade/scale in one by one (each takes ~1.2 s to settle), counter-rotating
 * ALIGN    every layer spins up and pulses while its runes lock (2 s)
 * COLLAPSE the stack telescopes down onto the ground circle (1.7 s)
 * PILLAR   a column of light, plus an echo flare at layer 5+ (3 s)
 * SCAR     a rune scar left on the ground fades out (6 s)
 * </pre>
 */
public class SiebenkreisRenderer extends EntityRenderer<SiebenkreisEntity> {
    private static final ResourceLocation[] RINGS = {
            tex("ring_0"), tex("ring_1"), tex("ring_2"), tex("ring_3")
    };
    private static final ResourceLocation INNER = tex("inner");
    private static final ResourceLocation BEAM = tex("beam");
    private static final ResourceLocation SCAR = tex("scar");

    /** How long a new layer takes to unfold, in ticks. */
    private static final float LAYER_UNFOLD = 24f;

    public SiebenkreisRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0;
    }

    private static ResourceLocation tex(String name) {
        return SiebenkreisMod.id("textures/entity/siebenkreis/" + name + ".png");
    }

    @Override
    public ResourceLocation getTextureLocation(SiebenkreisEntity entity) {
        return RINGS[0];
    }

    @Override
    public boolean shouldRender(SiebenkreisEntity entity, Frustum frustum, double camX, double camY, double camZ) {
        return entity.shouldRenderAtSqrDistance(entity.distanceToSqr(camX, camY, camZ));
    }

    @Override
    public void render(SiebenkreisEntity e, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        float t = e.tickCount + partialTick;
        int phase = e.getPhase();
        float pAge = t - e.clientPhaseStart;
        int built = e.getLayersBuilt();
        float radius = e.getRadius();

        // collapse progress 0..1
        float collapse = switch (phase) {
            case PHASE_COLLAPSE -> easeInOut(Mth.clamp(pAge / e.collapseTicks(), 0, 1));
            case PHASE_PILLAR, PHASE_SCAR -> 1f;
            default -> 0f;
        };
        // extra spin accumulated during the release (keeps rotation continuous)
        float spin = 0f, pulse = 1f;
        if (phase == PHASE_ALIGN) {
            float p = Mth.clamp(pAge / ALIGN_TICKS, 0, 1);
            spin = 2.5f * p * pAge;
            pulse = 1f + 0.35f * Mth.sin(pAge * 0.5f) * p;
        } else if (phase == PHASE_COLLAPSE) {
            spin = (e.isFullRelease() ? 2.5f * ALIGN_TICKS : 0f) + 5f * pAge;
            pulse = 1.2f + 0.6f * collapse;
        }

        // ---------------- circle tower
        if (phase <= PHASE_COLLAPSE) {
            float topY = 0;
            for (int i = 0; i < built; i++) {
                int born = e.layerBornTick[i] >= 0 ? e.layerBornTick[i] : 0;
                float age = Math.max(0, t - born);
                float grow = Mth.clamp(age / LAYER_UNFOLD, 0, 1);
                float scale = easeOutBack(grow);
                float fade = grow;
                if (phase == PHASE_COLLAPSE) fade *= 1f - 0.4f * collapse;

                float y = layerHeight(i) * (1f - collapse);
                float r = layerRadius(i, radius) * scale * (1f - 0.45f * collapse);
                float dir = (i % 2 == 0) ? 1f : -1f;
                float speed = 0.55f + 0.12f * i;
                float angle = dir * (t * speed + spin * (1f + 0.2f * i));
                // each layer descends a little while it unfolds
                y += (1f - easeOut(grow)) * 2.0f;
                topY = Math.max(topY, y);

                float[] c = tint(i, built);
                float b = fade * pulse;
                disc(pose, buffers, RINGS[i % RINGS.length], y, r, angle, c[0] * b, c[1] * b, c[2] * b);
                disc(pose, buffers, INNER, y + 0.01f, r * 0.56f, -angle * 1.6f, c[0] * b * 0.9f, c[1] * b * 0.9f, c[2] * b * 0.9f);

                // birth flare: a ring that expands and fades as the layer locks in
                if (age < 16) {
                    float f = age / 16f;
                    float fb = (1f - f) * 0.7f;
                    disc(pose, buffers, RINGS[(i + 1) % RINGS.length], y, r * (1.0f + 0.5f * f), angle * 0.5f, fb, fb, fb);
                }
            }
            // the thread of light linking the layers
            if (built >= 2 && topY > 0.5f) {
                float b = 0.45f * pulse;
                column(pose, buffers, 0, topY, 0.18f + 0.3f * collapse, t, 1f * b, 0.95f * b, 0.8f * b);
            }
        }

        // ---------------- pillar
        if (phase == PHASE_PILLAR) {
            float p = Mth.clamp(pAge / PILLAR_TICKS, 0, 1);
            float open = Mth.clamp(pAge / 5f, 0, 1);
            float width = radius * (built >= 4 ? 0.42f : 0.6f) * easeOut(open) * (1f - 0.75f * easeInOut(p));
            if (built >= 5) {
                float echo = pAge - ECHO_DELAY;
                if (echo > 0 && echo < 12) width *= 1f + 0.6f * (1f - echo / 12f);
            }
            float b = (float) Math.sqrt(1f - p);
            column(pose, buffers, 0, 64f, width, t, 1f * b, 0.95f * b, 0.82f * b);
            column(pose, buffers, 0, 64f, width * 0.45f, t * 1.7f, 1f * b, 1f * b, 1f * b);
            // ground flash
            float g = (1f - p);
            disc(pose, buffers, RINGS[0], 0.08f, radius * (1.0f + 0.6f * p), t * 2f, g, g * 0.92f, g * 0.75f);
        }

        // ---------------- rune scar
        if (phase >= PHASE_PILLAR) {
            float a = phase == PHASE_PILLAR ? Mth.clamp(pAge / 10f, 0, 1)
                    : 1f - Mth.clamp(pAge / SCAR_TICKS, 0, 1);
            a *= 0.85f;
            disc(pose, buffers, SCAR, 0.04f, radius * 1.05f, t * 0.15f, a, a * 0.62f, a * 0.28f);
        }

        super.render(e, yaw, partialTick, pose, buffers, light);
    }

    // ------------------------------------------------------------------ drawing

    private static void disc(PoseStack pose, MultiBufferSource buffers, ResourceLocation texture,
                             float y, float r, float angleDeg, float red, float green, float blue) {
        if (r <= 0.01f || (red + green + blue) <= 0.003f) return;
        VertexConsumer vc = buffers.getBuffer(RenderType.eyes(texture));
        pose.pushPose();
        pose.translate(0, y, 0);
        pose.mulPose(Axis.YP.rotationDegrees(angleDeg));
        PoseStack.Pose p = pose.last();
        // top face
        v(vc, p, -r, 0, -r, 0, 0, red, green, blue, 1);
        v(vc, p, -r, 0, r, 0, 1, red, green, blue, 1);
        v(vc, p, r, 0, r, 1, 1, red, green, blue, 1);
        v(vc, p, r, 0, -r, 1, 0, red, green, blue, 1);
        // bottom face
        v(vc, p, r, 0, -r, 1, 0, red, green, blue, -1);
        v(vc, p, r, 0, r, 1, 1, red, green, blue, -1);
        v(vc, p, -r, 0, r, 0, 1, red, green, blue, -1);
        v(vc, p, -r, 0, -r, 0, 0, red, green, blue, -1);
        pose.popPose();
    }

    /** Four crossed, double-sided vertical planes forming a soft column of light. */
    private static void column(PoseStack pose, MultiBufferSource buffers, float y0, float y1, float w,
                               float t, float red, float green, float blue) {
        if (w <= 0.01f) return;
        VertexConsumer vc = buffers.getBuffer(RenderType.eyes(BEAM));
        float scroll = (t * 0.02f) % 1f;
        float vTop = (y1 - y0) / 16f;
        for (int k = 0; k < 4; k++) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(45f * k + t * 0.8f));
            PoseStack.Pose p = pose.last();
            float v0 = -scroll, v1 = vTop - scroll;
            cv(vc, p, -w, y0, 0, v0, red, green, blue);
            cv(vc, p, -w, y1, 0, v1, red, green, blue);
            cv(vc, p, w, y1, 1, v1, red, green, blue);
            cv(vc, p, w, y0, 1, v0, red, green, blue);
            cv(vc, p, w, y0, 1, v0, red, green, blue);
            cv(vc, p, w, y1, 1, v1, red, green, blue);
            cv(vc, p, -w, y1, 0, v1, red, green, blue);
            cv(vc, p, -w, y0, 0, v0, red, green, blue);
            pose.popPose();
        }
    }

    private static void cv(VertexConsumer vc, PoseStack.Pose p, float x, float y, float u, float v, float r, float g, float b) {
        vc.addVertex(p, x, y, 0).setColor(r, g, b, 1f).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT).setNormal(p, 0, 0, 1);
    }

    private static void v(VertexConsumer vc, PoseStack.Pose p, float x, float y, float z, float u, float v,
                          float r, float g, float b, float ny) {
        vc.addVertex(p, x, y, z).setColor(r, g, b, 1f).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT).setNormal(p, 0, ny, 0);
    }

    // ------------------------------------------------------------------ easing / colour

    /** Pale blue-white at the ground, warm gold at the crown. */
    private static float[] tint(int i, int built) {
        float f = built <= 1 ? 0f : i / (float) (SiebenkreisSpell.MAX_LAYERS - 1);
        return new float[]{
                Mth.lerp(f, 0.70f, 1.00f),
                Mth.lerp(f, 0.88f, 0.82f),
                Mth.lerp(f, 1.00f, 0.45f)
        };
    }

    private static float easeOut(float x) {
        return 1f - (1f - x) * (1f - x) * (1f - x);
    }

    private static float easeInOut(float x) {
        return x < 0.5f ? 4f * x * x * x : 1f - (float) Math.pow(-2f * x + 2f, 3) / 2f;
    }

    private static float easeOutBack(float x) {
        float c1 = 1.70158f, c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(x - 1f, 3) + c1 * (float) Math.pow(x - 1f, 2);
    }
}
