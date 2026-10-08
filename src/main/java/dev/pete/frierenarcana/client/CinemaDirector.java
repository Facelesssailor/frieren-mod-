package dev.pete.frierenarcana.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;

public final class CinemaDirector {
    private static long lockedFor = -1L;
    private static float lockYaw;
    private static float lockPitch;
    private static Vec3 lockLook = new Vec3(0.0, 0.0, 1.0);
    private static Vec3 anchor = null;
    private static Vec3 sEye;
    private static Vec3 sEyePrev;
    private static Vec3 sFlat;
    private static Vec3 sSide;
    private static Vec3 sCenter;
    private static Vec3 sHit;
    private static double sRadius;
    private static boolean sFrac;
    private static boolean sReady;
    private static int shot = -1;
    private static float bands = 0.0F;
    private static float descent = 0.0F;
    private static long fractureSeen = 0L;
    static final double END_AFTER_SHATTER = 15.799999999999999;

    private CinemaDirector() {
    }

    public static Vec3 lockedLook() {
        return lockLook;
    }

    public static float bandTag(double var0) {
        double var2 = 1.0 - 2.4 * (double)descent;
        double var4 = (double)bands * Math.max(0.0, Math.min(1.0, (var0 - var2) / 0.4));
        return 0.6F + 0.04F * (float)var4;
    }

    public static long fractureNanos() {
        return ArcanaCinematic.active() ? fractureSeen : 0L;
    }

    public static void tick(Post var0) {
        Minecraft var1 = Minecraft.getInstance();

        while (ArcanaKeys.SKIP.consumeClick()) {
            ArcanaCinematic.skip();
        }

        if (!ArcanaCinematic.active()) {
            lockedFor = -1L;
            shot = -1;
            bands = 0.0F;
            descent = 0.0F;
            sReady = false;
            sEye = null;
        } else {
            LocalPlayer var2 = var1.player;
            if (var2 != null && var1.level != null && ArcanaCinematic.camera.level() == var1.level && var2.isAlive() && var1.screen == null) {
                if (BlackHoleCinema.running()) {
                    BlackHoleCinema.tick(var1, var2);
                } else {
                    long var3 = System.nanoTime();
                    double var5 = (double)(var3 - ArcanaCinematic.started) / 1.0E9;
                    double var7 = ArcanaCinematic.release == 0L ? 0.0 : (double)(var3 - ArcanaCinematic.release) / 1.0E9;
                    bands = ArcanaCinematic.release == 0L ? 0.0F : 1.0F;
                    double var9 = Math.min(1.0, Math.max(0.0, (var5 - 0.3) / 3.6));
                    descent = (float)(var9 * var9 * (3.0 - 2.0 * var9));
                    if (lockedFor != ArcanaCinematic.started) {
                        lockedFor = ArcanaCinematic.started;
                        lockYaw = var2.getYRot();
                        lockPitch = var2.getXRot();
                        lockLook = var2.getLookAngle().normalize();
                        shot = -1;
                        fractureSeen = 0L;
                        anchor = var2.position().add(0.0, 1.3, 0.0);
                    }

                    var2.setYRot(lockYaw);
                    var2.setXRot(lockPitch);
                    var2.yRotO = lockYaw;
                    var2.xRotO = lockPitch;
                    var2.yHeadRot = lockYaw;
                    var2.yBodyRot = lockYaw;
                    var2.yBodyRotO = lockYaw;
                    Vec3 var38 = lockLook;
                    Vec3 var10 = new Vec3(var38.x, 0.0, var38.z);
                    var10 = var10.lengthSqr() < 0.01 ? new Vec3(0.0, 0.0, 1.0) : var10.normalize();
                    Vec3 var11 = new Vec3(-var10.z, 0.0, var10.x);
                    Vec3 var12 = var2.position().add(0.0, 1.3, 0.0);
                    anchor = anchor != null && !(anchor.distanceToSqr(var12) > 64.0) ? anchor.lerp(var12, 0.08) : var12;
                    Vec3 var13 = anchor;
                    ArcanaClient.Fracture var14 = null;

                    for (ArcanaClient.Fracture var16 : ArcanaClient.fractures()) {
                        if (ArcanaCinematic.release != 0L
                            && var16.startNanos() >= ArcanaCinematic.release - 400000000L
                            && (var14 == null || var16.startNanos() > var14.startNanos())) {
                            var14 = var16;
                        }
                    }

                    Vec3 var40 = null;
                    double var41 = 0.0;
                    Vec3 var18 = null;
                    if (var14 != null) {
                        var40 = var14.field().center();
                        var41 = (double)var14.field().radius();
                        var18 = var14.impact();
                        if (fractureSeen == 0L) {
                            fractureSeen = var14.startNanos();
                        }
                    } else {
                        double var19 = Double.POSITIVE_INFINITY;

                        for (ArcanaClient.VisualField var22 : ArcanaClient.fields()) {
                            Vec3 var23 = var13.subtract(var22.center());
                            double var24 = var23.dot(var38);
                            double var26 = var23.lengthSqr() - (double)var22.radius() * (double)var22.radius();
                            double var28 = var24 * var24 - var26;
                            if (!(var28 < 0.0)) {
                                double var30 = Math.sqrt(var28);
                                double var32 = -var24 - var30;
                                double var34 = -var24 + var30;
                                double var36 = var32 > 0.5 ? var32 : (var34 > 0.5 ? var34 : Double.POSITIVE_INFINITY);
                                if (var36 < var19) {
                                    var19 = var36;
                                    var40 = var22.center();
                                    var41 = (double)var22.radius();
                                    var18 = var13.add(var38.scale(var36));
                                }
                            }
                        }
                    }

                    if (var14 != null) {
                        bands = 1.0F;
                    }

                    double var42 = fractureSeen == 0L ? -1.0 : (double)(var3 - fractureSeen) / 1.0E9;
                    if (!(var5 > 40.0)
                        && (fractureSeen == 0L || !(var42 > 15.799999999999999))
                        && (fractureSeen != 0L || ArcanaCinematic.release == 0L || !(var7 > 3.6))) {
                        sEyePrev = sEye == null ? var13 : sEye;
                        sEye = var13;
                        sFlat = var10;
                        sSide = var11;
                        sCenter = var40;
                        sRadius = var41;
                        sHit = var18;
                        sFrac = var14 != null;
                        sReady = true;
                    } else {
                        ArcanaCinematic.restore();
                    }
                }
            } else {
                ArcanaCinematic.restore();
            }
        }
    }

