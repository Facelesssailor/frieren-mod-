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

    public static Vec3 movement(Entity e, Vec3 movement) {
        if (e.level().isClientSide) {
            return ArcanaClient.movement(e, movement);
        } else if (ExpandedMagic.held(e)) {
            return Vec3.ZERO;
        } else {
            if (e.level() instanceof ServerLevel level && !e.isRemoved() && !(movement.lengthSqr() < 1.0E-10)) {
                Vec3 start = ShipSpace.world(level, e.getBoundingBox().getCenter());
                Vec3 end = ShipSpace.world(level, e.getBoundingBox().getCenter().add(movement));
                BarrierData.Field field = BarrierData.get(level).firstBoundary(start, end, e instanceof Projectile);
                if (field == null) {
                    return movement;
                }

                if (e instanceof Projectile) {
                    e.discard();
                    return Vec3.ZERO;
                }

                Vec3 rel = start.subtract(field.center);
                Vec3 delta = end.subtract(start);
                double t = BarrierGeometry.firstHit(rel.x, rel.y, rel.z, delta.x, delta.y, delta.z, (double)field.radius);
                e.setDeltaMovement(Vec3.ZERO);
                return movement.scale(Math.max(0.0, t - 0.01));
            }

            return movement;
        }
    }

    public static boolean teleportBlocked(Entity e, double x, double y, double z) {
        if (!(e.level() instanceof ServerLevel level) || !e.isAddedToLevel() && e.tickCount < 1 || e.isRemoved()) {
            return false;
        }

        if (ExpandedMagic.blocksPosition(e, new Vec3(x, y, z))) {
            return true;
        } else {
            boolean blocked = BarrierData.get(level).crosses(ShipSpace.world(e), ShipSpace.world(level, new Vec3(x, y, z)), e instanceof Projectile);
            if (blocked && e instanceof Projectile) {
                e.discard();
            }

            return blocked;
        }
    }

    public static Vec3 clip(Level level, Vec3 from, Vec3 to) {
        Vec3 start = ShipSpace.world(level, from);
        Vec3 end = ShipSpace.world(level, to);
        double t = Double.POSITIVE_INFINITY;
        if (level instanceof ServerLevel server) {
            BarrierData.Field field = BarrierData.get(server).firstBoundary(start, end, true);
            if (field != null) {
                t = hit(field.center, field.radius, start, end);
            }
        } else {
            t = ArcanaClient.boundaryHit(start, end, true);
        }

        return Double.isFinite(t) ? from.lerp(to, Math.max(0.0, t - 1.0E-5)) : to;
    }

    private static double hit(Vec3 center, int radius, Vec3 start, Vec3 end) {
        Vec3 r = start.subtract(center);
        Vec3 d = end.subtract(start);
        return BarrierGeometry.firstHit(r.x, r.y, r.z, d.x, d.y, d.z, (double)radius);
    }

    public static boolean clipProjectile(Projectile projectile) {
        Vec3 from = projectile.position();
        Vec3 to = from.add(projectile.getDeltaMovement());
        Vec3 end = clip(projectile.level(), from, to);
        if (end.distanceToSqr(to) < 1.0E-10) {
            return false;
        } else {
            projectile.setDeltaMovement(end.subtract(from));
            return true;
        }
    }

    public static boolean rainBlocked(Level level, BlockPos pos) {
        return level instanceof ServerLevel serverLevel
            ? BarrierData.get(serverLevel).containsConfining(ShipSpace.world(level, Vec3.atCenterOf(pos)))
            : ArcanaClient.rainBlocked(ShipSpace.world(level, Vec3.atCenterOf(pos)));
    }

    public static boolean transitionBlocked(Entity e, DimensionTransition transition) {
        if (ExpandedMagic.held(e)) {
            return true;
        } else if (e.level() instanceof ServerLevel source) {
            ServerLevel var6 = transition.newLevel();
            Vec3 from = ShipSpace.world(e);
            Vec3 to = ShipSpace.world(var6, transition.pos());
            return source == var6
                ? BarrierData.get(source).crosses(from, to, false)
                : BarrierData.get(source).containsConfining(from) || BarrierData.get(var6).containsConfining(to);
        } else {
            return false;
        }
    }
}
