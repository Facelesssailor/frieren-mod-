package dev.pete.frierenarcana;

import java.util.Locale;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class ZoltraakAim {
    private static final double RANGE = 64.0;
    private static final double SLACK = 0.55;
    private static final double MAX_COS = Math.cos(Math.toRadians(9.0));

    private ZoltraakAim() {
    }

    public static Vec3 dir(ServerPlayer var0) {
        Vec3 var1 = var0.getLookAngle();
        Vec3 var2 = var0.getEyePosition();
        Vec3 var3 = var2.add(var1.scale(64.0));
        BlockHitResult var4 = var0.level().clip(new ClipContext(var2, var3, Block.COLLIDER, Fluid.NONE, var0));
        if (var4.getType() != Type.MISS) {
            var3 = var4.getLocation();
        }

        AABB var5 = var0.getBoundingBox().expandTowards(var3.subtract(var2)).inflate(2.0);
        LivingEntity var6 = null;
        double var7 = Double.POSITIVE_INFINITY;

        for (LivingEntity var10 : var0.level()
            .getEntitiesOfClass(
                LivingEntity.class,
                var5,
                var1x -> var1x != var0 && var1x.isAlive() && !var1x.isSpectator() && !(var1x instanceof ArmorStand) && var1x.isPickable()
            )) {
            Optional var11 = var10.getBoundingBox().inflate(0.55).clip(var2, var3);
            if (var11.isPresent()) {
                double var12 = ((Vec3)var11.get()).distanceToSqr(var2);
                if (var12 < var7) {
                    var7 = var12;
                    var6 = var10;
                }
            }
        }

        if (var6 == null) {
            return var1;
        } else {
            Vec3 var14 = var6.getBoundingBox().getCenter().subtract(var2).normalize();
            return var1.dot(var14) < MAX_COS ? var1 : var14;
        }
    }

    public static Object barrageDrain(Object var0) {
        double var1 = var0 instanceof Number var3 ? var3.doubleValue() * 0.3 : 12.0;
        return String.format(Locale.ROOT, "%.0f", var1);
    }
}
