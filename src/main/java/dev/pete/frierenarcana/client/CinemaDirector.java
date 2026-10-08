package dev.pete.frierenarcana.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
    private static int shot = -1;
    private static float bands = 0.0F;
    private static float descent = 0.0F;
    private static long fractureSeen = 0L;
    private static final double SHATTER_LIFE = 3.2;
    private static final double END_AFTER_SHATTER = 4.3;

    private CinemaDirector() {
    }

    public static float bandTag(double var0) {
        double var2 = 1.0 - 2.4 * (double)descent;
        double var4 = (double)bands * Math.max(0.0, Math.min(1.0, (var0 - var2) / 0.4));
        return 0.6F + 0.04F * (float)var4;
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
                }

                var2.setYRot(lockYaw);
                var2.setXRot(lockPitch);
                var2.yRotO = lockYaw;
                var2.xRotO = lockPitch;
                var2.yHeadRot = lockYaw;
                Vec3 var37 = lockLook;
                Vec3 var10 = new Vec3(var37.x, 0.0, var37.z);
                var10 = var10.lengthSqr() < 0.01 ? new Vec3(0.0, 0.0, 1.0) : var10.normalize();
                Vec3 var11 = new Vec3(-var10.z, 0.0, var10.x);
                Vec3 var12 = var2.position().add(0.0, 1.3, 0.0);
                ArcanaClient.Fracture var13 = null;

                for (ArcanaClient.Fracture var15 : ArcanaClient.fractures()) {
                    if ((ArcanaCinematic.release == 0L || var15.startNanos() >= ArcanaCinematic.release - 400000000L)
                        && (var13 == null || var15.startNanos() > var13.startNanos())) {
                        var13 = var15;
                    }
                }

                Vec3 var39 = null;
                double var40 = 0.0;
                Vec3 var17 = null;
                if (var13 != null) {
                    var39 = var13.field().center();
                    var40 = (double)var13.field().radius();
                    var17 = var13.impact();
                    if (fractureSeen == 0L) {
                        fractureSeen = var13.startNanos();
                    }
                } else {
                    double var18 = Double.POSITIVE_INFINITY;

                    for (ArcanaClient.VisualField var21 : ArcanaClient.fields()) {
                        Vec3 var22 = var12.subtract(var21.center());
                        double var23 = var22.dot(var37);
                        double var25 = var22.lengthSqr() - (double)var21.radius() * (double)var21.radius();
                        double var27 = var23 * var23 - var25;
                        if (!(var27 < 0.0)) {
                            double var29 = Math.sqrt(var27);
                            double var31 = -var23 - var29;
                            double var33 = -var23 + var29;
                            double var35 = var31 > 0.5 ? var31 : (var33 > 0.5 ? var33 : Double.POSITIVE_INFINITY);
                            if (var35 < var18) {
                                var18 = var35;
                                var39 = var21.center();
                                var40 = (double)var21.radius();
                                var17 = var12.add(var37.scale(var35));
                            }
                        }
                    }
                }

                if (var13 != null) {
                    bands = 1.0F;
                }

                double var41 = fractureSeen == 0L ? -1.0 : (double)(var3 - fractureSeen) / 1.0E9;
                if (!(var5 > 22.0) && (fractureSeen == 0L || !(var41 > 4.3)) && (fractureSeen != 0L || ArcanaCinematic.release == 0L || !(var7 > 2.6))) {
                    Vec3 var42;
                    Vec3 var43;
                    byte var44;
                    if (ArcanaCinematic.release == 0L) {
                        if (var5 < 1.8) {
                            var44 = 10;
                            var42 = var12.add(var10.scale(1.55)).add(var11.scale(0.25)).add(0.0, -0.3, 0.0);
                            var43 = var12.add(0.0, -0.5, 0.0);
                        } else if (var5 < 4.2) {
                            var44 = 11;
                            var42 = var12.add(var10.scale(9.0)).add(var11.scale(6.0)).add(0.0, 0.6, 0.0);
                            var43 = var12.add(0.0, 3.2, 0.0);
                        } else {
                            var44 = 0;
                            double var45 = Math.min(1.0, (var5 - 4.2) / 4.0);
                            double var53 = Math.sin(var45 * Math.PI * 0.65) * 0.8;
                            Vec3 var57 = var10.scale(-Math.cos(var53)).add(var11.scale(Math.sin(var53)));
                            var42 = var12.add(var57.scale(5.4 - 1.5 * var45)).add(0.0, -0.25 + 0.85 * var45, 0.0);
                            var43 = var12.add(0.0, 0.2, 0.0).add(var10.scale(0.4));
                        }
                    } else if (var13 == null) {
                        var44 = 1;
                        var42 = var12.add(var10.scale(-5.0)).add(var11.scale(1.7)).add(0.0, 1.2 + 0.25 * Math.min(var7, 2.0), 0.0);
                        var43 = var17 != null ? var17 : var12.add(var37.scale(24.0));
                    } else if (var41 > 2.75) {
                        var44 = 4;
                        var42 = var12.add(var10.scale(2.9)).add(var11.scale(0.5)).add(0.0, 0.05, 0.0);
                        var43 = var12.add(0.0, -0.05, 0.0);
                    } else if (var41 < 0.9) {
                        var44 = 2;
                        Vec3 var46 = var12.subtract(var17);
                        Vec3 var24 = new Vec3(var46.x, 0.0, var46.z);
                        var24 = var24.lengthSqr() < 0.01 ? var10.scale(-1.0) : var24.normalize();
                        double var54 = Math.min(7.5, Math.max(4.5, var40 * 0.35));
                        var42 = var17.add(var24.scale(var54)).add(new Vec3(-var24.z, 0.0, var24.x).scale(1.6)).add(0.0, 1.0 + 0.3 * var41, 0.0);
                        var43 = var17;
                    } else {
                        var44 = 3;
                        Vec3 var47 = new Vec3(var17.x - var39.x, 0.0, var17.z - var39.z);
                        var47 = var47.lengthSqr() < 0.01 ? var10 : var47.normalize();
                        double var51 = Math.cos(0.6);
                        double var26 = Math.sin(0.6);
                        Vec3 var28 = new Vec3(var47.x * var51 - var47.z * var26, 0.0, var47.x * var26 + var47.z * var51);
                        double var60 = var40 * 1.55 + 10.0 + 0.9 * (var41 - 0.9);
                        var42 = var39.add(var28.scale(var60)).add(0.0, var40 * 0.5 + 2.5 + 0.35 * (var41 - 0.9), 0.0);
                        var43 = var39.add(0.0, var40 * 0.3, 0.0);
                    }

                    BlockHitResult var49 = var1.level.clip(new ClipContext(var43, var42, Block.VISUAL, Fluid.NONE, var2));
                    if (var49.getType() != Type.MISS) {
                        var42 = var49.getLocation().lerp(var43, 0.12);
                    }

                    boolean var52 = var44 != shot;
                    Vec3 var55 = !var52 && ArcanaCinematic.previous != null
                        ? ArcanaCinematic.previous.lerp(var42, var44 != 3 && var44 != 11 ? 0.24 : 0.12)
                        : var42;
                    shot = var44;
                    ArcanaCinematic.previous = var55;
                    ArmorStand var56 = ArcanaCinematic.camera;
                    var56.xo = var56.getX();
                    var56.yo = var56.getY();
                    var56.zo = var56.getZ();
                    var56.yRotO = var56.getYRot();
                    var56.xRotO = var56.getXRot();
                    var56.setPos(var55.x, var55.y - (double)var56.getEyeHeight(), var55.z);
                    Vec3 var58 = var43.subtract(var55);
                    float var59 = (float)(Math.toDegrees(Math.atan2(var58.z, var58.x)) - 90.0);
                    float var61 = (float)(-Math.toDegrees(Math.atan2(var58.y, var58.horizontalDistance())));
                    if (var52) {
                        var56.yRotO = var59;
                        var56.xRotO = var61;
                        var56.xo = var56.getX();
                        var56.yo = var56.getY();
                        var56.zo = var56.getZ();
                    }

                    var56.setYRot(var59);
                    var56.setXRot(var61);
                    var56.setYHeadRot(var59);
                    var56.setYBodyRot(var59);
                } else {
                    ArcanaCinematic.restore();
                }
            } else {
                ArcanaCinematic.restore();
            }
        }
    }
}
