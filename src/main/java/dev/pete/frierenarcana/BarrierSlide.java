package dev.pete.frierenarcana;

import dev.pete.frierenarcana.client.ArcanaClient;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public final class BarrierSlide {
    private static final double SKIN = 0.02;

    private BarrierSlide() {
    }

    public static Vec3 slide(Entity var0, Vec3 var1, Vec3 var2, double var3, double var5) {
        Vec3 var7 = resolve(var1, var2, var3, var5);
        Vec3 var8 = var1.add(var2.scale(Math.max(0.0, Math.min(1.0, var3))));
        if (var8.lengthSqr() > 1.0E-9) {
            Vec3 var9 = var8.normalize();
            Vec3 var10 = var0.getDeltaMovement();
            var0.setDeltaMovement(var10.subtract(var9.scale(var10.dot(var9))));
        }

        return var7;
    }

    static Vec3 resolve(Vec3 var0, Vec3 var1, double var2, double var4) {
        double var6 = Math.max(0.0, Math.min(1.0, var2) - 0.01);
        Vec3 var8 = var0.add(var1.scale(var6));
        if (var8.lengthSqr() < 1.0E-9) {
            return var1.scale(var6);
        } else {
            Vec3 var9 = var8.normalize();
            Vec3 var10 = var1.scale(1.0 - var6);
            Vec3 var11 = var10.subtract(var9.scale(var10.dot(var9)));
            Vec3 var12 = var8.add(var11);
            boolean var13 = var0.lengthSqr() < var4 * var4;
            double var14 = var12.length();
            if (var13 && var14 > var4 - 0.02) {
                var12 = var12.scale((var4 - 0.02) / var14);
            } else if (!var13 && var14 < var4 + 0.02) {
                var12 = var12.scale((var4 + 0.02) / var14);
            }

            return var12.subtract(var0);
        }
    }

    public static Vec3 client(Entity var0, Vec3 var1) {
        if (var1.lengthSqr() < 1.0E-10) {
            return var1;
        } else {
            Vec3 var2 = ShipSpace.world(var0.level(), var0.getBoundingBox().getCenter());
            Vec3 var3 = ShipSpace.world(var0.level(), var0.getBoundingBox().getCenter().add(var1));
            Vec3 var4 = var3.subtract(var2);
            double var5 = Double.POSITIVE_INFINITY;
            ArcanaClient.VisualField var7 = null;

            for (ArcanaClient.VisualField var9 : ArcanaClient.fields()) {
                Vec3 var10 = var2.subtract(var9.center());
                if (!var9.defensive() || var0 instanceof Projectile && !(var2.distanceToSqr(var9.center()) < (double)var9.radius() * (double)var9.radius())) {
                    double var11 = BarrierGeometry.firstHit(var10.x, var10.y, var10.z, var4.x, var4.y, var4.z, (double)var9.radius());
                    if (var11 < var5) {
                        var5 = var11;
                        var7 = var9;
                    }
                }
            }

            if (var7 == null || !Double.isFinite(var5)) {
                return var1;
            } else {
                return var0 instanceof Projectile
                    ? var1.scale(Math.max(0.0, var5 - 0.01))
                    : resolve(var2.subtract(var7.center()), var1, var5, (double)var7.radius());
            }
        }
    }
}