    static void frame(float var0) {
        if (sReady && sEye != null) {
            Minecraft var1 = Minecraft.getInstance();
            if (var1.level != null && var1.player != null) {
                long var2 = System.nanoTime();
                double var4 = (double)(var2 - ArcanaCinematic.started) / 1.0E9;
                double var6 = ArcanaCinematic.release == 0L ? 0.0 : (double)(var2 - ArcanaCinematic.release) / 1.0E9;
                Vec3 var8 = sEyePrev.lerp(sEye, (double)var0);
                Vec3 var9 = sFlat;
                Vec3 var10 = sSide;
                Vec3 var11 = sCenter;
                Vec3 var12 = sHit;
                double var13 = sRadius;
                if (!sFrac || var11 != null && var12 != null) {
                    Vec3 var15;
                    Vec3 var16;
                    byte var17;
                    if (ArcanaCinematic.release == 0L) {
                        if (var4 < 3.4) {
                            var17 = 10;
                            double var18 = var4 / 3.4;
                            var15 = var8.add(var9.scale(3.9 - 0.6 * var18)).add(var10.scale(0.45 - 0.2 * var18)).add(0.0, -0.15, 0.0);
                            var16 = var8.add(0.0, -0.3, 0.0).add(var9.scale(0.6));
                        } else if (var4 < 7.0) {
                            var17 = 11;
                            double var30 = (var4 - 3.4) / 3.6;
                            var15 = var8.add(var9.scale(10.0 + 1.5 * var30)).add(var10.scale(6.5)).add(0.0, 0.3 + 0.4 * var30, 0.0);
                            var16 = var8.add(0.0, 0.5 + 0.5 * var30, 0.0);
                        } else {
                            var17 = 0;
                            double var31 = Math.min(1.0, (var4 - 7.0) / 3.2);
                            double var20 = Math.sin(var31 * Math.PI * 0.6) * 0.9;
                            Vec3 var22 = var9.scale(-Math.cos(var20)).add(var10.scale(Math.sin(var20)));
                            var15 = var8.add(var22.scale(5.2 - 1.6 * var31)).add(0.0, -0.45 + 0.9 * var31, 0.0);
                            var16 = var8.add(0.0, 0.15, 0.0).add(var9.scale(0.5));
                        }
                    } else if (!sFrac) {
                        var17 = 1;
                        var15 = var8.add(var9.scale(-4.2)).add(var10.scale(1.5)).add(0.0, 0.9, 0.0);
                        var16 = var8.add(0.0, 6.0 + 10.0 * Math.min(1.0, var6), 0.0);
                    } else if (var6 < 0.9500000000000001) {
                        var17 = 0;
                        double var32 = Math.sin(Math.PI * 3.0 / 5.0) * 0.9;
                        Vec3 var43 = var9.scale(-Math.cos(var32)).add(var10.scale(Math.sin(var32)));
                        double var21 = Math.max(0.0, Math.min(1.0, (var6 - 0.4) / 0.55));
                        var15 = var8.add(var43.scale(3.6)).add(0.0, 0.45, 0.0);
                        var16 = var8.add(0.0, 0.15 + 2.6 * var21 * var21, 0.0).add(var9.scale(0.5));
                    } else if (var6 < 3.1) {
                        var17 = 1;
                        Vec3 var33 = new Vec3(var8.x - var11.x, 0.0, var8.z - var11.z);
                        var33 = var33.lengthSqr() < 0.25 ? var9.scale(-1.0) : var33.normalize();
                        double var19 = Math.cos(0.5);
                        double var44 = Math.sin(0.5);
                        Vec3 var23 = new Vec3(var33.x * var19 - var33.z * var44, 0.0, var33.x * var44 + var33.z * var19);
                        double var24 = Math.pow(Math.min(1.0, var6 / 2.6), 1.6);
                        var15 = var11.add(var23.scale(var13 * 1.45 + 12.0)).add(0.0, var13 * 0.22 + 2.0, 0.0);
                        var16 = var8.lerp(var12, 0.35 + 0.45 * var24);
                    } else if (var6 < 5.800000000000001) {
                        var17 = 2;
                        Vec3 var35 = new Vec3(var8.x - var11.x, 0.0, var8.z - var11.z);
                        var35 = var35.lengthSqr() < 0.25 ? var9.scale(-1.0) : var35.normalize();
                        double var41 = (var6 - 2.6 - 0.5) / 2.7;
                        double var45 = Math.min(16.0, Math.max(7.0, var13 * 0.5)) * (1.0 - 0.15 * var41);
                        var15 = var12.add(var35.scale(var45)).add(new Vec3(-var35.z, 0.0, var35.x).scale(2.5)).add(0.0, -var45 * 0.35 + 1.0 * var41, 0.0);
                        var16 = var12.add(0.0, -var13 * 0.1 * var41, 0.0);
                    } else if (var6 < 11.799999999999999) {
                        var17 = 3;
                        Vec3 var37 = new Vec3(var8.x - var11.x, 0.0, var8.z - var11.z);
                        var37 = var37.lengthSqr() < 0.25 ? var9 : var37.normalize();
                        double var42 = (var6 - 2.6 - 3.2) / 6.0;
                        double var46 = 0.6 + 0.25 * var42;
                        double var47 = Math.cos(var46);
                        double var25 = Math.sin(var46);
                        Vec3 var27 = new Vec3(var37.x * var47 - var37.z * var25, 0.0, var37.x * var25 + var37.z * var47);
                        double var28 = var13 * 1.55 + 10.0 + 4.0 * var42;
                        var15 = var11.add(var27.scale(var28)).add(0.0, var13 * 0.45 + 2.0 + 2.5 * var42, 0.0);
                        var16 = var11.add(0.0, var13 * (0.35 - 0.1 * var42), 0.0);
                    } else {
                        var17 = 4;
                        double var39 = (var6 - 2.6 - 9.2) / 3.0;
                        var15 = var8.add(var9.scale(3.0 - 0.3 * var39)).add(var10.scale(0.7 - 0.4 * var39)).add(0.0, 0.05 + 0.1 * var39, 0.0);
                        var16 = var8.add(0.0, -0.05, 0.0);
                    }

                    BlockHitResult var40 = var1.level.clip(new ClipContext(var16, var15, Block.VISUAL, Fluid.NONE, var1.player));
                    if (var40.getType() != Type.MISS) {
                        var15 = var40.getLocation().lerp(var16, 0.12);
                    }

                    CinemaFrame.place(var15, var16, var17, var17 != 1 && var17 != 3 && var17 != 11 ? 0.24 : 0.12);
                }
            }
        }
    }
}
