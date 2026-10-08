package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class SpellFx {
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
    static final CircleArt.Look ZOLTRAAK = new CircleArt.Look(CircleArt.Design.ATTACK, 0.97F, 0.96F, 1.0F, 0.68F, 0.58F, 1.0F);
    static final CircleArt.Look ZOLTRAAK_HEAVY = new CircleArt.Look(CircleArt.Design.HEAVY, 1.0F, 0.97F, 0.92F, 0.95F, 0.78F, 0.5F);
    private static VertexConsumer CUR_VC;
    private static Matrix4f CUR_M;
    static final CircleArt.Look CATASTRAVIA = new CircleArt.Look(CircleArt.Design.ARROWS, 1.0F, 0.98F, 0.9F, 1.0F, 0.82F, 0.42F);
    static final CircleArt.Look HOLY = new CircleArt.Look(CircleArt.Design.HOLY, 1.0F, 0.98F, 0.86F, 1.0F, 0.84F, 0.45F);
    static final CircleArt.Look FIRE_C = new CircleArt.Look(CircleArt.Design.FIRE, 1.0F, 0.93F, 0.8F, 1.0F, 0.42F, 0.1F);
    static final CircleArt.Look EARTH_C = new CircleArt.Look(CircleArt.Design.EARTH, 1.0F, 0.93F, 0.8F, 0.82F, 0.58F, 0.3F);
    static final CircleArt.Look WATER_C = new CircleArt.Look(CircleArt.Design.WATER, 0.9F, 0.96F, 1.0F, 0.25F, 0.55F, 1.0F);
    static final CircleArt.Look WIND_C = new CircleArt.Look(CircleArt.Design.WIND, 0.93F, 1.0F, 0.96F, 0.42F, 0.95F, 0.72F);
    static final CircleArt.Look BIND_C = new CircleArt.Look(CircleArt.Design.BIND, 1.0F, 1.0F, 0.86F, 0.82F, 0.88F, 0.42F);
    static final CircleArt.Look GOLD_C = new CircleArt.Look(CircleArt.Design.GOLD, 1.0F, 0.95F, 0.75F, 1.0F, 0.78F, 0.25F);
    static final CircleArt.Look LIGHTNING_C = new CircleArt.Look(CircleArt.Design.LIGHTNING, 0.96F, 0.92F, 1.0F, 0.58F, 0.32F, 1.0F);
    static final CircleArt.Look GOLEM_C = new CircleArt.Look(CircleArt.Design.GOLEM, 0.92F, 0.86F, 1.0F, 0.55F, 0.36F, 0.88F);
    static final CircleArt.Look SHIELD_C = new CircleArt.Look(CircleArt.Design.BARRIER, 0.92F, 0.97F, 1.0F, 0.45F, 0.75F, 1.0F);
    static final double BH_FORM = 1.0;
    static final double BH_SPEED = 12.0;
    static final double BH_HOLD = 2.4;
    static final double BH_COLLAPSE = 0.8;

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
                return 4.4;
            case 4:
                return 1.2;
            case 5:
                return 0.9;
            case 6:
                return 0.6;
            case 7:
                return 6.8;
            case 8:
                return 0.7;
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
            case 26:
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
            case 24:
                return 5.2;
            case 25:
                return 1.4;
            case 27:
                return 0.8;
            case 28:
                return 2.6;
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

    static void tube(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3[] var3, double var4, float var6, float var7, float var8, float var9) {
        if (!(var9 <= 0.003F) && !(var4 <= 0.0) && var3.length >= 2) {
            Vec3 var10 = null;
            Vec3[] var11 = null;

            for (int var12 = 0; var12 < var3.length; var12++) {
                Vec3 var13 = (var12 + 1 < var3.length ? var3[var12 + 1] : var3[var12]).subtract(var12 > 0 ? var3[var12 - 1] : var3[var12]);
                if (!(var13.lengthSqr() < 1.0E-10)) {
                    var13 = var13.normalize();
                    Vec3 var14 = var10 == null ? perp(var13) : var10.subtract(var13.scale(var10.dot(var13))).normalize();
                    if (var14.lengthSqr() < 1.0E-6) {
                        var14 = perp(var13);
                    }

                    Vec3 var15 = var13.cross(var14).normalize();
                    var10 = var14;
                    Vec3[] var16 = new Vec3[12];

                    for (int var17 = 0; var17 < 12; var17++) {
                        double var18 = (Math.PI * 2) * (double)var17 / 12.0;
                        var16[var17] = var3[var12].add(var14.scale(Math.cos(var18) * var4)).add(var15.scale(Math.sin(var18) * var4));
                    }

                    if (var11 != null) {
                        for (int var23 = 0; var23 < 12; var23++) {
                            Vec3 var24 = var11[var23];
                            Vec3 var19 = var11[(var23 + 1) % 12];
                            Vec3 var20 = var16[(var23 + 1) % 12];
                            Vec3 var21 = var16[var23];
                            var0.addVertex(var1, (float)(var24.x - var2.x), (float)(var24.y - var2.y), (float)(var24.z - var2.z))
                                .setColor(var6, var7, var8, var9);
                            var0.addVertex(var1, (float)(var19.x - var2.x), (float)(var19.y - var2.y), (float)(var19.z - var2.z))
                                .setColor(var6, var7, var8, var9);
                            var0.addVertex(var1, (float)(var20.x - var2.x), (float)(var20.y - var2.y), (float)(var20.z - var2.z))
                                .setColor(var6, var7, var8, var9);
                            var0.addVertex(var1, (float)(var21.x - var2.x), (float)(var21.y - var2.y), (float)(var21.z - var2.z))
                                .setColor(var6, var7, var8, var9);
                        }
                    }

                    var11 = var16;
                }
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

    static void circle(
        VertexConsumer var0,
        Matrix4f var1,
        Vec3 var2,
        Vec3 var3,
        Vec3 var4,
        double var5,
        CircleArt.Look var7,
        double var8,
        double var10,
        float var12,
        int var13
    ) {
        if (!(var12 <= 0.004F) && !(var5 <= 0.0)) {
            SpellCircleFx.Ctx var14 = new SpellCircleFx.Ctx();
            var14.vc = var0;
            var14.m = var1;
            var14.camX = var2.x;
            var14.camY = var2.y;
            var14.camZ = var2.z;
            Vec3 var15 = perp(var4);
            Vec3 var16 = var4.cross(var15).normalize();
            var14.cx = var3.x;
            var14.cy = var3.y;
            var14.cz = var3.z;
            var14.ux = var15.x;
            var14.uy = var15.y;
            var14.uz = var15.z;
            var14.vx = var16.x;
            var14.vy = var16.y;
            var14.vz = var16.z;
            CircleArt.draw(var14, var7, var5, var8, var10, var12, var13);
        }
    }

    private static void castCircle(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, double var7, float var9, float var10, float var11, float var12
    ) {
        boolean var13 = var5 > 1.0;
        circle(
            var0,
            var1,
            var2,
            var3,
            var4,
            var5,
            var13 ? ZOLTRAAK_HEAVY : ZOLTRAAK,
            var7,
            Math.min(1.0, var7 / (var13 ? 0.3 : 0.12)),
            var12,
            (int)(var3.x * 7.0 + var3.z * 13.0)
        );
        PixelFx.sprite(var3, var5 * 2.4, 4, 0.72F, 0.66F, 1.0F, 0.3F * var12);
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

    static void shock(Vec3 var0, Vec3 var1, double var2, float var4, float var5, float var6, float var7) {
        Vec3 var8 = ArcanaClient.camera();
        VertexConsumer var9 = CUR_VC;
        if (var8 != null && var9 != null && !(var7 <= 0.004F) && !(var2 <= 0.01)) {
            if (Math.abs(var5 - 0.9F) < 0.006F) {
                var5 = 0.915F;
            }

            Matrix4f var10 = CUR_M;
            Vec3 var11 = var1 == null ? var8.subtract(var0).normalize() : var1;
            Vec3 var12 = perp(var11);
            Vec3 var13 = var11.cross(var12).normalize();
            double var14 = Math.max(0.12, var2 * 0.22);
            double var16 = Math.max(0.0, var2 - var14);
            int var18 = Math.max(28, Math.min(96, (int)(var2 * 20.0)));

            for (int var19 = 0; var19 < var18; var19++) {
                double var20 = (Math.PI * 2) * (double)var19 / (double)var18;
                double var22 = (Math.PI * 2) * (double)(var19 + 1) / (double)var18;
                Vec3 var24 = var12.scale(Math.cos(var20)).add(var13.scale(Math.sin(var20)));
                Vec3 var25 = var12.scale(Math.cos(var22)).add(var13.scale(Math.sin(var22)));
                Vec3 var26 = var0.add(var24.scale(var16));
                Vec3 var27 = var0.add(var25.scale(var16));
                Vec3 var28 = var0.add(var24.scale(var2));
                Vec3 var29 = var0.add(var25.scale(var2));
                Vec3 var30 = var0.add(var24.scale(var2 + var14 * 0.18));
                Vec3 var31 = var0.add(var25.scale(var2 + var14 * 0.18));
                v(var9, var10, var8, var26, var4, var5, var6, 0.0F);
                v(var9, var10, var8, var27, var4, var5, var6, 0.0F);
                v(var9, var10, var8, var29, var4, var5, var6, var7 * 0.55F);
                v(var9, var10, var8, var28, var4, var5, var6, var7 * 0.55F);
                v(var9, var10, var8, var28, var4, var5, var6, var7 * 0.55F);
                v(var9, var10, var8, var29, var4, var5, var6, var7 * 0.55F);
                v(var9, var10, var8, var31, Math.min(1.0F, var4 + 0.25F), Math.min(1.0F, var5 + 0.25F), Math.min(1.0F, var6 + 0.25F), 0.0F);
                v(var9, var10, var8, var30, Math.min(1.0F, var4 + 0.25F), Math.min(1.0F, var5 + 0.25F), Math.min(1.0F, var6 + 0.25F), 0.0F);
            }

            ring(
                var9,
                var10,
                var8,
                var0,
                var11,
                var2,
                Math.max(0.025, var14 * 0.12),
                Math.min(1.0F, var4 + 0.2F),
                Math.min(1.0F, var5 + 0.2F),
                Math.min(1.0F, var6 + 0.2F),
                var7 * 0.85F
            );
        }
    }

    private static void v(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, float var4, float var5, float var6, float var7) {
        var0.addVertex(var1, (float)(var3.x - var2.x), (float)(var3.y - var2.y), (float)(var3.z - var2.z)).setColor(var4, var5, var6, var7);
    }

    private static void burst(Vec3 var0, double var1, double var3, double var5, float var7, float var8, float var9, float var10) {
        double var11 = clamp(var3 / var5);
        if (!(var11 >= 1.0)) {
            PixelFx.sprite(
                var0, Math.min(2.4, var1 * (0.6 + 0.8 * var11)), 48 + Math.min(3, (int)(var11 * 4.0)), var7, var8, var9, var10 * (float)(1.0 - var11 * 0.6)
            );
            PixelFx.sprite(var0, var1 * 0.9 * (1.0 - var11 * 0.5), 4, var7, var8, var9, 0.45F * var10 * (float)(1.0 - var11));
            shock(var0, null, var1 * (0.7 + 1.3 * var11) * 0.5, var7, var8, var9, var10 * (float)(1.0 - var11));
        }
    }

    public static boolean beam(VertexConsumer var0, Matrix4f var1, int var2, Vec3 var3, Vec3 var4, double var5) {
        if (var2 == 3) {
            return false;
        } else {
            CUR_VC = var0;
            CUR_M = var1;
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
                            fern(var0, var1, var7, handStart(var3, var11), var4, var11, var5, var12);
                            return true;
                        case 3:
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
                        case 6:
                            fern(var0, var1, var7, var3, var4, var11, Math.min(var5, 0.45), 0.45);
                            float var31 = env(var5, 0.6, 0.02, 0.35);
                            castCircle(var0, var1, var7, var3, var11, 0.42, var5, 0.9F, 0.94F, 1.0F, var31);
                            PixelFx.sprite(var3, 0.9, 4, 0.75F, 0.82F, 1.0F, 0.6F * var31);
                            PixelFx.sprite(var3, 0.5, 48 + Math.min(3, (int)(var5 * 12.0)), 0.95F, 0.97F, 1.0F, var31 * (float)clamp(1.0 - var5 * 3.0));
                            return true;
                        case 7:
                            blackHoleShot(var0, var1, var7, var3, var4, var5);
                            return true;
                        case 8:
                            float var30 = env(var5, var12, 0.02, 0.4);
                            Vec3 var32 = handStart(var3, var11);
                            Vec3 var33 = perp(var11);
                            Vec3 var34 = var11.cross(var33).normalize();

                            for (int var18 = 0; var18 < 12; var18++) {
                                double var19 = hash(var18, 1) * 6.283;
                                double var21 = 0.2 + 0.9 * hash(var18, 2);
                                double var23 = clamp(var5 * 2.2 - hash(var18, 3) * 0.3);
                                double var25 = clamp(var23 + 0.35);
                                Vec3 var27 = var33.scale(Math.cos(var19) * var21).add(var34.scale(Math.sin(var19) * var21));
                                Vec3 var28 = var32.lerp(var4, var23).add(var27.scale(1.0 + var23 * 4.0));
                                Vec3 var29 = var32.lerp(var4, var25).add(var27.scale(1.0 + var25 * 4.0));
                                PixelFx.streak(var28, var29, 0.5, 40, 1.0F, 1.0F, 1.0F, 0.9F * var30);
                            }

                            shock(var32.add(var11.scale(0.8)), null, (1.6 + var5 * 6.0) * 0.5, 0.95F, 0.97F, 1.0F, 0.7F * (float)clamp(1.0 - var5 * 1.5));
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
                                    var16 == 0 ? 0.1 : 0.06
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

        tube(var0, var1, var2, var23, 0.3 * var16 * var14, 0.66F, 0.76F, 1.0F, 0.16F * var13);
        tube(var0, var1, var2, var23, 0.15 * var16 * var14, 0.86F, 0.92F, 1.0F, 0.45F * var13);
        tube(var0, var1, var2, var23, 0.065 * var16 * var14, 1.0F, 1.0F, 1.0F, var13);
        Vec3 var40 = perp(var5);
        Vec3 var41 = var5.cross(var40).normalize();

        for (int var26 = 0; var26 < 2; var26++) {
            Vec3 var44 = null;
            int var28 = Math.max(8, (int)(var3.distanceTo(var23[var22]) * 3.0));

            for (int var29 = 0; var29 <= var28; var29++) {
                double var30 = (double)var29 / (double)var28;
                double var32 = var30 * var3.distanceTo(var23[var22]) * 1.6 - var6 * 18.0 + (double)var26 * Math.PI;
                Vec3 var34 = var23[Math.min(var22, (int)Math.round(var30 * (double)var22))];
                double var35 = 0.26 * var16 * var14 * (0.7 + 0.3 * Math.sin(var30 * 9.0 + var6 * 5.0));
                Vec3 var37 = var34.add(var40.scale(Math.cos(var32) * var35)).add(var41.scale(Math.sin(var32) * var35));
                if (var44 != null) {
                    line(var0, var1, var2, var44, var37, 0.018 * var16, 0.86F, 0.8F, 1.0F, 0.55F * var13);
                }

                var44 = var37;
            }
        }

        for (int var42 = 0; var42 < (var10 ? 3 : 2); var42++) {
            double var45 = var6 - (double)var42 * 0.06;
            if (!(var45 < 0.0) && !(var45 > 0.4)) {
                ring(
                    var0,
                    var1,
                    var2,
                    var3.add(var5.scale(0.3 + var45 * (double)(var10 ? 9 : 4))),
                    var5,
                    (0.25 + var45 * 2.2) * var16,
                    0.03 * var16,
                    0.9F,
                    0.88F,
                    1.0F,
                    (float)(1.0 - var45 / 0.4) * 0.7F * var13
                );
            }
        }

        Vec3 var43 = var23[var22];
        double var46 = var3.distanceTo(var43);
        int var47 = (int)(var46 * (var10 ? 2.5 : 1.2));
        Vec3 var48 = perp(var5);
        Vec3 var31 = var5.cross(var48).normalize();

        for (int var49 = 0; var49 < var47; var49++) {
            double var33 = hash(var49, 1);
            double var56 = hash(var49, 2) * 6.28 + var6 * 6.0;
            double var57 = (0.15 + var6 * (var10 ? 1.6 : 0.8)) * var16 * (0.4 + hash(var49, 3));
            Vec3 var39 = var23[Math.min(var22, (int)(var33 * (double)var22))]
                .add(var48.scale(Math.cos(var56) * var57))
                .add(var31.scale(Math.sin(var56) * var57));
            PixelFx.sprite(
                var39, (var10 ? 0.45 : 0.24) * (0.6 + hash(var49, 4)), var49 % 3 == 0 ? 1 : 58, 0.86F, 0.9F, 1.0F, var13 * (float)clamp(1.3 - var6 / var8)
            );
        }

        if (var10) {
            for (int var50 = 0; var50 < 5; var50++) {
                double var52 = ((double)var50 + 0.5) / 5.0 + var6 * 0.5;
                var52 -= Math.floor(var52);
                if (var52 <= var11) {
                    ring(
                        var0,
                        var1,
                        var2,
                        var3.lerp(var4, var52),
                        var5,
                        0.9 + 0.4 * Math.sin(var6 * 9.0 + (double)var50),
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

            for (int var51 = 0; var51 < (var10 ? 18 : 7); var51++) {
                Vec3 var54 = new Vec3(hash(var51, 6) - 0.5, hash(var51, 7) - 0.3, hash(var51, 8) - 0.5).normalize();
                double var55 = var6 - 0.05;
                PixelFx.sprite(
                    var4.add(var54.scale(var55 * (var10 ? 7.0 : 3.5))).add(0.0, -var55 * var55 * 3.0, 0.0),
                    var10 ? 0.4 : 0.22,
                    13,
                    0.9F,
                    0.93F,
                    1.0F,
                    var13 * (float)clamp(1.0 - var55 * 2.0)
                );
            }
        }
    }

    private static void fern(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8) {
        float var10 = env(var6, var8, 0.02, 0.3);
        Vec3[] var11 = new Vec3[]{var3, var4};
        tube(var0, var1, var2, var11, 0.11, 0.78F, 0.85F, 1.0F, 0.3F * var10);
        tube(var0, var1, var2, var11, 0.045, 1.0F, 1.0F, 1.0F, 0.95F * var10);
        PixelFx.sprite(var4, 0.55, 1 + (int)(var6 * 20.0) % 2, 0.9F, 0.93F, 1.0F, var10);
        double var12 = var3.distanceTo(var4);

        for (int var14 = 0; var14 < (int)(var12 * 0.8); var14++) {
            PixelFx.sprite(
                var3.lerp(var4, hash(var14, 3)).add((hash(var14, 4) - 0.5) * 0.3, (hash(var14, 5) - 0.5) * 0.3 + var6 * 0.4, (hash(var14, 6) - 0.5) * 0.3),
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
        float var10 = env(var6, var8, 0.03, 0.4);
        Vec3 var11 = perp(var5);
        Vec3 var12 = var5.cross(var11).normalize();
        double var13 = var3.distanceTo(var4);

        for (int var15 = 0; var15 < 5; var15++) {
            double var16 = (double)var15 * 0.05;
            double var18 = sstep(var16, var16 + 0.22, var6);
            if (!(var18 <= 0.0)) {
                Vec3 var20 = var11.scale((hash(var15, 31) - 0.5) * 1.2).add(var12.scale((hash(var15, 32) - 0.5) * 0.9));
                Vec3 var21 = var3.add(var20.scale(0.6));
                Vec3 var22 = var4.add(var20.scale(0.35));
                Vec3 var23 = var22.subtract(var21).normalize();
                Vec3 var24 = var21.lerp(var22, var18);
                double var25 = var15 == 0 ? 2.6 : 1.6 + 0.6 * hash(var15, 33);
                double var27 = var15 == 0 ? 0.22 : 0.14;
                iceSpear(
                    var0,
                    var1,
                    var2,
                    var24,
                    var23,
                    Math.min(var25, var24.distanceTo(var21) + 0.3),
                    var27,
                    var10 * (var18 < 1.0 ? 1.0F : (float)clamp(1.0 - (var6 - var16 - 0.22) * 3.0))
                );
                if (var18 < 1.0) {
                    for (int var29 = 0; var29 < 4; var29++) {
                        Vec3 var30 = var21.lerp(var22, Math.max(0.0, var18 - 0.05 * (double)var29 - 0.05))
                            .add((hash(var15 * 9 + var29, 2) - 0.5) * 0.3, (hash(var15 * 9 + var29, 3) - 0.5) * 0.3, (hash(var15 * 9 + var29, 4) - 0.5) * 0.3);
                        PixelFx.sprite(
                            var30,
                            0.3 - 0.05 * (double)var29,
                            var29 % 2 == 0 ? 14 : 1,
                            0.82F,
                            0.95F,
                            1.0F,
                            var10 * (1.0F - (float)var29 / 4.0F),
                            (double)var29 + var6 * 3.0
                        );
                    }
                }
            }
        }

        double var31 = var6 - 0.22;
        if (var31 > 0.0) {
            burst(var4, 2.2, var31, 0.45, 0.8F, 0.95F, 1.0F, var10);

            for (int var17 = 0; var17 < 10; var17++) {
                Vec3 var32 = new Vec3(hash(var17, 6) - 0.5, hash(var17, 7), hash(var17, 8) - 0.5).normalize();
                Vec3 var19 = var4.add(var32.scale(var31 * 3.0)).add(0.0, -var31 * var31 * 5.0, 0.0);
                PixelFx.streak(var19.subtract(var32.scale(0.25)), var19.add(var32.scale(0.25)), 0.25, 15, 0.85F, 0.97F, 1.0F, var10 * (float)clamp(1.0 - var31));
            }

            PixelFx.flat(var4.add(0.0, -0.4, 0.0), UP, 1.6 + var31 * 3.0, 14, var31, 0.85F, 0.97F, 1.0F, 0.6F * var10 * (float)clamp(1.0 - var31 * 1.5));
        }
    }

    private static void iceSpear(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, double var7, float var9) {
        crystal(var0, var1, var2, var3, var4, var5, var7, 6, 0.42F, 0.78F, 1.0F, 0.92F, 1.0F, 1.0F, var9);
        PixelFx.sprite(var3.subtract(var4.scale(var5 * 0.4)), var7 * 9.0, 4, 0.6F, 0.85F, 1.0F, 0.3F * var9);
    }

    static void crystal(
        VertexConsumer var0,
        Matrix4f var1,
        Vec3 var2,
        Vec3 var3,
        Vec3 var4,
        double var5,
        double var7,
        int var9,
        float var10,
        float var11,
        float var12,
        float var13,
        float var14,
        float var15,
        float var16
    ) {
        if (!(var16 <= 0.01F) && !(var5 <= 0.0)) {
            if (Math.abs(var11 - 0.9F) < 0.006F) {
                var11 = 0.915F;
            }

            Vec3 var17 = perp(var4);
            Vec3 var18 = var4.cross(var17).normalize();
            Vec3 var19 = var3.subtract(var4.scale(var5));
            Vec3 var20 = var3.subtract(var4.scale(var5 * 0.22));
            Vec3 var21 = var2.subtract(var3).normalize();

            for (int var22 = 0; var22 < var9; var22++) {
                double var23 = (double)var22 * Math.PI * 2.0 / (double)var9;
                double var25 = (double)(var22 + 1) * Math.PI * 2.0 / (double)var9;
                Vec3 var27 = var17.scale(Math.cos(var23) * var7).add(var18.scale(Math.sin(var23) * var7));
                Vec3 var28 = var17.scale(Math.cos(var25) * var7).add(var18.scale(Math.sin(var25) * var7));
                Vec3 var29 = var17.scale(Math.cos((var23 + var25) / 2.0)).add(var18.scale(Math.sin((var23 + var25) / 2.0)));
                float var30 = (float)(0.5 + 0.5 * Math.abs(var29.dot(var21)));
                float var31 = Math.min(1.0F, var10 * var30 + 0.25F * (1.0F - var30));
                float var32 = Math.min(1.0F, var11 * var30 + 0.25F * (1.0F - var30));
                float var33 = Math.min(1.0F, var12 * var30 + 0.3F * (1.0F - var30));
                if (Math.abs(var32 - 0.9F) < 0.006F) {
                    var32 = 0.915F;
                }

                Vec3[][] var34 = new Vec3[][]{
                    {var19.add(var27.scale(0.55)), var19.add(var28.scale(0.55)), var20.add(var28), var20.add(var27)},
                    {var20.add(var27), var20.add(var28), var3, var3}
                };

                for (Vec3[] var38 : var34) {
                    for (Vec3 var42 : var38) {
                        v(var0, var1, var2, var42, var31, var32, var33, var16 * 0.62F);
                    }
                }

                line(var0, var1, var2, var20.add(var27), var3, 0.012 + var7 * 0.08, var13, var14, var15, var16 * 0.85F);
                line(var0, var1, var2, var19.add(var27.scale(0.55)), var20.add(var27), 0.01 + var7 * 0.06, var13, var14, var15, var16 * 0.55F);
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
        double var10 = sstep(0.0, 0.09, var6);
        float var12 = env(var6, var8, 0.01, 0.3);
        Vec3 var13 = perp(var5);
        Vec3 var14 = var5.cross(var13).normalize();
        Vec3 var15 = var13.scale(0.8).add(var14.scale(0.6)).normalize();
        Vec3 var16 = var3.lerp(var4, 0.55);
        double var17 = Math.min(3.2, var3.distanceTo(var4) * 0.5);
        byte var19 = 28;
        Vec3 var20 = null;
        Vec3 var21 = null;

        for (int var22 = 0; var22 <= var19; var22++) {
            double var23 = (double)var22 / (double)var19;
            if (var23 > var10) {
                break;
            }

            double var25 = Math.sin(Math.PI * var23);
            double var27 = 1.1 * var25;
            Vec3 var29 = var16.add(var15.scale((var23 - 0.5) * 2.0 * var17)).add(var5.scale(-var27));
            double var30 = 0.05 + 0.28 * var25;
            Vec3 var32 = var29.add(var5.scale(var30 * 0.3));
            Vec3 var33 = var29.subtract(var5.scale(var30));
            if (var20 != null) {
                float var34 = var12 * (float)(0.35 * var25 + 0.1);
                v(var0, var1, var2, var20, 0.85F, 0.88F, 1.0F, var34);
                v(var0, var1, var2, var32, 0.85F, 0.88F, 1.0F, var34);
                v(var0, var1, var2, var33, 0.6F, 0.65F, 1.0F, 0.0F);
                v(var0, var1, var2, var21, 0.6F, 0.65F, 1.0F, 0.0F);
                line(var0, var1, var2, var20, var32, 0.015 + 0.04 * var25, 1.0F, 1.0F, 1.0F, 0.95F * var12);
            }

            var20 = var32;
            var21 = var33;
        }

        for (int var35 = 1; var35 <= 2; var35++) {
            float var36 = var12 * (float)clamp(1.0 - var6 * 4.0) * (0.4F / (float)var35);
            Vec3 var24 = null;

            for (int var37 = 0; var37 <= 16; var37++) {
                double var26 = (double)var37 / 16.0;
                double var28 = Math.sin(Math.PI * var26);
                Vec3 var38 = var16.add(var15.scale((var26 - 0.5) * 2.0 * var17 * (1.0 + 0.1 * (double)var35)))
                    .add(var5.scale(-1.1 * var28 - 0.25 * (double)var35));
                if (var24 != null) {
                    line(var0, var1, var2, var24, var38, 0.012, 0.85F, 0.88F, 1.0F, var36);
                }

                var24 = var38;
            }
        }

        if (var6 < 0.15) {
            PixelFx.sprite(var16, 1.4, 4, 0.85F, 0.88F, 1.0F, 0.4F * var12);
        }
    }

    private static void catastravia(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, double var6, double var8, double var10) {
        float var12 = env(var8, var10, 0.05, 0.45);
        Vec3 var13 = var3.add(0.0, 11.0, 0.0).add(var5.scale(Math.min(10.0, var6 * 0.25)));
        circle(var0, var1, var2, var13, UP, 3.6, CATASTRAVIA, var8, Math.min(1.0, var8 / 0.35), var12, 31);
        PixelFx.sprite(var13, 6.0, 4, 1.0F, 0.92F, 0.7F, 0.3F * var12);
        byte var14 = 26;

        for (int var15 = 0; var15 < var14; var15++) {
            double var16 = 0.1 + 0.9 * ((double)var15 + hash(var15, 1) * 0.6) / (double)var14;
            Vec3 var18 = var3.lerp(var4, var16).add((hash(var15, 2) - 0.5) * 2.0, -0.2, (hash(var15, 3) - 0.5) * 2.0);
            Vec3 var19 = var13.add((hash(var15, 4) - 0.5) * 4.5, 0.0, (hash(var15, 5) - 0.5) * 4.5);
            double var20 = 0.1 + (double)var15 * 0.04;
            double var22 = (var8 - var20) / 0.3;
            if (!(var22 < 0.0)) {
                if (var22 < 1.0) {
                    Vec3 var27 = var19.lerp(var18, var22);
                    Vec3 var25 = var18.subtract(var19).normalize();
                    crystal(var0, var1, var2, var27, var25, 2.2, 0.07, 4, 1.0F, 0.94F, 0.7F, 1.0F, 1.0F, 0.92F, 1.0F);
                    line(var0, var1, var2, var27.subtract(var25.scale(4.5)), var27.subtract(var25.scale(2.0)), 0.05, 1.0F, 0.88F, 0.5F, 0.35F);
                    PixelFx.sprite(var27, 0.7, 4, 1.0F, 0.92F, 0.65F, 0.6F);
                } else {
                    double var24 = (var22 - 1.0) * 0.3;
                    if (var24 < 0.5) {
                        PixelFx.sprite(
                            var18.add(0.0, 0.3, 0.0),
                            Math.min(2.0, 1.2 + var24 * 3.0),
                            48 + Math.min(3, (int)(var24 * 8.0)),
                            1.0F,
                            0.94F,
                            0.7F,
                            (float)(1.0 - var24 * 2.0)
                        );
                        PixelFx.sprite(var18.add(0.0, 0.3, 0.0), 1.8 * (1.0 - var24), 4, 1.0F, 0.9F, 0.6F, 0.5F * (float)(1.0 - var24 * 2.0));
                    }

                    shock(var18.add(0.0, 0.25, 0.0), UP, (1.0 + var24 * 7.0) * 0.5, 1.0F, 0.9F, 0.6F, (float)clamp(1.0 - var24 * 2.0));

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
                    crystal(var0, var1, var2, var24.add(var26.scale(0.3)), var26, 0.6, 0.05, 2, 0.86F, 0.88F, 0.95F, 1.0F, 1.0F, 1.0F, var25);
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
        crystal(var0, var1, var2, var15, var5, Math.min(2.8, var15.distanceTo(var3) + 0.2), 0.11, 4, 1.0F, 0.94F, 0.68F, 1.0F, 1.0F, 0.9F, var14);
        PixelFx.sprite(var15.subtract(var5.scale(1.0)), 1.3, 4, 1.0F, 0.9F, 0.6F, 0.45F * var14);
        line(var0, var1, var2, var3, var15, 0.04, 1.0F, 0.95F, 0.75F, 0.35F * var14);
        ring(var0, var1, var2, var3.add(var5.scale(0.2)), var5, 0.35 + var8 * 0.5, 0.035, 1.0F, 0.88F, 0.6F, 0.8F * var14);

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
        CUR_VC = var0;
        CUR_M = var1;
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
                case 26:
                default:
                    return false;
                case 11:
                    flowers(var3, var12, var5, var10, var7);
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
                    float var21 = env(var5, var10, 0.02, 0.4);

                    for (int var25 = 0; var25 < 30; var25++) {
                        Vec3 var28 = new Vec3(hash(var25, 1) - 0.5, hash(var25, 2) - 0.2, hash(var25, 3) - 0.5).normalize();
                        Vec3 var30 = var3.add(var28.scale(var5 * 5.0));
                        PixelFx.streak(var30.subtract(var28.scale(0.28)), var30.add(var28.scale(0.28)), 0.22, 29, 1.0F, 1.0F, 1.0F, var21);
                    }

                    return true;
                case 20:
                    float var20 = env(var5, var10, 0.1, 0.5);
                    PixelFx.flat(var3.add(0.0, 0.05, 0.0), UP, 1.2 + 0.1 * Math.sin(var7 * 4.0), 5, var7, 0.7F, 0.95F, 1.0F, 0.8F * var20);

                    for (int var24 = 0; var24 < 6; var24++) {
                        double var27 = var7 * 2.0 + (double)var24 * 1.047;
                        PixelFx.sprite(
                            var3.add(Math.cos(var27) * 0.55, 0.2 + 0.3 * Math.sin(var7 * 3.0 + (double)var24), Math.sin(var27) * 0.55),
                            0.25,
                            1,
                            0.8F,
                            1.0F,
                            1.0F,
                            0.9F * var20
                        );
                    }

                    return true;
                case 21:
                    tornado(var0, var1, var9, var3, var12, var5, var10, true);
                    return true;
                case 22:
                case 23:
                    float var19 = env(var5, var10, 0.02, 0.4);
                    boolean var23 = var2 == 23;
                    shieldHexes(var0, var1, var9, var3, var5, var23 ? 2.2 : 1.5, var19);
                    burst(var3, var23 ? 3.2 : 1.8, var5, var23 ? 0.6 : 0.4, var23 ? 1.0F : 0.75F, var23 ? 1.0F : 0.95F, 1.0F, var19);
                    PixelFx.sprite(var3, (var23 ? 1.6 : 1.0) + var5 * 3.5, 6, 0.72F, 0.95F, 1.0F, var19 * (float)clamp(1.0 - var5 * 1.6));
                    PixelFx.sprite(var3, var23 ? 2.8 : 1.6, 4, 0.7F, 0.92F, 1.0F, 0.6F * var19);
                    int var26 = 2 + var4 * 2;

                    for (int var16 = 0; var16 < var26; var16++) {
                        PixelFx.sprite(
                            var3.add((hash(var16, 1) - 0.5) * 1.2, (hash(var16, 2) - 0.5) * 1.2, (hash(var16, 3) - 0.5) * 1.2),
                            0.6 + 0.3 * hash(var16, 4),
                            42 + var16 % 4,
                            0.8F,
                            0.97F,
                            1.0F,
                            0.9F * var19,
                            hash(var16, 5) * 6.28
                        );
                    }

                    for (int var29 = 0; var29 < (var23 ? 16 : 8); var29++) {
                        Vec3 var17 = new Vec3(hash(var29, 6) - 0.5, hash(var29, 7) - 0.5, hash(var29, 8) - 0.5).normalize();
                        PixelFx.sprite(var3.add(var17.scale(var5 * (double)(var23 ? 5 : 3))), 0.3, 1, var29 % 2 == 0 ? 1.0F : 0.75F, 1.0F, 1.0F, var19);
                    }

                    return true;
                case 24:
                    blackHole(var0, var1, var9, var3, var12, var5, var10, var7);
                    return true;
                case 25:
                    float var18 = env(var5, var10, 0.02, 0.6);
                    burst(var3, 6.0, var5, 0.7, 1.0F, 0.6F, 0.2F, var18);
                    PixelFx.sprite(var3, 4.0 * (1.0 - var5 / var10) + 1.0, 4, 1.0F, 0.45F, 0.1F, 0.8F * var18);
                    shock(var3, UP, (2.0 + var5 * 14.0) * 0.5, 1.0F, 0.55F, 0.2F, var18);

                    for (int var22 = 0; var22 < 24; var22++) {
                        Vec3 var15 = new Vec3(hash(var22, 1) - 0.5, hash(var22, 2) - 0.3, hash(var22, 3) - 0.5).normalize();
                        PixelFx.sprite(
                            var3.add(var15.scale(var5 * (6.0 + 4.0 * hash(var22, 4)))).add(0.0, -var5 * var5 * 4.0, 0.0),
                            0.4 + 0.3 * hash(var22, 5),
                            var22 % 3 == 0 ? 13 : 33,
                            1.0F,
                            var22 % 3 == 0 ? 0.7F : 1.0F,
                            var22 % 3 == 0 ? 0.3F : 1.0F,
                            var18,
                            var5 * 8.0 + (double)var22
                        );
                    }

                    return true;
                case 27:
                    float var13 = env(var5, var10, 0.02, 0.4);
                    shock(var3, null, (1.2 + var5 * 4.0) * 0.5, 0.95F, 0.97F, 1.0F, var13);

                    for (int var14 = 0; var14 < 6; var14++) {
                        PixelFx.sprite(
                            var3.add((hash(var14, 1) - 0.5) * var5 * 3.0, (hash(var14, 2) - 0.5) * var5 * 2.0, (hash(var14, 3) - 0.5) * var5 * 3.0),
                            0.9 + var5,
                            7 + var14 % 4,
                            0.85F,
                            0.85F,
                            0.82F,
                            0.7F * var13
                        );
                    }

                    return true;
                case 28:
                    golemFist(var0, var1, var9, var3, var12, var5, var10, var7);
                    return true;
            }
        }
    }

    private static void shieldHexes(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, float var8) {
        Vec3 var9 = var2.subtract(var3).normalize();
        Vec3 var10 = perp(var9);
        Vec3 var11 = var9.cross(var10).normalize();
        double var12 = 0.22;
        double var14 = var12 * Math.sqrt(3.0);
        double var16 = var4 * 6.0;

        for (int var18 = -7; var18 <= 7; var18++) {
            for (int var19 = -7; var19 <= 7; var19++) {
                double var20 = var14 * ((double)var18 + (double)var19 * 0.5);
                double var22 = var12 * 1.5 * (double)var19;
                double var24 = Math.sqrt(var20 * var20 + var22 * var22);
                if (!(var24 > var6)) {
                    double var26 = Math.max(0.0, 1.0 - Math.abs(var24 - var16) / 0.6) + 0.25 * Math.max(0.0, 1.0 - var24 / var6);
                    float var28 = (float)(var26 * (double)var8 * (1.0 - var24 / var6 * 0.5));
                    if (!(var28 < 0.02F)) {
                        Vec3 var29 = var3.add(var10.scale(var20)).add(var11.scale(var22)).add(var9.scale(0.02));
                        Vec3 var30 = null;

                        for (int var31 = 0; var31 <= 6; var31++) {
                            double var32 = (Math.PI / 6) + (double)var31 * Math.PI / 3.0;
                            Vec3 var34 = var29.add(var10.scale(Math.cos(var32) * var12 * 0.92)).add(var11.scale(Math.sin(var32) * var12 * 0.92));
                            if (var30 != null) {
                                line(var0, var1, var2, var30, var34, 0.03, 0.8F, 0.95F, 1.0F, var28);
                            }

                            var30 = var34;
                        }

                        Vec3[] var41 = new Vec3[6];

                        for (int var42 = 0; var42 < 6; var42++) {
                            double var33 = (Math.PI / 6) + (double)var42 * Math.PI / 3.0;
                            var41[var42] = var29.add(var10.scale(Math.cos(var33) * var12 * 0.88)).add(var11.scale(Math.sin(var33) * var12 * 0.88));
                        }

                        for (byte var43 = 0; var43 < 6; var43 += 2) {
                            Vec3 var44 = var41[var43];
                            Vec3 var35 = var41[(var43 + 1) % 6];
                            Vec3 var36 = var41[(var43 + 2) % 6];

                            for (Vec3 var40 : new Vec3[]{var29, var44, var35, var36}) {
                                var0.addVertex(var1, (float)(var40.x - var2.x), (float)(var40.y - var2.y), (float)(var40.z - var2.z))
                                    .setColor(0.45F, 0.75F, 1.0F, var28 * 0.3F);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void waterSphere(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7) {
        double var9 = 1.1 + 0.35 * (double)var4;
        Vec3 var11 = var3.add(0.0, 3.2 + var9, 0.0);
        float var12 = env(var5, var7, 0.05, 0.5);
        circle(var0, var1, var2, var3.add(0.0, 0.06, 0.0), UP, var9 + 1.4, WATER_C, var5, Math.min(1.0, var5 / 0.35), var12 * (float)clamp(2.0 - var5), 5);
        if (var5 < 0.9) {
            double var13 = sstep(0.0, 0.5, var5);
            double var15 = sstep(0.55, 0.9, var5);
            Vec3 var17 = var11.lerp(var3.add(0.0, var9 * 0.6, 0.0), var15 * var15);
            double var18 = var9 * var13;
            waterBall(var0, var1, var2, var17, var18, var5, var12, var15);

            for (int var20 = 0; var20 < 26; var20++) {
                double var21 = (double)var20 * 0.449 + var5 * 4.0;
                double var23 = var18 * (var13 < 1.0 ? 2.4 - 1.4 * var13 : 0.6 * hash(var20, 3));
                Vec3 var25 = var17.add(Math.cos(var21) * var23, Math.sin((double)var20 * 1.3 + var5 * 3.0) * var23 * 0.8, Math.sin(var21) * var23);
                if (var13 < 1.0) {
                    PixelFx.streak(var25, var25.add(var17.subtract(var25).normalize().scale(0.5)), 0.12, 11, 0.55F, 0.82F, 1.0F, 0.8F * var12);
                }
            }
        } else {
            double var30 = var5 - 0.9;
            shock(var3.add(0.0, 0.08, 0.0), UP, var9 + var30 * 6.0, 0.55F, 0.82F, 1.0F, 0.8F * var12 * (float)clamp(1.0 - var30));
            shock(var3.add(0.0, 0.09, 0.0), UP, var9 * 0.6 + var30 * 3.5, 0.8F, 0.95F, 1.0F, 0.6F * var12 * (float)clamp(1.0 - var30));
            byte var31 = 28;

            for (int var16 = 0; var16 < var31; var16++) {
                double var33 = (Math.PI * 2) * (double)var16 / (double)var31 + hash(var16, 4) * 0.1;
                double var19 = var9 * 0.7 + var30 * (2.6 + hash(var16, 5));
                double var37 = Math.max(0.0, (1.6 + 1.4 * hash(var16, 6)) * Math.sin(Math.min(Math.PI, var30 * 3.2)));
                if (!(var37 <= 0.02)) {
                    Vec3 var38 = var3.add(Math.cos(var33 - 0.09) * var19, 0.05, Math.sin(var33 - 0.09) * var19);
                    Vec3 var24 = var3.add(Math.cos(var33 + 0.09) * var19, 0.05, Math.sin(var33 + 0.09) * var19);
                    Vec3 var39 = var3.add(Math.cos(var33) * (var19 + var37 * 0.35), var37, Math.sin(var33) * (var19 + var37 * 0.35));

                    for (Vec3 var29 : new Vec3[]{var38, var24, var39, var39}) {
                        v(var0, var1, var2, var29, 0.62F, 0.85F, 1.0F, 0.55F * var12);
                    }

                    line(var0, var1, var2, var3.add(Math.cos(var33) * var19, 0.05, Math.sin(var33) * var19), var39, 0.03, 0.9F, 0.97F, 1.0F, 0.7F * var12);
                    PixelFx.sprite(var39.add(0.0, 0.15, 0.0), 0.25, 11, 0.6F, 0.85F, 1.0F, 0.9F * var12, var33);
                }
            }

            for (int var32 = 0; var32 < 40; var32++) {
                Vec3 var34 = new Vec3(Math.cos((double)var32 * 0.157), 0.0, Math.sin((double)var32 * 0.157));
                double var35 = 2.5 + 3.0 * hash(var32, 1);
                Vec3 var36 = var3.add(var34.scale(var9 * 0.6 + var30 * var35)).add(0.0, var30 * (5.0 + 3.0 * hash(var32, 2)) - 9.8 * var30 * var30 * 0.6, 0.0);
                if (!(var36.y < var3.y)) {
                    PixelFx.sprite(var36, 0.3, 11, 0.55F, 0.82F, 1.0F, 0.9F * var12, (double)var32);
                }
            }
        }
    }

    private static void waterBall(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, float var8, double var9) {
        if (!(var4 <= 0.02)) {
            byte var11 = 10;
            byte var12 = 18;
            Vec3[][] var13 = new Vec3[var11 + 1][var12 + 1];

            for (int var14 = 0; var14 <= var11; var14++) {
                for (int var15 = 0; var15 <= var12; var15++) {
                    double var16 = Math.PI * (double)var14 / (double)var11;
                    double var18 = (Math.PI * 2) * (double)var15 / (double)var12;
                    double var20 = 1.0
                        + 0.06 * Math.sin(var18 * 3.0 + var6 * 7.0) * Math.sin(var16 * 2.0 + var6 * 5.0)
                        + 0.04 * Math.sin(var16 * 5.0 - var6 * 9.0);
                    double var22 = var4 * var20;
                    var13[var14][var15] = var3.add(
                        Math.sin(var16) * Math.cos(var18) * var22 * (1.0 + 0.25 * var9),
                        Math.cos(var16) * var22 * (1.0 - 0.35 * var9),
                        Math.sin(var16) * Math.sin(var18) * var22 * (1.0 + 0.25 * var9)
                    );
                }
            }

            Vec3 var30 = var2.subtract(var3).normalize();

            for (int var31 = 0; var31 < var11; var31++) {
                for (int var34 = 0; var34 < var12; var34++) {
                    Vec3 var17 = var13[var31][var34];
                    Vec3 var37 = var13[var31][var34 + 1];
                    Vec3 var19 = var13[var31 + 1][var34 + 1];
                    Vec3 var39 = var13[var31 + 1][var34];
                    Vec3 var21 = var17.add(var19).scale(0.5).subtract(var3).normalize();
                    double var41 = Math.pow(1.0 - Math.abs(var21.dot(var30)), 2.0);
                    float var24 = (float)(0.16 + 0.5 * var41) * var8;
                    float var25 = (float)(0.55 + 0.45 * var41);

                    for (Vec3 var29 : new Vec3[]{var17, var37, var19, var39}) {
                        v(var0, var1, var2, var29, 0.3F * var25 + 0.15F, 0.6F * var25 + 0.2F, 1.0F, var24);
                    }
                }
            }

            for (int var32 = 0; var32 < 3; var32++) {
                Vec3 var35 = null;

                for (int var36 = 0; var36 <= 20; var36++) {
                    double var38 = (double)var36 / 20.0;
                    double var40 = var38 * Math.PI * 2.0 + var6 * (double)(2 + var32);
                    double var42 = Math.sin(var38 * Math.PI * 2.0 * (double)(var32 + 1) + var6 * 3.0) * 0.6;
                    Vec3 var43 = var3.add(
                        Math.cos(var40) * Math.cos(var42) * var4 * 0.65, Math.sin(var42) * var4 * 0.65, Math.sin(var40) * Math.cos(var42) * var4 * 0.65
                    );
                    if (var35 != null) {
                        line(var0, var1, var2, var35, var43, 0.03 + var4 * 0.01, 0.75F, 0.92F, 1.0F, 0.45F * var8);
                    }

                    var35 = var43;
                }
            }

            Vec3 var33 = var30.cross(UP).normalize();
            PixelFx.sprite(
                var3.add(var33.scale(-var4 * 0.35)).add(0.0, var4 * 0.4, 0.0).add(var30.scale(var4 * 0.8)), var4 * 0.5, 4, 1.0F, 1.0F, 1.0F, 0.6F * var8
            );
            PixelFx.sprite(var3, var4 * 3.0, 4, 0.3F, 0.6F, 1.0F, 0.3F * var8);
        }
    }

    private static void spikes(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7) {
        int var9 = 9 + 3 * var4;
        double var10 = 2.4 + (double)var4;
        circle(var0, var1, var2, var3.add(0.0, 0.06, 0.0), UP, var10 + 0.8, EARTH_C, var5, Math.min(1.0, var5 / 0.25), (float)clamp(1.5 - var5), 6);

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

        shock(var3.add(0.0, 0.04, 0.0), UP, (0.5 + var5 * 6.0) * 2.0 * 0.5, 0.68F, 0.6F, 0.5F, 0.6F * (float)clamp(1.0 - var5));
    }

    private static void jilwer(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6) {
        float var8 = env(var4, var6, 0.02, 0.5);
        shock(var3.add(0.0, 0.05, 0.0), UP, (0.5 + var4 * 5.0) * 2.0 * 0.5, 0.85F, 1.0F, 0.9F, 0.8F * var8);

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

        circle(var0, var1, var2, var3.add(0.0, 0.06, 0.0), UP, 1.35, BIND_C, var9, Math.min(1.0, var5 / 0.4), var11, 8);

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
        circle(var0, var1, var2, var3.add(0.0, 0.06, 0.0), UP, 1.15, GOLD_C, var8, Math.min(1.0, var4 / 0.5), var10 * 0.9F, 9);
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
        circle(var0, var1, var2, var11, UP, var12 * 1.15, HOLY, var8, Math.min(1.0, var4 / 0.5), var10, 12);
        PixelFx.flat(var11, UP, var12 * 2.4, 4, 0.0, 1.0F, 0.9F, 0.6F, 0.4F * var10);
        tube(var0, var1, var2, new Vec3[]{var11, var11.add(0.0, 3.2, 0.0)}, var12 * 0.75, 1.0F, 0.92F, 0.65F, 0.07F * var10);
        tube(var0, var1, var2, new Vec3[]{var11, var11.add(0.0, 2.4, 0.0)}, var12 * 0.45, 1.0F, 0.96F, 0.8F, 0.08F * var10);

        for (int var14 = 0; var14 < 22; var14++) {
            double var15 = (var4 * 0.7 + hash(var14, 1)) % 1.0;
            double var17 = hash(var14, 2) * 6.283;
            double var19 = var12 * (0.2 + 0.7 * hash(var14, 3));
            Vec3 var21 = var3.add(Math.cos(var17) * var19, 0.2 + var15 * 2.6, Math.sin(var17) * var19);
            PixelFx.sprite(
                var21,
                0.45 + 0.2 * hash(var14, 4),
                var14 % 3 == 0 ? 1 : PixelFx.anim(24, 4, var8 + (double)var14, 8.0),
                1.0F,
                0.94F,
                0.72F,
                (float)Math.sin(var15 * Math.PI) * var10
            );
        }

        PixelFx.sprite(var3.add(0.0, 1.1, 0.0), 2.6, 4, 1.0F, 0.92F, 0.65F, 0.35F * var10);
    }

    private static void hellfire(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7, double var9) {
        int var11 = 4 + 2 * var4;
        double var12 = 1.2 + (double)var4;
        float var14 = env(var5, var7, 0.05, 0.9);
        PixelFx.flat(var3.add(0.0, 0.05, 0.0), UP, (var12 + 1.5) * 2.0, 4, 0.0, 1.0F, 0.42F, 0.1F, 0.85F * var14);
        circle(var0, var1, var2, var3.add(0.0, 0.07, 0.0), UP, var12 + 1.2, FIRE_C, var9, Math.min(1.0, var5 / 0.3), var14 * (float)clamp(1.6 - var5), 13);

        for (int var15 = 0; var15 < 10; var15++) {
            double var16 = hash(var15, 21) * 6.283;
            double var18 = (var12 + 1.0) * sstep(0.0, 0.5, var5) * (0.6 + 0.4 * hash(var15, 22));
            Vec3 var20 = var3.add(0.0, 0.08, 0.0);

            for (int var21 = 1; var21 <= 6; var21++) {
                double var22 = var18 * (double)var21 / 6.0;
                double var24 = (hash(var15 * 7 + var21, 23) - 0.5) * 0.5;
                Vec3 var26 = var3.add(Math.cos(var16 + var24 / Math.max(0.5, var22)) * var22, 0.08, Math.sin(var16 + var24 / Math.max(0.5, var22)) * var22);
                flatLine(var0, var1, var2, var20, var26, UP, 0.16 * (1.0 - (double)var21 / 7.0), 1.0F, 0.55F, 0.15F, 0.9F * var14);
                flatLine(var0, var1, var2, var20, var26, UP, 0.05 * (1.0 - (double)var21 / 7.0), 1.0F, 0.92F, 0.6F, var14);
                var20 = var26;
            }
        }

        for (int var35 = 0; var35 < var11; var35++) {
            double var36 = hash(var35, 1) * 6.283;
            double var37 = var12 * Math.sqrt(hash(var35, 2));
            Vec3 var38 = var3.add(Math.cos(var36) * var37, 0.0, Math.sin(var36) * var37);
            double var39 = 0.06 * (double)var35;
            double var23 = sstep(var39, var39 + 0.25, var5);
            if (!(var23 <= 0.0)) {
                double var25 = (3.2 + 3.2 * hash(var35, 3)) * var23 * (1.0 - 0.5 * sstep(var7 - 0.9, var7, var5));
                int var27 = (int)Math.max(3.0, var25 * 2.2);

                for (int var28 = 0; var28 < var27; var28++) {
                    double var29 = var25 * (double)var28 / (double)var27;
                    double var31 = (1.7 - 1.0 * (double)var28 / (double)var27) * (0.85 + 0.3 * hash(var35 * 31 + var28, 4));
                    double var33 = Math.sin(var9 * 9.0 + (double)var28 * 1.3 + (double)var35) * 0.12;
                    PixelFx.sprite(
                        var38.add(var33, var29 + var31 * 0.4, -var33),
                        var31,
                        PixelFx.anim(16, 8, var9 + hash(var35 * 31 + var28, 5), 14.0),
                        1.0F,
                        1.0F,
                        1.0F,
                        var14
                    );
                }

                PixelFx.sprite(
                    var38.add(0.0, var25 + 0.6, 0.0), 1.5 + var25 * 0.2, 7 + (int)(var9 * 4.0 + (double)var35) % 4, 0.25F, 0.18F, 0.16F, 0.55F * var14
                );

                for (int var40 = 0; var40 < 6; var40++) {
                    double var41 = (var5 * 1.3 + hash(var35 * 7 + var40, 5)) % 1.0;
                    Vec3 var42 = var38.add((hash(var35 * 7 + var40, 6) - 0.5) * 1.4, var41 * (var25 + 2.0), (hash(var35 * 7 + var40, 7) - 0.5) * 1.4);
                    PixelFx.sprite(var42, 0.22, 13, 1.0F, 0.7F, 0.25F, (float)(1.0 - var41) * var14);
                }
            }
        }
    }

    private static void tornado(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7, boolean var9) {
        float var10 = env(var5, var7, 0.4, 0.8);
        double var11 = 6.0 + 1.5 * (double)var4;
        circle(
            var0,
            var1,
            var2,
            var3.add(0.0, 0.06, 0.0),
            UP,
            2.0 + 0.5 * (double)var4,
            var9 ? FIRE_C : WIND_C,
            var5,
            Math.min(1.0, var5 / 0.4),
            var10 * 0.9F,
            var9 ? 21 : 14
        );
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
        Vec3 var12 = var3.add(0.0, 15.0, 0.0);
        circle(var0, var1, var2, var12, UP, var10 + 3.0, LIGHTNING_C, var5, Math.min(1.0, var5 / 0.3), env(var5, var7, 0.05, 0.5), 15);
        PixelFx.sprite(var12, (var10 + 3.0) * 2.4, 4, 0.6F, 0.4F, 1.0F, 0.3F * env(var5, var7, 0.05, 0.5));
        Vec3[] var13 = new Vec3[var9];

        for (int var14 = 0; var14 < var9; var14++) {
            double var15 = hash(var14, 2) * 6.283;
            double var17 = var10 * Math.sqrt(hash(var14, 3));
            var13[var14] = var3.add(Math.cos(var15) * var17, 0.0, Math.sin(var15) * var17);
            double var19 = hash(var14, 1) * (var7 - 0.5);
            double var21 = var5 - var19;
            if (!(var21 < 0.0) && !(var21 > 0.45)) {
                double var23 = !(var21 < 0.08) && (!(var21 > 0.16) || !(var21 < 0.22)) && (!(var21 > 0.3) || !(var21 < 0.34)) ? 0.25 : 1.0;
                float var25 = (float)(clamp(1.0 - var21 / 0.45) * var23);
                Vec3 var26 = var12.add((hash(var14, 4) - 0.5) * (var10 + 2.0), -0.3, (hash(var14, 5) - 0.5) * (var10 + 2.0));
                bolt(var0, var1, var2, var26, var13[var14], var5, var14 * 13 + 5, 1.3, var25, 3, 0.11);
                burst(var13[var14].add(0.0, 0.4, 0.0), 2.6, var21, 0.45, 0.82F, 0.6F, 1.0F, var25);

                for (int var27 = 0; var27 < 3; var27++) {
                    PixelFx.sprite(
                        var13[var14]
                            .add((hash(var14 * 3 + var27, 11) - 0.5) * 1.6, 0.5 + var21 * 1.2 + (double)var27 * 0.4, (hash(var14 * 3 + var27, 12) - 0.5) * 1.6),
                        1.6 + var21 * 3.0,
                        7 + Math.min(3, (int)(var21 * 6.0)),
                        0.62F,
                        0.4F,
                        0.95F,
                        0.75F * (float)clamp(1.0 - var21 * 1.6)
                    );
                }

                shock(var13[var14].add(0.0, 0.06, 0.0), UP, (2.4 + var21 * 6.0) * 0.5, 0.75F, 0.55F, 1.0F, (float)clamp(1.0 - var21 * 2.2));

                for (int var31 = 0; var31 < 5; var31++) {
                    PixelFx.sprite(
                        var13[var14]
                            .add(
                                (hash(var14 * 5 + var31, 6) - 0.5) * var21 * 8.0,
                                var21 * 3.0 - var21 * var21 * 8.0 + 0.2,
                                (hash(var14 * 5 + var31, 7) - 0.5) * var21 * 8.0
                            ),
                        0.3,
                        1,
                        0.9F,
                        0.75F,
                        1.0F,
                        var25
                    );
                }
            }
        }

        for (byte var28 = 0; var28 + 1 < var9; var28 += 2) {
            double var29 = hash(var28, 1) * (var7 - 0.5);
            double var30 = var5 - var29;
            if (!(var30 < 0.05) && !(var30 > 0.3)) {
                bolt(
                    var0,
                    var1,
                    var2,
                    var13[var28].add(0.0, 0.3, 0.0),
                    var13[var28 + 1].add(0.0, 0.3, 0.0),
                    var5,
                    var28 * 7 + 3,
                    0.5,
                    (float)clamp(1.0 - var30 / 0.3) * 0.8F,
                    1,
                    0.035
                );
            }
        }

        PixelFx.sprite(var3.add(0.0, 1.0, 0.0), var10 * 2.5, 4, 0.7F, 0.45F, 1.0F, 0.16F * env(var5, var7, 0.05, 0.4));
    }

    static double bhTravel(double var0) {
        return Math.max(0.4, Math.min(2.5, var0 / 12.0));
    }

    static void blackCore(Vec3 var0, Vec3 var1, double var2, double var4, float var6) {
        Vec3 var7 = var1.subtract(var0).normalize();
        Vec3 var8 = var7.cross(UP);
        var8 = var8.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : var8.normalize();
        double var9 = 1.0 + 0.08 * Math.sin(var4 * 31.0);
        PixelFx.streak(var0.subtract(var8.scale(var2 * 9.0 * var9)), var0.add(var8.scale(var2 * 9.0 * var9)), var2 * 0.45, 4, 0.95F, 0.15F, 0.05F, 0.9F * var6);
        PixelFx.streak(var0.subtract(var8.scale(var2 * 5.0)), var0.add(var8.scale(var2 * 5.0)), var2 * 0.7, 4, 1.0F, 0.45F, 0.1F, 0.9F * var6);
        PixelFx.streak(var0.subtract(var8.scale(var2 * 2.4)), var0.add(var8.scale(var2 * 2.4)), var2 * 1.0, 4, 1.0F, 0.85F, 0.45F, var6);
        PixelFx.sprite(var0.subtract(var7.scale(0.15)), var2 * 3.8, 4, 1.0F, 0.5F, 0.08F, 0.85F * var6);
        PixelFx.sprite(var0.subtract(var7.scale(0.1)), var2 * 2.3, 4, 1.0F, 0.82F, 0.35F, var6);
        PixelFx.sprite(var0.subtract(var7.scale(0.05)), var2 * 1.75, 6, 1.0F, 0.95F, 0.7F, var6, var4 * 4.0);
        PixelFx.sprite(var0.add(var7.scale(0.05)), var2 * 1.25, 12, 0.0F, 0.0F, 0.0F, var6);
        PixelFx.sprite(var0.add(var7.scale(0.08)), var2 * 1.1, 58, 0.0F, 0.0F, 0.0F, var6);
    }

    private static void blackHoleShot(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5) {
        double var7 = var3.distanceTo(var4);
        double var9 = bhTravel(var7);
        double var11 = 1.0;
        double var13 = 1.0 + var9;
        double var15 = var13 + 2.4;
        double var17 = var15 + 0.8;
        if (!(var5 > var17)) {
            Vec3 var21 = var7 < 0.001 ? new Vec3(0.0, 0.0, 1.0) : var4.subtract(var3).scale(1.0 / var7);
            if (var5 < var11) {
                double var37 = var5 / var11;
                blackCore(var3, var2, 0.12 + 0.28 * var37 * var37, var5, (float)Math.min(1.0, var5 * 4.0));
                if (var37 > 0.8) {
                    PixelFx.sprite(
                        var3, 3.0 + 10.0 * (var37 - 0.8), 48 + Math.min(3, (int)((var37 - 0.8) * 20.0)), 1.0F, 0.97F, 0.9F, (float)(1.0 - (var37 - 0.8) * 5.0)
                    );
                }
            } else {
                float var23 = 1.0F;
                if (var5 < var13) {
                    double var39 = (var5 - var11) / var9;
                    Vec3 var36 = var3.lerp(var4, var39);
                    Vec3[] var41 = new Vec3[]{var3.lerp(var36, Math.max(0.0, 1.0 - 6.0 / Math.max(1.0, var7 * var39))), var36};
                    tube(var0, var1, var2, var41, 0.18, 0.9F, 0.18F, 0.04F, 0.45F);
                    tube(var0, var1, var2, var41, 0.07, 1.0F, 0.6F, 0.2F, 0.85F);
                    Vec3 var27 = perp(var21);
                    Vec3 var43 = var21.cross(var27).normalize();

                    for (int var45 = 0; var45 < 18; var45++) {
                        double var30 = 1.0 + 7.0 * hash(var45, 1);
                        Vec3 var32 = var36.subtract(var21.scale(var30))
                            .add(var27.scale((hash(var45, 2) - 0.5) * 2.4))
                            .add(var43.scale((hash(var45, 3) - 0.5) * 2.4));
                        PixelFx.streak(var32, var32.add(var21.scale(1.2 + hash(var45, 4))), 0.12, 4, 0.85F, 0.55F, 0.95F, 0.55F);
                    }

                    for (int var46 = 0; var46 < 16; var46++) {
                        double var47 = (var5 * 0.9 + hash(var46, 5)) % 1.0;
                        Vec3 var49 = var36.add(var27.scale((hash(var46, 6) - 0.5) * 5.0 * (1.0 - var47)))
                            .add(var43.scale((hash(var46, 7) - 0.3) * 4.0 * (1.0 - var47)))
                            .subtract(var21.scale(3.0 * (1.0 - var47)));
                        PixelFx.sprite(var49, 0.35 + 0.4 * hash(var46, 8), 33, 1.0F, 1.0F, 1.0F, (float)Math.sin(var47 * Math.PI), var47 * 9.0 + (double)var46);
                    }

                    blackCore(var36, var2, 0.42, var5, var23);
                } else {
                    Vec3 var22 = var4;
                    if (!(var5 < var15)) {
                        double var38 = (var5 - var15) / 0.8;
                        float var40 = (float)(1.0 - var38);
                        PixelFx.sprite(var4, 1.5 + 9.0 * var38, 48 + Math.min(3, (int)(var38 * 4.0)), 1.0F, 0.8F, 0.4F, var40);
                        PixelFx.sprite(var4, 4.0 * (1.0 - var38) + 0.5, 4, 1.0F, 0.9F, 0.7F, var40);
                        shock(var4, null, (2.0 + 16.0 * var38) * 0.5, 1.0F, 0.55F, 0.2F, var40);
                    } else {
                        double var24 = (var5 - var13) / 2.4;
                        double var26 = 1.6 * (1.0 - 0.45 * var24);

                        for (int var28 = 0; var28 < 40; var28++) {
                            double var29 = hash(var28, 1) * 6.283 + var5 * (1.5 + 2.0 * hash(var28, 2));
                            double var31 = (hash(var28, 3) - 0.5) * 2.6;
                            double var33 = var26 * (0.8 + 0.5 * hash(var28, 4)) + Math.max(0.0, 1.0 - var24 * 3.0) * 6.0 * hash(var28, 5);
                            Vec3 var35 = var22.add(
                                Math.cos(var29) * Math.cos(var31) * var33, Math.sin(var31) * var33, Math.sin(var29) * Math.cos(var31) * var33
                            );
                            PixelFx.sprite(
                                var35,
                                0.45 + 0.4 * hash(var28, 6),
                                33,
                                (float)(0.55 + 0.45 * var24),
                                (float)(0.5 - 0.2 * var24),
                                (float)(0.45 - 0.3 * var24),
                                1.0F,
                                var29
                            );
                        }

                        for (int var42 = 0; var42 < 12; var42++) {
                            double var44 = hash(var42, 9) * 6.283 + var5 * 0.6;
                            double var48 = (hash(var42, 10) - 0.5) * 2.0;
                            Vec3 var50 = var22.add(
                                Math.cos(var44) * Math.cos(var48) * var26 * 0.8, Math.sin(var48) * var26 * 0.8, Math.sin(var44) * Math.cos(var48) * var26 * 0.8
                            );
                            PixelFx.sprite(var50, 0.6 + 0.6 * var24, 42 + var42 % 4, 1.0F, 0.25F, 0.2F, (float)(0.3 + 0.7 * var24), hash(var42, 11) * 6.28);
                        }

                        PixelFx.sprite(var22, var26 * 2.6, 4, 0.9F, 0.15F, 0.08F, (float)(0.25 + 0.5 * var24));
                        blackCore(var22, var2, 0.5 * (1.0 - 0.5 * var24), var5, (float)(1.0 - 0.6 * var24));
                    }
                }
            }
        }
    }

    private static void blackHole(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7, double var9) {
        float var11 = env(var5, var7, 0.15, 0.4);
        double var12 = sstep(0.0, 0.5, var5);
        double var14 = 6.0 + 1.5 * (double)var4;
        double var16 = (0.7 + 0.15 * (double)var4) * var12 * (1.0 + 0.04 * Math.sin(var9 * 20.0));
        Vec3 var18 = var2.subtract(var3).normalize();
        PixelFx.sprite(var3.subtract(var18.scale(0.3)), var16 * 5.5, 4, 1.0F, 0.32F, 0.05F, 0.75F * var11);
        PixelFx.sprite(var3.subtract(var18.scale(0.2)), var16 * 3.2, 4, 1.0F, 0.62F, 0.18F, 0.9F * var11);
        PixelFx.sprite(var3.subtract(var18.scale(0.1)), var16 * 2.6, 6, 1.0F, 0.82F, 0.4F, var11, var9 * 3.0);
        PixelFx.sprite(var3.add(var18.scale(0.15)), var16 * 2.0, 12, 0.0F, 0.0F, 0.0F, var11);
        PixelFx.sprite(var3.add(var18.scale(0.2)), var16 * 1.75, 58, 0.0F, 0.0F, 0.0F, var11);
        PixelFx.sprite(var3.add(var18.scale(0.25)), var16 * 1.3, 4, 0.0F, 0.0F, 0.0F, var11);

        for (int var19 = 0; var19 < 14; var19++) {
            double var20 = (double)var19 * 0.4488 + var9 * 2.4;
            Vec3 var22 = var3.subtract(var2).normalize();
            Vec3 var23 = perp(var22);
            Vec3 var24 = var22.cross(var23).normalize();
            Vec3 var25 = var3.add(var23.scale(Math.cos(var20) * var16 * 1.15)).add(var24.scale(Math.sin(var20) * var16 * 1.15));
            PixelFx.sprite(var25, var16 * 0.9, PixelFx.anim(16, 8, var9 + (double)var19 * 0.17, 14.0), 1.0F, 1.0F, 1.0F, var11, var20 - (Math.PI / 2));
        }

        for (int var31 = 0; var31 < 46; var31++) {
            double var32 = (var5 * (0.35 + 0.3 * hash(var31, 1)) + hash(var31, 2)) % 1.0;
            double var33 = var14 * (1.0 - var32) + var16;
            double var34 = hash(var31, 3) * 6.283 + var32 * 9.0;
            double var26 = (hash(var31, 4) - 0.5) * var33 * 0.5 * (1.0 - var32);
            Vec3 var28 = var3.add(Math.cos(var34) * var33, var26, Math.sin(var34) * var33);
            int var29 = var31 % 4 == 0 ? 13 : (var31 % 4 == 1 ? 46 : 33);
            float var30 = var29 == 13 ? 1.0F : 0.85F;
            PixelFx.sprite(
                var28,
                var29 == 33 ? 0.45 + 0.4 * hash(var31, 5) : 0.5,
                var29,
                var30,
                var29 == 13 ? 0.65F : 0.8F,
                var29 == 13 ? 0.25F : 0.75F,
                (float)Math.sin(var32 * Math.PI) * var11,
                var32 * 12.0 + (double)var31
            );
        }

        ring(var0, var1, var2, var3, UP, var16 * 1.6 + 0.3 * Math.sin(var9 * 5.0), 0.05, 1.0F, 0.55F, 0.15F, 0.7F * var11);
    }

    private static void golemFist(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, int var4, double var5, double var7, double var9) {
        double var11 = 1.0 + 0.25 * (double)var4;
        circle(var0, var1, var2, var3.add(0.0, 0.06, 0.0), UP, 2.3 * var11, GOLEM_C, var9, Math.min(1.0, var5 / 0.18), (float)clamp(1.4 - var5 * 0.8), 28);
        PixelFx.flat(var3.add(0.0, 0.05, 0.0), UP, 5.0 * var11, 4, 0.0, 0.6F, 0.4F, 0.95F, 0.35F * (float)clamp(1.2 - var5));
        double var13 = sstep(0.1, 0.35, var5);
        double var15 = sstep(var7 - 0.8, var7, var5);
        double var17 = (2.6 * var13 - 1.8 * var15) * var11;
        float var19 = 1.0F - (float)var15 * 0.3F;

        for (int var20 = 0; var20 < 4; var20++) {
            double var21 = var17 - (double)(var20 + 1) * 0.75 * var11;
            if (!(var21 < -0.9 * var11)) {
                stoneBox(var3.add(0.0, var21, 0.0), 0.5 * var11 - (double)var20 * 0.03, 0.15 * (double)var20, 0.04 * (double)var20, 0.9F, var19);
            }
        }

        stoneBox(var3.add(0.0, var17 + 0.3 * var11, 0.0), 0.95 * var11, 0.3, 0.12, 1.0F, var19);

        for (int var24 = -1; var24 <= 2; var24++) {
            stoneBox(var3.add(((double)var24 - 0.5) * 0.5 * var11, var17 + 1.3 * var11, 0.45 * var11), 0.3 * var11, 0.3, 0.12, 0.95F, var19);
        }

        stoneBox(var3.add(1.05 * var11, var17 + 0.55 * var11, -0.2 * var11), 0.32 * var11, 0.5, 0.3, 0.9F, var19);
        double var25 = var5 - 0.3;
        if (var25 > 0.0 && var25 < 1.4) {
            shock(var3.add(0.0, 0.05, 0.0), UP, (1.5 + var25 * 7.0) * var11 * 0.5, 0.75F, 0.68F, 0.55F, (float)clamp(1.0 - var25));

            for (int var22 = 0; var22 < 16; var22++) {
                Vec3 var23 = new Vec3(hash(var22, 1) - 0.5, 0.0, hash(var22, 2) - 0.5).normalize();
                PixelFx.sprite(
                    var3.add(var23.scale((0.8 + var25 * 3.0) * var11)).add(0.0, var25 * (4.0 + 3.0 * hash(var22, 3)) - var25 * var25 * 9.0 + 0.2, 0.0),
                    0.4 + 0.35 * hash(var22, 4),
                    33,
                    1.0F,
                    1.0F,
                    1.0F,
                    (float)clamp(1.4 - var25),
                    var25 * 7.0 + (double)var22
                );
            }

            for (int var26 = 0; var26 < 8; var26++) {
                PixelFx.sprite(
                    var3.add((hash(var26, 6) - 0.5) * 3.0 * var11, 0.4 + var25 * 0.6, (hash(var26, 7) - 0.5) * 3.0 * var11),
                    (1.4 + var25 * 1.5) * var11,
                    7 + Math.min(3, (int)(var25 * 3.0)),
                    0.78F,
                    0.72F,
                    0.62F,
                    0.75F * (float)clamp(1.2 - var25)
                );
            }
        }

        if (var15 > 0.0) {
            for (int var27 = 0; var27 < 10; var27++) {
                PixelFx.sprite(
                    var3.add((hash(var27, 8) - 0.5) * 1.6 * var11, var17 + hash(var27, 9) * 2.0 * var11 - var15 * 2.0, (hash(var27, 10) - 0.5) * 1.6 * var11),
                    0.45,
                    33,
                    1.0F,
                    1.0F,
                    1.0F,
                    (float)(1.0 - var15),
                    var15 * 6.0 + (double)var27
                );
            }
        }
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
