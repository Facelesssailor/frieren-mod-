package dev.pete.frierenarcana;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class BlackHoleShape {
    public static final double RISE = 3.0;
    public static final double MAX_R = 3.4;

    private BlackHoleShape() {
    }

    public static double rise(Level var0, Vec3 var1) {
        if (var0 == null) {
            return 3.0;
        } else {
            int var2 = (int)Math.ceil(6.4);

            for (int var3 = 1; var3 <= var2; var3++) {
                BlockPos var4 = BlockPos.containing(var1.x, var1.y + (double)var3, var1.z);
                if (var0.getBlockState(var4).blocksMotion()) {
                    return Math.max(0.0, Math.min(3.0, (double)(var3 - 1) - 2.38));
                }
            }

            return 3.0;
        }
    }

    public static double lift(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0 / 0.6));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }

    public static double grow(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0));
        return 1.0 - (1.0 - var2) * (1.0 - var2);
    }

    public static Vec3 center(Vec3 var0, double var1, double var3) {
        return var0.add(0.0, var1 * lift(var3), 0.0);
    }
}
