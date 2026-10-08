package dev.pete.frierenarcana.client;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

final class BreakerSound {
    private static final Set<Long> PLAYED = new HashSet<>();

    private BreakerSound() {
    }

    static void shatter(ArcanaClient.Fracture var0) {
        try {
            if (PLAYED.size() > 64) {
                PLAYED.clear();
            }

            if (!PLAYED.add(var0.startNanos())) {
                return;
            }

            Minecraft var1 = Minecraft.getInstance();
            if (var1 == null || var1.level == null) {
                return;
            }

            Vec3 var2 = var0.impact();
            var1.level.playLocalSound(var2.x, var2.y, var2.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 4.0F, 0.55F, false);
            var1.level.playLocalSound(var2.x, var2.y, var2.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 3.0F, 0.8F, false);
        } catch (Throwable var3) {
        }
    }
}
