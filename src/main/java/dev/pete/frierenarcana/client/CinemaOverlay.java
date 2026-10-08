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
            double var5 = Math.max(0.0, 1.0 - var3 / 0.7);
            double var7 = 0.0;
            long var9 = CinemaDirector.fractureNanos();
            if (var9 != 0L) {
                double var11 = (double)(var1 - var9) / 1.0E9;
                double var13 = var11 - 1.5;
                if (var13 > 0.0) {
                    var7 = var13 < 0.12 ? var13 / 0.12 * 0.8 : Math.max(0.0, 0.8 * (1.0 - (var13 - 0.12) / 0.7));
                }

                var5 = Math.max(var5, (var11 - 7.8999999999999995) / 0.7);
            }

            GuiGraphics var15 = var0.getGuiGraphics();
            int var12 = var15.guiWidth();
            int var16 = var15.guiHeight();
            if (var7 > 0.004) {
                var15.fill(0, 0, var12, var16, argb(var7, 15925238));
            }

            if (var5 > 0.004) {
                var15.fill(0, 0, var12, var16, argb(Math.min(1.0, var5), 0));
            }
        }
    }

    private static int argb(double var0, int var2) {
        return (int)Math.round(Math.max(0.0, Math.min(1.0, var0)) * 255.0) << 24 | var2;
    }
}
