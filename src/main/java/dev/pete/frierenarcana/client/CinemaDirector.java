package dev.pete.frierenarcana.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
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
    private static int shot = -1;
    private static float bands = 0.0F;
    private static float descent = 0.0F;
    private static long fractureSeen = 0L;
    static final double END_AFTER_SHATTER = 15.0;

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
        } else {
            LocalPlayer var2 = var1.player;
            if (var2 != null && var1.level != null && ArcanaCinematic.camera.level() == var1.level && var2.isAlive() && var1.screen == null) {
                if (BlackHoleCinema.running()) {
                    BlackHoleCinema.tick(var1, var2);
                } else {
                    long var3 = System.nanoTime();
                    double var5 = (double)(var3 - ArcanaCinematic.started) / 1.0E9;
                    double var7 = ArcanaCinematic.release == 0L ? 0.0 : (double)(var3 - ArcanaCinematic.release) / 1.0E9;
                    bands = ArcanaCinematic.release == 0L
                        ? (float)(0.25 + 0.45 * Math.min(1.0, var5 / 5.0))
                        : (float)Math.min(1.0, 0.75 + 0.25 * Math.min(1.0, var7 / 1.5));
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
                        if ((ArcanaCinematic.release == 0L || var16.startNanos() >= ArcanaCinematic.release - 400000000L)
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
                    if (!(var5 > 40.0) && (fractureSeen == 0L || !(var42 > 15.0)) && (fractureSeen != 0L || ArcanaCinematic.release == 0L || !(var7 > 3.6))) {
                        Vec3 var43;
                        Vec3 var44;
                        byte var45;
                        if (ArcanaCinematic.release == 0L) {
                            if (var5 < 3.4) {
                                var45 = 10;
                                double var46 = var5 / 3.4;
                                var43 = var13.add(var10.scale(3.9 - 0.6 * var46)).add(var11.scale(0.45 - 0.2 * var46)).add(0.0, -0.15, 0.0);
                                var44 = var13.add(0.0, -0.3, 0.0).add(var10.scale(0.6));
                            } else if (var5 < 7.0) {
                                var45 = 11;
                                double var47 = (var5 - 3.4) / 3.6;
                                var43 = var13.add(var10.scale(10.0 + 1.5 * var47)).add(var11.scale(6.5)).add(0.0, 0.3 + 0.4 * var47, 0.0);
                                var44 = var13.add(0.0, 2.0 + 4.0 * var47, 0.0);
                            } else {
                                var45 = 0;
                                double var48 = Math.min(1.0, (var5 - 7.0) / 3.2);
                                double var60 = Math.sin(var48 * Math.PI * 0.6) * 0.9;
                                Vec3 var65 = var10.scale(-Math.cos(var60)).add(var11.scale(Math.sin(var60)));
                                var43 = var13.add(var65.scale(5.2 - 1.6 * var48)).add(0.0, -0.45 + 0.9 * var48, 0.0);
                                var44 = var13.add(0.0, 0.15, 0.0).add(var10.scale(0.5));
                            }
                        } else if (var14 == null) {
                            var45 = 1;
                            var43 = var13.add(var10.scale(-4.2)).add(var11.scale(1.5)).add(0.0, 0.9, 0.0);
                            var44 = var13.add(0.0, 6.0 + 10.0 * Math.min(1.0, var7), 0.0);
                        } else if (var7 < 2.3) {
                            var45 = 1;
                            Vec3 var49 = new Vec3(var13.x - var40.x, 0.0, var13.z - var40.z);
                            var49 = var49.lengthSqr() < 0.25 ? var10.scale(-1.0) : var49.normalize();
                            double var25 = Math.cos(0.5);
                            double var27 = Math.sin(0.5);
                            Vec3 var29 = new Vec3(var49.x * var25 - var49.z * var27, 0.0, var49.x * var27 + var49.z * var25);
                            double var69 = Math.pow(Math.min(1.0, var7 / 1.8), 1.6);
                            var43 = var40.add(var29.scale(var41 * 1.45 + 12.0)).add(0.0, var41 * 0.22 + 2.0, 0.0);
                            var44 = var13.lerp(var18, 0.35 + 0.45 * var69);
                        } else if (var7 < 5.0) {
                            var45 = 2;
                            Vec3 var51 = new Vec3(var13.x - var40.x, 0.0, var13.z - var40.z);
                            var51 = var51.lengthSqr() < 0.25 ? var10.scale(-1.0) : var51.normalize();
                            double var57 = (var7 - 1.8 - 0.5) / 2.7;
                            double var62 = Math.min(16.0, Math.max(7.0, var41 * 0.5)) * (1.0 - 0.15 * var57);
                            var43 = var18.add(var51.scale(var62)).add(new Vec3(-var51.z, 0.0, var51.x).scale(2.5)).add(0.0, -var62 * 0.35 + 1.0 * var57, 0.0);
                            var44 = var18.add(0.0, -var41 * 0.1 * var57, 0.0);
                        } else if (var7 < 11.0) {
                            var45 = 3;
                            Vec3 var53 = new Vec3(var13.x - var40.x, 0.0, var13.z - var40.z);
                            var53 = var53.lengthSqr() < 0.25 ? var10 : var53.normalize();
                            double var58 = (var7 - 1.8 - 3.2) / 6.0;
                            double var63 = 0.6 + 0.25 * var58;
                            double var67 = Math.cos(var63);
                            double var31 = Math.sin(var63);
                            Vec3 var33 = new Vec3(var53.x * var67 - var53.z * var31, 0.0, var53.x * var31 + var53.z * var67);
                            double var71 = var41 * 1.55 + 10.0 + 4.0 * var58;
                            var43 = var40.add(var33.scale(var71)).add(0.0, var41 * 0.45 + 2.0 + 2.5 * var58, 0.0);
                            var44 = var40.add(0.0, var41 * (0.35 - 0.1 * var58), 0.0);
                        } else {
                            var45 = 4;
                            double var55 = (var7 - 1.8 - 9.2) / 3.0;
                            var43 = var13.add(var10.scale(3.0 - 0.3 * var55)).add(var11.scale(0.7 - 0.4 * var55)).add(0.0, 0.05 + 0.1 * var55, 0.0);
                            var44 = var13.add(0.0, -0.05, 0.0);
                        }

                        BlockHitResult var56 = var1.level.clip(new ClipContext(var44, var43, Block.VISUAL, Fluid.NONE, var2));
                        if (var56.getType() != Type.MISS) {
                            var43 = var56.getLocation().lerp(var44, 0.12);
                        }

                        boolean var59 = var45 != shot;
                        Vec3 var61 = !var59 && ArcanaCinematic.previous != null
                            ? ArcanaCinematic.previous.lerp(var43, var45 != 1 && var45 != 3 && var45 != 11 ? 0.24 : 0.12)
                            : var43;
                        shot = var45;
                        ArcanaCinematic.previous = var61;
                        ArmorStand var64 = ArcanaCinematic.camera;
                        var64.xo = var64.getX();
                        var64.yo = var64.getY();
                        var64.zo = var64.getZ();
                        var64.yRotO = var64.getYRot();
                        var64.xRotO = var64.getXRot();
                        var64.setPos(var61.x, var61.y - (double)var64.getEyeHeight(), var61.z);
                        Vec3 var66 = var44.subtract(var61);
                        float var68 = (float)(Math.toDegrees(Math.atan2(var66.z, var66.x)) - 90.0);
                        if (!var59) {
                            var68 = var64.getYRot() + Mth.wrapDegrees(var68 - var64.getYRot());
                        }

                        float var70 = (float)(-Math.toDegrees(Math.atan2(var66.y, var66.horizontalDistance())));
                        if (var59) {
                            var64.yRotO = var68;
                            var64.xRotO = var70;
                            var64.xo = var64.getX();
                            var64.yo = var64.getY();
                            var64.zo = var64.getZ();
                        }

                        var64.setYRot(var68);
                        var64.setXRot(var70);
                        var64.setYHeadRot(var68);
                        var64.setYBodyRot(var68);
                    } else {
                        ArcanaCinematic.restore();
                    }
                }
            } else {
                ArcanaCinematic.restore();
            }
        }
    }
}
