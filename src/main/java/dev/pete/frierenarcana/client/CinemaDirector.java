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
    private static int shot = -1;
    private static float bands = 0.0F;
    private static float descent = 0.0F;
    private static long fractureSeen = 0L;
    static final double END_AFTER_SHATTER = 8.6;

    private CinemaDirector() {
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
                if (!(var5 > 30.0) && (fractureSeen == 0L || !(var41 > 8.6)) && (fractureSeen != 0L || ArcanaCinematic.release == 0L || !(var7 > 2.8))) {
                    Vec3 var42;
                    Vec3 var43;
                    byte var44;
                    if (ArcanaCinematic.release == 0L) {
                        if (var5 < 2.4) {
                            var44 = 10;
                            double var45 = var5 / 2.4;
                            var42 = var12.add(var10.scale(1.75 - 0.3 * var45)).add(var11.scale(0.3 - 0.1 * var45)).add(0.0, -0.28, 0.0);
                            var43 = var12.add(0.0, -0.5, 0.0);
                        } else if (var5 < 5.0) {
                            var44 = 11;
                            double var46 = (var5 - 2.4) / 2.6;
                            var42 = var12.add(var10.scale(10.0 + 1.5 * var46)).add(var11.scale(6.5)).add(0.0, 0.3 + 0.4 * var46, 0.0);
                            var43 = var12.add(0.0, 2.0 + 4.0 * var46, 0.0);
                        } else {
                            var44 = 0;
                            double var47 = Math.min(1.0, (var5 - 5.0) / 2.6);
                            double var56 = Math.sin(var47 * Math.PI * 0.6) * 0.9;
                            Vec3 var60 = var10.scale(-Math.cos(var56)).add(var11.scale(Math.sin(var56)));
                            var42 = var12.add(var60.scale(5.2 - 1.6 * var47)).add(0.0, -0.45 + 0.9 * var47, 0.0);
                            var43 = var12.add(0.0, 0.15, 0.0).add(var10.scale(0.5));
                        }
                    } else if (var13 == null || var7 < 1.0) {
                        var44 = 1;
                        var42 = var12.add(var10.scale(-4.2)).add(var11.scale(1.5)).add(0.0, 0.9 + 0.25 * Math.min(var7, 1.0), 0.0);
                        var43 = var17 != null ? var17 : var12.add(var37.scale(24.0));
                    } else if (var7 < 2.6) {
                        var44 = 2;
                        Vec3 var48 = var12.subtract(var17);
                        Vec3 var24 = new Vec3(var48.x, 0.0, var48.z);
                        var24 = var24.lengthSqr() < 0.01 ? var10.scale(-1.0) : var24.normalize();
                        double var57 = (var7 - 1.0) / 1.6;
                        double var61 = Math.min(14.0, Math.max(6.0, var40 * 0.45)) * (1.0 - 0.15 * var57);
                        var42 = var17.add(var24.scale(var61)).add(new Vec3(-var24.z, 0.0, var24.x).scale(2.2)).add(0.0, 1.2 + 0.6 * var57, 0.0);
                        var43 = var17.add(0.0, var40 * 0.08 * var57, 0.0);
                    } else if (var7 < 6.6) {
                        var44 = 3;
                        Vec3 var49 = new Vec3(var17.x - var39.x, 0.0, var17.z - var39.z);
                        var49 = var49.lengthSqr() < 0.01 ? var10 : var49.normalize();
                        double var54 = (var7 - 2.6) / 4.0;
                        double var26 = 0.6 + 0.25 * var54;
                        double var28 = Math.cos(var26);
                        double var30 = Math.sin(var26);
                        Vec3 var32 = new Vec3(var49.x * var28 - var49.z * var30, 0.0, var49.x * var30 + var49.z * var28);
                        double var65 = var40 * 1.55 + 10.0 + 4.0 * var54;
                        var42 = var39.add(var32.scale(var65)).add(0.0, var40 * 0.45 + 2.0 + 2.5 * var54, 0.0);
                        var43 = var39.add(0.0, var40 * (0.35 - 0.1 * var54), 0.0);
                    } else {
                        var44 = 4;
                        double var51 = (var7 - 6.6) / 2.0;
                        var42 = var12.add(var10.scale(3.0 - 0.3 * var51)).add(var11.scale(0.7 - 0.4 * var51)).add(0.0, 0.05 + 0.1 * var51, 0.0);
                        var43 = var12.add(0.0, -0.05, 0.0);
                    }

                    BlockHitResult var52 = var1.level.clip(new ClipContext(var43, var42, Block.VISUAL, Fluid.NONE, var2));
                    if (var52.getType() != Type.MISS) {
                        var42 = var52.getLocation().lerp(var43, 0.12);
                    }

                    boolean var55 = var44 != shot;
                    Vec3 var58 = !var55 && ArcanaCinematic.previous != null
                        ? ArcanaCinematic.previous.lerp(var42, var44 != 3 && var44 != 11 ? 0.24 : 0.12)
                        : var42;
                    shot = var44;
                    ArcanaCinematic.previous = var58;
                    ArmorStand var59 = ArcanaCinematic.camera;
                    var59.xo = var59.getX();
                    var59.yo = var59.getY();
                    var59.zo = var59.getZ();
                    var59.yRotO = var59.getYRot();
                    var59.xRotO = var59.getXRot();
                    var59.setPos(var58.x, var58.y - (double)var59.getEyeHeight(), var58.z);
                    Vec3 var62 = var43.subtract(var58);
                    float var63 = (float)(Math.toDegrees(Math.atan2(var62.z, var62.x)) - 90.0);
                    if (!var55) {
                        var63 = var59.getYRot() + Mth.wrapDegrees(var63 - var59.getYRot());
                    }

                    float var64 = (float)(-Math.toDegrees(Math.atan2(var62.y, var62.horizontalDistance())));
                    if (var55) {
                        var59.yRotO = var63;
                        var59.xRotO = var64;
                        var59.xo = var59.getX();
                        var59.yo = var59.getY();
                        var59.zo = var59.getZ();
                    }

                    var59.setYRot(var63);
                    var59.setXRot(var64);
                    var59.setYHeadRot(var63);
                    var59.setYBodyRot(var63);
                } else {
                    ArcanaCinematic.restore();
                }
            } else {
                ArcanaCinematic.restore();
            }
        }
    }
}
