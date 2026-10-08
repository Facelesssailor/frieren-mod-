package dev.pete.frierenarcana.client;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class CinemaOverlay {
    private CinemaOverlay() {
    }

    @SubscribeEvent
    public static void hud(Post var0) {
        if (ArcanaCinematic.breakerActive()) {
            long var1 = System.nanoTime();
            double var3 = (double)(var1 - ArcanaCinematic.startedNanos()) / 1.0E9;
            long var5 = CinemaDirector.fractureNanos();
            double var7 = var5 == 0L ? -1.0 : (double)(var1 - var5) / 1.0E9;
            GuiGraphics var9 = var0.getGuiGraphics();
            int var10 = var9.guiWidth();
            int var11 = var9.guiHeight();

            for (int var15 : layers(var3, var7)) {
                if (var15 >>> 24 > 1) {
                    var9.fill(0, 0, var10, var11, var15);
                }
            }
        }
    }

    private static double ss(double var0, double var2, double var4) {
        var4 = Math.max(0.0, Math.min(1.0, (var4 - var0) / (var2 - var0)));
        return var4 * var4 * (3.0 - 2.0 * var4);
    }

    static int[] layers(double var0, double var2) {
        double var4 = Math.max(0.0, 1.0 - var0 / 0.7);
        double var6 = 0.0;
        double var8 = 0.0;
        double var10 = 0.0;
        if (var2 >= 0.0) {
            double var12 = var2 - 6.2;
            double var14 = 5.0;
            if (var12 > var14 - 0.3 && var12 < var14 - 0.15) {
                var8 = 0.3;
            }

            if (var12 > var14 - 0.15 && var12 < var14) {
                var10 = 0.35 + 0.4 * ss(var14 - 0.15, var14, var12);
            }

            double var16 = var12 - var14;
            if (var16 > 0.0) {
                var6 = 0.97 * ss(0.0, 0.06, var16) * (1.0 - ss(0.32, 0.75, var16));
            }

            var4 = Math.max(var4, (var2 - 20.099999999999998) / 0.7);
        }

        return new int[]{argb(var8, 14217312), argb(var10, 15138815), argb(var6, 16514559), argb(Math.min(1.0, var4), 0)};
    }

    private static int argb(double var0, int var2) {
        return (int)Math.round(Math.max(0.0, Math.min(1.0, var0)) * 255.0) << 24 | var2;
    }
}
