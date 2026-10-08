package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class SpellFx {
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);

    private SpellFx() {
    }

    public static double beamLife(int var0) {
        switch (var0) {
            case 0:
                return 0.7;
            case 1:
                return 0.45;
            case 2:
                return 1.8;
            case 3:
                return 3.4;
            case 4:
                return 1.2;
            case 5:
                return 0.9;
            case 6:
            case 7:
            case 8:
            case 9:
            case 11:
            case 12:
            case 13:
            case 14:
            default:
                return 0.5;
            case 10:
                return 0.55;
            case 15:
                return 0.75;
            case 16:
                return 2.1;
            case 17:
                return 1.3;
            case 18:
                return 1.3;
            case 19:
                return 1.0;
        }
    }

    public static double effectLife(int var0) {
        switch (var0) {
            case 5:
                return 2.2;
            case 6:
                return 2.6;
            case 7:
                return 1.0;
            case 8:
                return 6.0;
            case 9:
                return 10.0;
            case 10:
            case 16:
            case 18:
            case 19:
            default:
                return 1.3;
            case 11:
                return 15.0;
            case 12:
                return 2.6;
            case 13:
                return 3.0;
            case 14:
            case 21:
                return 4.0;
            case 15:
                return 1.6;
            case 17:
                return 1.0;
            case 20:
                return 1.3;
            case 22:
                return 0.7;
            case 23:
                return 0.9;
        }
    }

    static double hash(int var0, int var1) {
        double var2 = Math.sin((double)var0 * 12.9898 + (double)var1 * 78.233) * 43758.5453;
        return var2 - Math.floor(var2);
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    private static double sstep(double var0, double var2, double var4) {
        double var6 = clamp((var4 - var0) / (var2 - var0));
        return var6 * var6 * (3.0 - 2.0 * var6);
    }

    private static float env(double var0, double var2, double var4, double var6) {
        return (float)(clamp(var0 / var4) * clamp((var2 - var0) / var6));
    }

    private static Vec3 perp(Vec3 var0) {
        Vec3 var1 = Math.abs(var0.y) < 0.95 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
        return var0.cross(var1).normalize();
    }

    private static void line(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, float var7, float var8, float var9, float var10) {
        BreakerFx.ribbon(var0, var1, var2, var3, var4, var5, var7, var8, var9, var10);
    }

    private static void ring(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, double var7, float var9, float var10, float var11, float var12
    ) {
        if (!(var12 <= 0.003F) && !(var5 <= 0.0)) {
            Vec3 var13 = perp(var4);
            Vec3 var14 = var4.cross(var13).normalize();
            Vec3 var15 = null;
            int var16 = Math.max(24, (int)(var5 * 14.0));

            for (int var17 = 0; var17 <= var16; var17++) {
                double var18 = (Math.PI * 2) * (double)var17 / (double)var16;
                Vec3 var20 = var3.add(var13.scale(Math.cos(var18) * var5)).add(var14.scale(Math.sin(var18) * var5));
                if (var15 != null) {
                    flatLine(var0, var1, var2, var15, var20, var4, var7, var9, var10, var11, var12);
                }

                var15 = var20;
            }
        }
    }

    private static void flatLine(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, float var8, float var9, float var10, float var11
    ) {
        Vec3 var12 = var4.subtract(var3);
        Vec3 var13 = var12.cross(var5);
        if (!(var13.lengthSqr() < 1.0E-12)) {
            var13 = var13.normalize().scale(var6 * 0.5);
            Vec3[] var14 = new Vec3[]{var3.subtract(var13), var3.add(var13), var4.add(var13), var4.subtract(var13)};

            for (Vec3 var18 : var14) {
                var0.addVertex(var1, (float)(var18.x - var2.x), (float)(var18.y - var2.y), (float)(var18.z - var2.z)).setColor(var8, var9, var10, var11);
            }
        }
    }

    private static void bolt(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, int var7, double var8, float var10, int var11, double var12
    ) {
        Vec3 var14 = var4.subtract(var3);
        double var15 = var14.length();
        if (!(var15 < 0.08) && !(var10 <= 0.01F)) {
            Vec3 var17 = var14.scale(1.0 / var15);
            Vec3 var18 = perp(var17);
            Vec3 var19 = var17.cross(var18);
            int var20 = (int)Math.floor(var5 * 24.0);
            int var21 = Math.max(5, (int)(var15 * 2.2));
            Vec3 var22 = var3;

            for (int var23 = 1; var23 <= var21; var23++) {
                double var24 = (double)var23 / (double)var21;
                double var26 = var23 == var21 ? 0.0 : var8 * (0.35 + 0.65 * Math.sin(Math.PI * var24));
                Vec3 var28 = var3.add(var14.scale(var24))
                    .add(var18.scale((hash(var7 * 97 + var23, var20) - 0.5) * 2.0 * var26))
                    .add(var19.scale((hash(var7 * 31 + var23, var20 + 7) - 0.5) * 2.0 * var26));
                line(var0, var1, var2, var22, var28, var12 * 7.0, 0.62F, 0.32F, 1.0F, 0.13F * var10);
                line(var0, var1, var2, var22, var28, var12 * 2.6, 0.78F, 0.52F, 1.0F, 0.55F * var10);
                line(var0, var1, var2, var22, var28, var12, 1.0F, 1.0F, 1.0F, 0.95F * var10);
                if (var11 > 0 && var23 < var21 - 1 && hash(var7 * 13 + var23, var20 + 5) < 0.42) {
                    double var29 = var15 * (0.18 + 0.3 * hash(var7 + var23 * 3, var20 + 9)) * (1.0 - var24 * 0.5);
                    Vec3 var31 = var17.add(var18.scale((hash(var7 + var23, var20 + 2) - 0.5) * 2.2))
                        .add(var19.scale((hash(var7 + var23, var20 + 4) - 0.5) * 2.2))
                        .normalize();
                    bolt(var0, var1, var2, var28, var28.add(var31.scale(var29)), var5, var7 * 7 + var23, var8 * 0.5, var10 * 0.8F, var11 - 1, var12 * 0.65);
                }

                if (var11 > 0 && var23 % 3 == 0) {
                    PixelFx.sprite(
                        var28,
                        0.5 + 0.4 * hash(var23, var20),
                        42 + (int)(hash(var7 + var23, var20) * 4.0),
                        0.82F,
                        0.62F,
                        1.0F,
                        0.8F * var10,
                        hash(var23, var20 + 1) * 6.28
                    );
                }

                var22 = var28;
            }
        }
    }

    private static void castCircle(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, double var7, float var9, float var10, float var11, float var12
    ) {
        SpellCircleFx.Ctx var13 = new SpellCircleFx.Ctx();
        var13.vc = var0;
        var13.m = var1;
        var13.camX = var2.x;
        var13.camY = var2.y;
        var13.camZ = var2.z;
        Vec3 var14 = perp(var4);
        Vec3 var15 = var4.cross(var14).normalize();
        var13.cx = var3.x;
        var13.cy = var3.y;
        var13.cz = var3.z;
        var13.ux = var14.x;
        var13.uy = var14.y;
        var13.uz = var14.z;
        var13.vx = var15.x;
        var13.vy = var15.y;
        var13.vz = var15.z;
        var13.r = var9;
        var13.g = var10;
        var13.b = var11;
        var13.glow = 3.0;
        var13.a = var12;
        var13.ring(var5, 0.035 * var5 + 0.01);
        var13.ring(var5 * 0.82, 0.02 * var5 + 0.006);
        var13.ticks(var5 * 0.82, var5 * 0.95, 24, var7 * 2.0, 0.012 * var5 + 0.004);
        var13.star(var5 * 0.62, 6, 2, -var7 * 3.0, 0.02 * var5 + 0.006);
        var13.ring(var5 * 0.25, 0.03 * var5 + 0.006);
        var13.glow = 0.0;

        for (int var16 = 0; var16 < 6; var16++) {
            double var17 = -var7 * 3.0 + (double)var16 * 1.0472;
            PixelFx.sprite(
                var3.add(var14.scale(Math.cos(var17) * var5 * 0.62)).add(var15.scale(Math.sin(var17) * var5 * 0.62)),
                var5 * 0.22,
                58,
                var9,
                var10,
                var11,
                var12
            );
        }
    }

    private static Vec3 handStart(Vec3 var0, Vec3 var1) {
        Minecraft var2 = Minecraft.getInstance();
        if (var2.level == null) {
            return var0;
        } else {
            for (Player var4 : var2.level.players()) {
                if (var4.getEyePosition().distanceToSqr(var0) < 0.5) {
                    return var0.add(var1.scale(0.6)).add(perp(var1).scale(0.34)).add(0.0, -0.36, 0.0);
                }
            }

            return var0;
        }
    }

    private static void burst(Vec3 var0, double var1, double var3, double var5, float var7, float var8, float var9, float var10) {
        double var11 = clamp(var3 / var5);
        if (!(var11 >= 1.0)) {
            PixelFx.sprite(var0, var1 * (0.6 + 0.8 * var11), 48 + Math.min(3, (int)(var11 * 4.0)), var7, var8, var9, var10 * (float)(1.0 - var11 * 0.6));
            PixelFx.sprite(var0, var1 * (0.7 + 1.3 * var11), 52 + Math.min(3, (int)(var11 * 4.0)), var7, var8, var9, var10 * (float)(1.0 - var11));
        }
    }

    public static boolean beam(VertexConsumer var0, Matrix4f var1, int var2, Vec3 var3, Vec3 var4, double var5) {
        if (var2 == 3) {
            return false;
        } else {
            Vec3 var7 = ArcanaClient.camera();
            if (var7 == null) {
                return true;
            } else {
                Vec3 var8 = var4.subtract(var3);
                double var9 = var8.length();
                if (var9 < 0.05) {
                    return true;
                } else {
                    Vec3 var11 = var8.scale(1.0 / var9);
                    double var12 = beamLife(var2);
                    switch (var2) {
                        case 0:
                        case 2:
                            zoltraak(var0, var1, var7, handStart(var3, var11), var4, var11, var5, var12, var2 == 2);
                            return true;
                        case 1:
                            fern(var0, var1, var7, var3, var4, var11, var5, var12);
                            return true;
                        case 3:
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        default:
                            return false;
                        case 4:
                            iceSpears(var0, var1, var7, handStart(var3, var11), var4, var11, var5, var12);
                            return true;
                        case 5:
                            waterStream(var0, var1, var7, handStart(var3, var11), var4, var5, var12);
                            return true;
                        case 10:
                            cut(var0, var1, var7, handStart(var3, var11), var4, var11, var5, var12);
                            return true;
                        case 15:
                            Vec3 var14 = handStart(var3, var11);
                            float var15 = env(var5, var12, 0.03, 0.3);

                            for (int var16 = 0; var16 < 3; var16++) {
                                double var17 = hash(var16, (int)(var5 * 12.0)) > 0.25 ? 1.0 : 0.25;
                                bolt(
                                    var0,
                                    var1,
                                    var7,
                                    var14,
                                    var4,
                                    var5 + (double)var16 * 0.37,
                                    (int)(var4.x * 7.0 + var4.z * 13.0) + var16 * 101,
                                    Math.min(1.6, var9 * 0.13),
                                    var15 * (float)var17 * (var16 == 0 ? 1.0F : 0.6F),
                                    2,
                                    var16 == 0 ? 0.05 : 0.03
                                );
                            }

                            PixelFx.sprite(var14, 1.2, 48 + (int)(var5 * 20.0) % 2, 0.85F, 0.65F, 1.0F, var15);
                            burst(var4, 2.6, var5 % 0.3, 0.3, 0.8F, 0.6F, 1.0F, var15);
                            return true;
                        case 16:
                            catastravia(var0, var1, var7, var3, var4, var11, var9, var5, var12);
                            return true;
                        case 17:
                            petals(var0, var1, var7, handStart(var3, var11), var4, var11, var9, var5, var12);
                            return true;
                        case 18:
                            rock(var0, var1, var7, handStart(var3, var11), var4, var11, var9, var5, var12);
                            return true;
                        case 19:
                            goddessSpear(var0, var1, var7, handStart(var3, var11), var4, var11, var9, var5, var12);
                            return true;
                    }
                }
            }
        }
    }

    private static Vec3 casterLook(Vec3 var0) {
        Minecraft var1 = Minecraft.getInstance();
        if (var1.level == null) {
            return null;
        } else {
            for (Player var3 : var1.level.players()) {
                if (var3.getEyePosition().distanceToSqr(var0) < 0.5) {
                    return var3.getViewVector(1.0F);
                }
            }

            return null;
        }
    }

    private static void zoltraak(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8, boolean var10) {
        double var11 = sstep(0.0, var10 ? 0.14 : 0.05, var6);
        float var13 = env(var6, var8, 0.02, var10 ? 0.8 : 0.35);
        double var14 = 1.0 - 0.55 * clamp((var6 - var8 * 0.35) / (var8 * 0.65));
        double var16 = var10 ? 4.4 : 1.9;
        Vec3 var18 = casterLook(var3.subtract(var5.scale(0.6)).add(perp(var5).scale(-0.34)).add(0.0, 0.36, 0.0));
        double var19 = var3.distanceTo(var4);
        Vec3 var21 = null;
        if (var18 != null && var18.dot(var5) < 0.9995 && var18.dot(var5) > 0.9) {
            var21 = var3.add(var18.scale(var19 * 0.45));
        }

        int var22 = var21 == null ? 1 : 18;
        Vec3[] var23 = new Vec3[var22 + 1];

        for (int var24 = 0; var24 <= var22; var24++) {
            double var25 = (double)var24 / (double)var22 * var11;
            if (var21 == null) {
                var23[var24] = var3.lerp(var4, var25);
            } else {
                double var27 = 1.0 - var25;
                var23[var24] = var3.scale(var27 * var27).add(var21.scale(2.0 * var27 * var25)).add(var4.scale(var25 * var25));
            }
        }

        for (int var38 = 0; var38 < var22; var38++) {
            line(var0, var1, var2, var23[var38], var23[var38 + 1], 0.34 * var16 * var14, 0.7F, 0.77F, 1.0F, 0.28F * var13);
            line(var0, var1, var2, var23[var38], var23[var38 + 1], 0.14 * var16 * var14, 0.95F, 0.97F, 1.0F, 0.95F * var13);
            line(var0, var1, var2, var23[var38], var23[var38 + 1], 0.055 * var16 * var14, 1.0F, 1.0F, 1.0F, var13);
        }

        Vec3 var39 = var23[var22];
        double var40 = var3.distanceTo(var39);
        int var41 = (int)(var40 * (var10 ? 2.5 : 1.2));
        Vec3 var28 = perp(var5);
        Vec3 var29 = var5.cross(var28).normalize();

        for (int var30 = 0; var30 < var41; var30++) {
            double var31 = hash(var30, 1);
            double var33 = hash(var30, 2) * 6.28 + var6 * 6.0;
            double var35 = (0.15 + var6 * (var10 ? 1.6 : 0.8)) * var16 * (0.4 + hash(var30, 3));
            Vec3 var37 = var23[Math.min(var22, (int)(var31 * (double)var22))]
                .add(var28.scale(Math.cos(var33) * var35))
                .add(var29.scale(Math.sin(var33) * var35));
            PixelFx.sprite(
                var37, (var10 ? 0.45 : 0.24) * (0.6 + hash(var30, 4)), var30 % 3 == 0 ? 1 : 58, 0.86F, 0.9F, 1.0F, var13 * (float)clamp(1.3 - var6 / var8)
            );
        }

        if (var10) {
            for (int var42 = 0; var42 < 5; var42++) {
                double var44 = ((double)var42 + 0.5) / 5.0 + var6 * 0.5;
                var44 -= Math.floor(var44);
                if (var44 <= var11) {
                    ring(
                        var0,
                        var1,
                        var2,
                        var3.lerp(var4, var44),
                        var5,
                        0.9 + 0.4 * Math.sin(var6 * 9.0 + (double)var42),
                        0.05,
                        0.85F,
                        0.9F,
                        1.0F,
                        0.45F * var13
                    );
                }
            }
        }

        castCircle(
            var0,
            var1,
            var2,
            var3.add((var18 != null ? var18 : var5).scale(0.25)),
            var18 != null ? var18 : var5,
            var10 ? 1.9 : 0.6,
            var6,
            0.92F,
            0.95F,
            1.0F,
            var13
        );
        PixelFx.sprite(var3.add(var5.scale(0.25)), var10 ? 2.2 : 0.7, 4, 0.8F, 0.85F, 1.0F, 0.6F * var13);
        if (var11 > 0.99) {
            burst(var4, var10 ? 5.0 : 1.8, var6 - (var10 ? 0.14 : 0.05), var10 ? 0.6 : 0.35, 0.92F, 0.95F, 1.0F, var13);
            PixelFx.sprite(var4, var10 ? 3.0 : 1.0, 4, 0.8F, 0.85F, 1.0F, 0.7F * var13);

            for (int var43 = 0; var43 < (var10 ? 18 : 7); var43++) {
                Vec3 var46 = new Vec3(hash(var43, 6) - 0.5, hash(var43, 7) - 0.3, hash(var43, 8) - 0.5).normalize();
                double var32 = var6 - 0.05;
                PixelFx.sprite(
                    var4.add(var46.scale(var32 * (var10 ? 7.0 : 3.5))).add(0.0, -var32 * var32 * 3.0, 0.0),
                    var10 ? 0.4 : 0.22,
                    13,
                    0.9F,
                    0.93F,
                    1.0F,
                    var13 * (float)clamp(1.0 - var32 * 2.0)
                );
            }
        }
    }

    private static void fern(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8) {
        float var10 = env(var6, var8, 0.02, 0.3);
        line(var0, var1, var2, var3, var4, 0.16, 0.74F, 0.8F, 1.0F, 0.25F * var10);
        line(var0, var1, var2, var3, var4, 0.055, 1.0F, 1.0F, 1.0F, 0.95F * var10);
        PixelFx.sprite(var4, 0.55, 1 + (int)(var6 * 20.0) % 2, 0.9F, 0.93F, 1.0F, var10);
        double var11 = var3.distanceTo(var4);

        for (int var13 = 0; var13 < (int)(var11 * 0.8); var13++) {
            PixelFx.sprite(
                var3.lerp(var4, hash(var13, 3)).add((hash(var13, 4) - 0.5) * 0.3, (hash(var13, 5) - 0.5) * 0.3 + var6 * 0.4, (hash(var13, 6) - 0.5) * 0.3),
                0.14,
                58,
                0.85F,
                0.9F,
                1.0F,
                0.8F * var10
            );
        }
    }

    private static void iceSpears(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8) {
        double var10 = sstep(0.0, 0.22, var6);
        float var12 = env(var6, var8, 0.03, 0.4);
        Vec3 var13 = var3.lerp(var4, var10);
        PixelFx.streak(var13.subtract(var5.scale(2.6)), var13, 0.9, 15, 0.78F, 0.92F, 1.0F, var12);
        PixelFx.streak(var13.subtract(var5.scale(2.2)), var13.subtract(var5.scale(0.1)), 0.45, 15, 0.95F, 1.0F, 1.0F, var12);

        for (int var14 = 0; var14 < 14; var14++) {
            double var15 = var10 * hash(var14, 1);
            double var17 = Math.max(0.0, var6 - var15 * 0.22);
            Vec3 var19 = var3.lerp(var4, var15).add((hash(var14, 2) - 0.5) * 0.5, (hash(var14, 3) - 0.5) * 0.5 - var17 * 0.6, (hash(var14, 4) - 0.5) * 0.5);
            PixelFx.sprite(
                var19,
                0.22 + 0.12 * hash(var14, 5),
                var14 % 2 == 0 ? 14 : 1,
                0.82F,
                0.95F,
                1.0F,
                var12 * (float)clamp(1.0 - var17 * 1.2),
                var17 * 3.0 + (double)var14
            );
        }

        if (var10 > 0.99) {
            double var20 = var6 - 0.22;
            burst(var4, 1.8, var20, 0.4, 0.8F, 0.95F, 1.0F, var12);

            for (int var16 = 0; var16 < 8; var16++) {
                Vec3 var21 = new Vec3(hash(var16, 6) - 0.5, hash(var16, 7), hash(var16, 8) - 0.5).normalize();
                Vec3 var18 = var4.add(var21.scale(var20 * 3.0)).add(0.0, -var20 * var20 * 5.0, 0.0);
                PixelFx.streak(var18.subtract(var21.scale(0.25)), var18.add(var21.scale(0.25)), 0.25, 15, 0.85F, 0.97F, 1.0F, var12 * (float)clamp(1.0 - var20));
            }
        }
    }

    private static void waterStream(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, double var7) {
        float var9 = env(var5, var7, 0.05, 0.4);
        Vec3 var10 = var4.subtract(var3);
        Vec3 var11 = perp(var10.normalize());
        Vec3 var12 = var10.normalize().cross(var11);

        for (int var13 = 0; var13 < 2; var13++) {
            Vec3 var14 = var3;

            for (int var15 = 1; var15 <= 24; var15++) {
                double var16 = (double)var15 / 24.0;
                double var18 = var16 * 9.0 + var5 * 12.0 + (double)var13 * Math.PI;
                Vec3 var20 = var3.add(var10.scale(var16))
                    .add(var11.scale(Math.cos(var18) * 0.2 * Math.sin(Math.PI * var16)))
                    .add(var12.scale(Math.sin(var18) * 0.2 * Math.sin(Math.PI * var16)));
                line(var0, var1, var2, var14, var20, 0.07, 0.75F, 0.92F, 1.0F, 0.75F * var9);
                if (var15 % 2 == 0) {
                    PixelFx.sprite(var20, 0.35, var13 == 0 ? 11 : 12, 0.45F, 0.72F, 1.0F, 0.85F * var9, var18);
                }

                var14 = var20;
            }
        }
    }

    private static void cut(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8) {
        double var10 = sstep(0.0, 0.1, var6);
        float var12 = env(var6, var8, 0.01, 0.35);
        Vec3 var13 = perp(var5);
        Vec3 var14 = var5.cross(var13).normalize();
        Vec3 var15 = var13.scale(0.55).add(var14.scale(0.35));
        Vec3 var16 = null;
        byte var17 = 20;

        for (int var18 = 0; var18 <= var17; var18++) {
            double var19 = (double)var18 / (double)var17;
            if (var19 > var10) {
                break;
            }

            double var21 = Math.sin(Math.PI * var19);
            Vec3 var23 = var3.lerp(var4, 0.15 + 0.85 * var19).add(var15.scale((var19 - 0.5) * 3.2)).add(var14.scale(var21 * 0.6));
            if (var16 != null) {
                line(var0, var1, var2, var16, var23, 0.02 + 0.05 * var21, 0.96F, 0.97F, 1.0F, 0.95F * var12);
            }

            if (var18 % 4 == 2) {
                PixelFx.sprite(var23, 0.25, 1, 0.85F, 0.82F, 1.0F, var12);
            }

            var16 = var23;
        }

        Vec3 var24 = var3.lerp(var4, 0.6);
        PixelFx.streak(
            var24.subtract(var15.scale(1.6)), var24.add(var15.scale(1.6)), 1.4, 41, 0.9F, 0.88F, 1.0F, 0.75F * var12 * (float)clamp(1.0 - var6 * 3.0)
        );
    }

    private static void catastravia(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8, double var10) {
        float var12 = env(var8, var10, 0.05, 0.45);
        Vec3 var13 = var3.add(0.0, 11.0, 0.0).add(var5.scale(Math.min(10.0, var6 * 0.25)));
        ring(var0, var1, var2, var13, UP, 3.4, 0.08, 1.0F, 0.95F, 0.75F, 0.75F * var12);
        ring(var0, var1, var2, var13, UP, 2.7, 0.04, 1.0F, 0.95F, 0.75F, 0.55F * var12);

        for (int var14 = 0; var14 < 8; var14++) {
            PixelFx.flat(
                var13.add(Math.cos(var8 * 0.6 + (double)var14 * 0.785) * 3.05, 0.0, Math.sin(var8 * 0.6 + (double)var14 * 0.785) * 3.05),
                UP,
                0.55,
                34 + var14 % 4,
                var8,
                1.0F,
                0.95F,
                0.75F,
                0.9F * var12
            );
        }

        PixelFx.sprite(var13, 5.0, 4, 1.0F, 0.92F, 0.7F, 0.35F * var12);
        byte var27 = 26;

        for (int var15 = 0; var15 < var27; var15++) {
            double var16 = 0.1 + 0.9 * ((double)var15 + hash(var15, 1) * 0.6) / (double)var27;
            Vec3 var18 = var3.lerp(var4, var16).add((hash(var15, 2) - 0.5) * 2.0, -0.2, (hash(var15, 3) - 0.5) * 2.0);
            Vec3 var19 = var13.add((hash(var15, 4) - 0.5) * 4.5, 0.0, (hash(var15, 5) - 0.5) * 4.5);
            double var20 = 0.1 + (double)var15 * 0.04;
            double var22 = (var8 - var20) / 0.3;
            if (!(var22 < 0.0)) {
                if (var22 < 1.0) {
                    Vec3 var28 = var19.lerp(var18, var22);
                    Vec3 var25 = var18.subtract(var19).normalize();
                    PixelFx.streak(var28.subtract(var25.scale(2.6)), var28, 0.7, 38, 1.0F, 0.95F, 0.75F, 1.0F);
                    PixelFx.sprite(var28, 0.5, 4, 1.0F, 0.92F, 0.65F, 0.6F);
                } else {
                    double var24 = (var22 - 1.0) * 0.3;
                    burst(var18, 2.4, var24, 0.5, 1.0F, 0.94F, 0.7F, 1.0F);
                    PixelFx.flat(
                        var18.add(0.0, 0.25, 0.0),
                        UP,
                        1.0 + var24 * 7.0,
                        52 + Math.min(3, (int)(var24 * 8.0)),
                        0.0,
                        1.0F,
                        0.9F,
                        0.6F,
                        (float)clamp(1.0 - var24 * 2.0)
                    );

                    for (int var26 = 0; var26 < 3; var26++) {
                        PixelFx.sprite(
                            var18.add(
                                (hash(var15 * 3 + var26, 6) - 0.5) * var24 * 6.0,
                                var24 * 3.0 - var24 * var24 * 6.0 + 0.3,
                                (hash(var15 * 3 + var26, 7) - 0.5) * var24 * 6.0
                            ),
                            0.25,
                            13,
                            1.0F,
                            0.9F,
                            0.6F,
                            (float)clamp(1.0 - var24 * 2.0)
                        );
                    }
                }
            }
        }
    }

    private static void petals(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8, double var10) {
        float var12 = env(var8, var10, 0.03, 0.3);
        Vec3 var13 = perp(var5);
        Vec3 var14 = var5.cross(var13).normalize();

        for (int var15 = 0; var15 < 54; var15++) {
            double var16 = (var8 - hash(var15, 1) * 0.3) / 0.5;
            if (!(var16 < 0.0)) {
                double var18 = Math.min(1.0, var16);
                double var20 = hash(var15, 2) * 6.28 + var18 * 9.0;
                double var22 = 0.2 + 0.6 * hash(var15, 3) * (1.0 - var18 * 0.6);
                Vec3 var24 = var3.lerp(var4, var18).add(var13.scale(Math.cos(var20) * var22)).add(var14.scale(Math.sin(var20) * var22));
                float var25 = var12 * (var16 > 1.0 ? (float)clamp(1.0 - (var16 - 1.0) * 2.0) : 1.0F);
                if (var16 < 0.22) {
                    PixelFx.sprite(var24, 0.32, 28, 1.0F, 0.68F, 0.8F, var25, var20 * 2.0);
                } else {
                    Vec3 var26 = var5.add(var13.scale(Math.cos(var20 * 2.0) * 0.35)).normalize();
                    PixelFx.streak(var24.subtract(var26.scale(0.3)), var24.add(var26.scale(0.3)), 0.24, 29, 1.0F, 1.0F, 1.0F, var25);
                    if (hash(var15, (int)(var8 * 12.0)) > 0.9) {
                        PixelFx.sprite(var24, 0.35, 1, 1.0F, 1.0F, 1.0F, var25);
                    }
                }
            }
        }
    }

    private static void stoneBox(Vec3 var0, double var1, double var3, double var5, float var7, float var8) {
        double var9 = Math.cos(var3);
        double var11 = Math.sin(var3);
        double var13 = Math.cos(var5);
        double var15 = Math.sin(var5);
        Vec3[] var17 = new Vec3[]{new Vec3(var9, 0.0, var11), new Vec3(-var11 * var15, var13, var9 * var15), new Vec3(-var11 * var13, -var15, var9 * var13)};

        for (int var18 = 0; var18 < 6; var18++) {
            int var19 = var18 / 2;
            double var20 = var18 % 2 == 0 ? 1.0 : -1.0;
            Vec3 var22 = var17[var19].scale(var20);
            Vec3 var23 = var17[(var19 + 1) % 3];
            Vec3 var24 = var17[(var19 + 2) % 3];
            Vec3 var25 = var0.add(var22.scale(var1));
            float var26 = var7 * (float)(0.6 + 0.4 * Math.max(0.0, var22.y) + 0.1 * Math.abs(var22.x));
            PixelFx.quad(
                var25.add(var23.scale(var1)).add(var24.scale(var1)),
                var25.add(var23.scale(-var1)).add(var24.scale(var1)),
                var25.add(var23.scale(-var1)).add(var24.scale(-var1)),
                var25.add(var23.scale(var1)).add(var24.scale(-var1)),
                32,
                var26,
                var26,
                var26,
                var8
            );
        }
    }

    private static void rock(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8, double var10) {
        int var12 = (int)(var4.x * 3.0 + var4.z * 5.0 + var4.y * 7.0);
        Vec3 var13 = var3.add(perp(var5).scale((hash(var12, 1) - 0.5) * 1.8)).add(0.0, 0.7 + 0.5 * hash(var12, 2), 0.0);
        float var14 = env(var8, var10, 0.02, 0.35);
        double var15 = 0.24 + 0.12 * hash(var12, 3);
        if (var8 < 0.25) {
            Vec3 var17 = var13.add(0.0, -0.6 * (1.0 - var8 / 0.25), 0.0);
            stoneBox(var17, var15, var8 * 4.0, var8 * 3.0, 1.0F, var14);

            for (int var18 = 0; var18 < 4; var18++) {
                PixelFx.sprite(var17.add((hash(var18, 9) - 0.5) * 0.6, -0.4 - var8, (hash(var18, 10) - 0.5) * 0.6), 0.35, 46, 0.75F, 0.68F, 0.58F, 0.7F * var14);
            }
        } else if (var8 < 0.55) {
            double var22 = (var8 - 0.25) / 0.3;
            Vec3 var19 = var13.lerp(var4, var22).add(0.0, Math.sin(Math.PI * var22) * 0.6, 0.0);
            stoneBox(var19, var15, var8 * 14.0, var8 * 9.0, 1.0F, var14);

            for (int var20 = 0; var20 < 6; var20++) {
                PixelFx.sprite(
                    var13.lerp(var4, Math.max(0.0, var22 - 0.06 * (double)var20)),
                    0.3 + 0.08 * (double)var20,
                    46,
                    0.72F,
                    0.66F,
                    0.56F,
                    0.5F * var14 * (1.0F - (float)var20 / 6.0F)
                );
            }
        } else {
            double var23 = var8 - 0.55;

            for (int var24 = 0; var24 < 9; var24++) {
                Vec3 var26 = new Vec3(hash(var12 + var24, 4) - 0.5, 0.6 + hash(var12 + var24, 5), hash(var12 + var24, 6) - 0.5).normalize();
                Vec3 var21 = var4.add(var26.scale(var23 * 4.0)).add(0.0, -4.9 * var23 * var23, 0.0);
                PixelFx.sprite(var21, 0.35 + 0.2 * hash(var24, 7), 33, 1.0F, 1.0F, 1.0F, var14, var23 * 9.0 + (double)var24);
            }

            for (int var25 = 0; var25 < 5; var25++) {
                PixelFx.sprite(
                    var4.add((hash(var25, 11) - 0.5) * 1.5, 0.2 + var23, (hash(var25, 12) - 0.5) * 1.5),
                    1.0 + var23 * 2.0,
                    7 + Math.min(3, (int)(var23 * 5.0)),
                    0.72F,
                    0.66F,
                    0.56F,
                    0.7F * var14
                );
            }
        }
    }

    private static void goddessSpear(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8, double var10) {
        double var12 = sstep(0.0, 0.18, var8);
        float var14 = env(var8, var10, 0.02, 0.4);
        Vec3 var15 = var3.lerp(var4, var12);
        PixelFx.streak(var15.subtract(var5.scale(2.8)), var15, 0.9, 39, 1.0F, 0.95F, 0.75F, var14);
        PixelFx.sprite(var15.subtract(var5.scale(1.0)), 1.3, 4, 1.0F, 0.9F, 0.6F, 0.45F * var14);
        line(var0, var1, var2, var3, var15, 0.04, 1.0F, 0.95F, 0.75F, 0.35F * var14);
        ring(var0, var1, var2, var3.add(var5.scale(0.2)), var5, 0.35 + var8 * 0.5, 0.035, 1.0F, 0.9F, 0.6F, 0.8F * var14);

        for (int var16 = 0; var16 < 6; var16++) {
            PixelFx.sprite(
                var3.lerp(var15, hash(var16, 1)).add(0.0, var8 * 0.5 * hash(var16, 2), 0.0), 0.25, 1, 1.0F, 0.95F, 0.75F, var14 * (float)clamp(1.0 - var8)
            );
        }

        if (var12 > 0.99) {
            burst(var4, 2.0, var8 - 0.18, 0.45, 1.0F, 0.95F, 0.75F, var14);
        }
    }

    public static boolean effect(VertexConsumer var0, Matrix4f var1, int var2, Vec3 var3, int var4, double var5, double var7) {
        Vec3 var9 = ArcanaClient.camera();
        if (var9 == null) {
            return true;
        } else {
            double var10 = effectLife(var2);
            int var12 = Math.max(1, var4);
            switch (var2) {
                case 5:
                    waterSphere(var0, var1, var9, var3, var12, var5, var10);
                    return true;
                case 6:
                    spikes(var0, var1, var9, var3, var12, var5, var10);
                    return true;
                case 7:
                    jilwer(var0, var1, var9, var3, var5, var10);
                    return true;
                case 8:
                    bind(var0, var1, var9, var3, var12, var5, var10, var7);
                    return true;
                case 9:
                    gold(var0, var1, var9, var3, var5, var10, var7);
                    return true;
                case 10:
                case 16:
                case 18:
                case 19:
                default:
                    return false;
                case 11:
                    flowers(var3, var12, var5, var10, var7);
                    return true;
                case 12:
                    goddess(var0, var1, var9, var3, var5, var10, var7);
                    return true;
                case 13:
                    hellfire(var3, var12, var5, var10, var7);
                    return true;
                case 14:
                    tornado(var0, var1, var9, var3, var12, var5, var10, false);
                    return true;
                case 15:
                    strikes(var0, var1, var9, var3, var12, var5, var10);
                    return true;
                case 17:
                    float var19 = env(var5, var10, 0.02, 0.4);

                    for (int var21 = 0; var21 < 30; var21++) {
                        Vec3 var23 = new Vec3(hash(var21, 1) - 0.5, hash(var21, 2) - 0.2, hash(var21, 3) - 0.5).normalize();
                        Vec3 var25 = var3.add(var23.scale(var5 * 5.0));
                        PixelFx.streak(var25.subtract(var23.scale(0.28)), var25.add(var23.scale(0.28)), 0.22, 29, 1.0F, 1.0F, 1.0F, var19);
                    }

                    return true;
                case 20:
                    float var18 = env(var5, var10, 0.1, 0.5);
                    PixelFx.flat(var3.add(0.0, 0.05, 0.0), UP, 1.2 + 0.1 * Math.sin(var7 * 4.0), 5, var7, 0.7F, 0.95F, 1.0F, 0.8F * var18);

                    for (int var20 = 0; var20 < 6; var20++) {
                        double var22 = var7 * 2.0 + (double)var20 * 1.047;
                        PixelFx.sprite(
                            var3.add(Math.cos(var22) * 0.55, 0.2 + 0.3 * Math.sin(var7 * 3.0 + (double)var20), Math.sin(var22) * 0.55),
                            0.25,
                            1,
                            0.8F,
                            1.0F,
                            1.0F,
                            0.9F * var18
                        );
                    }

                    return true;
                case 21:
                    tornado(var0, var1, var9, var3, var12, var5, var10, true);
                    return true;
                case 22:
                case 23:
                    float var13 = env(var5, var10, 0.02, 0.4);
                    boolean var14 = var2 == 23;
                    burst(var3, var14 ? 3.2 : 1.8, var5, var14 ? 0.6 : 0.4, var14 ? 1.0F : 0.75F, var14 ? 1.0F : 0.95F, 1.0F, var13);
                    PixelFx.sprite(var3, (var14 ? 1.6 : 1.0) + var5 * 3.5, 6, 0.72F, 0.95F, 1.0F, var13 * (float)clamp(1.0 - var5 * 1.6));
                    PixelFx.sprite(var3, var14 ? 2.8 : 1.6, 4, 0.7F, 0.92F, 1.0F, 0.6F * var13);
                    int var15 = 2 + var4 * 2;

                    for (int var16 = 0; var16 < var15; var16++) {
                        PixelFx.sprite(
                            var3.add((hash(var16, 1) - 0.5) * 1.2, (hash(var16, 2) - 0.5) * 1.2, (hash(var16, 3) - 0.5) * 1.2),
                            0.6 + 0.3 * hash(var16, 4),
                            42 + var16 % 4,
                            0.8F,
                            0.97F,
                            1.0F,
                            0.9F * var13,
                            hash(var16, 5) * 6.28
                        );
                    }

                    for (int var24 = 0; var24 < (var14 ? 16 : 8); var24++) {
                        Vec3 var17 = new Vec3(hash(var24, 6) - 0.5, hash(var24, 7) - 0.5, hash(var24, 8) - 0.5).normalize();
                        PixelFx.sprite(var3.add(var17.scale(var5 * (double)(var14 ? 5 : 3))), 0.3, 1, var24 % 2 == 0 ? 1.0F : 0.75F, 1.0F, 1.0F, var13);
                    }

                    return true;
            }
        }
    }

    private static void waterSphere(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7) {
        double var9 = 1.1 + 0.35 * (double)var4;
        Vec3 var11 = var3.add(0.0, 3.2 + var9, 0.0);
        float var12 = env(var5, var7, 0.05, 0.5);
        if (var5 < 0.9) {
            double var13 = sstep(0.0, 0.5, var5);
            double var15 = sstep(0.55, 0.9, var5);
            Vec3 var17 = var11.lerp(var3.add(0.0, var9 * 0.6, 0.0), var15 * var15);
            double var18 = var9 * var13 * (1.0 + 0.05 * Math.sin(var5 * 30.0));
            PixelFx.sprite(var17, var18 * 2.3, 12, 0.45F, 0.72F, 1.0F, 0.95F * var12, var5 * 0.5);
            PixelFx.sprite(var17, var18 * 2.0, 4, 0.3F, 0.6F, 1.0F, 0.55F * var12);

            for (int var20 = 0; var20 < 26; var20++) {
                double var21 = (double)var20 * 0.449 + var5 * 4.0;
                double var23 = var18 * (var13 < 1.0 ? 2.2 - 1.2 * var13 : 0.6 * hash(var20, 3));
                Vec3 var25 = var17.add(Math.cos(var21) * var23, Math.sin((double)var20 * 1.3 + var5 * 3.0) * var23 * 0.8, Math.sin(var21) * var23);
                PixelFx.sprite(var25, 0.3, var20 % 3 == 0 ? 12 : 11, 0.5F, 0.78F, 1.0F, 0.85F * var12, var21);
            }
        } else {
            double var26 = var5 - 0.9;
            PixelFx.flat(var3.add(0.0, 0.08, 0.0), UP, (var9 + var26 * 6.0) * 2.0, 52 + Math.min(3, (int)(var26 * 4.0)), 0.0, 0.55F, 0.82F, 1.0F, 0.8F * var12);
            PixelFx.flat(var3.add(0.0, 0.09, 0.0), UP, (var9 * 0.6 + var26 * 3.5) * 2.0, 6, var26, 0.8F, 0.95F, 1.0F, 0.6F * var12);
            PixelFx.sprite(var3.add(0.0, var9 * 0.6, 0.0), var9 * 2.4 * (1.0 + var26), 56, 0.55F, 0.82F, 1.0F, (float)clamp(1.0 - var26 * 1.5) * var12);

            for (int var27 = 0; var27 < 40; var27++) {
                Vec3 var16 = new Vec3(Math.cos((double)var27 * 0.157), 0.0, Math.sin((double)var27 * 0.157));
                double var28 = 2.5 + 3.0 * hash(var27, 1);
                Vec3 var19 = var3.add(var16.scale(var9 * 0.6 + var26 * var28)).add(0.0, var26 * (5.0 + 3.0 * hash(var27, 2)) - 9.8 * var26 * var26 * 0.6, 0.0);
                if (!(var19.y < var3.y)) {
                    PixelFx.sprite(var19, 0.3, 11, 0.55F, 0.82F, 1.0F, 0.9F * var12, (double)var27);
                }
            }
        }
    }

    private static void spikes(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7) {
        int var9 = 9 + 3 * var4;
        double var10 = 2.4 + (double)var4;

        for (int var12 = 0; var12 < var9; var12++) {
            double var13 = hash(var12, 1) * 6.283;
            double var15 = 0.6 + var10 * Math.sqrt(hash(var12, 2));
            Vec3 var17 = var3.add(Math.cos(var13) * var15, 0.0, Math.sin(var13) * var15);
            double var18 = var15 / var10 * 0.3;
            double var20 = sstep(var18, var18 + 0.12, var5) * (1.0 - sstep(var7 - 0.6, var7, var5));
            if (!(var20 <= 0.01)) {
                double var22 = (1.1 + 1.7 * hash(var12, 3)) * var20;
                double var24 = 0.3 + 0.28 * hash(var12, 4);
                Vec3 var26 = var17.add((hash(var12, 5) - 0.5) * 0.5, var22, (hash(var12, 6) - 0.5) * 0.5);
                double var27 = hash(var12, 7);
                Vec3[] var29 = new Vec3[4];

                for (int var30 = 0; var30 < 4; var30++) {
                    var29[var30] = var17.add(Math.cos(var27 + (double)var30 * 1.5708) * var24, 0.0, Math.sin(var27 + (double)var30 * 1.5708) * var24);
                }

                for (int var34 = 0; var34 < 4; var34++) {
                    float var31 = var34 % 2 == 0 ? 0.95F : 0.7F;
                    Vec3 var32 = var26.lerp(var29[var34].lerp(var29[(var34 + 1) % 4], 0.5), 0.0);
                    PixelFx.quad(var26, var32, var29[(var34 + 1) % 4], var29[var34], 32, var31, var31 * 0.95F, var31 * 0.9F, 1.0F);
                }

                double var35 = var5 - var18;
                if (var35 > 0.0 && var35 < 0.8) {
                    for (int var36 = 0; var36 < 3; var36++) {
                        Vec3 var33 = new Vec3(hash(var12 * 3 + var36, 8) - 0.5, 0.0, hash(var12 * 3 + var36, 9) - 0.5).normalize();
                        PixelFx.sprite(
                            var17.add(var33.scale(var35 * 2.0)).add(0.0, var35 * 3.0 - var35 * var35 * 6.0 + 0.1, 0.0),
                            0.28,
                            33,
                            1.0F,
                            1.0F,
                            1.0F,
                            (float)clamp(1.0 - var35 * 1.25),
                            var35 * 8.0 + (double)var36
                        );
                    }
                }

                if (var35 > 0.0 && var35 < 1.2 && var12 % 2 == 0) {
                    PixelFx.sprite(
                        var17.add(0.0, 0.3 + var35 * 0.4, 0.0),
                        0.9 + var35 * 1.0,
                        7 + Math.min(3, (int)(var35 * 3.5)),
                        0.72F,
                        0.65F,
                        0.55F,
                        0.75F * (float)clamp(1.2 - var35)
                    );
                }
            }
        }

        PixelFx.flat(
            var3.add(0.0, 0.04, 0.0),
            UP,
            (0.5 + var5 * 6.0) * 2.0,
            52 + Math.min(3, (int)(var5 * 3.0)),
            0.0,
            0.68F,
            0.6F,
            0.5F,
            0.6F * (float)clamp(1.0 - var5)
        );
    }

    private static void jilwer(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6) {
        float var8 = env(var4, var6, 0.02, 0.5);
        PixelFx.flat(var3.add(0.0, 0.05, 0.0), UP, (0.5 + var4 * 5.0) * 2.0, 52 + Math.min(3, (int)(var4 * 5.0)), 0.0, 0.85F, 1.0F, 0.9F, 0.8F * var8);

        for (int var9 = 0; var9 < 18; var9++) {
            double var10 = hash(var9, 1) * 6.283;
            double var12 = 0.2 + 1.6 * hash(var9, 2);
            Vec3 var14 = new Vec3(Math.cos(var10), 0.0, Math.sin(var10));
            Vec3 var15 = var3.add(var14.scale(0.4 + var4 * 3.0)).add(0.0, var12, 0.0);
            PixelFx.streak(var15, var15.add(var14.scale(1.0 + hash(var9, 3))), 0.4, 40, 0.9F, 1.0F, 0.95F, 0.9F * var8);
        }

        for (int var16 = 0; var16 < 6; var16++) {
            PixelFx.sprite(
                var3.add((hash(var16, 4) - 0.5) * 1.4, 0.2 + var4 * 0.5, (hash(var16, 5) - 0.5) * 1.4), 0.8 + var4, 46, 0.8F, 0.75F, 0.65F, 0.6F * var8
            );
        }
    }

    private static void bind(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7, double var9) {
        float var11 = env(var5, var7, 0.25, 0.8);

        for (int var12 = 0; var12 < 3; var12++) {
            double var13 = 0.3 + 0.7 * (double)var12;
            Vec3 var15 = new Vec3(Math.sin(var9 * 0.7 + (double)var12) * 0.25, 1.0, Math.cos(var9 * 0.6 + (double)var12) * 0.25).normalize();
            double var16 = 0.8 + 0.08 * Math.sin(var9 * 2.0 + (double)var12);
            ring(var0, var1, var2, var3.add(0.0, var13, 0.0), var15, var16, 0.04, 0.92F, 1.0F, 0.62F, 0.8F * var11);

            for (int var18 = 0; var18 < 10; var18++) {
                double var19 = var9 * (var12 % 2 == 0 ? 1.1 : -1.1) + (double)var18 * 0.628;
                Vec3 var21 = var3.add(Math.cos(var19) * var16 * 1.12, var13, Math.sin(var19) * var16 * 1.12);
                PixelFx.sprite(var21, 0.32, 34 + (var18 + var12) % 4, 0.95F, 1.0F, 0.66F, 0.95F * var11);
            }
        }

        PixelFx.flat(var3.add(0.0, 0.05, 0.0), UP, 2.4, 57, var9 * 0.4, 0.92F, 1.0F, 0.62F, 0.7F * var11);

        for (int var22 = 0; var22 < 8; var22++) {
            double var23 = (var9 * 0.5 + hash(var22, 1)) % 1.0;
            double var24 = hash(var22, 2) * 6.28;
            PixelFx.sprite(
                var3.add(Math.cos(var24) * 0.6, var23 * 2.2, Math.sin(var24) * 0.6), 0.22, 58, 0.95F, 1.0F, 0.7F, (float)Math.sin(var23 * Math.PI) * var11
            );
        }
    }

    private static void gold(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, double var8) {
        float var10 = env(var4, var6, 0.1, 1.5);
        double var11 = Math.min(2.0, var4 * 1.1);
        ring(var0, var1, var2, var3.add(0.0, var11, 0.0), UP, 0.55, 0.05, 1.0F, 0.85F, 0.35F, 0.75F * var10 * (float)clamp(2.0 - var11));

        for (int var13 = 0; var13 < 34; var13++) {
            double var14 = hash(var13, 1) * var11;
            double var16 = hash(var13, 2) * 6.283;
            Vec3 var18 = var3.add(Math.cos(var16) * 0.42, var14, Math.sin(var16) * 0.42);
            double var19 = 0.5 + 0.5 * Math.sin(var8 * 6.0 + (double)var13 * 1.7);
            PixelFx.sprite(var18, 0.28, var19 > 0.8 ? 47 : 58, 1.0F, 0.84F, 0.32F, var10);
        }

        for (int var21 = 0; var21 < 5; var21++) {
            double var22 = (var8 * 0.4 + hash(var21, 4)) % 1.0;
            PixelFx.sprite(
                var3.add((hash(var21, 5) - 0.5) * 0.8, var22 * 2.4, (hash(var21, 6) - 0.5) * 0.8),
                0.3,
                1,
                1.0F,
                0.92F,
                0.5F,
                (float)Math.sin(var22 * Math.PI) * var10
            );
        }
    }

    private static void flowers(Vec3 var0, int var1, double var2, double var4, double var6) {
        double var8 = 2.5 + 1.6 * (double)var1;
        float var10 = (float)clamp((var4 - var2) / 3.0);
        PixelFx.flat(var0.add(0.0, 0.05, 0.0), UP, Math.min(var8, var2 * var8 / 1.5) * 2.0, 5, 0.0, 1.0F, 0.95F, 0.8F, 0.6F * (float)clamp(1.8 - var2));
        float[][] var11 = new float[][]{
            {1.0F, 1.0F, 1.0F}, {1.0F, 0.72F, 0.82F}, {1.0F, 0.92F, 0.45F}, {0.72F, 0.82F, 1.0F}, {0.86F, 0.72F, 1.0F}, {1.0F, 0.6F, 0.6F}
        };
        int var12 = 30 + 25 * var1;

        for (int var13 = 0; var13 < var12; var13++) {
            double var14 = (double)var13 * 2.39996;
            double var16 = var8 * Math.sqrt(((double)var13 + 0.5) / (double)var12);
            double var18 = var16 / var8 * 1.5;
            double var20 = sstep(var18, var18 + 0.5, var2);
            if (!(var20 <= 0.01)) {
                Vec3 var22 = var0.add(Math.cos(var14) * var16, 0.07 + 0.08 * hash(var13, 1), Math.sin(var14) * var16);
                float[] var23 = var11[(int)(hash(var13, 2) * (double)var11.length) % var11.length];
                double var24 = (0.42 + 0.25 * hash(var13, 3)) * var20;
                double var26 = Math.sin(var6 * 1.5 + (double)var13) * 0.15;
                PixelFx.flat(var22, UP, var24, 30, hash(var13, 4) * 6.28 + var26, var23[0], var23[1], var23[2], var10);
                PixelFx.flat(var22.add(0.0, 0.01, 0.0), UP, var24, 31, 0.0, 1.0F, 1.0F, 1.0F, var10);
            }
        }

        if (var2 < 3.0) {
            for (int var28 = 0; var28 < 20; var28++) {
                double var29 = (var2 * 0.6 + hash(var28, 5)) % 1.0;
                double var30 = hash(var28, 6) * 6.28;
                double var31 = var8 * Math.sqrt(hash(var28, 7));
                PixelFx.sprite(
                    var0.add(Math.cos(var30) * var31, 0.2 + var29 * 1.5, Math.sin(var30) * var31),
                    0.3,
                    28,
                    1.0F,
                    0.8F,
                    0.88F,
                    (float)(Math.sin(var29 * Math.PI) * clamp(3.0 - var2)),
                    var29 * 6.0 + (double)var28
                );
            }
        }
    }

    private static void goddess(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, double var8) {
        float var10 = env(var4, var6, 0.15, 0.9);
        Vec3 var11 = var3.add(0.0, 0.05, 0.0);
        double var12 = 1.4 + 0.3 * sstep(0.0, 0.4, var4);
        ring(var0, var1, var2, var11, UP, var12, 0.06, 1.0F, 0.92F, 0.62F, 0.85F * var10);
        ring(var0, var1, var2, var11, UP, var12 * 0.84, 0.03, 1.0F, 0.92F, 0.62F, 0.7F * var10);

        for (int var14 = 0; var14 < 12; var14++) {
            double var15 = var8 * 0.5 + (double)var14 * 0.5236;
            PixelFx.flat(
                var11.add(Math.cos(var15) * var12 * 0.92, 0.01, Math.sin(var15) * var12 * 0.92),
                UP,
                0.36,
                34 + var14 % 4,
                var15 + 1.57,
                1.0F,
                0.94F,
                0.7F,
                0.95F * var10
            );
        }

        PixelFx.flat(var11, UP, var12 * 2.2, 4, 0.0, 1.0F, 0.9F, 0.6F, 0.45F * var10);

        for (int var22 = 0; var22 < 22; var22++) {
            double var23 = (var4 * 0.7 + hash(var22, 1)) % 1.0;
            double var17 = hash(var22, 2) * 6.283;
            double var19 = var12 * (0.2 + 0.7 * hash(var22, 3));
            Vec3 var21 = var3.add(Math.cos(var17) * var19, 0.2 + var23 * 2.6, Math.sin(var17) * var19);
            PixelFx.sprite(
                var21,
                0.45 + 0.2 * hash(var22, 4),
                var22 % 3 == 0 ? 1 : PixelFx.anim(24, 4, var8 + (double)var22, 8.0),
                1.0F,
                0.94F,
                0.72F,
                (float)Math.sin(var23 * Math.PI) * var10
            );
        }

        PixelFx.sprite(var3.add(0.0, 1.1, 0.0), 2.6, 4, 1.0F, 0.92F, 0.65F, 0.35F * var10);
    }

    private static void hellfire(Vec3 var0, int var1, double var2, double var4, double var6) {
        int var8 = 4 + 2 * var1;
        double var9 = 1.2 + (double)var1;
        float var11 = env(var2, var4, 0.05, 0.9);
        PixelFx.flat(var0.add(0.0, 0.05, 0.0), UP, (var9 + 1.5) * 2.0, 4, 0.0, 1.0F, 0.42F, 0.1F, 0.85F * var11);

        for (int var12 = 0; var12 < var8; var12++) {
            double var13 = hash(var12, 1) * 6.283;
            double var15 = var9 * Math.sqrt(hash(var12, 2));
            Vec3 var17 = var0.add(Math.cos(var13) * var15, 0.0, Math.sin(var13) * var15);
            double var18 = 0.06 * (double)var12;
            double var20 = sstep(var18, var18 + 0.25, var2);
            if (!(var20 <= 0.0)) {
                double var22 = (3.2 + 3.2 * hash(var12, 3)) * var20 * (1.0 - 0.5 * sstep(var4 - 0.9, var4, var2));
                int var24 = (int)Math.max(3.0, var22 * 2.2);

                for (int var25 = 0; var25 < var24; var25++) {
                    double var26 = var22 * (double)var25 / (double)var24;
                    double var28 = (1.7 - 1.0 * (double)var25 / (double)var24) * (0.85 + 0.3 * hash(var12 * 31 + var25, 4));
                    double var30 = Math.sin(var6 * 9.0 + (double)var25 * 1.3 + (double)var12) * 0.12;
                    PixelFx.sprite(
                        var17.add(var30, var26 + var28 * 0.4, -var30),
                        var28,
                        PixelFx.anim(16, 8, var6 + hash(var12 * 31 + var25, 5), 14.0),
                        1.0F,
                        1.0F,
                        1.0F,
                        var11
                    );
                }

                PixelFx.sprite(
                    var17.add(0.0, var22 + 0.6, 0.0), 1.5 + var22 * 0.2, 7 + (int)(var6 * 4.0 + (double)var12) % 4, 0.25F, 0.18F, 0.16F, 0.55F * var11
                );

                for (int var32 = 0; var32 < 6; var32++) {
                    double var33 = (var2 * 1.3 + hash(var12 * 7 + var32, 5)) % 1.0;
                    Vec3 var34 = var17.add((hash(var12 * 7 + var32, 6) - 0.5) * 1.4, var33 * (var22 + 2.0), (hash(var12 * 7 + var32, 7) - 0.5) * 1.4);
                    PixelFx.sprite(var34, 0.22, 13, 1.0F, 0.7F, 0.25F, (float)(1.0 - var33) * var11);
                }
            }
        }
    }

    private static void tornado(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7, boolean var9) {
        float var10 = env(var5, var7, 0.4, 0.8);
        double var11 = 6.0 + 1.5 * (double)var4;
        byte var13 = 6;

        for (int var14 = 0; var14 < var13; var14++) {
            Vec3 var15 = null;

            for (int var16 = 0; var16 <= 18; var16++) {
                double var17 = var11 * (double)var16 / 18.0;
                double var19 = 0.5 + var17 * (0.32 + 0.04 * (double)var4);
                double var21 = (double)var14 * 6.283 / (double)var13 + var5 * (var9 ? 4.5 : 3.6) + var17 * 0.45;
                Vec3 var23 = var3.add(Math.cos(var21) * var19, var17, Math.sin(var21) * var19);
                if (var15 != null) {
                    float var24 = var10 * (float)(0.3 + 0.7 * Math.sin(Math.PI * (double)var16 / 18.0));
                    if (var9) {
                        line(var0, var1, var2, var15, var23, 0.12, 1.0F, 0.62F, 0.15F, 0.55F * var24);
                    } else {
                        line(var0, var1, var2, var15, var23, 0.05, 0.94F, 1.0F, 0.97F, 0.4F * var24);
                    }
                }

                var15 = var23;
            }
        }

        int var46 = (int)Math.ceil(var11 / 0.7);
        byte var47 = 14;
        double var48 = var5 * (var9 ? 4.5 : 3.6);

        for (int var18 = 0; var18 < 2; var18++) {
            double var50 = var18 == 0 ? 1.0 : 0.72;
            double var54 = var18 == 0 ? 0.45 : 0.62;

            for (int var56 = 0; var56 < var46; var56++) {
                double var57 = var11 * (double)var56 / (double)var46;
                double var26 = var11 * (double)(var56 + 1) / (double)var46;
                double var28 = (0.5 + var57 * (0.32 + 0.04 * (double)var4)) * var50;
                double var30 = (0.5 + var26 * (0.32 + 0.04 * (double)var4)) * var50;
                float var32 = var10 * (float)(0.55 + 0.45 * Math.sin(Math.PI * ((double)var56 + 0.5) / (double)var46)) * (var18 == 0 ? 0.85F : 1.0F);

                for (int var33 = 0; var33 < var47; var33++) {
                    double var34 = (double)var33 * 6.283 / (double)var47 + var48 * (var18 == 0 ? 1.0 : 1.35) + var57 * var54;
                    double var36 = var34 + 6.283 / (double)var47 * 1.15;
                    double var38 = var34 + (var26 - var57) * var54;
                    double var40 = var36 + (var26 - var57) * var54;
                    Vec3 var42 = var3.add(Math.cos(var38) * var30, var26, Math.sin(var38) * var30);
                    Vec3 var43 = var3.add(Math.cos(var40) * var30, var26, Math.sin(var40) * var30);
                    Vec3 var44 = var3.add(Math.cos(var36) * var28, var57, Math.sin(var36) * var28);
                    Vec3 var45 = var3.add(Math.cos(var34) * var28, var57, Math.sin(var34) * var28);
                    if (var9) {
                        PixelFx.quad(
                            var42,
                            var43,
                            var44,
                            var45,
                            PixelFx.anim(16, 8, var5 + (double)var33 * 0.13 + (double)var56 * 0.29, 12.0),
                            1.0F,
                            var18 == 0 ? 0.95F : 0.8F,
                            var18 == 0 ? 0.9F : 0.7F,
                            var32
                        );
                    } else {
                        PixelFx.quad(
                            var42,
                            var43,
                            var44,
                            var45,
                            7 + (var33 + var56 + var18) % 4,
                            var18 == 0 ? 0.84F : 0.7F,
                            var18 == 0 ? 0.9F : 0.78F,
                            var18 == 0 ? 0.86F : 0.72F,
                            var32 * 0.95F
                        );
                    }
                }
            }
        }

        int var49 = 120 + 15 * var4;

        for (int var51 = 0; var51 < var49; var51++) {
            double var20 = (var5 * (var9 ? 0.55 : 0.45) + hash(var51, 1)) % 1.0;
            double var22 = var20 * var11;
            double var58 = 0.45 + var22 * (0.32 + 0.04 * (double)var4) * (0.8 + 0.4 * hash(var51, 3));
            double var59 = hash(var51, 2) * 6.28 + var5 * (var9 ? 4.5 : 3.6) + var22 * 0.45;
            Vec3 var60 = var3.add(Math.cos(var59) * var58, var22, Math.sin(var59) * var58);
            float var29 = var10 * (float)Math.sin(var20 * Math.PI);
            if (var9) {
                PixelFx.sprite(var60, 1.2 + var22 * 0.15, PixelFx.anim(16, 8, var5 + hash(var51, 4), 14.0), 1.0F, 1.0F, 1.0F, var29);
                if (var51 % 4 == 0) {
                    PixelFx.sprite(var60.add(0.0, 0.4, 0.0), 0.2, 13, 1.0F, 0.75F, 0.3F, var29);
                }
            } else {
                Vec3 var61 = new Vec3(-Math.sin(var59), 0.15, Math.cos(var59)).normalize();
                if (var51 % 3 == 0) {
                    PixelFx.sprite(var60, 1.2 + var22 * 0.18, 7 + (int)(hash(var51, 5) * 4.0), 0.86F, 0.9F, 0.86F, 0.8F * var29);
                } else {
                    PixelFx.streak(
                        var60.subtract(var61.scale(0.6 + var22 * 0.06)),
                        var60.add(var61.scale(0.6 + var22 * 0.06)),
                        0.35,
                        40,
                        0.92F,
                        1.0F,
                        0.95F,
                        0.85F * var29
                    );
                }
            }
        }

        for (int var52 = 0; var52 < 10; var52++) {
            double var53 = var5 * 3.0 + (double)var52 * 0.628;
            double var55 = 1.2 + 0.5 * (double)var4;
            PixelFx.sprite(
                var3.add(Math.cos(var53) * var55, 0.3, Math.sin(var53) * var55),
                1.2,
                var9 ? 7 + var52 % 4 : 46,
                var9 ? 0.3F : 0.75F,
                var9 ? 0.22F : 0.7F,
                var9 ? 0.2F : 0.6F,
                0.6F * var10
            );
        }

        if (var9) {
            PixelFx.flat(var3.add(0.0, 0.05, 0.0), UP, (1.6 + 0.5 * (double)var4) * 2.4, 4, 0.0, 1.0F, 0.45F, 0.1F, 0.7F * var10);
        }
    }

    private static void strikes(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7) {
        int var9 = 10 + 4 * var4;
        double var10 = 1.5 + (double)var4;
        Vec3[] var12 = new Vec3[var9];

        for (int var13 = 0; var13 < var9; var13++) {
            double var14 = hash(var13, 2) * 6.283;
            double var16 = var10 * Math.sqrt(hash(var13, 3));
            var12[var13] = var3.add(Math.cos(var14) * var16, 0.0, Math.sin(var14) * var16);
            double var18 = hash(var13, 1) * (var7 - 0.5);
            double var20 = var5 - var18;
            if (!(var20 < 0.0) && !(var20 > 0.45)) {
                double var22 = !(var20 < 0.08) && (!(var20 > 0.16) || !(var20 < 0.22)) && (!(var20 > 0.3) || !(var20 < 0.34)) ? 0.25 : 1.0;
                float var24 = (float)(clamp(1.0 - var20 / 0.45) * var22);
                Vec3 var25 = var12[var13].add((hash(var13, 4) - 0.5) * 4.0, 14.0 + 4.0 * hash(var13, 9), (hash(var13, 5) - 0.5) * 4.0);
                bolt(var0, var1, var2, var25, var12[var13], var5, var13 * 13 + 5, 1.3, var24, 3, 0.06);
                burst(var12[var13].add(0.0, 0.4, 0.0), 2.6, var20, 0.45, 0.82F, 0.6F, 1.0F, var24);
                PixelFx.flat(
                    var12[var13].add(0.0, 0.06, 0.0),
                    UP,
                    2.4 + var20 * 6.0,
                    52 + Math.min(3, (int)(var20 * 9.0)),
                    0.0,
                    0.75F,
                    0.55F,
                    1.0F,
                    (float)clamp(1.0 - var20 * 2.2)
                );

                for (int var26 = 0; var26 < 5; var26++) {
                    PixelFx.sprite(
                        var12[var13]
                            .add(
                                (hash(var13 * 5 + var26, 6) - 0.5) * var20 * 8.0,
                                var20 * 3.0 - var20 * var20 * 8.0 + 0.2,
                                (hash(var13 * 5 + var26, 7) - 0.5) * var20 * 8.0
                            ),
                        0.3,
                        1,
                        0.9F,
                        0.75F,
                        1.0F,
                        var24
                    );
                }
            }
        }

        for (byte var27 = 0; var27 + 1 < var9; var27 += 2) {
            double var28 = hash(var27, 1) * (var7 - 0.5);
            double var29 = var5 - var28;
            if (!(var29 < 0.05) && !(var29 > 0.3)) {
                bolt(
                    var0,
                    var1,
                    var2,
                    var12[var27].add(0.0, 0.3, 0.0),
                    var12[var27 + 1].add(0.0, 0.3, 0.0),
                    var5,
                    var27 * 7 + 3,
                    0.5,
                    (float)clamp(1.0 - var29 / 0.3) * 0.8F,
                    1,
                    0.035
                );
            }
        }

        PixelFx.sprite(var3.add(0.0, 1.0, 0.0), var10 * 2.5, 4, 0.7F, 0.45F, 1.0F, 0.16F * env(var5, var7, 0.05, 0.4));
    }

    public static boolean aura(VertexConsumer var0, Matrix4f var1, Vec3 var2, double var3, double var5, double var7, float var9) {
        if (var5 > 0.5) {
            return true;
        } else {
            double var10 = var7;
            double var12 = 0.6 + var3 * 0.35;
            double var14 = 2.0 + var3 * 0.3;
            int var16 = 10 + (int)(var3 * 6.0);

            for (int var17 = 0; var17 < var16; var17++) {
                double var18 = (var10 * 0.6 + hash(var17, 1)) % 1.0;
                double var20 = hash(var17, 2) * 6.283;
                double var22 = var12 * (0.5 + 0.5 * hash(var17, 3));
                Vec3 var24 = var2.add(Math.cos(var20) * var22, var18 * var14, Math.sin(var20) * var22);
                PixelFx.sprite(
                    var24,
                    0.5 + var3 * 0.12,
                    PixelFx.anim(24, 4, var10 + (double)var17 * 0.3, 8.0),
                    0.55F,
                    0.8F,
                    1.0F,
                    0.55F * (float)Math.sin(var18 * Math.PI)
                );
            }

            return true;
        }
    }
}
