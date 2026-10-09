package dev.pete.frierenarcana;

import dev.pete.frierenarcana.client.ArcanaClient;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

public final class BarrierHooks {
    private BarrierHooks() {
    }

    public static Vec3 movement(Entity var0, Vec3 var1) {
        if (var0.level().isClientSide) {
            return ArcanaClient.movement(var0, var1);
        } else if (ExpandedMagic.held(var0)) {
            return Vec3.ZERO;
        } else {
            if (var0.level() instanceof ServerLevel var2 && !var0.isRemoved() && !(var1.lengthSqr() < 1.0E-10)) {
                Vec3 var10 = ShipSpace.world(var2, var0.getBoundingBox().getCenter());
                Vec3 var4 = ShipSpace.world(var2, var0.getBoundingBox().getCenter().add(var1));
                BarrierData.Field var5 = BarrierData.get(var2).firstBoundary(var10, var4, var0 instanceof Projectile);
                if (var5 == null) {
                    return var1;
                }

                if (var0 instanceof Projectile) {
                    var0.discard();
                    return Vec3.ZERO;
                }

                Vec3 var6 = var10.subtract(var5.center);
                Vec3 var7 = var4.subtract(var10);
                double var8 = BarrierGeometry.firstHit(var6.x, var6.y, var6.z, var7.x, var7.y, var7.z, (double)var5.radius);
                return BarrierSlide.slide(var0, var6, var1, var8, (double)var5.radius);
            }

            return var1;
        }
    }

    public static boolean teleportBlocked(Entity var0, double var1, double var3, double var5) {
        if (!(var0.level() instanceof ServerLevel var7) || !var0.isAddedToLevel() && var0.tickCount < 1 || var0.isRemoved()) {
            return false;
        }

        if (ExpandedMagic.blocksPosition(var0, new Vec3(var1, var3, var5))) {
            return true;
        } else {
            boolean var9 = BarrierData.get(var7).crosses(ShipSpace.world(var0), ShipSpace.world(var7, new Vec3(var1, var3, var5)), var0 instanceof Projectile);
            if (var9 && var0 instanceof Projectile) {
                var0.discard();
            }

            return var9;
        }
    }

    public static Vec3 clip(Level var0, Vec3 var1, Vec3 var2) {
        Vec3 var3 = ShipSpace.world(var0, var1);
        Vec3 var4 = ShipSpace.world(var0, var2);
        double var5 = Double.POSITIVE_INFINITY;
        if (var0 instanceof ServerLevel var7) {
            BarrierData.Field var8 = BarrierData.get(var7).firstBoundary(var3, var4, true);
            if (var8 != null) {
                var5 = hit(var8.center, var8.radius, var3, var4);
            }
        } else {
            var5 = ArcanaClient.boundaryHit(var3, var4, true);
        }

        return Double.isFinite(var5) ? var1.lerp(var2, Math.max(0.0, var5 - 1.0E-5)) : var2;
    }

    private static double hit(Vec3 var0, int var1, Vec3 var2, Vec3 var3) {
        Vec3 var4 = var2.subtract(var0);
        Vec3 var5 = var3.subtract(var2);
        return BarrierGeometry.firstHit(var4.x, var4.y, var4.z, var5.x, var5.y, var5.z, (double)var1);
    }

    public static boolean clipProjectile(Projectile var0) {
        Vec3 var1 = var0.position();
        Vec3 var2 = var1.add(var0.getDeltaMovement());
        Vec3 var3 = clip(var0.level(), var1, var2);
        if (var3.distanceToSqr(var2) < 1.0E-10) {
            return false;
        } else {
            var0.setDeltaMovement(var3.subtract(var1));
            return true;
        }
    }

    public static boolean rainBlocked(Level var0, BlockPos var1) {
        if (var0 instanceof ServerLevel && RainHold.blocked((ServerLevel)var0, var1)) {
            return true;
        } else {
            return var0 instanceof ServerLevel var2
                ? BarrierData.get(var2).containsConfining(ShipSpace.world(var0, Vec3.atCenterOf(var1)))
                : ArcanaClient.rainBlocked(ShipSpace.world(var0, Vec3.atCenterOf(var1)));
        }
    }

    public static boolean transitionBlocked(Entity var0, DimensionTransition var1) {
        if (ExpandedMagic.held(var0)) {
            return true;
        } else if (var0.level() instanceof ServerLevel var2) {
            ServerLevel var6 = var1.newLevel();
            Vec3 var4 = ShipSpace.world(var0);
            Vec3 var5 = ShipSpace.world(var6, var1.pos());
            return var2 == var6
                ? BarrierData.get(var2).crosses(var4, var5, false)
                : BarrierData.get(var2).containsConfining(var4) || BarrierData.get(var6).containsConfining(var5);
        } else {
            return false;
        }
    }
}
