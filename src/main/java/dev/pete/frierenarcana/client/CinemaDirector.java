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
    static final double END_AFTER_SHATTER = 13.2;

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
                if (!(var5 > 40.0) && (fractureSeen == 0L || !(var42 > 13.2)) && (fractureSeen != 0L || ArcanaCinematic.release == 0L || !(var7 > 3.6))) {
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
                            double var57 = Math.sin(var48 * Math.PI * 0.6) * 0.9;
                            Vec3 var61 = var10.scale(-Math.cos(var57)).add(var11.scale(Math.sin(var57)));
                            var43 = var13.add(var61.scale(5.2 - 1.6 * var48)).add(0.0, -0.45 + 0.9 * var48, 0.0);
                            var44 = var13.add(0.0, 0.15, 0.0).add(var10.scale(0.5));
                        }
                    } else if (var14 == null || var7 < 1.6) {
                        var45 = 1;
                        var43 = var13.add(var10.scale(-4.2)).add(var11.scale(1.5)).add(0.0, 0.9 + 0.25 * Math.min(var7 / 1.6, 1.0), 0.0);
                        var44 = var18 != null ? var18 : var13.add(var38.scale(24.0));
                    } else if (var7 < 4.2) {
                        var45 = 2;
                        Vec3 var49 = var13.subtract(var18);
                        Vec3 var25 = new Vec3(var49.x, 0.0, var49.z);
                        var25 = var25.lengthSqr() < 0.01 ? var10.scale(-1.0) : var25.normalize();
                        double var58 = (var7 - 1.6) / 2.6;
                        double var62 = Math.min(14.0, Math.max(6.0, var41 * 0.45)) * (1.0 - 0.15 * var58);
                        var43 = var18.add(var25.scale(var62)).add(new Vec3(-var25.z, 0.0, var25.x).scale(2.2)).add(0.0, 1.2 + 0.6 * var58, 0.0);
                        var44 = var18.add(0.0, var41 * 0.08 * var58, 0.0);
                    } else if (var7 < 10.2) {
                        var45 = 3;
                        Vec3 var50 = new Vec3(var18.x - var40.x, 0.0, var18.z - var40.z);
                        var50 = var50.lengthSqr() < 0.01 ? var10 : var50.normalize();
                        double var55 = (var7 - 4.2) / 6.0;
                        double var27 = 0.6 + 0.25 * var55;
                        double var29 = Math.cos(var27);
                        double var31 = Math.sin(var27);
                        Vec3 var33 = new Vec3(var50.x * var29 - var50.z * var31, 0.0, var50.x * var31 + var50.z * var29);
                        double var66 = var41 * 1.55 + 10.0 + 4.0 * var55;
                        var43 = var40.add(var33.scale(var66)).add(0.0, var41 * 0.45 + 2.0 + 2.5 * var55, 0.0);
                        var44 = var40.add(0.0, var41 * (0.35 - 0.1 * var55), 0.0);
                    } else {
                        var45 = 4;
                        double var52 = (var7 - 10.2) / 3.0;
                        var43 = var13.add(var10.scale(3.0 - 0.3 * var52)).add(var11.scale(0.7 - 0.4 * var52)).add(0.0, 0.05 + 0.1 * var52, 0.0);
                        var44 = var13.add(0.0, -0.05, 0.0);
                    }

                    BlockHitResult var53 = var1.level.clip(new ClipContext(var44, var43, Block.VISUAL, Fluid.NONE, var2));
                    if (var53.getType() != Type.MISS) {
                        var43 = var53.getLocation().lerp(var44, 0.12);
                    }

                    boolean var56 = var45 != shot;
                    Vec3 var59 = !var56 && ArcanaCinematic.previous != null
                        ? ArcanaCinematic.previous.lerp(var43, var45 != 3 && var45 != 11 ? 0.24 : 0.12)
                        : var43;
                    shot = var45;
                    ArcanaCinematic.previous = var59;
                    ArmorStand var60 = ArcanaCinematic.camera;
                    var60.xo = var60.getX();
                    var60.yo = var60.getY();
                    var60.zo = var60.getZ();
                    var60.yRotO = var60.getYRot();
                    var60.xRotO = var60.getXRot();
                    var60.setPos(var59.x, var59.y - (double)var60.getEyeHeight(), var59.z);
                    Vec3 var63 = var44.subtract(var59);
                    float var64 = (float)(Math.toDegrees(Math.atan2(var63.z, var63.x)) - 90.0);
                    if (!var56) {
                        var64 = var60.getYRot() + Mth.wrapDegrees(var64 - var60.getYRot());
                    }

                    float var65 = (float)(-Math.toDegrees(Math.atan2(var63.y, var63.horizontalDistance())));
                    if (var56) {
                        var60.yRotO = var64;
                        var60.xRotO = var65;
                        var60.xo = var60.getX();
                        var60.yo = var60.getY();
                        var60.zo = var60.getZ();
                    }

                    var60.setYRot(var64);
                    var60.setXRot(var65);
                    var60.setYHeadRot(var64);
                    var60.setYBodyRot(var64);
                } else {
                    ArcanaCinematic.restore();
                }
            } else {
                ArcanaCinematic.restore();
            }
        }
    }
}
