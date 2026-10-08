package dev.pete.frierenarcana.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class BlackHoleCinema {
    private static boolean on;
    private static long start;
    private static Vec3 a;
    private static Vec3 b;
    private static double travel;
    private static int shot = -1;

    private BlackHoleCinema() {
    }

    private static double tFly() {
        return 1.0;
    }

    private static double tHold() {
        return 1.0 + travel;
    }

    private static double tCol() {
        return tHold() + 2.4;
    }

    private static double tEnd() {
        return tCol() + 0.8 + 1.4;
    }

    public static void start(CompoundTag var0) {
        Minecraft var1 = Minecraft.getInstance();
        if (var1.player != null) {
            a = new Vec3(var0.getDouble("ax"), var0.getDouble("ay"), var0.getDouble("az"));
            b = new Vec3(var0.getDouble("bx"), var0.getDouble("by"), var0.getDouble("bz"));
            travel = SpellFx.bhTravel(a.distanceTo(b));
            if (!ArcanaCinematic.active()) {
                ArcanaCinematic.charge(var1.player.getUUID(), true, false);
                start = System.nanoTime();
                shot = -1;
                on = true;
            }
        }
    }

    public static boolean running() {
        if (on && (!ArcanaCinematic.active() || ArcanaCinematic.breaker)) {
            on = false;
        }

        return on;
    }

    private static double t() {
        return (double)(System.nanoTime() - start) / 1.0E9;
    }

    public static void tick(Minecraft var0, LocalPlayer var1) {
        double var2 = t();
        if (var2 > tEnd()) {
            on = false;
            ArcanaCinematic.restore();
        } else {
            Vec3 var4 = b.subtract(a);
            var4 = var4.lengthSqr() < 1.0E-6 ? var1.getLookAngle() : var4.normalize();
            Vec3 var5 = new Vec3(var4.x, 0.0, var4.z);
            var5 = var5.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : var5.normalize();
            Vec3 var6 = new Vec3(-var5.z, 0.0, var5.x);
            Vec3 var7 = new Vec3(0.0, 1.0, 0.0);
            Vec3 var8;
            Vec3 var9;
            byte var10;
            if (var2 < tFly()) {
                var10 = 1;
                double var11 = var2 / tFly();
                var8 = a.add(var4.scale(1.25 - 0.25 * var11)).add(var6.scale(0.35)).add(var7.scale(0.05));
                var9 = a.add(var6.scale(0.05));
            } else if (var2 < tHold()) {
                var10 = 2;
                Vec3 var19 = a.lerp(b, (var2 - tFly()) / travel);
                var8 = var19.subtract(var5.scale(3.6)).add(var6.scale(2.3)).add(var7.scale(0.9));
                var9 = var19.add(var4.scale(3.0));
            } else if (var2 < tCol()) {
                var10 = 3;
                double var20 = (var2 - tHold()) / 2.4;
                double var13 = 0.9 + var20 * 0.7;
                Vec3 var15 = var6.scale(Math.cos(var13)).add(var5.scale(-Math.sin(var13)));
                var8 = b.add(var15.scale(8.5 - 1.5 * var20)).add(var7.scale(-0.2 + 0.9 * var20));
                var9 = b.add(var7.scale(0.2));
            } else {
                var10 = 4;
                double var21 = Math.min(1.0, (var2 - tCol()) / 2.2);
                var8 = b.subtract(var5.scale(10.0 + 3.0 * var21)).add(var6.scale(4.0)).add(var7.scale(2.5 + 1.0 * var21));
                var9 = b;
            }

            if (var0.level != null) {
                BlockHitResult var22 = var0.level.clip(new ClipContext(var9, var8, Block.VISUAL, Fluid.NONE, var1));
                if (var22.getType() != Type.MISS) {
                    var8 = var22.getLocation().lerp(var9, 0.12);
                }
            }

            boolean var23 = var10 != shot;
            Vec3 var12 = !var23 && ArcanaCinematic.previous != null ? ArcanaCinematic.previous.lerp(var8, var10 == 2 ? 0.6 : 0.2) : var8;
            shot = var10;
            ArcanaCinematic.previous = var12;
            ArmorStand var24 = ArcanaCinematic.camera;
            var24.xo = var24.getX();
            var24.yo = var24.getY();
            var24.zo = var24.getZ();
            var24.yRotO = var24.getYRot();
            var24.xRotO = var24.getXRot();
            var24.setPos(var12.x, var12.y - (double)var24.getEyeHeight(), var12.z);
            Vec3 var14 = var9.subtract(var12);
            float var25 = (float)(Math.toDegrees(Math.atan2(var14.z, var14.x)) - 90.0);
            if (!var23) {
                var25 = var24.getYRot() + Mth.wrapDegrees(var25 - var24.getYRot());
            }

            float var16 = (float)(-Math.toDegrees(Math.atan2(var14.y, var14.horizontalDistance())));
            if (var23) {
                var24.yRotO = var25;
                var24.xRotO = var16;
                var24.xo = var24.getX();
                var24.yo = var24.getY();
                var24.zo = var24.getZ();
            }

            var24.setYRot(var25);
            var24.setXRot(var16);
            var24.setYHeadRot(var25);
            var24.setYBodyRot(var25);
        }
    }

    @SubscribeEvent
    public static void overlay(Post var0) {
        if (running()) {
            double var1 = t();
            double var3 = Math.max(0.0, 1.0 - var1 / 0.35);
            var3 = Math.max(var3, (var1 - (tEnd() - 0.6)) / 0.6);
            double var5 = 0.0;
            double var7 = var1 - (tFly() - 0.12);
            if (var7 > 0.0 && var7 < 0.5) {
                var5 = var7 < 0.1 ? var7 / 0.1 * 0.85 : 0.85 * (1.0 - (var7 - 0.1) / 0.4);
            }

            double var9 = var1 - tCol();
            double var11 = var9 > 0.0 && var9 < 0.7 ? (var9 < 0.12 ? var9 / 0.12 : 1.0 - (var9 - 0.12) / 0.58) * 0.75 : 0.0;
            GuiGraphics var13 = var0.getGuiGraphics();
            int var14 = var13.guiWidth();
            int var15 = var13.guiHeight();
            if (var5 > 0.004) {
                var13.fill(0, 0, var14, var15, argb(var5, 16054527));
            }

            if (var11 > 0.004) {
                var13.fill(0, 0, var14, var15, argb(var11, 16769712));
            }

            if (var3 > 0.004) {
                var13.fill(0, 0, var14, var15, argb(Math.min(1.0, var3), 0));
            }
        }
    }

    private static int argb(double var0, int var2) {
        return (int)Math.round(Math.max(0.0, Math.min(1.0, var0)) * 255.0) << 24 | var2;
    }
}
