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
        double var7 = (var5 & 1) == 0 ? 1.0 : -1.0;
        double var9 = 2.2 + (double)(var5 % 5) * 0.45;
        Vec3 var11 = var2.add(var6.scale(0.35 * -var7)).add(0.0, -0.3, 0.0).add(var3.scale(0.4));
        Vec3 var12 = var11.add(var6.scale(var7 * var9)).add(0.0, 1.2 + (double)(var5 % 3) * 0.5, 0.0).add(var3.scale(2.0));
        Vec3 var13 = var4.add(var6.scale(-var7 * var9 * 0.6)).add(0.0, 1.0, 0.0).subtract(var4.subtract(var11).normalize().scale(3.0));
        byte var14 = 9;
        Vec3 var15 = var11;

        for (int var16 = 1; var16 <= var14; var16++) {
            double var17 = (double)var16 / (double)var14;
            double var19 = 1.0 - var17;
            Vec3 var21 = var11.scale(var19 * var19 * var19)
                .add(var12.scale(3.0 * var19 * var19 * var17))
                .add(var13.scale(3.0 * var19 * var17 * var17))
                .add(var4.scale(var17 * var17 * var17));
            double var22 = Math.sin(var17 * Math.PI * 3.0 + (double)var5) * 0.18 * Math.sin(var17 * Math.PI);
            var21 = var21.add(var6.scale(var22)).add(0.0, var22 * 0.6, 0.0);
            ArcanaNetwork.magicBeam(var1, var15, var21, 1);
            var15 = var21;
        }
    }
}
