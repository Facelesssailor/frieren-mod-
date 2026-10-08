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
    static final double END_AFTER_SHATTER = 19.799999999999997;

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
                        && (fractureSeen == 0L || !(var42 > 19.799999999999997))
                        && (fractureSeen != 0L || ArcanaCinematic.release == 0L || !(var7 > 6.1))) {
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
                    int[] var15 = new int[1];
                    Vec3[] var16 = shot(var4, var6, ArcanaCinematic.release != 0L, sFrac, var8, var9, var10, var11, var13, var12, var15);
                    Vec3 var17 = var16[0];
                    Vec3 var18 = var16[1];
                    int var19 = var15[0];
                    BlockHitResult var20 = var1.level.clip(new ClipContext(var18, var17, Block.VISUAL, Fluid.NONE, var1.player));
                    if (var20.getType() != Type.MISS) {
                        var17 = var20.getLocation().lerp(var18, 0.12);
                    }

                    CinemaFrame.place(var17, var18, var19, ease(var19));
                }
            }
        }
    }

    static double ease(int var0) {
        return var0 != 1 && var0 != 3 && var0 != 11 && var0 != 6 ? 0.24 : 0.12;
    }

    private static double ss(double var0, double var2, double var4) {
        var4 = Math.max(0.0, Math.min(1.0, (var4 - var0) / (var2 - var0)));
        return var4 * var4 * (3.0 - 2.0 * var4);
    }

    static Vec3[] shot(double var0, double var2, boolean var4, boolean var5, Vec3 var6, Vec3 var7, Vec3 var8, Vec3 var9, double var10, Vec3 var12, int[] var13) {
        double var17 = var2 - 6.2;
        Vec3 var14;
        Vec3 var15;
        byte var16;
        if (!var4) {
            if (var0 < 3.4) {
                var16 = 10;
                double var19 = var0 / 3.4;
                var14 = var6.add(var7.scale(3.9 - 0.6 * var19)).add(var8.scale(0.45 - 0.2 * var19)).add(0.0, -0.15, 0.0);
                var15 = var6.add(0.0, -0.3, 0.0).add(var7.scale(0.6));
            } else if (var0 < 7.0) {
                var16 = 11;
                double var25 = (var0 - 3.4) / 3.6;
                var14 = var6.add(var7.scale(10.0 + 1.5 * var25)).add(var8.scale(6.5)).add(0.0, 0.3 + 0.4 * var25, 0.0);
                var15 = var6.add(0.0, 0.5 + 0.5 * var25, 0.0);
            } else {
                var16 = 0;
                double var26 = Math.min(1.0, (var0 - 7.0) / 3.2);
                double var21 = Math.sin(var26 * Math.PI * 0.6) * 0.9;
                Vec3 var23 = var7.scale(-Math.cos(var21)).add(var8.scale(Math.sin(var21)));
                var14 = var6.add(var23.scale(5.2 - 1.6 * var26)).add(0.0, -0.45 + 0.9 * var26, 0.0);
                var15 = var6.add(0.0, 0.15, 0.0).add(var7.scale(0.5));
            }
        } else if (!var5 || var9 == null || var12 == null) {
            var16 = 5;
            double var35 = ss(3.1, 4.3, var2);
            var14 = var6.add(var7.scale(2.7)).add(var8.scale(0.25)).add(0.0, -0.25 + 0.4 * var35, 0.0);
            var15 = var6.add(0.0, -0.25 + 8.0 * var35 * var35, 0.0).add(var7.scale(0.4));
        } else if (var2 < 4.2) {
            var16 = 5;
            double var27 = ss(3.25, 4.15, var2);
            var14 = var6.add(var7.scale(2.7 - 0.3 * ss(0.0, 3.1, var2))).add(var8.scale(0.25)).add(0.0, -0.25 + 0.2 * var27, 0.0);
            var15 = var6.add(0.0, -0.28 + 1.9 * var27, 0.0).add(var7.scale(0.4));
        } else if (var2 < 5.1) {
            var16 = 6;
            double var28 = Math.pow(Math.max(0.0, Math.min(1.0, (var2 - 3.1) / 3.1)), 1.6);
            Vec3 var37 = var6.add(0.0, -0.57, 0.0).lerp(var12, var28);
            Vec3 var22 = new Vec3(var6.x - var9.x, 0.0, var6.z - var9.z);
            var22 = var22.lengthSqr() < 0.25 ? var7 : var22.normalize();
            var14 = var37.add(var22.scale(4.5)).add(new Vec3(-var22.z, 0.0, var22.x).scale(1.5)).add(0.0, -2.5, 0.0);
            var15 = var37.add(0.0, 1.5, 0.0);
        } else if (var17 < 1.6) {
            var16 = 1;
            Vec3 var29 = new Vec3(var6.x - var9.x, 0.0, var6.z - var9.z);
            var29 = var29.lengthSqr() < 0.25 ? var7.scale(-1.0) : var29.normalize();
            double var20 = Math.cos(0.5);
            double var39 = Math.sin(0.5);
            Vec3 var24 = new Vec3(var29.x * var20 - var29.z * var39, 0.0, var29.x * var39 + var29.z * var20);
            var14 = var9.add(var24.scale(var10 * 1.35 + 10.0)).add(0.0, var10 * 0.22 + 3.0, 0.0);
            var15 = var9.add(0.0, var10 * 0.38, 0.0);
        } else if (var17 < 7.4) {
            var16 = 2;
            Vec3 var31 = new Vec3(var9.x - var6.x, 0.0, var9.z - var6.z);
            var31 = var31.lengthSqr() < 1.0 ? var7 : var31.normalize();
            double var36 = Math.max(6.0, Math.min(18.0, var10 * 0.4));
            double var40 = ss(5.3, 6.8, var17);
            var14 = var6.add(var31.scale(var36)).add(0.0, -0.7, 0.0);
            var15 = var6.add(0.0, var36 * (0.3 + 0.12 * var40), 0.0);
        } else if (var17 < 9.6) {
            var16 = 3;
            double var33 = (var17 - 5.0 - 2.4) / 2.2;
            var14 = var6.add(var7.scale(-2.2)).add(var8.scale(1.4)).add(0.0, -0.2, 0.0);
            var15 = var6.add(var7.scale(7.0)).add(var8.scale(3.0 + 2.0 * var33)).add(0.0, 5.5 - 1.0 * var33, 0.0);
        } else {
            var16 = 4;
            double var34 = (var17 - 5.0 - 4.6) / 3.0;
            var14 = var6.add(var7.scale(3.2 - 0.3 * var34)).add(var8.scale(0.7 - 0.4 * var34)).add(0.0, 0.05 + 0.1 * var34, 0.0);
            var15 = var6.add(0.0, 0.05, 0.0);
        }

        var13[0] = var16;
        return new Vec3[]{var14, var15};
    }
}
