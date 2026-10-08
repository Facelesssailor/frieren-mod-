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
    private static long fractureSeen = 0L;
    private static final double SHATTER_LIFE = 3.2;
    private static final double END_AFTER_SHATTER = 4.3;

    private CinemaDirector() {
    }

    public static float bandTag() {
        return 0.6F + 0.04F * bands;
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
        } else {
            LocalPlayer var2 = var1.player;
            if (var2 != null && var1.level != null && ArcanaCinematic.camera.level() == var1.level && var2.isAlive() && var1.screen == null) {
                long var3 = System.nanoTime();
                double var5 = (double)(var3 - ArcanaCinematic.started) / 1.0E9;
                double var7 = ArcanaCinematic.release == 0L ? 0.0 : (double)(var3 - ArcanaCinematic.release) / 1.0E9;
                bands = ArcanaCinematic.release == 0L
                    ? (float)(0.25 + 0.45 * Math.min(1.0, var5 / 5.0))
                    : (float)Math.min(1.0, 0.75 + 0.25 * Math.min(1.0, var7 / 1.5));
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
                Vec3 var9 = lockLook;
                Vec3 var10 = new Vec3(var9.x, 0.0, var9.z);
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

                Vec3 var38 = null;
                double var39 = 0.0;
                Vec3 var17 = null;
                if (var13 != null) {
                    var38 = var13.field().center();
                    var39 = (double)var13.field().radius();
                    var17 = var13.impact();
                    if (fractureSeen == 0L) {
                        fractureSeen = var13.startNanos();
                    }
                } else {
                    double var18 = Double.POSITIVE_INFINITY;

                    for (ArcanaClient.VisualField var21 : ArcanaClient.fields()) {
                        Vec3 var22 = var12.subtract(var21.center());
                        double var23 = var22.dot(var9);
                        double var25 = var22.lengthSqr() - (double)var21.radius() * (double)var21.radius();
                        double var27 = var23 * var23 - var25;
                        if (!(var27 < 0.0)) {
                            double var29 = Math.sqrt(var27);
                            double var31 = -var23 - var29;
                            double var33 = -var23 + var29;
                            double var35 = var31 > 0.5 ? var31 : (var33 > 0.5 ? var33 : Double.POSITIVE_INFINITY);
                            if (var35 < var18) {
                                var18 = var35;
                                var38 = var21.center();
                                var39 = (double)var21.radius();
                                var17 = var12.add(var9.scale(var35));
                            }
                        }
                    }
                }

                if (var13 != null) {
                    bands = 1.0F;
                }

                double var40 = fractureSeen == 0L ? -1.0 : (double)(var3 - fractureSeen) / 1.0E9;
                if (!(var5 > 22.0) && (fractureSeen == 0L || !(var40 > 4.3)) && (fractureSeen != 0L || ArcanaCinematic.release == 0L || !(var7 > 2.6))) {
                    Vec3 var41;
                    Vec3 var42;
                    byte var43;
                    if (ArcanaCinematic.release == 0L) {
                        if (var5 < 1.8) {
                            var43 = 10;
                            var41 = var12.add(var10.scale(1.55)).add(var11.scale(0.25)).add(0.0, -0.3, 0.0);
                            var42 = var12.add(0.0, -0.5, 0.0);
                        } else if (var5 < 4.2) {
                            var43 = 11;
                            var41 = var12.add(var10.scale(9.0)).add(var11.scale(6.0)).add(0.0, 0.6, 0.0);
                            var42 = var12.add(0.0, 3.2, 0.0);
                        } else {
                            var43 = 0;
                            double var44 = Math.min(1.0, (var5 - 4.2) / 4.0);
                            double var52 = Math.sin(var44 * Math.PI * 0.65) * 0.8;
                            Vec3 var56 = var10.scale(-Math.cos(var52)).add(var11.scale(Math.sin(var52)));
                            var41 = var12.add(var56.scale(5.4 - 1.5 * var44)).add(0.0, -0.25 + 0.85 * var44, 0.0);
                            var42 = var12.add(0.0, 0.2, 0.0).add(var10.scale(0.4));
                        }
                    } else if (var13 == null) {
                        var43 = 1;
                        var41 = var12.add(var10.scale(-5.0)).add(var11.scale(1.7)).add(0.0, 1.2 + 0.25 * Math.min(var7, 2.0), 0.0);
                        var42 = var17 != null ? var17 : var12.add(var9.scale(24.0));
                    } else if (var40 > 2.75) {
                        var43 = 4;
                        var41 = var12.add(var10.scale(2.9)).add(var11.scale(0.5)).add(0.0, 0.05, 0.0);
                        var42 = var12.add(0.0, -0.05, 0.0);
                    } else if (var40 < 0.9) {
                        var43 = 2;
                        Vec3 var45 = var12.subtract(var17);
                        Vec3 var24 = new Vec3(var45.x, 0.0, var45.z);
                        var24 = var24.lengthSqr() < 0.01 ? var10.scale(-1.0) : var24.normalize();
                        double var53 = Math.min(7.5, Math.max(4.5, var39 * 0.35));
                        var41 = var17.add(var24.scale(var53)).add(new Vec3(-var24.z, 0.0, var24.x).scale(1.6)).add(0.0, 1.0 + 0.3 * var40, 0.0);
                        var42 = var17;
                    } else {
                        var43 = 3;
                        Vec3 var46 = new Vec3(var17.x - var38.x, 0.0, var17.z - var38.z);
                        var46 = var46.lengthSqr() < 0.01 ? var10 : var46.normalize();
                        double var50 = Math.cos(0.6);
                        double var26 = Math.sin(0.6);
                        Vec3 var28 = new Vec3(var46.x * var50 - var46.z * var26, 0.0, var46.x * var26 + var46.z * var50);
                        double var59 = var39 * 1.55 + 10.0 + 0.9 * (var40 - 0.9);
                        var41 = var38.add(var28.scale(var59)).add(0.0, var39 * 0.5 + 2.5 + 0.35 * (var40 - 0.9), 0.0);
                        var42 = var38.add(0.0, var39 * 0.3, 0.0);
                    }

                    BlockHitResult var48 = var1.level.clip(new ClipContext(var42, var41, Block.VISUAL, Fluid.NONE, var2));
                    if (var48.getType() != Type.MISS) {
                        var41 = var48.getLocation().lerp(var42, 0.12);
                    }

                    boolean var51 = var43 != shot;
                    Vec3 var54 = !var51 && ArcanaCinematic.previous != null
                        ? ArcanaCinematic.previous.lerp(var41, var43 != 3 && var43 != 11 ? 0.24 : 0.12)
                        : var41;
                    shot = var43;
                    ArcanaCinematic.previous = var54;
                    ArmorStand var55 = ArcanaCinematic.camera;
                    var55.xo = var55.getX();
                    var55.yo = var55.getY();
                    var55.zo = var55.getZ();
                    var55.yRotO = var55.getYRot();
                    var55.xRotO = var55.getXRot();
                    var55.setPos(var54.x, var54.y - (double)var55.getEyeHeight(), var54.z);
                    Vec3 var57 = var42.subtract(var54);
                    float var58 = (float)(Math.toDegrees(Math.atan2(var57.z, var57.x)) - 90.0);
                    float var60 = (float)(-Math.toDegrees(Math.atan2(var57.y, var57.horizontalDistance())));
                    if (var51) {
                        var55.yRotO = var58;
                        var55.xRotO = var60;
                        var55.xo = var55.getX();
                        var55.yo = var55.getY();
                        var55.zo = var55.getZ();
                    }

                    var55.setYRot(var58);
                    var55.setXRot(var60);
                    var55.setYHeadRot(var58);
                    var55.setYBodyRot(var58);
                } else {
                    ArcanaCinematic.restore();
                }
            } else {
                ArcanaCinematic.restore();
            }
        }
    }
}
