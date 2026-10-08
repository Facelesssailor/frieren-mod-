package dev.pete.frierenarcana.client;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent.Pre;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class CinemaFrame {
    private static ArmorStand lastCam;
    private static int lastShot = -1;
    private static long lastNanos;
    private static float lastYaw;

    private CinemaFrame() {
    }

    @SubscribeEvent
    public static void frame(Pre var0) {
        if (ArcanaCinematic.active() && ArcanaCinematic.camera != null) {
            float var1 = var0.getPartialTick().getGameTimeDeltaPartialTick(true);
            if (BlackHoleCinema.running()) {
                BlackHoleCinema.frame(var1);
            } else {
                CinemaDirector.frame(var1);
            }
        }
    }

    static void place(Vec3 var0, Vec3 var1, int var2, double var3) {
        ArmorStand var5 = ArcanaCinematic.camera;
        if (var5 != null) {
            if (var5 != lastCam) {
                lastCam = var5;
                lastShot = -1;
                lastNanos = 0L;
                ArcanaCinematic.previous = null;
            }

            long var6 = System.nanoTime();
            double var8 = lastNanos == 0L ? 0.0 : Math.min(0.1, (double)(var6 - lastNanos) / 1.0E9);
            lastNanos = var6;
            boolean var10 = var2 != lastShot || ArcanaCinematic.previous == null;
            Vec3 var11 = var10 ? var0 : ArcanaCinematic.previous.lerp(var0, 1.0 - Math.pow(1.0 - var3, var8 * 20.0));
            lastShot = var2;
            ArcanaCinematic.previous = var11;
            var5.setPos(var11.x, var11.y - (double)var5.getEyeHeight(), var11.z);
            var5.xo = var5.getX();
            var5.yo = var5.getY();
            var5.zo = var5.getZ();
            Vec3 var12 = var1.subtract(var11);
            float var13 = (float)(Math.toDegrees(Math.atan2(var12.z, var12.x)) - 90.0);
            if (!var10) {
                var13 = lastYaw + Mth.wrapDegrees(var13 - lastYaw);
            }

            float var14 = (float)(-Math.toDegrees(Math.atan2(var12.y, Math.sqrt(var12.x * var12.x + var12.z * var12.z))));
            lastYaw = var13;
            var5.setYRot(var13);
            var5.yRotO = var13;
            var5.setXRot(var14);
            var5.xRotO = var14;
            var5.setYHeadRot(var13);
            var5.yHeadRotO = var13;
            var5.setYBodyRot(var13);
            var5.yBodyRotO = var13;
        }
    }
}
