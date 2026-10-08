package dev.pete.frierenarcana.client;

import dev.pete.frierenarcana.BlackHoleShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
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
    private static double rise;

    private BlackHoleCinema() {
    }

    private static double tFly() {
        return 1.0;
    }

    private static double tHold() {
        return 1.0 + travel;
    }

    private static double tCol() {
        return tHold() + 3.2;
    }

    private static double tEnd() {
        return tCol() + 0.8 + 1.4;
    }

    public static void start(CompoundTag var0) {
        Minecraft var1 = Minecraft.getInstance();
        if (var1.player != null) {
            if (!ArcanaCinematic.active()) {
                a = new Vec3(var0.getDouble("ax"), var0.getDouble("ay"), var0.getDouble("az"));
                b = new Vec3(var0.getDouble("bx"), var0.getDouble("by"), var0.getDouble("bz"));
                travel = SpellFx.bhTravel(a.distanceTo(b));
                rise = BlackHoleShape.rise(var1.level, b);
                ArcanaCinematic.charge(var1.player.getUUID(), true, false);
                start = System.nanoTime();
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
        if (t() > tEnd()) {
            on = false;
            ArcanaCinematic.restore();
        }
    }

    static void frame(float var0) {
        Minecraft var1 = Minecraft.getInstance();
        LocalPlayer var2 = var1.player;
        if (var2 != null && a != null && b != null) {
            double var3 = t();
            if (!(var3 > tEnd())) {
                Vec3 var5 = b.subtract(a);
                var5 = var5.lengthSqr() < 1.0E-6 ? var2.getLookAngle() : var5.normalize();
                Vec3 var6 = new Vec3(var5.x, 0.0, var5.z);
                var6 = var6.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : var6.normalize();
                Vec3 var7 = new Vec3(-var6.z, 0.0, var6.x);
                Vec3 var8 = new Vec3(0.0, 1.0, 0.0);
                Vec3 var9;
                Vec3 var10;
                byte var11;
                if (var3 < tFly()) {
                    var11 = 1;
                    double var12 = var3 / tFly();
                    var9 = a.add(var5.scale(1.25 - 0.25 * var12)).add(var7.scale(0.35)).add(var8.scale(0.05));
                    var10 = a.add(var7.scale(0.05));
                } else if (var3 < tHold()) {
                    var11 = 2;
                    Vec3 var21 = a.lerp(b, (var3 - tFly()) / travel);
                    var9 = var21.subtract(var6.scale(3.6)).add(var7.scale(2.3)).add(var8.scale(0.9));
                    var10 = var21.add(var5.scale(3.0));
                } else if (var3 < tCol()) {
                    var11 = 3;
                    double var22 = (var3 - tHold()) / 3.2;
                    double var14 = BlackHoleShape.grow(var22);
                    double var16 = 0.9 + var22 * 0.7;
                    Vec3 var18 = var7.scale(Math.cos(var16)).add(var6.scale(-Math.sin(var16)));
                    var9 = b.add(var18.scale(8.5 + 2.0 * var14)).add(var8.scale(-0.6));
                    var10 = b.add(var8.scale(1.0 + 0.9 * var22));
                } else {
                    var11 = 4;
                    double var23 = Math.min(1.0, (var3 - tCol()) / 2.2);
                    Vec3 var25 = BlackHoleShape.center(b, rise, 1.0);
                    var9 = b.subtract(var6.scale(12.0 + 3.0 * var23)).add(var7.scale(4.0)).add(var8.scale(1.0 + 1.0 * var23));
                    var10 = var25;
                }

                if (var1.level != null) {
                    BlockHitResult var24 = var1.level.clip(new ClipContext(var10, var9, Block.VISUAL, Fluid.NONE, var2));
                    if (var24.getType() != Type.MISS) {
                        var9 = var24.getLocation().lerp(var10, 0.12);
                    }
                }

                CinemaFrame.place(var9, var10, 100 + var11, var11 == 2 ? 0.6 : 0.2);
            }
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
                var13.fill(0, 0, var14, var15, argb(var11, 15260927));
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
