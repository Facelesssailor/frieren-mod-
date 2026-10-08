package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class BreakerFx {
    public static final double PINCH = 3.1;
    public static final double NEEDLE = 6.2;
    static final double CRACK_END = 4.95;
    static final double SHATTER = 5.0;
    static final double BANDS_BEGIN = 4.6;
    public static final double LIFE = 23.7;
    public static final double BEAM_LIFE = 11.5;
    private static final Map<Long, BreakerFx.ShardSet> SETS = new HashMap<>();

    private BreakerFx() {
    }

    static double hash(int var0, int var1) {
        double var2 = Math.sin((double)var0 * 12.9898 + (double)var1 * 78.233) * 43758.5453;
        return var2 - Math.floor(var2);
    }

    private static double sstep(double var0, double var2, double var4) {
        double var6 = Math.max(0.0, Math.min(1.0, (var4 - var0) / (var2 - var0)));
        return var6 * var6 * (3.0 - 2.0 * var6);
    }

    private static void v(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, double var3, double var5, double var7, float var9, float var10, float var11, float var12
    ) {
        var0.addVertex(var1, (float)(var3 - var2.x), (float)(var5 - var2.y), (float)(var7 - var2.z)).setColor(var9, var10, var11, var12);
    }

    static void ribbon(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, float var7, float var8, float var9, float var10) {
        if (!(var10 <= 0.003F)) {
            double var11 = var4.x - var3.x;
            double var13 = var4.y - var3.y;
            double var15 = var4.z - var3.z;
            double var17 = (var3.x + var4.x) * 0.5 - var2.x;
            double var19 = (var3.y + var4.y) * 0.5 - var2.y;
            double var21 = (var3.z + var4.z) * 0.5 - var2.z;
            double var23 = var13 * var21 - var15 * var19;
            double var25 = var15 * var17 - var11 * var21;
            double var27 = var11 * var19 - var13 * var17;
            double var29 = Math.sqrt(var23 * var23 + var25 * var25 + var27 * var27);
            if (!(var29 < 1.0E-9)) {
                double var31 = var5 * 0.5 / var29;
                var23 *= var31;
                var25 *= var31;
                var27 *= var31;
                v(var0, var1, var2, var3.x - var23, var3.y - var25, var3.z - var27, var7, var8, var9, var10);
                v(var0, var1, var2, var3.x + var23, var3.y + var25, var3.z + var27, var7, var8, var9, var10);
                v(var0, var1, var2, var4.x + var23, var4.y + var25, var4.z + var27, var7, var8, var9, var10);
                v(var0, var1, var2, var4.x - var23, var4.y - var25, var4.z - var27, var7, var8, var9, var10);
            }
        }
    }

    private static Vec3[] basis(Vec3 var0, Vec3 var1) {
        double var2 = var0.x - var1.x;
        double var4 = var0.y - var1.y;
        double var6 = var0.z - var1.z;
        double var8 = Math.sqrt(var2 * var2 + var4 * var4 + var6 * var6);
        if (var8 < 1.0E-6) {
            var2 = 0.0;
            var4 = 0.0;
            var6 = 1.0;
            var8 = 1.0;
        }

        var2 /= var8;
        var4 /= var8;
        var6 /= var8;
        double var10 = -var6;
        double var12 = var2;
        double var14 = Math.sqrt(var10 * var10 + var2 * var2);
        if (var14 < 1.0E-4) {
            var10 = 1.0;
            var12 = 0.0;
            var14 = 1.0;
        }

        var10 /= var14;
        var12 /= var14;
        Vec3 var16 = new Vec3(var10, 0.0, var12);
        Vec3 var17 = new Vec3(var4 * var12, var6 * var10 - var2 * var12, -var4 * var10);
        return new Vec3[]{var16, var17.normalize()};
    }

    static void disc(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, float var6, float var7, float var8, float var9) {
        if (!(var9 <= 0.003F) && !(var4 <= 0.0)) {
            PixelFx.sprite(var3, var4 * 2.3, 4, var6, var7, var8, Math.min(1.0F, var9 * 1.4F));
        }
    }

    static void softGlow(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, float var6, float var7, float var8, float var9) {
        if (!(var9 <= 0.003F) && !(var4 <= 0.0)) {
            Vec3[] var10 = basis(var3, var2);
            double[] var11 = new double[]{0.0, 0.25, 0.55, 1.0};
            double[] var12 = new double[]{1.0, 0.62, 0.22, 0.0};

            for (int var13 = 0; var13 < var11.length - 1; var13++) {
                for (int var14 = 0; var14 < 28; var14++) {
                    double var15 = (double)var14 * Math.PI * 2.0 / 28.0;
                    double var17 = (double)(var14 + 1) * Math.PI * 2.0 / 28.0;
                    double var19 = var11[var13] * var4;
                    double var21 = var11[var13 + 1] * var4;
                    float var23 = (float)((double)var9 * var12[var13]);
                    float var24 = (float)((double)var9 * var12[var13 + 1]);
                    Vec3 var25 = var3.add(var10[0].scale(Math.cos(var15) * var19)).add(var10[1].scale(Math.sin(var15) * var19));
                    Vec3 var26 = var3.add(var10[0].scale(Math.cos(var17) * var19)).add(var10[1].scale(Math.sin(var17) * var19));
                    Vec3 var27 = var3.add(var10[0].scale(Math.cos(var17) * var21)).add(var10[1].scale(Math.sin(var17) * var21));
                    Vec3 var28 = var3.add(var10[0].scale(Math.cos(var15) * var21)).add(var10[1].scale(Math.sin(var15) * var21));
                    v(var0, var1, var2, var25.x, var25.y, var25.z, var6, var7, var8, var23);
                    v(var0, var1, var2, var26.x, var26.y, var26.z, var6, var7, var8, var23);
                    v(var0, var1, var2, var27.x, var27.y, var27.z, var6, var7, var8, var24);
                    v(var0, var1, var2, var28.x, var28.y, var28.z, var6, var7, var8, var24);
                }
            }
        }
    }

    static void sparkle(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, float var6, float var7, float var8, float var9) {
        if (!(var9 <= 0.003F) && !(var4 <= 0.0)) {
            int var10 = var4 < 0.25 ? 0 : (var4 < 0.6 ? 1 : (var4 < 1.2 ? 2 : 3));
            PixelFx.sprite(var3, var4 * 2.1, var10, var6, var7, var8, var9);
        }
    }

    private static float g(float var0, float var1) {
        return Math.abs(var0 - 0.9F) < 0.012F && var1 > 0.57F && var1 < 0.67F ? 0.925F : var0;
    }

    public static void beam(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4) {
        Vec3 var6 = ArcanaClient.camera();
        if (var6 != null && !(var4 > 11.5) && !(var4 < 3.1)) {
            Vec3 var7 = ArcanaCinematic.breakerActive() ? CinemaDirector.lockedLook() : Vec3.ZERO;
            var7 = new Vec3(var7.x, 0.0, var7.z);
            var7 = var7.lengthSqr() < 1.0E-4 ? Vec3.ZERO : var7.normalize().scale(0.42);
            var2 = var2.add(var7.x, -0.57, var7.z);
            double var8 = Math.min(1.0, (var4 - 3.1) / 3.1);
            double var10 = Math.pow(var8, 1.6);
            double var12 = sstep(0.0, 0.6, var4 - 3.1);
            float var14 = (float)(1.0 - sstep(11.149999999999999, 11.5, var4));
            float var15 = (float)(0.9 + 0.1 * Math.sin(var4 * 37.0));
            Vec3 var16 = var2.lerp(var3, var10);
            if (var4 > 6.2) {
                var16 = var3.add(0.0, Math.min(70.0, (var4 - 6.2) * 55.0), 0.0);
            }

            Vec3[] var17 = var4 > 6.2 ? new Vec3[]{var2, var3, var16} : new Vec3[]{var2, var16};
            SpellFx.tube(var0, var1, var6, new Vec3[]{var2, var2.lerp(var16, 0.25)}, 0.05 + 0.85 * var12, 0.55F, 1.0F, 0.86F, 0.1F * var14);
            SpellFx.tube(var0, var1, var6, var17, 0.04 + 0.38 * var12, 0.55F, 1.0F, 0.86F, 0.12F * var14 * var15);
            SpellFx.tube(var0, var1, var6, var17, 0.16, 0.78F, 1.0F, 0.95F, 0.5F * var14);
            SpellFx.tube(var0, var1, var6, var17, 0.055, 1.0F, 1.0F, 1.0F, 0.98F * var14);
            sparkle(var0, var1, var6, var16, 0.6 * (double)var15, 0.92F, 1.0F, 0.98F, var14);
            softGlow(var0, var1, var6, var16, 1.1, 0.82F, 1.0F, 0.97F, 0.4F * var14);
            Vec3 var18 = var16.subtract(var2);

            for (int var19 = 0; var19 < 30; var19++) {
                double var20 = (hash(var19, 1) + var4 * (0.5 + 0.5 * hash(var19, 2))) % 1.0;
                Vec3 var22 = var2.add(var18.scale(var20))
                    .add(Math.sin((double)var19 * 2.3 + var4 * 6.0) * 0.22, 0.0, Math.cos((double)var19 * 1.7 + var4 * 5.0) * 0.22);
                disc(var0, var1, var6, var22, 0.06 + 0.05 * hash(var19, 3), 0.8F, 1.0F, 0.95F, 0.7F * var14);
            }

            float var26 = (float)(0.85 + 0.15 * Math.sin(var4 * 22.0));
            sparkle(var0, var1, var6, var2, 0.7 * (double)var26 * (0.4 + 0.6 * var12), 0.92F, 1.0F, 0.98F, 0.8F * var14);
            if (var4 >= 6.2) {
                double var27 = var4 - 6.2;
                sparkle(var0, var1, var6, var3, (2.6 + 0.6 * Math.sin(var4 * 18.0)) * (double)var14, 1.0F, 1.0F, 1.0F, 0.95F * var14);
                softGlow(var0, var1, var6, var3, 1.6 + var27 * 1.2, 0.88F, 1.0F, 0.98F, 0.35F * var14);
            }
        }
    }

    static double remap(ArcanaClient.Fracture var0, double var1) {
        if (!var0.field().defensive()) {
            return var1 - 6.2;
        } else {
            return var1 < 0.3 ? var1 * 16.666666666666668 : 5.0 + (var1 - 0.3) * 1.8;
        }
    }

    public static void fracture(VertexConsumer var0, Matrix4f var1, ArcanaClient.Fracture var2, double var3, double var5) {
        Vec3 var7 = ArcanaClient.camera();
        if (var7 != null) {
            boolean var8 = var2.field().defensive();
            var5 = remap(var2, var5);
            if (!var8 && var5 >= 5.0 && var5 < 6.0) {
                BreakerSound.shatter(var2);
            }

            Vec3 var9 = var2.field().center();
            double var10 = (double)var2.field().radius();
            Vec3 var12 = var2.impact();
            if (!var8 && var5 < 5.05) {
                double var13 = var5 + 6.2;
                double var15 = Math.pow(Math.max(0.0, Math.min(1.0, (var13 - 4.6) / 5.200000000000001)), 2.8);
                float var17 = (float)(1.0 - sstep(4.98, 5.05, var5));
                if (var5 > 4.85) {
                    BarrierLook.shell(var0, var1, var9, var10, var7, var17, 2.5F);
                } else if (var5 > 4.7) {
                    BarrierLook.shell(var0, var1, var9, var10, var7, 1.0F, 1.5F);
                } else if (var15 < 1.0) {
                    BarrierLook.shellFalling(var0, var1, var9, var10, var7, (float)(0.4 + 0.6 * sstep(3.1, 5.1, var13)), (float)(0.62 + 0.38 * var15), var15);
                } else {
                    BarrierLook.shell(var0, var1, var9, var10, var7, (float)(1.0 + 0.4 * sstep(3.6, 4.7, var5)), 1.0F);
                }

                if (var5 > 0.0) {
                    double var18 = sstep(3.4, 5.0, var5) * (double)var17;
                    softGlow(var0, var1, var7, var12, var10 * (0.06 + 0.45 * var18), 1.0F, 1.0F, 1.0F, (float)(0.12 + 0.6 * var18));
                }

                if (var5 < 0.0) {
                    return;
                }
            }

            if (var8 && var5 < 5.3) {
                cracks(var0, var1, var7, var9, var10, var12, var5, (float)(1.0 - sstep(5.0, 5.3, var5)));
            }

            double var36 = var5 - 4.95;
            if (var36 > 0.0 && var36 < 1.1) {
                float var37 = (float)(sstep(0.0, 0.08, var36) * (1.0 - sstep(0.4, 1.1, var36)));
                softGlow(var0, var1, var7, var12, (var8 ? var10 * 1.2 : var10 * 0.9) + var36 * var10 * 0.6, 1.0F, 1.0F, 1.0F, var37 * (var8 ? 0.55F : 0.85F));
            }

            if (!var8) {
                double var38 = var5 - 5.0;
                if (var38 > 0.1) {
                    sky(var0, var1, var7, var9, var10, var38, var3);
                }

                if (var38 > 0.1) {
                    glitter(var0, var1, var7, var9, var10, var38, var3);
                }
            }

            BreakerFx.ShardSet var39 = set(var2);
            Vec3 var16 = var12.subtract(var9).normalize();
            BreakerFx.Pose var40 = new BreakerFx.Pose();

            for (int var41 = 0; var41 < var39.pieces.size(); var41++) {
                BreakerFx.Piece var19 = var39.pieces.get(var41);
                if (pose(var19, var41, var9, var10, var16, var5, var7, var8, var40)) {
                    int var20 = var40.n;

                    for (int var21 = 0; var21 < var20; var21++) {
                        int var22 = (var21 + 1) % var20;
                        double var23 = var40.facet[var21];
                        float var25;
                        float var26;
                        float var27;
                        if (var8) {
                            var25 = 0.78F;
                            var26 = 0.95F;
                            var27 = 1.0F;
                        } else if (var40.white > 0.0) {
                            double[] var28 = crystal(var19, var21, var5, var23);
                            double var29 = var28[0];
                            double var31 = var28[1];
                            double var33 = var28[2];
                            var25 = (float)(var29 + (0.97 - var29) * var40.white);
                            var26 = (float)(var31 + (0.96 - var31) * var40.white);
                            var27 = (float)(var33 + (1.0 - var33) * var40.white);
                        } else {
                            double[] var43 = crystal(var19, var21, var5, var23);
                            var25 = (float)var43[0];
                            var26 = (float)var43[1];
                            var27 = (float)var43[2];
                        }

                        var26 = g(var26, var27);
                        float var44 = var40.alpha * (var8 ? 0.1F : 0.3F);
                        Vec3 var45 = var40.ctr;
                        Vec3 var30 = var40.vert[var21];
                        Vec3 var46 = var40.vert[var22];
                        v(var0, var1, var7, var45.x, var45.y, var45.z, var25, var26, var27, var44);
                        v(var0, var1, var7, var30.x, var30.y, var30.z, var25, var26, var27, var44);
                        v(var0, var1, var7, var46.x, var46.y, var46.z, var25, var26, var27, var44);
                        v(var0, var1, var7, var46.x, var46.y, var46.z, var25, var26, var27, var44);
                        double var32 = hash(var41 * 13 + var21, 31);
                        float var34 = var40.alpha * (float)(var8 ? 0.85 : (var32 > 0.45 ? 0.15 + 0.55 * var23 : 0.0));
                        if (var34 > 0.01F) {
                            ribbon(var0, var1, var7, var30, var46, Math.max(0.02, var19.size * (var8 ? 0.035 : 0.02)), 0.93F, 0.98F, 1.0F, var34);
                        }
                    }

                    if (var41 % 6 == 0 && var40.facet[0] > 0.8) {
                        sparkle(
                            var0,
                            var1,
                            var7,
                            var40.vert[0],
                            Math.min(1.2, var19.size * 0.35),
                            1.0F,
                            1.0F,
                            1.0F,
                            var40.alpha * (float)((var40.facet[0] - 0.8) / 0.2)
                        );
                    }
                }
            }
        }
    }

    private static double[] crystal(BreakerFx.Piece var0, int var1, double var2, double var4) {
        double var6 = hash((int)(var0.h0 * 9973.0) + var1 * 17, 61);
        double[] var8 = var6 < 0.38
            ? new double[]{0.72, 0.62, 0.95}
            : (
                var6 < 0.58
                    ? new double[]{0.58, 0.66, 0.98}
                    : (
                        var6 < 0.72
                            ? new double[]{0.55, 0.9, 0.95}
                            : (var6 < 0.84 ? new double[]{0.95, 0.86, 0.55} : (var6 < 0.93 ? new double[]{0.95, 0.7, 0.86} : new double[]{0.92, 0.94, 1.0}))
                    )
            );
        double var9 = 0.08 * Math.sin(6.2831 * (var0.h1 + var2 * 0.06 + (double)var1 * 0.11));
        double var11 = 0.25 + 0.75 * var4;
        double[] var13 = new double[]{0.3, 0.28, 0.5};
        return new double[]{
            (var8[0] + var9) * var11 + var13[0] * (1.0 - var11),
            (var8[1] + var9 * 0.5) * var11 + var13[1] * (1.0 - var11),
            var8[2] * var11 + var13[2] * (1.0 - var11)
        };
    }

    private static void cracks(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, Vec3 var6, double var7, float var9) {
        Vec3 var10 = var6.subtract(var3).normalize();
        Vec3 var11 = var10.cross(Math.abs(var10.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0)).normalize();
        Vec3 var12 = var10.cross(var11);
        double var13 = Math.PI * Math.pow(Math.min(1.0, var7 / 4.95), 0.8);
        double var15 = var4 + 0.06;
        double var17 = 0.05 + var4 * 0.0025;
        float var19 = (float)(0.75 + 0.25 * Math.sin(var7 * 25.0)) * var9;
        byte var20 = 22;

        for (int var21 = 0; var21 < var20; var21++) {
            double var22 = (double)var21 * Math.PI * 2.0 / (double)var20 + hash(var21, 1) * 0.2;
            Vec3 var24 = var6;
            byte var25 = 26;

            for (int var26 = 1; var26 <= var25; var26++) {
                double var27 = Math.PI * (double)var26 / (double)var25;
                if (var27 > var13) {
                    break;
                }

                double var29 = (hash(var21 * 31 + var26, 2) - 0.5) * 0.35;
                Vec3 var31 = var11.scale(Math.cos(var22 + var29)).add(var12.scale(Math.sin(var22 + var29)));
                Vec3 var32 = var3.add(var10.scale(Math.cos(var27) * var15)).add(var31.scale(Math.sin(var27) * var15));
                ribbon(var0, var1, var2, var24, var32, var17 * 4.0, 0.62F, 0.86F, 1.0F, 0.16F * var19);
                ribbon(var0, var1, var2, var24, var32, var17, 0.94F, 0.98F, 1.0F, 0.95F * var19);
                var24 = var32;
            }
        }
    }

    private static double vnoise(double var0, double var2) {
        int var4 = (int)Math.floor(var0);
        int var5 = (int)Math.floor(var2);
        double var6 = var0 - (double)var4;
        double var8 = var2 - (double)var5;
        var6 = var6 * var6 * (3.0 - 2.0 * var6);
        var8 = var8 * var8 * (3.0 - 2.0 * var8);
        double var10 = hash(var4 * 57 + var5 * 131, 41);
        double var12 = hash((var4 + 1) * 57 + var5 * 131, 41);
        double var14 = hash(var4 * 57 + (var5 + 1) * 131, 41);
        double var16 = hash((var4 + 1) * 57 + (var5 + 1) * 131, 41);
        return var10 + (var12 - var10) * var6 + (var14 + (var16 - var14) * var6 - (var10 + (var12 - var10) * var6)) * var8;
    }

    private static void sky(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, double var8) {
        double var10 = sstep(0.1, 1.2, var6) * (1.0 - sstep(6.0, 12.0, var6));
        if (!(var10 <= 0.01)) {
            double var12 = var4 * 1.5;
            double var14 = 2.0 * var12 / 20.0;
            double var16 = 3.2 / var4;

            for (int var18 = 0; var18 < 2; var18++) {
                double var19 = var3.y + var4 * (0.38 + 0.16 * (double)var18) - var6 * var4 * 0.004;

                for (int var21 = 0; var21 < 20; var21++) {
                    for (int var22 = 0; var22 < 20; var22++) {
                        double[][] var23 = new double[][]{
                            {(double)var21, (double)var22},
                            {(double)(var21 + 1), (double)var22},
                            {(double)(var21 + 1), (double)(var22 + 1)},
                            {(double)var21, (double)(var22 + 1)}
                        };
                        float[] var24 = new float[4];
                        float[][] var25 = new float[4][];
                        double[][] var26 = new double[4][];
                        boolean var27 = false;

                        for (int var28 = 0; var28 < 4; var28++) {
                            double var29 = -var12 + var23[var28][0] * var14;
                            double var31 = -var12 + var23[var28][1] * var14;
                            double var33 = Math.sqrt(var29 * var29 + var31 * var31) / var12;
                            double var35 = vnoise(var29 * var16 + (double)var18 * 7.3 + var6 * 0.06, var31 * var16 - (double)var18 * 3.1);
                            double var37 = sstep(0.42, 0.85, var35) * Math.max(0.0, 1.0 - var33 * var33);
                            double var39 = var33 * 1.5 + var35 * 0.6 + var8 * 0.03 + (double)var18 * 0.2;
                            float var41 = (float)(0.76 + 0.24 * Math.cos(6.2831 * var39));
                            float var42 = (float)(0.74 + 0.24 * Math.cos(6.2831 * (var39 - 0.33)));
                            float var43 = (float)(0.8 + 0.2 * Math.cos(6.2831 * (var39 + 0.33)));
                            var25[var28] = new float[]{var41, g(var42, var43), var43};
                            var24[var28] = (float)(0.3 * var10 * var37);
                            var27 |= var24[var28] > 0.004F;
                            var26[var28] = new double[]{var3.x + var29, var19, var3.z + var31};
                        }

                        if (var27) {
                            for (int var44 = 0; var44 < 4; var44++) {
                                v(
                                    var0,
                                    var1,
                                    var2,
                                    var26[var44][0],
                                    var26[var44][1],
                                    var26[var44][2],
                                    var25[var44][0],
                                    var25[var44][1],
                                    var25[var44][2],
                                    var24[var44]
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    private static void glitter(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, double var8) {
        double var10 = sstep(0.1, 0.6, var6) * (1.0 - sstep(7.0, 10.5, var6));
        if (!(var10 <= 0.01)) {
            int var12 = (int)Math.max(80.0, Math.min(260.0, var4 * 5.0));
            double var13 = Math.sqrt(Math.max(0.5, var4 / 24.0));

            for (int var15 = 0; var15 < var12; var15++) {
                double var16 = 0.15 + 0.85 * hash(var15, 51);
                double var18 = 6.2831 * hash(var15, 52);
                double var20 = Math.sqrt(1.0 - var16 * var16) * (0.35 + 0.65 * hash(var15, 53));
                double var22 = var3.y + var4 * var16 * (0.4 + 0.6 * hash(var15, 54)) - var6 * (0.8 + 1.2 * hash(var15, 55)) * var13;
                if (!(var22 < var3.y)) {
                    Vec3 var24 = new Vec3(
                        var3.x + Math.cos(var18) * var20 * var4 + Math.sin(var6 * 0.7 + (double)var15) * 0.4 * var13,
                        var22,
                        var3.z + Math.sin(var18) * var20 * var4
                    );
                    double var25 = 6.2831 * hash(var15, 56);
                    float var27 = (float)(0.35 + 0.65 * Math.max(0.0, Math.sin(var8 * (3.0 + 4.0 * hash(var15, 57)) + var25)));
                    float var28 = (float)(0.8 + 0.2 * Math.cos(var25));
                    float var29 = (float)(0.8 + 0.2 * Math.cos(var25 - 2.1));
                    float var30 = (float)(0.88 + 0.12 * Math.cos(var25 + 2.1));
                    sparkle(
                        var0, var1, var2, var24, (0.1 + 0.16 * hash(var15, 58)) * var13, var28, g(var29, var30), var30, (float)(var10 * (double)var27 * 0.85)
                    );
                }
            }
        }
    }

    static BreakerFx.ShardSet set(ArcanaClient.Fracture var0) {
        double var1 = (double)var0.field().radius();
        boolean var3 = var0.field().defensive();
        long var4 = var0.startNanos() * 31L + Double.doubleToLongBits(var1) + (long)(var3 ? 7 : 0);
        BreakerFx.ShardSet var6 = SETS.get(var4);
        if (var6 == null) {
            if (SETS.size() > 6) {
                SETS.clear();
            }

            var6 = build(var1, var3);
            SETS.put(var4, var6);
        }

        return var6;
    }

    static int defCount(double var0) {
        return (int)Math.max(50.0, Math.min(220.0, var0 * var0 * 5.0));
    }

    static int shardCount(double var0) {
        return (int)Math.max(500.0, Math.min(2000.0, var0 * var0 * 2.0));
    }

    private static BreakerFx.ShardSet build(double var0, boolean var2) {
        BreakerFx.ShardSet var3 = new BreakerFx.ShardSet();
        int var4 = var2 ? defCount(var0) : shardCount(var0);
        double var5 = var2 ? -1.0 : -0.15;
        int var7 = Math.max(16, var4 / 2);
        int var8 = (int)Math.ceil((double)var7 * 2.0 / (1.0 - var5));
        ArrayList var9 = new ArrayList();

        for (int var10 = 0; var10 < var8; var10++) {
            double var11 = 1.0 - 2.0 * ((double)var10 + 0.5) / (double)var8 + (hash(var10, 22) - 0.5) * (1.6 / (double)var8);
            var11 = Math.max(-1.0, Math.min(1.0, var11));
            if (!(var11 < var5)) {
                double var13 = (double)var10 * 2.399963229728653 + (hash(var10, 21) - 0.5) * 0.9;
                double var15 = Math.sqrt(Math.max(0.0, 1.0 - var11 * var11));
                var9.add(new double[]{Math.cos(var13) * var15, var11, Math.sin(var13) * var15});
            }
        }

        int var73 = var9.size();
        double var75 = Math.sqrt((Math.PI * 4) / (double)var8);
        double var76 = Math.cos(Math.min(1.2, var75 * 3.2));

        for (int var77 = 0; var77 < var73; var77++) {
            double[] var16 = (double[])var9.get(var77);
            double var17 = var16[0];
            double var19 = var16[1];
            double var21 = var16[2];
            double var23 = Math.abs(var19) < 0.95 ? 0.0 : 1.0;
            double var25 = Math.abs(var19) < 0.95 ? 1.0 : 0.0;
            double var27 = var19 * 0.0 - var21 * var25;
            double var29 = var21 * var23 - var17 * 0.0;
            double var31 = var17 * var25 - var19 * var23;
            double var33 = Math.sqrt(var27 * var27 + var29 * var29 + var31 * var31);
            var27 /= var33;
            var29 /= var33;
            var31 /= var33;
            double var35 = var19 * var31 - var21 * var29;
            double var37 = var21 * var27 - var17 * var31;
            double var39 = var17 * var29 - var19 * var27;
            double var41 = var75 * var0 * 2.5;
            Object var43 = new ArrayList();
            var43.add(new double[]{-var41, -var41});
            var43.add(new double[]{var41, -var41});
            var43.add(new double[]{var41, var41});
            var43.add(new double[]{-var41, var41});

            for (int var44 = 0; var44 < var73 && var43.size() >= 3; var44++) {
                if (var44 != var77) {
                    double[] var45 = (double[])var9.get(var44);
                    double var46 = var45[0] * var17 + var45[1] * var19 + var45[2] * var21;
                    if (!(var46 < var76) && !(var46 <= 0.05)) {
                        double var48 = var45[0] / var46 - var17;
                        double var50 = var45[1] / var46 - var19;
                        double var52 = var45[2] / var46 - var21;
                        double var54 = (var48 * var27 + var50 * var29 + var52 * var31) * var0;
                        double var56 = (var48 * var35 + var50 * var37 + var52 * var39) * var0;
                        var43 = clip((List<double[]>)var43, var54, var56, (var54 * var54 + var56 * var56) * 0.5);
                    }
                }
            }

            if (var43.size() >= 3) {
                double[] var81 = centroid((List<double[]>)var43);
                double var82 = Math.PI * hash(var77, 23);
                double var47 = Math.cos(var82);
                double var49 = Math.sin(var82);
                double var51 = var81[0] * var47 + var81[1] * var49 + (hash(var77, 24) - 0.5) * var75 * var0 * 0.5;
                ArrayList var53 = new ArrayList();
                var53.add(clip((List<double[]>)var43, var47, var49, var51));
                var53.add(clip((List<double[]>)var43, -var47, -var49, -var51));

                for (int var83 = 0; var83 < 2; var83++) {
                    List var55 = (List)var53.get(var83);
                    if (var55.size() >= 3) {
                        double[] var84 = centroid(var55);
                        double var57 = area(var55);
                        if (!(var57 < 1.0E-4)) {
                            BreakerFx.Piece var59 = new BreakerFx.Piece();
                            int var60 = var3.pieces.size();
                            var59.nx = var17;
                            var59.ny = var19;
                            var59.nz = var21;
                            var59.t1x = var27;
                            var59.t1y = var29;
                            var59.t1z = var31;
                            var59.t2x = var35;
                            var59.t2y = var37;
                            var59.t2z = var39;
                            var59.cu = var84[0];
                            var59.cv = var84[1];
                            var59.u = new double[var55.size()];
                            var59.v = new double[var55.size()];

                            for (int var61 = 0; var61 < var55.size(); var61++) {
                                var59.u[var61] = (((double[])var55.get(var61))[0] - var84[0]) * 0.965;
                                var59.v[var61] = (((double[])var55.get(var61))[1] - var84[1]) * 0.965;
                            }

                            var59.size = Math.sqrt(var57);
                            if (!var2) {
                                double var85 = -var29;
                                double var63 = -var37;
                                double var65 = Math.sqrt(var85 * var85 + var63 * var63);
                                if (var65 > 0.2) {
                                    var85 /= var65;
                                    var63 /= var65;
                                    int var67 = 0;
                                    double var68 = -1.0E9;

                                    for (int var70 = 0; var70 < var59.u.length; var70++) {
                                        double var71 = var59.u[var70] * var85 + var59.v[var70] * var63;
                                        if (var71 > var68) {
                                            var68 = var71;
                                            var67 = var70;
                                        }
                                    }

                                    double var92 = var59.size * (0.35 + 0.55 * hash(var60, 12));
                                    var59.u[var67] = var59.u[var67] + var85 * var92;
                                    var59.v[var67] = var59.v[var67] + var63 * var92;
                                }
                            }

                            var59.h0 = hash(var60, 1);
                            var59.h1 = hash(var60, 2);
                            var59.h2 = hash(var60, 3);
                            var59.h3 = hash(var60, 4);
                            var59.h4 = hash(var60, 5);
                            var59.lift = (hash(var60, 6) - 0.5) * 0.5 * var59.size;
                            double var87 = hash(var60, 7) - 0.5;
                            double var89 = hash(var60, 8) - 0.5;
                            double var90 = hash(var60, 9) - 0.5;
                            double var91 = Math.sqrt(var87 * var87 + var89 * var89 + var90 * var90) + 1.0E-6;
                            var59.ax = var87 / var91;
                            var59.ay = var89 / var91;
                            var59.az = var90 / var91;
                            var3.pieces.add(var59);
                        }
                    }
                }
            }
        }

        return var3;
    }

    private static List<double[]> clip(List<double[]> var0, double var1, double var3, double var5) {
        ArrayList var7 = new ArrayList();
        int var8 = var0.size();

        for (int var9 = 0; var9 < var8; var9++) {
            double[] var10 = (double[])var0.get(var9);
            double[] var11 = (double[])var0.get((var9 + 1) % var8);
            double var12 = var10[0] * var1 + var10[1] * var3 - var5;
            double var14 = var11[0] * var1 + var11[1] * var3 - var5;
            if (var12 <= 0.0) {
                var7.add(var10);
            }

            if (var12 < 0.0 != var14 < 0.0 && Math.abs(var12 - var14) > 1.0E-12) {
                double var16 = var12 / (var12 - var14);
                var7.add(new double[]{var10[0] + (var11[0] - var10[0]) * var16, var10[1] + (var11[1] - var10[1]) * var16});
            }
        }

        return var7;
    }

    private static double area(List<double[]> var0) {
        double var1 = 0.0;

        for (int var3 = 0; var3 < var0.size(); var3++) {
            double[] var4 = (double[])var0.get(var3);
            double[] var5 = (double[])var0.get((var3 + 1) % var0.size());
            var1 += var4[0] * var5[1] - var5[0] * var4[1];
        }

        return Math.abs(var1) * 0.5;
    }

    private static double[] centroid(List<double[]> var0) {
        double var1 = 0.0;
        double var3 = 0.0;

        for (double[] var6 : var0) {
            var1 += var6[0];
            var3 += var6[1];
        }

        return new double[]{var1 / (double)var0.size(), var3 / (double)var0.size()};
    }

    private static Vec3 rot(double var0, double var2, double var4, double var6, double var8, double var10, double var12) {
        double var14 = Math.cos(var12);
        double var16 = Math.sin(var12);
        double var18 = (var6 * var0 + var8 * var2 + var10 * var4) * (1.0 - var14);
        return new Vec3(
            var0 * var14 + (var8 * var4 - var10 * var2) * var16 + var6 * var18,
            var2 * var14 + (var10 * var0 - var6 * var4) * var16 + var8 * var18,
            var4 * var14 + (var6 * var2 - var8 * var0) * var16 + var10 * var18
        );
    }

    static boolean pose(BreakerFx.Piece var0, int var1, Vec3 var2, double var3, Vec3 var5, double var6, Vec3 var8, boolean var9, BreakerFx.Pose var10) {
        double var11 = var0.nx * var5.x + var0.ny * var5.y + var0.nz * var5.z;
        double var13 = Math.acos(Math.max(-1.0, Math.min(1.0, var11)));
        double var15 = 4.95 + 0.3 * var13 / Math.PI;
        double var17 = var6 - var15;
        if (var17 < 0.0) {
            return false;
        } else {
            double var19 = !var9 && var0.ny < 0.45 ? 5.6 + 0.8 * var0.h1 + 4.0 * Math.max(0.0, var0.ny) : 11.5 + 3.5 * var0.h1;
            float var21 = (float)(sstep(0.0, 0.08, var17) * (1.0 - sstep(var19 - 1.6, var19, var6)));
            if (var21 <= 0.004F) {
                return false;
            } else {
                double var22 = Math.sqrt(Math.max(0.25, var3 / 24.0));
                double var24 = var9 ? 0.0 : 1.0 + 1.4 * var0.h4;
                double var26 = Math.max(0.0, var17 - var24);
                double var28 = (var9 ? 0.6 + 1.2 * var0.h2 : 0.25 + 0.5 * var0.h2) * var22 * (1.0 - Math.exp(-var17 / 0.7));
                double var30 = (var9 ? 2.2 + 2.6 * var0.h3 : 0.9 + 1.5 * var0.h3) * var22;
                double var32 = var30 * (var26 - 1.1 * (1.0 - Math.exp(-var26 / 1.1)));
                double var34 = Math.sin(var26 * (0.7 + 0.6 * var0.h4) + (double)var1) * 0.6 * var22 * (1.0 - Math.exp(-var26));
                double var36 = var9 ? 0.0 : 0.35 * var22 * var26;
                double var38 = var2.x + var0.nx * var3 + var0.t1x * var0.cu + var0.t2x * var0.cv;
                double var40 = var2.y + var0.ny * var3 + var0.t1y * var0.cu + var0.t2y * var0.cv;
                double var42 = var2.z + var0.nz * var3 + var0.t1z * var0.cu + var0.t2z * var0.cv;
                double var44 = var38 + var0.nx * var28 + var0.t1x * var34 + var36;
                double var46 = var40 + var0.ny * var28 + var0.t1y * var34 - var32;
                double var48 = var42 + var0.nz * var28 + var0.t1z * var34 + var36 * 0.3;
                double var50 = var9 ? var2.y - Math.min(var3, 1.5) : var2.y;
                var21 *= (float)sstep(var50 - 0.4, var50 + 1.2, var46);
                if (var8 != null) {
                    double var52 = var44 - var8.x;
                    double var54 = var46 - var8.y;
                    double var56 = var48 - var8.z;
                    var21 *= (float)sstep(1.5, 5.0, Math.sqrt(var52 * var52 + var54 * var54 + var56 * var56));
                }

                if (var21 <= 0.004F) {
                    return false;
                } else {
                    var10.white = var9 ? 0.0 : 1.0 - sstep(0.15, 0.9, var17);
                    double var68 = (0.12 * Math.min(var17, var24 + 0.5) + (var9 ? 1.0 : 0.6) * var26) * (0.5 + 1.8 * var0.h0) / (0.6 + var0.size * 0.25);
                    int var69 = Math.min(15, var0.u.length);
                    Vec3 var55 = rot(var0.nx * var0.lift, var0.ny * var0.lift, var0.nz * var0.lift, var0.ax, var0.ay, var0.az, var68);
                    var10.ctr = new Vec3(var44 + var55.x, var46 + var55.y, var48 + var55.z);

                    for (int var70 = 0; var70 < var69; var70++) {
                        double var57 = var0.t1x * var0.u[var70] + var0.t2x * var0.v[var70];
                        double var59 = var0.t1y * var0.u[var70] + var0.t2y * var0.v[var70];
                        double var61 = var0.t1z * var0.u[var70] + var0.t2z * var0.v[var70];
                        Vec3 var63 = rot(var57, var59, var61, var0.ax, var0.ay, var0.az, var68);
                        var10.vert[var70] = new Vec3(var44 + var63.x, var46 + var63.y, var48 + var63.z);
                    }

                    var10.n = var69;

                    for (int var71 = 0; var71 < var69; var71++) {
                        Vec3 var72 = var10.vert[var71].subtract(var10.ctr);
                        Vec3 var58 = var10.vert[(var71 + 1) % var69].subtract(var10.ctr);
                        Vec3 var73 = var72.cross(var58);
                        double var60 = var73.length();
                        if (var8 != null && !(var60 < 1.0E-9)) {
                            Vec3 var62 = var8.subtract(var10.ctr);
                            double var74 = var62.length();
                            double var65 = var74 < 1.0E-6 ? 1.0 : Math.abs(var73.dot(var62)) / (var60 * var74);
                            var10.facet[var71] = Math.pow(var65, 4.0);
                        } else {
                            var10.facet[var71] = 0.5;
                        }
                    }

                    var10.alpha = var21;
                    return true;
                }
            }
        }
    }

    public static void prisms(VertexConsumer var0, Matrix4f var1, ArcanaClient.Fracture var2, double var3) {
        Vec3 var5 = ArcanaClient.camera();
        if (var5 != null) {
            var3 = remap(var2, var3);
            boolean var6 = var2.field().defensive();
            Vec3 var7 = var2.field().center();
            double var8 = (double)var2.field().radius();
            Vec3 var10 = var2.impact().subtract(var7).normalize();
            BreakerFx.ShardSet var11 = set(var2);
            BreakerFx.Pose var12 = new BreakerFx.Pose();

            for (int var13 = 0; var13 < var11.pieces.size(); var13++) {
                BreakerFx.Piece var14 = var11.pieces.get(var13);
                if (pose(var14, var13, var7, var8, var10, var3, var5, var6, var12)) {
                    float var15 = (float)var14.h0;
                    float var16 = (float)var14.h2;

                    for (int var17 = 0; var17 < var12.n; var17++) {
                        int var18 = (var17 + 1) % var12.n;
                        float var19 = var12.alpha * 0.62F;
                        Vec3 var20 = var12.ctr;
                        Vec3 var21 = var12.vert[var17];
                        Vec3 var22 = var12.vert[var18];
                        float var23 = (float)var12.facet[var17];
                        v(var0, var1, var5, var20.x, var20.y, var20.z, var15, var16, var23, var19);
                        v(var0, var1, var5, var21.x, var21.y, var21.z, var15, var16, var23, var19);
                        v(var0, var1, var5, var22.x, var22.y, var22.z, var15, var16, var23, var19);
                        v(var0, var1, var5, var22.x, var22.y, var22.z, var15, var16, var23, var19);
                    }
                }
            }
        }
    }

    static final class Piece {
        double nx;
        double ny;
        double nz;
        double t1x;
        double t1y;
        double t1z;
        double t2x;
        double t2y;
        double t2z;
        double cu;
        double cv;
        double[] u;
        double[] v;
        double size;
        double lift;
        double h0;
        double h1;
        double h2;
        double h3;
        double h4;
        double ax;
        double ay;
        double az;
    }

    static final class Pose {
        Vec3 ctr;
        Vec3[] vert = new Vec3[16];
        double[] facet = new double[16];
        int n;
        float alpha;
        double white;
    }

    static final class ShardSet {
        final List<BreakerFx.Piece> pieces = new ArrayList<>();
    }
}
