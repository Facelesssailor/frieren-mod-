package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.joml.Matrix4f;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class PixelFx {
    public static final int SPARK = 0;
    public static final int GLOW = 4;
    public static final int RING = 5;
    public static final int RING2 = 6;
    public static final int SMOKE = 7;
    public static final int DROP = 11;
    public static final int BUBBLE = 12;
    public static final int EMBER = 13;
    public static final int SNOW = 14;
    public static final int SHARD = 15;
    public static final int FIRE = 16;
    public static final int WISP = 24;
    public static final int PETAL = 28;
    public static final int BLADE = 29;
    public static final int FLOWER = 30;
    public static final int FCENTER = 31;
    public static final int STONE = 32;
    public static final int ROCK = 33;
    public static final int RUNE = 34;
    public static final int ARROW = 38;
    public static final int SPEAR = 39;
    public static final int WIND = 40;
    public static final int SLASH = 41;
    public static final int BOLT = 42;
    public static final int DUST = 46;
    public static final int GLINT = 47;
    public static final int BURST = 48;
    public static final int WAVE = 52;
    public static final int SPLASH = 56;
    public static final int HALO = 57;
    public static final int DOT = 58;
    private static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath("frieren_arcana", "textures/fx/pixel_fx.png");
    private static RenderType type;
    private static final int STRIDE = 20;
    private static final int MAX_QUADS = 40000;
    private static double[] q = new double[40960];
    private static int count = 0;

    private PixelFx() {
    }

    private static RenderType type() {
        if (type == null) {
            type = RenderType.entityTranslucentEmissive(ATLAS);
        }

        return type;
    }

    public static int anim(int var0, int var1, double var2, double var4) {
        int var6 = (int)Math.floor(var2 * var4) % var1;
        return var0 + (var6 < 0 ? var6 + var1 : var6);
    }

    private static void push(
        double var0,
        double var2,
        double var4,
        double var6,
        double var8,
        double var10,
        double var12,
        double var14,
        double var16,
        double var18,
        double var20,
        double var22,
        int var24,
        float var25,
        float var26,
        float var27,
        float var28
    ) {
        if (!(var28 <= 0.004F) && count < 40000) {
            if ((count + 1) * 20 > q.length) {
                double[] var29 = new double[q.length * 2];
                System.arraycopy(q, 0, var29, 0, q.length);
                q = var29;
            }

            int var30 = count * 20;
            q[var30] = var0;
            q[var30 + 1] = var2;
            q[var30 + 2] = var4;
            q[var30 + 3] = var6;
            q[var30 + 4] = var8;
            q[var30 + 5] = var10;
            q[var30 + 6] = var12;
            q[var30 + 7] = var14;
            q[var30 + 8] = var16;
            q[var30 + 9] = var18;
            q[var30 + 10] = var20;
            q[var30 + 11] = var22;
            q[var30 + 12] = (double)var24;
            q[var30 + 13] = (double)var25;
            q[var30 + 14] = (double)var26;
            q[var30 + 15] = (double)var27;
            q[var30 + 16] = (double)Math.min(1.0F, var28);
            count++;
        }
    }

    public static void sprite(Vec3 var0, double var1, int var3, float var4, float var5, float var6, float var7, double var8) {
        Vec3 var10 = ArcanaClient.camera();
        if (var10 != null && !(var1 <= 0.0)) {
            double var11 = var0.x - var10.x;
            double var13 = var0.y - var10.y;
            double var15 = var0.z - var10.z;
            double var17 = Math.sqrt(var11 * var11 + var13 * var13 + var15 * var15);
            if (!(var17 < 1.0E-4)) {
                var11 /= var17;
                var13 /= var17;
                var15 /= var17;
                double var19 = -var15;
                double var21 = var11;
                double var23 = Math.sqrt(var19 * var19 + var11 * var11);
                if (var23 < 1.0E-4) {
                    var19 = 1.0;
                    var21 = 0.0;
                    var23 = 1.0;
                }

                var19 /= var23;
                var21 /= var23;
                double var25 = var13 * var21;
                double var27 = var15 * var19 - var11 * var21;
                double var29 = -var13 * var19;
                double var31 = Math.sqrt(var25 * var25 + var27 * var27 + var29 * var29);
                var25 /= var31;
                var27 /= var31;
                var29 /= var31;
                double var33 = Math.cos(var8);
                double var35 = Math.sin(var8);
                double var37 = var1 * 0.5;
                double var39 = (var19 * var33 + var25 * var35) * var37;
                double var41 = var27 * var35 * var37;
                double var43 = (var21 * var33 + var29 * var35) * var37;
                double var45 = (-var19 * var35 + var25 * var33) * var37;
                double var47 = var27 * var33 * var37;
                double var49 = (-var21 * var35 + var29 * var33) * var37;
                push(
                    var0.x - var39 + var45,
                    var0.y - var41 + var47,
                    var0.z - var43 + var49,
                    var0.x + var39 + var45,
                    var0.y + var41 + var47,
                    var0.z + var43 + var49,
                    var0.x + var39 - var45,
                    var0.y + var41 - var47,
                    var0.z + var43 - var49,
                    var0.x - var39 - var45,
                    var0.y - var41 - var47,
                    var0.z - var43 - var49,
                    var3,
                    var4,
                    var5,
                    var6,
                    var7
                );
            }
        }
    }

    public static void sprite(Vec3 var0, double var1, int var3, float var4, float var5, float var6, float var7) {
        sprite(var0, var1, var3, var4, var5, var6, var7, 0.0);
    }

    public static void streak(Vec3 var0, Vec3 var1, double var2, int var4, float var5, float var6, float var7, float var8) {
        Vec3 var9 = ArcanaClient.camera();
        if (var9 != null) {
            double var10 = var1.x - var0.x;
            double var12 = var1.y - var0.y;
            double var14 = var1.z - var0.z;
            double var16 = (var1.x + var0.x) * 0.5 - var9.x;
            double var18 = (var1.y + var0.y) * 0.5 - var9.y;
            double var20 = (var1.z + var0.z) * 0.5 - var9.z;
            double var22 = var12 * var20 - var14 * var18;
            double var24 = var14 * var16 - var10 * var20;
            double var26 = var10 * var18 - var12 * var16;
            double var28 = Math.sqrt(var22 * var22 + var24 * var24 + var26 * var26);
            if (!(var28 < 1.0E-9)) {
                double var30 = var2 * 0.5 / var28;
                var22 *= var30;
                var24 *= var30;
                var26 *= var30;
                push(
                    var1.x - var22,
                    var1.y - var24,
                    var1.z - var26,
                    var1.x + var22,
                    var1.y + var24,
                    var1.z + var26,
                    var0.x + var22,
                    var0.y + var24,
                    var0.z + var26,
                    var0.x - var22,
                    var0.y - var24,
                    var0.z - var26,
                    var4,
                    var5,
                    var6,
                    var7,
                    var8
                );
            }
        }
    }

    public static void flat(Vec3 var0, Vec3 var1, double var2, int var4, double var5, float var7, float var8, float var9, float var10) {
        Vec3 var11 = Math.abs(var1.y) < 0.95 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
        Vec3 var12 = var1.cross(var11).normalize();
        Vec3 var13 = var1.cross(var12).normalize();
        double var14 = Math.cos(var5);
        double var16 = Math.sin(var5);
        double var18 = var2 * 0.5;
        Vec3 var20 = var12.scale(var14 * var18).add(var13.scale(var16 * var18));
        Vec3 var21 = var13.scale(var14 * var18).subtract(var12.scale(var16 * var18));
        quad(
            var0.subtract(var20).add(var21),
            var0.add(var20).add(var21),
            var0.add(var20).subtract(var21),
            var0.subtract(var20).subtract(var21),
            var4,
            var7,
            var8,
            var9,
            var10
        );
    }

    public static void quad(Vec3 var0, Vec3 var1, Vec3 var2, Vec3 var3, int var4, float var5, float var6, float var7, float var8) {
        push(var0.x, var0.y, var0.z, var1.x, var1.y, var1.z, var2.x, var2.y, var2.z, var3.x, var3.y, var3.z, var4, var5, var6, var7, var8);
    }

    @SubscribeEvent
    public static void flush(RenderLevelStageEvent var0) {
        if (var0.getStage() == Stage.AFTER_PARTICLES) {
            if (count != 0) {
                Minecraft var1 = Minecraft.getInstance();
                Vec3 var2 = var0.getCamera().getPosition();
                Matrix4f var3 = var0.getPoseStack().last().pose();
                BufferSource var4 = var1.renderBuffers().bufferSource();
                RenderType var5 = type();
                VertexConsumer var6 = var4.getBuffer(var5);

                for (int var7 = 0; var7 < count; var7++) {
                    int var8 = var7 * 20;
                    int var9 = (int)q[var8 + 12];
                    float var10 = (float)(var9 % 16) / 16.0F + 8.0E-4F;
                    float var11 = (float)(var9 / 16) / 16.0F + 8.0E-4F;
                    float var12 = (float)(var9 % 16 + 1) / 16.0F - 8.0E-4F;
                    float var13 = (float)(var9 / 16 + 1) / 16.0F - 8.0E-4F;
                    float var14 = (float)q[var8 + 13];
                    float var15 = (float)q[var8 + 14];
                    float var16 = (float)q[var8 + 15];
                    float var17 = (float)q[var8 + 16];
                    vert(var6, var3, var2, q[var8], q[var8 + 1], q[var8 + 2], var14, var15, var16, var17, var10, var11);
                    vert(var6, var3, var2, q[var8 + 3], q[var8 + 4], q[var8 + 5], var14, var15, var16, var17, var12, var11);
                    vert(var6, var3, var2, q[var8 + 6], q[var8 + 7], q[var8 + 8], var14, var15, var16, var17, var12, var13);
                    vert(var6, var3, var2, q[var8 + 9], q[var8 + 10], q[var8 + 11], var14, var15, var16, var17, var10, var13);
                }

                count = 0;
                var4.endBatch(var5);
            }
        }
    }

    private static void vert(
        VertexConsumer var0,
        Matrix4f var1,
        Vec3 var2,
        double var3,
        double var5,
        double var7,
        float var9,
        float var10,
        float var11,
        float var12,
        float var13,
        float var14
    ) {
        var0.addVertex(var1, (float)(var3 - var2.x), (float)(var5 - var2.y), (float)(var7 - var2.z))
            .setColor(var9, var10, var11, var12)
            .setUv(var13, var14)
            .setOverlay(655360)
            .setLight(15728880)
            .setNormal(0.0F, 1.0F, 0.0F);
    }

    public static int queued() {
        return count;
    }

    public static double[] raw() {
        return q;
    }

    public static void clear() {
        count = 0;
    }
}
