package dev.pete.frierenarcana.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Longer lens for the cutscene's far shots. */
@EventBusSubscriber(
   modid = "frieren_arcana",
   value = {Dist.CLIENT}
)
public final class CinemaFov {
   private CinemaFov() {
   }

   @SubscribeEvent
   public static void fov(ViewportEvent.ComputeFov event) {
      try {
         if (ArcanaCinematic.breakerActive()) {
            int shot = CinemaDirector.currentShot();
            if (shot >= 0) {
               net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
               double aspect = mc.getWindow().getHeight() <= 0 ? 16.0 / 9.0 : (double)mc.getWindow().getWidth() / (double)mc.getWindow().getHeight();
               event.setFOV(CinemaDirector.fov(shot, event.getFOV(), aspect));
            }
         }
      } catch (Throwable ignored) {
      }
   }
}
