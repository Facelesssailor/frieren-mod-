package dev.pete.frierenarcana;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class BreakerAim {
    private BreakerAim() {
    }

    public static BarrierData.Field target(ServerPlayer var0) {
        Vec3 var1 = ShipSpace.world(var0.level(), var0.getEyePosition());
        BarrierData.Field var2 = null;
        double var3 = Double.POSITIVE_INFINITY;

        for (BarrierData.Field var6 : BarrierData.get(var0.serverLevel()).fields()) {
            if (!var6.defensive) {
                double var7 = var1.x - var6.center.x;
                double var9 = var1.z - var6.center.z;
                double var11 = Math.sqrt(var7 * var7 + var9 * var9);
                if (!(var11 > (double)var6.radius - 0.5)) {
                    double var13 = var6.center.y + Math.sqrt(Math.max(0.0, (double)var6.radius * (double)var6.radius - var11 * var11));
                    if (!(var1.y >= var13) && (double)var6.radius < var3) {
                        var3 = (double)var6.radius;
                        var2 = var6;
                    }
                }
            }
        }

        return var2;
    }

    public static Vec3 direction(ServerPlayer var0) {
        return new Vec3(0.0, 1.0, 0.0);
    }
}
