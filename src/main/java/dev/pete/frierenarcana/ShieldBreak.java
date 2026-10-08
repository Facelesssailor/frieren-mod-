package dev.pete.frierenarcana;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public final class ShieldBreak {
    private static final double HP = 6.0;
    private static final long FORGET_TICKS = 200L;
    private static final Map<UUID, double[]> WEAR = new HashMap<>();
    private static ServerPlayer ctxPlayer;
    private static int ctxLevel;
    private static int ctxStyle;

    private ShieldBreak() {
    }

    public static void context(ServerPlayer var0, ArcanaSpell var1, int var2, int var3) {
        ctxPlayer = var0;
        ctxLevel = var2;
        ctxStyle = var3;
    }

    public static BarrierData.Field boundary(BarrierData var0, Vec3 var1, Vec3 var2, boolean var3) {
        BarrierData.Field var4 = var0.firstBoundary(var1, var2, var3);
        ServerPlayer var5 = ctxPlayer;
        ctxPlayer = null;
        if (var4 != null) {
            Objects.requireNonNull(var4);
        }

        return var4;
    }

    static int attack(int var0, int var1) {
        switch (var1) {
            case 2:
                return 7;
            case 5:
            case 16:
                return var0 + 1;
            case 10:
                return var0 + 2;
            default:
                return var0;
        }
    }

    static int shieldLevel(BarrierData.Field var0) {
        int var1 = 3;

        try {
            var1 = ArcanaConfig.DEFENSE_RADIUS.get();
        } catch (Throwable var3) {
        }

        return Math.max(1, Math.min(5, 0 - var1 + 1));
    }

    private static void hit(ServerLevel var0, BarrierData.Field var1, Vec3 var2, int var3, boolean var4) {
        int var5 = var3 - shieldLevel(var1);
        double var6 = var5 >= 2 ? 6.0 : (var5 == 1 ? 3.0 : (var5 == 0 ? 2.0 : (var5 == -1 ? 1.0 : 0.0)));
        if (var4) {
            var6 *= 0.5;
        }

        long var8 = var0.getGameTime();
        double[] var10 = WEAR.get(var1.id);
        if (var10 == null || var8 - (long)var10[1] > 200L) {
            var10 = new double[]{0.0, (double)var8};
        }

        var10[0] += var6;
        var10[1] = (double)var8;
        if (var10[0] >= 5.999999) {
            WEAR.remove(var1.id);
            ArcanaNetwork.shatter(var0, var1, var2);
            BarrierData.get(var0).remove(var0, var1.id, false);
            var0.playSound(null, BlockPos.containing(var2), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0F, 0.9F);
        } else {
            WEAR.put(var1.id, var10);
            ArcanaNetwork.effect(var0, var2, var5 == 0 ? 23 : 22, (int)Math.round(var10[0] / 6.0 * 5.0));
            var0.playSound(null, BlockPos.containing(var2), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.6F, var5 >= 0 ? 1.5F : 1.9F);
        }
    }
}
