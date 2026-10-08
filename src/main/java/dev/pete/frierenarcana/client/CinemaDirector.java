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
                                var44 = var13.add(0.0, 0.5 + 0.5 * var47, 0.0);
                            } else {
                                var45 = 0;
                                double var48 = Math.min(1.0, (var5 - 7.0) / 3.2);
                                double var61 = Math.sin(var48 * Math.PI * 0.6) * 0.9;
                                Vec3 var68 = var10.scale(-Math.cos(var61)).add(var11.scale(Math.sin(var61)));
                                var43 = var13.add(var68.scale(5.2 - 1.6 * var48)).add(0.0, -0.45 + 0.9 * var48, 0.0);
                                var44 = var13.add(0.0, 0.15, 0.0).add(var10.scale(0.5));
                            }
                        } else if (var14 == null) {
                            var45 = 1;
                            var43 = var13.add(var10.scale(-4.2)).add(var11.scale(1.5)).add(0.0, 0.9, 0.0);
                            var44 = var13.add(0.0, 6.0 + 10.0 * Math.min(1.0, var7), 0.0);
                        } else if (var7 < 0.9500000000000001) {
                            var45 = 0;
                            double var49 = Math.sin(Math.PI * 3.0 / 5.0) * 0.9;
                            Vec3 var62 = var10.scale(-Math.cos(var49)).add(var11.scale(Math.sin(var49)));
                            double var27 = Math.max(0.0, Math.min(1.0, (var7 - 0.4) / 0.55));
                            var43 = var13.add(var62.scale(3.6)).add(0.0, 0.45, 0.0);
                            var44 = var13.add(0.0, 0.15 + 2.6 * var27 * var27, 0.0).add(var10.scale(0.5));
                        } else if (var7 < 2.3) {
                            var45 = 1;
                            Vec3 var50 = new Vec3(var13.x - var40.x, 0.0, var13.z - var40.z);
                            var50 = var50.lengthSqr() < 0.25 ? var10.scale(-1.0) : var50.normalize();
                            double var25 = Math.cos(0.5);
                            double var64 = Math.sin(0.5);
                            Vec3 var29 = new Vec3(var50.x * var25 - var50.z * var64, 0.0, var50.x * var64 + var50.z * var25);
                            double var72 = Math.pow(Math.min(1.0, var7 / 1.8), 1.6);
                            var43 = var40.add(var29.scale(var41 * 1.45 + 12.0)).add(0.0, var41 * 0.22 + 2.0, 0.0);
                            var44 = var13.lerp(var18, 0.35 + 0.45 * var72);
                        } else if (var7 < 5.0) {
                            var45 = 2;
                            Vec3 var52 = new Vec3(var13.x - var40.x, 0.0, var13.z - var40.z);
                            var52 = var52.lengthSqr() < 0.25 ? var10.scale(-1.0) : var52.normalize();
                            double var58 = (var7 - 1.8 - 0.5) / 2.7;
                            double var65 = Math.min(16.0, Math.max(7.0, var41 * 0.5)) * (1.0 - 0.15 * var58);
                            var43 = var18.add(var52.scale(var65)).add(new Vec3(-var52.z, 0.0, var52.x).scale(2.5)).add(0.0, -var65 * 0.35 + 1.0 * var58, 0.0);
                            var44 = var18.add(0.0, -var41 * 0.1 * var58, 0.0);
                        } else if (var7 < 11.0) {
                            var45 = 3;
                            Vec3 var54 = new Vec3(var13.x - var40.x, 0.0, var13.z - var40.z);
                            var54 = var54.lengthSqr() < 0.25 ? var10 : var54.normalize();
                            double var59 = (var7 - 1.8 - 3.2) / 6.0;
                            double var66 = 0.6 + 0.25 * var59;
                            double var70 = Math.cos(var66);
                            double var31 = Math.sin(var66);
                            Vec3 var33 = new Vec3(var54.x * var70 - var54.z * var31, 0.0, var54.x * var31 + var54.z * var70);
                            double var74 = var41 * 1.55 + 10.0 + 4.0 * var59;
                            var43 = var40.add(var33.scale(var74)).add(0.0, var41 * 0.45 + 2.0 + 2.5 * var59, 0.0);
                            var44 = var40.add(0.0, var41 * (0.35 - 0.1 * var59), 0.0);
                        } else {
                            var45 = 4;
                            double var56 = (var7 - 1.8 - 9.2) / 3.0;
                            var43 = var13.add(var10.scale(3.0 - 0.3 * var56)).add(var11.scale(0.7 - 0.4 * var56)).add(0.0, 0.05 + 0.1 * var56, 0.0);
                            var44 = var13.add(0.0, -0.05, 0.0);
                        }

                        BlockHitResult var57 = var1.level.clip(new ClipContext(var44, var43, Block.VISUAL, Fluid.NONE, var2));
                        if (var57.getType() != Type.MISS) {
                            var43 = var57.getLocation().lerp(var44, 0.12);
                        }

                        boolean var60 = var45 != shot;
                        Vec3 var63 = !var60 && ArcanaCinematic.previous != null
                            ? ArcanaCinematic.previous.lerp(var43, var45 != 1 && var45 != 3 && var45 != 11 ? 0.24 : 0.12)
                            : var43;
                        shot = var45;
                        ArcanaCinematic.previous = var63;
                        ArmorStand var67 = ArcanaCinematic.camera;
                        var67.xo = var67.getX();
                        var67.yo = var67.getY();
                        var67.zo = var67.getZ();
                        var67.yRotO = var67.getYRot();
                        var67.xRotO = var67.getXRot();
                        var67.setPos(var63.x, var63.y - (double)var67.getEyeHeight(), var63.z);
                        Vec3 var69 = var44.subtract(var63);
                        float var71 = (float)(Math.toDegrees(Math.atan2(var69.z, var69.x)) - 90.0);
                        if (!var60) {
                            var71 = var67.getYRot() + Mth.wrapDegrees(var71 - var67.getYRot());
                        }

                        float var73 = (float)(-Math.toDegrees(Math.atan2(var69.y, var69.horizontalDistance())));
                        if (var60) {
                            var67.yRotO = var71;
                            var67.xRotO = var73;
                            var67.xo = var67.getX();
                            var67.yo = var67.getY();
                            var67.zo = var67.getZ();
                        }

                        var67.setYRot(var71);
                        var67.setXRot(var73);
                        var67.setYHeadRot(var71);
                        var67.setYBodyRot(var71);
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
