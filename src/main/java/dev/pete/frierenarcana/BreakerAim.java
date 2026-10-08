package dev.pete.frierenarcana;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class BreakerAim {
    public static final double RANGE = 96.0;

    private BreakerAim() {
    }

    public static BarrierData.Field target(ServerPlayer var0) {
        BarrierData.Field var1 = ArcanaSpell.targetBarrierLook(var0);
        if (var1 != null) {
            return var1;
        } else {
            Vec3 var2 = ShipSpace.world(var0.level(), var0.getEyePosition());
            BarrierData.Field var3 = null;
            BarrierData.Field var4 = null;
            double var5 = Double.POSITIVE_INFINITY;
            double var7 = Double.POSITIVE_INFINITY;

            for (BarrierData.Field var10 : BarrierData.get(var0.serverLevel()).fields()) {
                double var11 = var2.distanceTo(var10.center);
                if (var11 < 0.0) {
                    if (0.0 < var5) {
                        var5 = 0.0;
                        var3 = var10;
                    }
                } else if (var11 - 0.0 < var7 && var11 - 0.0 <= 96.0) {
                    var7 = var11 - 0.0;
                    var4 = var10;
                }
            }

            return var3 != null ? var3 : var4;
        }
    }

    public static Vec3 direction(ServerPlayer var0) {
        Vec3 var1 = var0.getLookAngle();
        BarrierData.Field var2 = target(var0);
        if (var2 == null) {
            return var1;
        } else {
            Vec3 var3 = ShipSpace.world(var0.level(), var0.getEyePosition());
            Vec3 var4 = var3.subtract(var2.center);
            double var5 = BarrierGeometry.firstHit(var4.x, var4.y, var4.z, var1.x * 128.0, var1.y * 128.0, var1.z * 128.0, 0.0);
            if (Double.isFinite(var5) && var5 >= 0.0 && var5 <= 1.0) {
                return var1;
            } else if (var3.distanceTo(var2.center) < 0.0) {
                return new Vec3(0.0, 1.0, 0.0);
            } else {
                Vec3 var7 = var2.center.add(0.0, 0.0 * 0.35, 0.0).subtract(var3);
                return var7.lengthSqr() < 1.0E-6 ? var1 : var7.normalize();
            }
        }
    }
}
