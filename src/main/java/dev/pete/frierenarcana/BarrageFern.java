package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.damage.DamageSources;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;

public final class BarrageFern {
    private static final double RANGE = 36.0;
    private static final int STYLE_INVISIBLE = 99;

    private BarrageFern() {
    }

    public static void beam(ServerLevel var0, Vec3 var1, Vec3 var2, int var3) {
        if (var3 != 99) {
            ArcanaNetwork.magicBeam(var0, var1, var2, var3);
        }
    }

    private static boolean valid(ServerPlayer var0, LivingEntity var1, Vec3 var2, Vec3 var3) {
        if (var1 != var0 && var1.isAlive() && !var1.isSpectator() && !DamageSources.isFriendlyFireBetween(var0, var1)) {
            if (var1 instanceof Enemy || var1 instanceof Mob var4 && var4.getTarget() == var0) {
                Vec3 var7 = var1.getBoundingBox().getCenter().subtract(var3);
                double var5 = var7.length();
                return !(var5 < 1.0) && !(var5 > 36.0) && !(var7.normalize().dot(var2) < 0.35) ? var0.hasLineOfSight(var1) : false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static Vec3 volley(ServerPlayer var0, ArcanaSpell var1, int var2, Vec3 var3, double var4, float var6, float var7, int var8, boolean var9) {
        ServerLevel var10 = var0.serverLevel();
        Vec3 var11 = var0.getEyePosition();
        Vec3 var12 = var0.getLookAngle().normalize();
        List var13 = var10.getEntitiesOfClass(LivingEntity.class, var0.getBoundingBox().inflate(36.0), var3x -> valid(var0, var3x, var12, var11));
        if (var0.tickCount % 20 == 0) {
            CircleNet.refresh(var0, var1, 30);
        }

        if (var13.isEmpty()) {
            Vec3 var21 = ArcanaModes.fire(var0, var1, var2, var3, var4, var6, var7 * 0.6F, 99, var9);
            drawBend(var0, var10, var11, var12, var21 == null ? var11.add(var3.scale(var4)) : var21, var0.tickCount / 3);
            return var21;
        } else {
            var13.sort((var2x, var3x) -> Double.compare(angle(var2x, var11, var12), angle(var3x, var11, var12)));
            int var14 = Math.min(var13.size(), Math.min(8, 3 + var2));
            LivingEntity var15 = (LivingEntity)var13.get(var0.tickCount / 3 % var14);
            Vec3 var16 = var15.getBoundingBox().getCenter();
            Vec3 var17 = var16.subtract(var11);
            Vec3 var18 = var17.normalize();
            float var19 = var7 * 0.6F;
            Vec3 var20 = ArcanaModes.fire(var0, var1, var2, var18, var17.length() + 1.0, var6, var19, 99, false);
            drawBend(var0, var10, var11, var12, var20 == null ? var16 : var20, var0.tickCount / 3 + var15.getId());
            return var20;
        }
    }

    private static double angle(LivingEntity var0, Vec3 var1, Vec3 var2) {
        return -var0.getBoundingBox().getCenter().subtract(var1).normalize().dot(var2);
    }

    private static void drawBend(ServerPlayer var0, ServerLevel var1, Vec3 var2, Vec3 var3, Vec3 var4, int var5) {
        Vec3 var6 = new Vec3(-var3.z, 0.0, var3.x);
        var6 = var6.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : var6.normalize();
        Vec3 var7 = var6.cross(var3).normalize();
        if (var7.y < 0.0) {
            var7 = var7.scale(-1.0);
        }

        double var8 = var0.getRandom().nextDouble() < 0.5 ? 1.0 : -1.0;
        Vec3 var10 = var2.add(var3.scale(0.8 + var0.getRandom().nextDouble() * 1.0))
            .add(var6.scale(var8 * (0.7 + var0.getRandom().nextDouble() * 1.25)))
            .add(var7.scale(-0.3 + var0.getRandom().nextDouble() * 1.2));
        Vec3 var11 = var4.subtract(var10);
        double var12 = var11.length();
        if (var12 < 0.5) {
            ArcanaNetwork.magicBeam(var1, var10, var4, 6);
        } else {
            Vec3 var14 = var11.scale(1.0 / var12);
            Vec3 var15 = var10.add(var3.scale(Math.min(3.0, var12 * 0.3))).add(var6.scale(var8 * 0.5));
            Vec3 var16 = var4.subtract(var14.scale(Math.min(3.0, var12 * 0.3))).add(var7.scale(0.4 * (var0.getRandom().nextDouble() - 0.5)));
            byte var17 = 9;
            Vec3 var18 = var10;

            for (int var19 = 1; var19 <= var17; var19++) {
                double var20 = (double)var19 / (double)var17;
                double var22 = 1.0 - var20;
                Vec3 var24 = var10.scale(var22 * var22 * var22)
                    .add(var15.scale(3.0 * var22 * var22 * var20))
                    .add(var16.scale(3.0 * var22 * var20 * var20))
                    .add(var4.scale(var20 * var20 * var20));
                double var25 = Math.sin(var20 * Math.PI * 3.0 + (double)var5) * 0.14 * Math.sin(var20 * Math.PI);
                var24 = var24.add(var6.scale(var25)).add(0.0, var25 * 0.6, 0.0);
                ArcanaNetwork.magicBeam(var1, var18, var24, var19 == 1 ? 6 : 1);
                var18 = var24;
            }
        }
    }
}
