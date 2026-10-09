package dev.pete.frierenarcana;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * An examination barrier is removed on the server the moment Barrier Breaker is released, but on screen it only shatters
 * about fifteen seconds later (15:03 -> 15:18 in ep. 21). Until then it still keeps the weather out: no rain falls inside it,
 * and once it has shattered the world's own rain (if it is raining there at all) simply comes back.
 */
public final class RainHold {
   /** Seconds from release to the visible shatter (BreakTimeline.SHATTER_AT, kept here so the server never loads client code). */
   public static final double SHATTER_SECONDS = 15.03;
   private static final List<RainHold.Hold> HOLDS = new ArrayList<>();

   private RainHold() {
   }

   /** Called when the server tells clients a barrier has broken. */
   public static void broken(ServerLevel level, BarrierData.Field field) {
      try {
         if (level == null || field == null || field.defensive) {
            return;
         }
         long until = level.getGameTime() + Math.round(SHATTER_SECONDS * 20.0);
         synchronized (HOLDS) {
            HOLDS.removeIf(h -> h.level == level && h.center.equals(field.center));
            HOLDS.add(new RainHold.Hold(level, field.center, field.radius, until));
         }
      } catch (Throwable ignored) {
      }
   }

   /** Server side: is rain still kept out at {@code pos} because a broken barrier has not visibly shattered yet? */
   public static boolean blocked(ServerLevel level, BlockPos pos) {
      if (HOLDS.isEmpty()) {
         return false;
      }
      try {
         long now = level.getGameTime();
         double x = pos.getX() + 0.5;
         double y = pos.getY() + 0.5;
         double z = pos.getZ() + 0.5;
         synchronized (HOLDS) {
            HOLDS.removeIf(h -> now >= h.until || now < h.until - 400L);
            for (RainHold.Hold h : HOLDS) {
               if (h.level == level) {
                  double dx = x - h.center.x;
                  double dy = y - h.center.y;
                  double dz = z - h.center.z;
                  if (dx * dx + dy * dy + dz * dz < (double)h.radius * h.radius) {
                     return true;
                  }
               }
            }
         }
      } catch (Throwable ignored) {
      }
      return false;
   }

   private record Hold(ServerLevel level, Vec3 center, int radius, long until) {
   }
}
