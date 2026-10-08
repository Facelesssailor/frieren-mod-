package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class SpellFx {
    private SpellFx() {
    }

    public static double beamLife(int var0) {
        switch (var0) {
            case 0:
                return 0.6;
            case 1:
                return 0.4;
            case 2:
                return 1.6;
            case 3:
                return 3.4;
            case 4:
                return 1.0;
            case 5:
                return 0.8;
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
                return 0.45;
            case 15:
                return 0.6;
            case 16:
                return 1.9;
            case 17:
                return 1.2;
            case 18:
                return 1.0;
            case 19:
                return 0.9;
        }
    }

    public static double effectLife(int var0) {
        switch (var0) {
            case 5:
                return 1.9;
            case 6:
                return 2.4;
            case 7:
                return 0.9;
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
                return 2.2;
            case 13:
                return 2.6;
            case 14:
            case 21:
                return 4.0;
            case 15:
                return 1.3;
            case 17:
                return 0.9;
            case 20:
                return 1.3;
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

    private static void v(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, float var4, float var5, float var6, float var7) {
        var0.addVertex(var1, (float)(var3.x - var2.x), (float)(var3.y - var2.y), (float)(var3.z - var2.z)).setColor(var4, var5, var6, var7);
    }

    private static Vec3 perp(Vec3 var0) {
        Vec3 var1 = Math.abs(var0.y) < 0.95 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
        return var0.cross(var1).normalize();
    }

    private static void spear(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, float var7, float var8, float var9, float var10) {
        if (!(var10 <= 0.003F)) {
            Vec3 var11 = var4.subtract(var3);
            Vec3 var12 = var3.add(var11.scale(0.7));
            Vec3 var13 = var12.subtract(var2);
            Vec3 var14 = var11.cross(var13);
            if (!(var14.lengthSqr() < 1.0E-9)) {
                var14 = var14.normalize().scale(var5 * 0.5);
                v(var0, var1, var2, var4, var7, var8, var9, var10);
                v(var0, var1, var2, var12.add(var14), var7, var8, var9, var10);
                v(var0, var1, var2, var3, var7, var8, var9, var10 * 0.4F);
                v(var0, var1, var2, var12.subtract(var14), var7, var8, var9, var10);
            }
        }
    }

    private static void ring(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, double var7, float var9, float var10, float var11, float var12
    ) {
        if (!(var12 <= 0.003F) && !(var5 <= 0.0)) {
            Vec3 var13 = perp(var4);
            Vec3 var14 = var4.cross(var13).normalize();
            Vec3 var15 = null;
            byte var16 = 40;

            for (int var17 = 0; var17 <= var16; var17++) {
                double var18 = (Math.PI * 2) * (double)var17 / (double)var16;
                Vec3 var20 = var3.add(var13.scale(Math.cos(var18) * var5)).add(var14.scale(Math.sin(var18) * var5));
                if (var15 != null) {
                    flat(var0, var1, var2, var15, var20, var4, var7, var9, var10, var11, var12);
                }

                var15 = var20;
            }
        }
    }

    private static void flat(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, float var8, float var9, float var10, float var11
    ) {
        Vec3 var12 = var4.subtract(var3);
        Vec3 var13 = var12.cross(var5);
        if (!(var13.lengthSqr() < 1.0E-12)) {
            var13 = var13.normalize().scale(var6 * 0.5);
            v(var0, var1, var2, var3.subtract(var13), var8, var9, var10, var11);
            v(var0, var1, var2, var3.add(var13), var8, var9, var10, var11);
            v(var0, var1, var2, var4.add(var13), var8, var9, var10, var11);
            v(var0, var1, var2, var4.subtract(var13), var8, var9, var10, var11);
        }
    }

    private static void planeDisc(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, float var7, float var8, float var9, float var10, float var11
    ) {
        planeDisc(var0, var1, var2, var3, var4, var5, var7, var8, var9, var10, var11, 28);
    }

    private static void planeDisc(
        VertexConsumer var0,
        Matrix4f var1,
        Vec3 var2,
        Vec3 var3,
        Vec3 var4,
        double var5,
        float var7,
        float var8,
        float var9,
        float var10,
        float var11,
        int var12
    ) {
        if (!(var10 <= 0.003F) && !(var5 <= 0.0)) {
            Vec3 var13 = perp(var4);
            Vec3 var14 = var4.cross(var13).normalize();

            for (int var15 = 0; var15 < var12; var15++) {
                double var16 = (Math.PI * 2) * (double)var15 / (double)var12;
                double var18 = (Math.PI * 2) * (double)(var15 + 1) / (double)var12;
                Vec3 var20 = var3.add(var13.scale(Math.cos(var16) * var5)).add(var14.scale(Math.sin(var16) * var5));
                Vec3 var21 = var3.add(var13.scale(Math.cos(var18) * var5)).add(var14.scale(Math.sin(var18) * var5));
                v(var0, var1, var2, var3, var7, var8, var9, var10);
                v(var0, var1, var2, var20, var7, var8, var9, var10 * var11);
                v(var0, var1, var2, var21, var7, var8, var9, var10 * var11);
                v(var0, var1, var2, var21, var7, var8, var9, var10 * var11);
            }
        }
    }

    private static void box(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, double var8, float var10, float var11, float var12, float var13
    ) {
        double var14 = Math.cos(var6);
        double var16 = Math.sin(var6);
        double var18 = Math.cos(var8);
        double var20 = Math.sin(var8);
        Vec3[] var22 = new Vec3[]{new Vec3(var14, 0.0, var16), new Vec3(-var16 * var20, var18, var14 * var20), new Vec3(-var16 * var18, -var20, var14 * var18)};

        for (int var23 = 0; var23 < 6; var23++) {
            int var24 = var23 / 2;
            double var25 = var23 % 2 == 0 ? 1.0 : -1.0;
            Vec3 var27 = var22[var24].scale(var25);
            Vec3 var28 = var22[(var24 + 1) % 3];
            Vec3 var29 = var22[(var24 + 2) % 3];
            Vec3 var30 = var3.add(var27.scale(var4));
            float var31 = (float)(0.62 + 0.38 * Math.max(0.0, var27.y) + 0.12 * Math.abs(var27.x));
            Vec3 var32 = var30.add(var28.scale(var4)).add(var29.scale(var4));
            Vec3 var33 = var30.add(var28.scale(-var4)).add(var29.scale(var4));
            Vec3 var34 = var30.add(var28.scale(-var4)).add(var29.scale(-var4));
            Vec3 var35 = var30.add(var28.scale(var4)).add(var29.scale(-var4));
            v(var0, var1, var2, var32, var10 * var31, var11 * var31, var12 * var31, var13);
            v(var0, var1, var2, var33, var10 * var31, var11 * var31, var12 * var31, var13);
            v(var0, var1, var2, var34, var10 * var31, var11 * var31, var12 * var31, var13);
            v(var0, var1, var2, var35, var10 * var31, var11 * var31, var12 * var31, var13);
        }
    }

    private static void bolt(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, int var7, double var8, float var10) {
        Vec3 var11 = var4.subtract(var3);
        double var12 = var11.length();
        if (!(var12 < 0.05)) {
            Vec3 var14 = perp(var11.normalize());
            Vec3 var15 = var11.normalize().cross(var14);
            int var16 = (int)Math.floor(var5 * 20.0);
            int var17 = Math.max(6, (int)(var12 * 1.6));
            Vec3 var18 = var3;

            for (int var19 = 1; var19 <= var17; var19++) {
                double var20 = (double)var19 / (double)var17;
                double var22 = var19 == var17 ? 0.0 : var8 * Math.sin(Math.PI * var20);
                Vec3 var24 = var3.add(var11.scale(var20))
                    .add(var14.scale((hash(var7 * 97 + var19, var16) - 0.5) * 2.0 * var22))
                    .add(var15.scale((hash(var7 * 31 + var19, var16 + 7) - 0.5) * 2.0 * var22));
                BreakerFx.ribbon(var0, var1, var2, var18, var24, 0.55, 0.64F, 0.36F, 1.0F, 0.22F * var10);
                BreakerFx.ribbon(var0, var1, var2, var18, var24, 0.16, 0.78F, 0.55F, 1.0F, 0.7F * var10);
                BreakerFx.ribbon(var0, var1, var2, var18, var24, 0.05, 1.0F, 1.0F, 1.0F, 0.95F * var10);
                if (var19 % 4 == 2 && hash(var7 + var19, var16 + 3) > 0.5) {
                    Vec3 var25 = var24.add(var14.scale((hash(var19, var16 + 11) - 0.5) * var8 * 2.4)).add(var11.normalize().scale(var12 * 0.08));
                    BreakerFx.ribbon(var0, var1, var2, var24, var25, 0.08, 0.78F, 0.55F, 1.0F, 0.55F * var10);
                }

                var18 = var24;
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
        var13.a = var12 * 0.12F;
        var13.disc(var5, 32);
        var13.glow = 3.5;
        var13.a = var12;
        var13.ring(var5, 0.035 * var5 + 0.01);
        var13.ring(var5 * 0.82, 0.02 * var5 + 0.006);
        var13.ticks(var5 * 0.82, var5 * 0.95, 24, var7 * 2.0, 0.012 * var5 + 0.004);
        var13.star(var5 * 0.62, 6, 2, -var7 * 3.0, 0.02 * var5 + 0.006);
        var13.ring(var5 * 0.25, 0.03 * var5 + 0.006);
        var13.glow = 0.0;
    }

    private static Vec3 handStart(Vec3 var0, Vec3 var1) {
        Minecraft var2 = Minecraft.getInstance();
        if (var2.level == null) {
            return var0;
        } else {
            for (Player var4 : var2.level.players()) {
                if (var4.getEyePosition().distanceToSqr(var0) < 0.5) {
                    Vec3 var5 = perp(var1);
                    return var0.add(var1.scale(0.6)).add(var5.scale(0.34)).add(0.0, -0.36, 0.0);
                }
            }

            return var0;
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
                            fern(var0, var1, var7, var3, var4, var5, var12);
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
                            float var14 = env(var5, var12, 0.03, 0.25) * (float)(0.7 + 0.3 * hash((int)(var5 * 30.0), 2));
                            Vec3 var15 = handStart(var3, var11);
                            bolt(var0, var1, var7, var15, var4, var5, (int)(var4.x * 7.0 + var4.z * 13.0), Math.min(1.4, var9 * 0.12), var14);
                            BreakerFx.disc(var0, var1, var7, var4, 1.4, 0.7F, 0.45F, 1.0F, 0.5F * var14);
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

    private static void zoltraak(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8, boolean var10) {
        double var11 = sstep(0.0, var10 ? 0.12 : 0.05, var6);
        float var13 = env(var6, var8, 0.02, var10 ? 0.7 : 0.3);
        double var14 = 1.0 - 0.6 * clamp((var6 - var8 * 0.4) / (var8 * 0.6));
        Vec3 var16 = var3.lerp(var4, var11);
        double var17 = var10 ? 3.2 : 1.0;
        BreakerFx.ribbon(var0, var1, var2, var3, var16, 0.85 * var17 * var14, 0.62F, 0.7F, 1.0F, 0.1F * var13);
        BreakerFx.ribbon(var0, var1, var2, var3, var16, 0.36 * var17 * var14, 0.75F, 0.8F, 1.0F, 0.32F * var13);
        BreakerFx.ribbon(var0, var1, var2, var3, var16, 0.13 * var17 * var14, 0.96F, 0.97F, 1.0F, 0.95F * var13);
        BreakerFx.ribbon(var0, var1, var2, var3, var16, 0.05 * var17 * var14, 1.0F, 1.0F, 1.0F, var13);
        if (var10) {
            for (int var19 = 0; var19 < 6; var19++) {
                double var20 = ((double)var19 + 0.5) / 6.0 + var6 * 0.4;
                var20 -= Math.floor(var20);
                if (!(var20 > var11)) {
                    ring(
                        var0,
                        var1,
                        var2,
                        var3.lerp(var4, var20),
                        var5,
                        0.9 + 0.5 * Math.sin(var6 * 9.0 + (double)var19),
                        0.05,
                        0.85F,
                        0.9F,
                        1.0F,
                        0.45F * var13
                    );
                }
            }
        }

        castCircle(var0, var1, var2, var3.add(var5.scale(0.25)), var5, var10 ? 1.5 : 0.42, var6, 0.92F, 0.95F, 1.0F, var13);
        if (var11 > 0.99) {
            BreakerFx.sparkle(var0, var1, var2, var4, (var10 ? 2.4 : 0.8) * (double)var13, 1.0F, 1.0F, 1.0F, var13);
            BreakerFx.disc(var0, var1, var2, var4, (var10 ? 2.2 : 0.7) + var6 * (var10 ? 2.5 : 1.2), 0.75F, 0.8F, 1.0F, 0.4F * var13);
        }
    }

    private static void fern(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, double var7) {
        float var9 = env(var5, var7, 0.02, 0.25);
        BreakerFx.ribbon(var0, var1, var2, var3, var4, 0.26, 0.7F, 0.76F, 1.0F, 0.14F * var9);
        BreakerFx.ribbon(var0, var1, var2, var3, var4, 0.07, 1.0F, 1.0F, 1.0F, 0.95F * var9);
    }

    private static void iceSpears(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8) {
        double var10 = sstep(0.0, 0.22, var6);
        float var12 = env(var6, var8, 0.03, 0.35);
        Vec3 var13 = var3.lerp(var4, var10);
        Vec3 var14 = var13.subtract(var5.scale(1.8));
        spear(var0, var1, var2, var14, var13, 0.55, 0.62F, 0.85F, 1.0F, 0.35F * var12);
        spear(var0, var1, var2, var14.add(var5.scale(0.3)), var13, 0.26, 0.86F, 0.96F, 1.0F, 0.95F * var12);

        for (int var15 = 0; var15 < 10; var15++) {
            double var16 = var10 * hash(var15, 1);
            Vec3 var18 = var3.lerp(var4, var16).add((hash(var15, 2) - 0.5) * 0.4, (hash(var15, 3) - 0.5) * 0.4, (hash(var15, 4) - 0.5) * 0.4);
            BreakerFx.sparkle(
                var0, var1, var2, var18, 0.12 * (double)var12, 0.85F, 0.97F, 1.0F, 0.7F * var12 * (float)(1.0 - clamp(var6 * 2.0 - hash(var15, 5)))
            );
        }

        if (var10 > 0.99) {
            BreakerFx.disc(var0, var1, var2, var4, 0.9, 0.75F, 0.92F, 1.0F, 0.35F * var12);
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
                    .add(var11.scale(Math.cos(var18) * 0.18 * Math.sin(Math.PI * var16)))
                    .add(var12.scale(Math.sin(var18) * 0.18 * Math.sin(Math.PI * var16)));
                BreakerFx.ribbon(var0, var1, var2, var14, var20, 0.22, 0.3F, 0.62F, 1.0F, 0.45F * var9);
                BreakerFx.ribbon(var0, var1, var2, var14, var20, 0.07, 0.8F, 0.93F, 1.0F, 0.75F * var9);
                var14 = var20;
            }
        }
    }

    private static void cut(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8) {
        double var10 = sstep(0.0, 0.1, var6);
        float var12 = env(var6, var8, 0.01, 0.3);
        Vec3 var13 = perp(var5);
        Vec3 var14 = var5.cross(var13).normalize();
        Vec3 var15 = var13.scale(0.55).add(var14.scale(0.35));
        Vec3 var16 = null;
        byte var17 = 18;

        for (int var18 = 0; var18 <= var17; var18++) {
            double var19 = (double)var18 / (double)var17;
            if (var19 > var10) {
                break;
            }

            double var21 = Math.sin(Math.PI * var19);
            Vec3 var23 = var3.lerp(var4, 0.15 + 0.85 * var19).add(var15.scale((var19 - 0.5) * 3.2)).add(var14.scale(var21 * 0.6));
            if (var16 != null) {
                double var24 = 0.02 + 0.06 * var21;
                BreakerFx.ribbon(var0, var1, var2, var16, var23, var24 * 5.0, 0.7F, 0.66F, 1.0F, 0.12F * var12);
                BreakerFx.ribbon(var0, var1, var2, var16, var23, var24, 0.95F, 0.97F, 1.0F, 0.95F * var12);
            }

            var16 = var23;
        }
    }

    private static void catastravia(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8, double var10) {
        float var12 = env(var8, var10, 0.05, 0.4);
        Vec3 var13 = var3.add(0.0, 11.0, 0.0).add(var5.scale(Math.min(10.0, var6 * 0.25)));
        ring(var0, var1, var2, var13, new Vec3(0.0, 1.0, 0.0), 3.2, 0.08, 1.0F, 0.95F, 0.75F, 0.7F * var12);
        ring(var0, var1, var2, var13, new Vec3(0.0, 1.0, 0.0), 2.6, 0.04, 1.0F, 0.95F, 0.75F, 0.55F * var12);
        planeDisc(var0, var1, var2, var13, new Vec3(0.0, 1.0, 0.0), 3.0, 1.0F, 0.95F, 0.75F, 0.12F * var12, 0.2F);
        byte var14 = 18;

        for (int var15 = 0; var15 < var14; var15++) {
            double var16 = 0.12 + 0.88 * ((double)var15 + hash(var15, 1) * 0.6) / (double)var14;
            Vec3 var18 = var3.lerp(var4, var16).add((hash(var15, 2) - 0.5) * 1.6, -0.2, (hash(var15, 3) - 0.5) * 1.6);
            Vec3 var19 = var13.add((hash(var15, 4) - 0.5) * 4.0, 0.0, (hash(var15, 5) - 0.5) * 4.0);
            double var20 = 0.08 + (double)var15 * 0.045;
            double var22 = (var8 - var20) / 0.32;
            if (!(var22 < 0.0)) {
                if (var22 < 1.0) {
                    Vec3 var24 = var19.lerp(var18, var22);
                    Vec3 var25 = var18.subtract(var19).normalize();
                    spear(var0, var1, var2, var24.subtract(var25.scale(2.2)), var24, 0.32, 1.0F, 0.92F, 0.65F, 0.35F);
                    spear(var0, var1, var2, var24.subtract(var25.scale(1.8)), var24, 0.12, 1.0F, 1.0F, 0.95F, 0.95F);
                } else {
                    double var27 = (var22 - 1.0) * 0.32;
                    float var26 = (float)clamp(1.0 - var27 / 0.45);
                    BreakerFx.sparkle(var0, var1, var2, var18, 1.3 * (double)var26, 1.0F, 0.98F, 0.85F, var26);
                    BreakerFx.disc(var0, var1, var2, var18, 0.6 + var27 * 5.0, 1.0F, 0.85F, 0.5F, 0.45F * var26);
                }
            }
        }
    }

    private static void petals(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8, double var10) {
        float var12 = env(var8, var10, 0.03, 0.3);
        Vec3 var13 = perp(var5);
        Vec3 var14 = var5.cross(var13).normalize();

        for (int var15 = 0; var15 < 46; var15++) {
            double var16 = (var8 - hash(var15, 1) * 0.25) / 0.45;
            if (!(var16 < 0.0)) {
                double var18 = Math.min(1.0, var16);
                double var20 = hash(var15, 2) * 6.28 + var18 * 9.0;
                double var22 = 0.15 + 0.5 * hash(var15, 3) * (1.0 - var18 * 0.6);
                Vec3 var24 = var3.lerp(var4, var18).add(var13.scale(Math.cos(var20) * var22)).add(var14.scale(Math.sin(var20) * var22));
                double var25 = sstep(0.12, 0.35, var16);
                float var27 = (float)(1.0 + -0.18000000000000005 * var25);
                float var28 = (float)(0.72 + 0.12 * var25);
                float var29 = (float)(0.82 + 0.06000000000000005 * var25);
                Vec3 var30 = var5.add(var13.scale(Math.cos(var20 * 2.0) * 0.4)).normalize();
                double var31 = 0.18 + 0.1 * hash(var15, 4);
                float var33 = var12 * (var16 > 1.0 ? (float)clamp(1.0 - (var16 - 1.0) * 2.0) : 1.0F);
                spear(
                    var0,
                    var1,
                    var2,
                    var24.subtract(var30.scale(var31)),
                    var24.add(var30.scale(var31)),
                    var31 * (1.1 - 0.6 * var25),
                    var27,
                    var28,
                    var29,
                    0.9F * var33
                );
                if (var25 > 0.5 && hash(var15, (int)(var8 * 10.0)) > 0.85) {
                    BreakerFx.sparkle(var0, var1, var2, var24, 0.15, 1.0F, 1.0F, 1.0F, var33);
                }
            }
        }
    }

    private static void rock(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8, double var10) {
        int var12 = (int)(var4.x * 3.0 + var4.z * 5.0 + var4.y * 7.0);
        Vec3 var13 = var3.add(perp(var5).scale((hash(var12, 1) - 0.5) * 1.6)).add(0.0, 0.6 + 0.5 * hash(var12, 2), 0.0);
        float var14 = env(var8, var10, 0.02, 0.3);
        double var15 = 0.22 + 0.12 * hash(var12, 3);
        if (var8 < 0.15) {
            Vec3 var17 = var13.add(0.0, -0.4 * (1.0 - var8 / 0.15), 0.0);
            box(var0, var1, var2, var17, var15, var8 * 6.0, var8 * 4.0, 0.52F, 0.46F, 0.4F, var14);
        } else if (var8 < 0.45) {
            double var22 = (var8 - 0.15) / 0.3;
            Vec3 var19 = var13.lerp(var4, var22).add(0.0, Math.sin(Math.PI * var22) * 0.6, 0.0);
            box(var0, var1, var2, var19, var15, var8 * 14.0, var8 * 9.0, 0.52F, 0.46F, 0.4F, var14);
            BreakerFx.ribbon(var0, var1, var2, var13.lerp(var4, Math.max(0.0, var22 - 0.25)), var19, var15 * 1.6, 0.55F, 0.48F, 0.4F, 0.18F * var14);
        } else {
            double var23 = var8 - 0.45;

            for (int var24 = 0; var24 < 7; var24++) {
                Vec3 var20 = new Vec3(hash(var12 + var24, 4) - 0.5, 0.6 + hash(var12 + var24, 5), hash(var12 + var24, 6) - 0.5).normalize();
                Vec3 var21 = var4.add(var20.scale(var23 * 4.0)).add(0.0, -4.9 * var23 * var23, 0.0);
                box(var0, var1, var2, var21, var15 * 0.35, var23 * 12.0 + (double)var24, var23 * 9.0, 0.5F, 0.44F, 0.38F, var14);
            }

            BreakerFx.disc(var0, var1, var2, var4, 0.8 + var23 * 3.0, 0.62F, 0.55F, 0.45F, 0.35F * var14);
        }
    }

    private static void goddessSpear(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8, double var10) {
        double var12 = sstep(0.0, 0.18, var8);
        float var14 = env(var8, var10, 0.02, 0.35);
        Vec3 var15 = var3.lerp(var4, var12);
        Vec3 var16 = var15.subtract(var5.scale(2.4));
        spear(var0, var1, var2, var16, var15, 0.7, 1.0F, 0.9F, 0.55F, 0.25F * var14);
        spear(var0, var1, var2, var16.add(var5.scale(0.4)), var15, 0.24, 1.0F, 0.97F, 0.85F, 0.95F * var14);
        BreakerFx.ribbon(var0, var1, var2, var3, var15, 0.05, 1.0F, 0.95F, 0.75F, 0.35F * var14);
        ring(var0, var1, var2, var3.add(var5.scale(0.2)), var5, 0.35 + var8 * 0.4, 0.035, 1.0F, 0.9F, 0.6F, 0.8F * var14);
        if (var12 > 0.99) {
            BreakerFx.sparkle(var0, var1, var2, var4, 1.0 * (double)var14, 1.0F, 0.97F, 0.85F, var14);
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
                    flowers(var0, var1, var9, var3, var12, var5, var10, var7);
                    return true;
                case 12:
                    goddess(var0, var1, var9, var3, var5, var10, var7);
                    return true;
                case 13:
                    hellfire(var0, var1, var9, var3, var12, var5, var10, var7);
                    return true;
                case 14:
                    tornado(var0, var1, var9, var3, var12, var5, var10, false);
                    return true;
                case 15:
                    strikes(var0, var1, var9, var3, var12, var5, var10);
                    return true;
                case 17:
                    float var17 = env(var5, var10, 0.02, 0.4);

                    for (int var18 = 0; var18 < 24; var18++) {
                        Vec3 var19 = new Vec3(hash(var18, 1) - 0.5, hash(var18, 2) - 0.2, hash(var18, 3) - 0.5).normalize();
                        Vec3 var16 = var3.add(var19.scale(var5 * 4.5));
                        spear(var0, var1, var9, var16.subtract(var19.scale(0.2)), var16.add(var19.scale(0.2)), 0.12, 0.84F, 0.86F, 0.9F, 0.9F * var17);
                    }

                    return true;
                case 20:
                    float var13 = env(var5, var10, 0.1, 0.5);
                    ring(
                        var0,
                        var1,
                        var9,
                        var3.add(0.0, 0.15, 0.0),
                        new Vec3(0.0, 1.0, 0.0),
                        0.45 + 0.1 * Math.sin(var7 * 4.0),
                        0.03,
                        0.7F,
                        0.95F,
                        1.0F,
                        0.6F * var13
                    );

                    for (int var14 = 0; var14 < 5; var14++) {
                        double var15 = var7 * 2.0 + (double)var14 * 1.2566;
                        BreakerFx.sparkle(
                            var0,
                            var1,
                            var9,
                            var3.add(Math.cos(var15) * 0.5, 0.2 + 0.3 * Math.sin(var7 * 3.0 + (double)var14), Math.sin(var15) * 0.5),
                            0.08,
                            0.8F,
                            1.0F,
                            1.0F,
                            0.8F * var13
                        );
                    }

                    return true;
                case 21:
                    tornado(var0, var1, var9, var3, var12, var5, var10, true);
                    return true;
            }
        }
    }

    private static void waterSphere(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7) {
        double var9 = 1.1 + 0.35 * (double)var4;
        Vec3 var11 = var3.add(0.0, 3.2 + var9, 0.0);
        float var12 = env(var5, var7, 0.05, 0.5);
        if (var5 < 0.75) {
            double var13 = sstep(0.0, 0.4, var5);
            double var15 = sstep(0.45, 0.75, var5);
            Vec3 var17 = var11.lerp(var3.add(0.0, var9 * 0.6, 0.0), var15 * var15);
            double var18 = var9 * var13 * (1.0 + 0.06 * Math.sin(var5 * 30.0));
            BreakerFx.disc(var0, var1, var2, var17, var18 * 1.15, 0.22F, 0.5F, 0.95F, 0.55F * var12);
            BreakerFx.disc(var0, var1, var2, var17, var18 * 0.85, 0.35F, 0.66F, 1.0F, 0.35F * var12);
            Vec3 var20 = var17.subtract(var2).normalize();
            ring(var0, var1, var2, var17, var20, var18, 0.07, 0.75F, 0.92F, 1.0F, 0.7F * var12);
            BreakerFx.disc(var0, var1, var2, var17.add(-var18 * 0.35, var18 * 0.35, 0.0), var18 * 0.18, 1.0F, 1.0F, 1.0F, 0.6F * var12);

            for (int var21 = 0; var21 < 14; var21++) {
                double var22 = (double)var21 * 0.449 + var5 * 4.0;
                Vec3 var24 = var17.add(Math.cos(var22) * var18 * (2.0 - var13), Math.sin((double)var21 * 1.3) * var18, Math.sin(var22) * var18 * (2.0 - var13));
                BreakerFx.disc(var0, var1, var2, var24, 0.09, 0.6F, 0.85F, 1.0F, 0.6F * (float)(1.0 - var13) * var12);
            }
        } else {
            double var25 = var5 - 0.75;
            ring(var0, var1, var2, var3.add(0.0, 0.1, 0.0), new Vec3(0.0, 1.0, 0.0), var9 + var25 * 6.0, 0.25, 0.55F, 0.82F, 1.0F, 0.55F * var12);
            ring(var0, var1, var2, var3.add(0.0, 0.1, 0.0), new Vec3(0.0, 1.0, 0.0), var9 * 0.6 + var25 * 4.0, 0.12, 0.85F, 0.95F, 1.0F, 0.5F * var12);

            for (int var26 = 0; var26 < 30; var26++) {
                Vec3 var16 = new Vec3(Math.cos((double)var26 * 0.21), 0.0, Math.sin((double)var26 * 0.21));
                double var27 = 2.5 + 3.0 * hash(var26, 1);
                Vec3 var19 = var3.add(var16.scale(var9 * 0.6 + var25 * var27)).add(0.0, var25 * (5.0 + 3.0 * hash(var26, 2)) - 9.8 * var25 * var25 * 0.6, 0.0);
                if (!(var19.y < var3.y)) {
                    BreakerFx.disc(var0, var1, var2, var19, 0.12, 0.55F, 0.82F, 1.0F, 0.7F * var12);
                }
            }
        }
    }

    private static void spikes(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7) {
        int var9 = 8 + 3 * var4;
        double var10 = 2.4 + (double)var4;

        for (int var12 = 0; var12 < var9; var12++) {
            double var13 = hash(var12, 1) * 6.283;
            double var15 = 0.6 + var10 * Math.sqrt(hash(var12, 2));
            Vec3 var17 = var3.add(Math.cos(var13) * var15, 0.0, Math.sin(var13) * var15);
            double var18 = var15 / var10 * 0.25;
            double var20 = sstep(var18, var18 + 0.12, var5) * (1.0 - sstep(var7 - 0.6, var7, var5));
            if (!(var20 <= 0.01)) {
                double var22 = (1.0 + 1.6 * hash(var12, 3)) * var20;
                double var24 = 0.3 + 0.25 * hash(var12, 4);
                Vec3 var26 = var17.add((hash(var12, 5) - 0.5) * 0.5, var22, (hash(var12, 6) - 0.5) * 0.5);
                Vec3[] var27 = new Vec3[]{var17.add(var24, 0.0, 0.0), var17.add(0.0, 0.0, var24), var17.add(-var24, 0.0, 0.0), var17.add(0.0, 0.0, -var24)};

                for (int var28 = 0; var28 < 4; var28++) {
                    float var29 = var28 % 2 == 0 ? 0.55F : 0.42F;
                    v(var0, var1, var2, var26, 0.64F * var29 * 1.5F, 0.57F * var29 * 1.5F, 0.48F * var29 * 1.5F, 1.0F);
                    v(var0, var1, var2, var27[var28], 0.52F * var29, 0.44F * var29, 0.36F * var29, 1.0F);
                    v(var0, var1, var2, var27[(var28 + 1) % 4], 0.52F * var29, 0.44F * var29, 0.36F * var29, 1.0F);
                    v(var0, var1, var2, var27[(var28 + 1) % 4], 0.52F * var29, 0.44F * var29, 0.36F * var29, 1.0F);
                }
            }
        }

        float var30 = env(var5, var7, 0.05, 1.0);
        planeDisc(var0, var1, var2, var3.add(0.0, 0.05, 0.0), new Vec3(0.0, 1.0, 0.0), var10 + 1.0 + var5, 0.55F, 0.47F, 0.38F, 0.28F * var30, 0.0F);
        ring(
            var0,
            var1,
            var2,
            var3.add(0.0, 0.06, 0.0),
            new Vec3(0.0, 1.0, 0.0),
            0.5 + var5 * 6.0,
            0.3,
            0.6F,
            0.52F,
            0.42F,
            0.3F * (float)clamp(1.0 - var5 * 1.5)
        );
    }

    private static void jilwer(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6) {
        float var8 = env(var4, var6, 0.02, 0.5);
        ring(var0, var1, var2, var3.add(0.0, 0.05, 0.0), new Vec3(0.0, 1.0, 0.0), 0.4 + var4 * 5.0, 0.12, 0.85F, 1.0F, 0.9F, 0.5F * var8);

        for (int var9 = 0; var9 < 16; var9++) {
            double var10 = hash(var9, 1) * 6.283;
            double var12 = 0.2 + 1.6 * hash(var9, 2);
            Vec3 var14 = new Vec3(Math.cos(var10), 0.0, Math.sin(var10));
            Vec3 var15 = var3.add(var14.scale(0.4 + var4 * 3.0)).add(0.0, var12, 0.0);
            BreakerFx.ribbon(var0, var1, var2, var15, var15.add(var14.scale(0.9 + hash(var9, 3))), 0.04, 0.9F, 1.0F, 0.95F, 0.7F * var8);
        }
    }

    private static void bind(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7, double var9) {
        float var11 = env(var5, var7, 0.2, 0.8);

        for (int var12 = 0; var12 < 3; var12++) {
            double var13 = 0.3 + 0.7 * (double)var12;
            Vec3 var15 = new Vec3(Math.sin(var9 * 0.7 + (double)var12) * 0.25, 1.0, Math.cos(var9 * 0.6 + (double)var12) * 0.25).normalize();
            double var16 = 0.75 + 0.1 * Math.sin(var9 * 2.0 + (double)var12);
            ring(var0, var1, var2, var3.add(0.0, var13, 0.0), var15, var16, 0.05, 0.92F, 1.0F, 0.62F, 0.8F * var11);
            ring(var0, var1, var2, var3.add(0.0, var13, 0.0), var15, var16 * 1.12, 0.02, 0.92F, 1.0F, 0.62F, 0.5F * var11);

            for (int var18 = 0; var18 < 8; var18++) {
                double var19 = var9 * (var12 % 2 == 0 ? 1.2 : -1.2) + (double)var18 * 0.785;
                Vec3 var21 = var3.add(Math.cos(var19) * var16 * 1.06, var13, Math.sin(var19) * var16 * 1.06);
                BreakerFx.sparkle(var0, var1, var2, var21, 0.06, 1.0F, 1.0F, 0.8F, 0.8F * var11);
            }
        }

        for (int var22 = 0; var22 < 4; var22++) {
            double var23 = (double)var22 * 1.5708 + var9 * 0.3;
            Vec3 var24 = var3.add(Math.cos(var23) * 0.95, 0.0, Math.sin(var23) * 0.95);
            BreakerFx.ribbon(var0, var1, var2, var24, var24.add(0.0, 2.2, 0.0), 0.03, 0.92F, 1.0F, 0.62F, 0.45F * var11);
        }
    }

    private static void gold(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, double var8) {
        float var10 = env(var4, var6, 0.1, 1.5);
        double var11 = Math.min(2.0, var4 * 1.1);
        ring(var0, var1, var2, var3.add(0.0, var11, 0.0), new Vec3(0.0, 1.0, 0.0), 0.55, 0.06, 1.0F, 0.85F, 0.35F, 0.75F * var10 * (float)clamp(2.0 - var11));

        for (int var13 = 0; var13 < 26; var13++) {
            double var14 = hash(var13, 1) * var11;
            double var16 = hash(var13, 2) * 6.283;
            Vec3 var18 = var3.add(Math.cos(var16) * 0.42, var14, Math.sin(var16) * 0.42);
            double var19 = 0.5 + 0.5 * Math.sin(var8 * 6.0 + (double)var13 * 1.7);
            BreakerFx.disc(var0, var1, var2, var18, 0.12, 0.83F, 0.69F, 0.22F, 0.65F * var10);
            if (var19 > 0.85) {
                BreakerFx.sparkle(var0, var1, var2, var18, 0.2, 1.0F, 0.95F, 0.6F, var10);
            }
        }
    }

    private static void flowers(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7, double var9) {
        double var11 = 2.5 + 1.6 * (double)var4;
        float var13 = (float)clamp((var7 - var5) / 3.0);
        ring(
            var0,
            var1,
            var2,
            var3.add(0.0, 0.06, 0.0),
            new Vec3(0.0, 1.0, 0.0),
            Math.min(var11, var5 * var11 / 1.5),
            0.15,
            1.0F,
            0.95F,
            0.8F,
            0.4F * (float)clamp(1.8 - var5)
        );
        float[][] var14 = new float[][]{
            {1.0F, 1.0F, 1.0F}, {1.0F, 0.72F, 0.82F}, {1.0F, 0.92F, 0.45F}, {0.72F, 0.82F, 1.0F}, {0.86F, 0.72F, 1.0F}, {1.0F, 0.6F, 0.6F}
        };
        int var15 = 30 + 25 * var4;
        Vec3 var16 = new Vec3(0.0, 1.0, 0.0);

        for (int var17 = 0; var17 < var15; var17++) {
            double var18 = (double)var17 * 2.39996;
            double var20 = var11 * Math.sqrt(((double)var17 + 0.5) / (double)var15);
            double var22 = var20 / var11 * 1.5;
            double var24 = sstep(var22, var22 + 0.5, var5);
            if (!(var24 <= 0.01)) {
                Vec3 var26 = var3.add(Math.cos(var18) * var20, 0.08 + 0.1 * hash(var17, 1), Math.sin(var18) * var20);
                float[] var27 = var14[(int)(hash(var17, 2) * (double)var14.length) % var14.length];
                double var28 = (0.07 + 0.06 * hash(var17, 3)) * var24;
                double var30 = Math.sin(var9 * 1.5 + (double)var17) * 0.03;

                for (int var32 = 0; var32 < 5; var32++) {
                    double var33 = (double)var32 * 1.2566 + hash(var17, 4) * 6.28;
                    Vec3 var35 = var26.add(Math.cos(var33) * var28 * 1.1 + var30, 0.0, Math.sin(var33) * var28 * 1.1);
                    planeDisc(var0, var1, var2, var35, var16, var28, var27[0], var27[1], var27[2], 0.95F * var13, 0.8F, 6);
                }

                planeDisc(var0, var1, var2, var26.add(var30, 0.005, 0.0), var16, var28 * 0.55, 1.0F, 0.85F, 0.3F, var13, 1.0F, 5);
            }
        }
    }

    private static void goddess(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, double var8) {
        float var10 = env(var4, var6, 0.15, 0.8);
        Vec3 var11 = new Vec3(0.0, 1.0, 0.0);
        Vec3 var12 = var3.add(0.0, 0.05, 0.0);
        double var13 = 1.3 + 0.3 * sstep(0.0, 0.4, var4);
        ring(var0, var1, var2, var12, var11, var13, 0.06, 1.0F, 0.92F, 0.62F, 0.85F * var10);
        ring(var0, var1, var2, var12, var11, var13 * 0.86, 0.03, 1.0F, 0.92F, 0.62F, 0.7F * var10);

        for (int var15 = 0; var15 < 24; var15++) {
            double var16 = var8 * 0.6 + (double)var15 * 0.2618;
            Vec3 var18 = var12.add(Math.cos(var16) * var13 * 0.93, 0.0, Math.sin(var16) * var13 * 0.93);
            flat(
                var0,
                var1,
                var2,
                var18,
                var18.add(Math.cos(var16 + 1.57) * 0.12, 0.0, Math.sin(var16 + 1.57) * 0.12),
                var11,
                0.06,
                1.0F,
                0.95F,
                0.7F,
                0.7F * var10
            );
        }

        planeDisc(var0, var1, var2, var12, var11, var13, 1.0F, 0.9F, 0.6F, 0.18F * var10, 0.3F);

        for (int var23 = 0; var23 < 2; var23++) {
            Vec3 var25 = var3.add(0.0, 1.0, 0.0).subtract(var2);
            Vec3 var17 = var25.cross(var11).normalize().scale((var23 == 0 ? 0.55 : 0.25) * var13);
            BreakerFx.ribbon(
                var0,
                var1,
                var2,
                var12,
                var12.add(0.0, 3.0, 0.0),
                var23 == 0 ? 1.4 * var13 : 0.6 * var13,
                1.0F,
                0.93F,
                0.7F,
                (var23 == 0 ? 0.08F : 0.12F) * var10
            );
        }

        for (int var24 = 0; var24 < 18; var24++) {
            double var26 = (var4 * 0.8 + hash(var24, 1)) % 1.0;
            double var27 = hash(var24, 2) * 6.283;
            double var20 = var13 * (0.3 + 0.6 * hash(var24, 3));
            Vec3 var22 = var3.add(Math.cos(var27) * var20, 0.2 + var26 * 2.4, Math.sin(var27) * var20);
            BreakerFx.sparkle(var0, var1, var2, var22, 0.08 + 0.05 * hash(var24, 4), 1.0F, 0.96F, 0.8F, (float)Math.sin(var26 * Math.PI) * var10);
        }
    }

    private static void hellfire(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7, double var9) {
        int var11 = 4 + 2 * var4;
        double var12 = 1.2 + (double)var4;
        float var14 = env(var5, var7, 0.05, 0.8);
        planeDisc(var0, var1, var2, var3.add(0.0, 0.05, 0.0), new Vec3(0.0, 1.0, 0.0), var12 + 1.2, 1.0F, 0.35F, 0.08F, 0.45F * var14, 0.0F);

        for (int var15 = 0; var15 < var11; var15++) {
            double var16 = hash(var15, 1) * 6.283;
            double var18 = var12 * Math.sqrt(hash(var15, 2));
            Vec3 var20 = var3.add(Math.cos(var16) * var18, 0.0, Math.sin(var16) * var18);
            double var21 = 0.05 * (double)var15;
            double var23 = sstep(var21, var21 + 0.25, var5);
            if (!(var23 <= 0.0)) {
                double var25 = (3.0 + 3.0 * hash(var15, 3)) * var23 * (1.0 - 0.4 * sstep(var7 - 0.8, var7, var5));

                for (int var27 = 0; var27 < 6; var27++) {
                    double var28 = var25 * (double)var27 / 6.0;
                    double var30 = var25 * (double)(var27 + 1) / 6.0;
                    double var32 = Math.sin(var9 * 14.0 + (double)var27 * 1.7 + (double)var15) * 0.12;
                    Vec3 var34 = var20.add(var32, var28, -var32);
                    Vec3 var35 = var20.add(-var32, var30, var32);
                    double var36 = (1.25 - 0.7 * (double)var27 / 6.0) * (0.9 + 0.2 * hash(var15, 4));
                    BreakerFx.ribbon(var0, var1, var2, var34, var35, var36 * 1.5, 0.55F, 0.07F, 0.0F, 0.35F * var14);
                    BreakerFx.ribbon(var0, var1, var2, var34, var35, var36, 1.0F, 0.34F, 0.08F, 0.6F * var14);
                    BreakerFx.ribbon(var0, var1, var2, var34, var35, var36 * 0.45, 1.0F, 0.78F, 0.25F, 0.85F * var14);
                }

                for (int var38 = 0; var38 < 6; var38++) {
                    double var39 = (var5 * 1.4 + hash(var15 * 7 + var38, 5)) % 1.0;
                    Vec3 var40 = var20.add((hash(var15 * 7 + var38, 6) - 0.5) * 1.2, var39 * (var25 + 1.5), (hash(var15 * 7 + var38, 7) - 0.5) * 1.2);
                    BreakerFx.disc(var0, var1, var2, var40, 0.07, 1.0F, 0.65F, 0.2F, (float)(1.0 - var39) * var14);
                }
            }
        }
    }

    private static void tornado(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7, boolean var9) {
        float var10 = env(var5, var7, 0.4, 0.8);
        double var11 = 6.0 + 1.5 * (double)var4;
        byte var13 = 14;

        for (int var14 = 0; var14 < var13; var14++) {
            Vec3 var15 = null;

            for (int var16 = 0; var16 <= 22; var16++) {
                double var17 = var11 * (double)var16 / 22.0;
                double var19 = 0.5 + var17 * (0.32 + 0.04 * (double)var4);
                double var21 = (double)var14 * 6.283 / (double)var13 + var5 * (var9 ? 4.5 : 3.6) + var17 * 0.45;
                Vec3 var23 = var3.add(Math.cos(var21) * var19, var17, Math.sin(var21) * var19);
                if (var15 != null) {
                    float var24 = var10 * (float)(0.35 + 0.65 * Math.sin(Math.PI * (double)var16 / 22.0));
                    if (var9) {
                        BreakerFx.ribbon(var0, var1, var2, var15, var23, 0.55, 0.85F, 0.18F, 0.02F, 0.3F * var24);
                        BreakerFx.ribbon(var0, var1, var2, var15, var23, 0.22, 1.0F, 0.62F, 0.15F, 0.65F * var24);
                    } else {
                        BreakerFx.ribbon(var0, var1, var2, var15, var23, 0.35, 0.8F, 0.92F, 0.88F, 0.14F * var24);
                        BreakerFx.ribbon(var0, var1, var2, var15, var23, 0.08, 0.94F, 1.0F, 0.97F, 0.45F * var24);
                    }
                }

                var15 = var23;
            }
        }

        planeDisc(
            var0,
            var1,
            var2,
            var3.add(0.0, 0.05, 0.0),
            new Vec3(0.0, 1.0, 0.0),
            1.5 + 0.5 * (double)var4,
            var9 ? 1.0F : 0.7F,
            var9 ? 0.45F : 0.8F,
            var9 ? 0.1F : 0.7F,
            0.3F * var10,
            0.0F
        );

        for (int var25 = 0; var25 < 24; var25++) {
            double var26 = (var5 * 0.7 + hash(var25, 1)) % 1.0;
            double var27 = var26 * var11;
            double var28 = 0.5 + var27 * 0.36;
            double var29 = hash(var25, 2) * 6.28 + var5 * 4.0 + var27 * 0.5;
            Vec3 var30 = var3.add(Math.cos(var29) * var28, var27, Math.sin(var29) * var28);
            if (var9) {
                BreakerFx.disc(var0, var1, var2, var30, 0.09, 1.0F, 0.7F, 0.2F, (float)(1.0 - var26) * var10);
            } else {
                BreakerFx.ribbon(var0, var1, var2, var30, var30.add(-Math.sin(var29) * 0.4, 0.1, Math.cos(var29) * 0.4), 0.05, 0.75F, 0.85F, 0.6F, 0.6F * var10);
            }
        }
    }

    private static void strikes(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7) {
        int var9 = 4 + 2 * var4;

        for (int var10 = 0; var10 < var9; var10++) {
            double var11 = hash(var10, 1) * 0.6;
            double var13 = var5 - var11;
            if (!(var13 < 0.0) && !(var13 > 0.35)) {
                float var15 = (float)(clamp(1.0 - var13 / 0.35) * (0.6 + 0.4 * hash(var10, (int)(var5 * 30.0))));
                double var16 = hash(var10, 2) * 6.283;
                double var18 = (1.5 + (double)var4) * Math.sqrt(hash(var10, 3));
                Vec3 var20 = var3.add(Math.cos(var16) * var18, 0.0, Math.sin(var16) * var18);
                bolt(var0, var1, var2, var20.add((hash(var10, 4) - 0.5) * 3.0, 14.0, (hash(var10, 5) - 0.5) * 3.0), var20, var5, var10 * 13 + 5, 1.1, var15);
                BreakerFx.disc(var0, var1, var2, var20.add(0.0, 0.2, 0.0), 1.6, 0.68F, 0.42F, 1.0F, 0.55F * var15);
                BreakerFx.sparkle(var0, var1, var2, var20.add(0.0, 0.3, 0.0), 0.9, 1.0F, 1.0F, 1.0F, var15);
            }
        }
    }

    public static boolean aura(VertexConsumer var0, Matrix4f var1, Vec3 var2, double var3, double var5, double var7, float var9) {
        if (var5 > 0.5) {
            return true;
        } else {
            Vec3 var10 = ArcanaClient.camera();
            if (var10 == null) {
                return true;
            } else {
                double var11 = var7;
                double var13 = 0.9 + var3 * 0.45;
                double var15 = 2.1 + var3 * 0.35;
                Vec3 var17 = new Vec3(0.0, 1.0, 0.0);

                for (int var18 = 0; var18 < 9; var18++) {
                    double var19 = (double)(var18 - 4) / 4.0;
                    double var21 = var15 * (0.75 + 0.25 * Math.sin(var11 * 3.0 + (double)var18 * 1.9));
                    double var23 = Math.sin(var11 * 2.3 + (double)var18) * 0.08;
                    Vec3 var25 = var2.subtract(var10);
                    Vec3 var26 = var25.cross(var17);
                    if (!(var26.lengthSqr() < 1.0E-9)) {
                        var26 = var26.normalize();
                        Vec3 var27 = var2.add(var26.scale(var19 * var13 * 0.5));
                        BreakerFx.ribbon(var0, var1, var10, var27, var27.add(var26.scale(var23)).add(0.0, var21, 0.0), var13 * 0.35, 0.55F, 0.75F, 1.0F, 0.07F);
                    }
                }

                BreakerFx.disc(var0, var1, var10, var2.add(0.0, 1.0, 0.0), var13 * 0.9, 0.6F, 0.8F, 1.0F, 0.12F);
                return true;
            }
        }
    }
}
