package dev.pete.frierenarcana.client;

import net.minecraft.world.phys.Vec3;

/** Client half of {@link dev.pete.frierenarcana.RainHold}: a broken examination barrier keeps its rain shadow until it visibly shatters. */
public final class ClientRainHold {
   private ClientRainHold() {
   }

   private static boolean holding(ArcanaClient.Fracture f, long now) {
      return !f.field().defensive() && (double)(now - f.startNanos()) / 1.0E9 < BreakTimeline.SHATTER_AT;
   }

   public static boolean blocked(Vec3 p) {
      try {
         long now = System.nanoTime();
         for (ArcanaClient.Fracture f : ArcanaClient.fractures()) {
            if (holding(f, now)) {
               double r = f.field().radius();
               if (f.field().center().distanceToSqr(p) < r * r) {
                  return true;
               }
            }
         }
      } catch (Throwable ignored) {
      }
      return false;
   }

   /** Raise the rain's floor to the top of a still-standing broken dome. */
   public static int height(int x, int z, int floor) {
      int h = floor;
      try {
         long now = System.nanoTime();
         for (ArcanaClient.Fracture f : ArcanaClient.fractures()) {
            if (holding(f, now)) {
               Vec3 c = f.field().center();
               double r = f.field().radius();
               double dx = x + 0.5 - c.x;
               double dz = z + 0.5 - c.z;
               double d2 = dx * dx + dz * dz;
               if (d2 < r * r) {
                  h = Math.max(h, (int)Math.ceil(c.y + Math.sqrt(r * r - d2)));
               }
            }
         }
      } catch (Throwable ignored) {
      }
      return h;
   }
}
