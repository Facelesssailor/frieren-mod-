package dev.pete.frierenarcana.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;

public final class CinemaDirector {
   private static long lockedFor = -1L;
   private static float lockYaw;
   private static float lockPitch;
   private static Vec3 lockLook = new Vec3(0.0, 0.0, 1.0);
   private static Vec3 anchor = null;
   private static Vec3 sEye;
   private static Vec3 sEyePrev;
   private static Vec3 sFlat;
   private static Vec3 sSide;
   private static Vec3 sCenter;
   private static Vec3 sHit;
   private static double sRadius;
   private static boolean sFrac;
   private static boolean sReady;
   private static int shot = -1;
   private static int current = -1;
   private static float bands = 0.0F;
   private static float descent = 0.0F;
   private static long fractureSeen = 0L;
   static final double END_AFTER_SHATTER = BreakTimeline.END;

   private CinemaDirector() {
   }

   /** The shot the cutscene camera is on (see {@link #shot}), or -1 outside the cutscene. */
   public static int currentShot() {
      return ArcanaCinematic.active() ? current : -1;
   }

   public static Vec3 lockedLook() {
      return lockLook;
   }

   public static float bandTag(double var0) {
      double var2 = 1.0 - 2.4 * (double)descent;
      double var4 = (double)bands * Math.max(0.0, Math.min(1.0, (var0 - var2) / 0.4));
      return 0.6F + 0.04F * (float)var4;
   }

   public static long fractureNanos() {
      return ArcanaCinematic.active() ? fractureSeen : 0L;
   }

   public static void tick(Post var0) {
      Minecraft var1 = Minecraft.getInstance();

      while (ArcanaKeys.SKIP.consumeClick()) {
         ArcanaCinematic.skip();
      }

      if (!ArcanaCinematic.active()) {
         current = -1;
         lockedFor = -1L;
         shot = -1;
         bands = 0.0F;
         descent = 0.0F;
         sReady = false;
         sEye = null;
      } else {
         LocalPlayer var2 = var1.player;
         if (var2 != null && var1.level != null && ArcanaCinematic.camera.level() == var1.level && var2.isAlive() && var1.screen == null) {
            if (BlackHoleCinema.running()) {
               BlackHoleCinema.tick(var1, var2);
            } else {
               long var3 = System.nanoTime();
               double var5 = (double)(var3 - ArcanaCinematic.started) / 1.0E9;
               double var7 = ArcanaCinematic.release == 0L ? 0.0 : (double)(var3 - ArcanaCinematic.release) / 1.0E9;
               bands = ArcanaCinematic.release == 0L ? 0.0F : 1.0F;
               double var9 = Math.min(1.0, Math.max(0.0, (var5 - 0.3) / 3.6));
               descent = (float)(var9 * var9 * (3.0 - 2.0 * var9));
               if (lockedFor != ArcanaCinematic.started) {
                  lockedFor = ArcanaCinematic.started;
                  lockYaw = var2.getYRot();
                  lockPitch = var2.getXRot();
                  lockLook = var2.getLookAngle().normalize();
                  shot = -1;
                  fractureSeen = 0L;
                  anchor = var2.position().add(0.0, 1.3, 0.0);
               }

               float pitchNow = pitch(lockPitch, var7, ArcanaCinematic.release != 0L);
               var2.setYRot(lockYaw);
               var2.setXRot(pitchNow);
               var2.yRotO = lockYaw;
               var2.xRotO = pitchNow;
               var2.yHeadRot = lockYaw;
               var2.yBodyRot = lockYaw;
               var2.yBodyRotO = lockYaw;
               Vec3 var38 = lockLook;
               Vec3 var10 = new Vec3(var38.x, 0.0, var38.z);
               var10 = var10.lengthSqr() < 0.01 ? new Vec3(0.0, 0.0, 1.0) : var10.normalize();
               Vec3 var11 = new Vec3(-var10.z, 0.0, var10.x);
               Vec3 var12 = var2.position().add(0.0, 1.3, 0.0);
               anchor = anchor != null && !(anchor.distanceToSqr(var12) > 64.0) ? anchor.lerp(var12, 0.08) : var12;
               Vec3 var13 = anchor;
               ArcanaClient.Fracture var14 = null;

               for (ArcanaClient.Fracture var16 : ArcanaClient.fractures()) {
                  if (ArcanaCinematic.release != 0L
                     && var16.startNanos() >= ArcanaCinematic.release - 400000000L
                     && (var14 == null || var16.startNanos() > var14.startNanos())) {
                     var14 = var16;
                  }
               }

               Vec3 var40 = null;
               double var41 = 0.0;
               Vec3 var18 = null;
               if (var14 != null) {
                  var40 = var14.field().center();
                  var41 = (double)var14.field().radius();
                  var18 = var14.impact();
                  if (fractureSeen == 0L) {
                     fractureSeen = var14.startNanos();
                  }
               } else {
                  double var19 = Double.POSITIVE_INFINITY;

                  for (ArcanaClient.VisualField var22 : ArcanaClient.fields()) {
                     Vec3 var23 = var13.subtract(var22.center());
                     double var24 = var23.dot(var38);
                     double var26 = var23.lengthSqr() - (double)var22.radius() * (double)var22.radius();
                     double var28 = var24 * var24 - var26;
                     if (!(var28 < 0.0)) {
                        double var30 = Math.sqrt(var28);
                        double var32 = -var24 - var30;
                        double var34 = -var24 + var30;
                        double var36 = var32 > 0.5 ? var32 : (var34 > 0.5 ? var34 : Double.POSITIVE_INFINITY);
                        if (var36 < var19) {
                           var19 = var36;
                           var40 = var22.center();
                           var41 = (double)var22.radius();
                           var18 = var13.add(var38.scale(var36));
                        }
                     }
                  }
               }

               if (var14 != null) {
                  bands = 1.0F;
               }

               double var42 = fractureSeen == 0L ? -1.0 : (double)(var3 - fractureSeen) / 1.0E9;
               if (!(var5 > 70.0)
                  && (fractureSeen == 0L || !(var42 > BreakTimeline.END))
                  && (fractureSeen != 0L || ArcanaCinematic.release == 0L || !(var7 > 6.1))) {
                  sEyePrev = sEye == null ? var13 : sEye;
                  sEye = var13;
                  sFlat = var10;
                  sSide = var11;
                  sCenter = var40;
                  sRadius = var41;
                  sHit = var18;
                  sFrac = var14 != null;
                  sReady = true;
               } else {
                  ArcanaCinematic.restore();
               }
            }
         } else {
            ArcanaCinematic.restore();
         }
      }
   }

   static void frame(float var0) {
      if (sReady && sEye != null) {
         Minecraft var1 = Minecraft.getInstance();
         if (var1.level != null && var1.player != null) {
            long var2 = System.nanoTime();
            double var4 = (double)(var2 - ArcanaCinematic.started) / 1.0E9;
            double var6 = ArcanaCinematic.release == 0L ? 0.0 : (double)(var2 - ArcanaCinematic.release) / 1.0E9;
            Vec3 var8 = sEyePrev.lerp(sEye, (double)var0);
            Vec3 var9 = sFlat;
            Vec3 var10 = sSide;
            Vec3 var11 = sCenter;
            Vec3 var12 = sHit;
            double var13 = sRadius;
            if (!sFrac || var11 != null && var12 != null) {
               int[] var15 = new int[1];
               Vec3[] var16 = shot(var4, var6, ArcanaCinematic.release != 0L, sFrac, var8, var9, var10, var11, var13, var12, var15);
               Vec3 var17 = var16[0];
               Vec3 var18 = var16[1];
               int var19 = var15[0];
               current = var19;
               BlockHitResult var20 = var1.level.clip(new ClipContext(var18, var17, Block.VISUAL, Fluid.NONE, var1.player));
               if (var20.getType() != Type.MISS) {
                  var17 = var20.getLocation().lerp(var18, 0.12);
               }

               CinemaFrame.place(var17, var18, var19, ease(var19));
            }
         }
      }
   }

   static double ease(int var0) {
      return var0 != 1 && var0 != 3 && var0 != 6 ? 0.24 : 0.12;
   }

   private static double ss(double var0, double var2, double var4) {
      var4 = Math.max(0.0, Math.min(1.0, (var4 - var0) / (var2 - var0)));
      return var4 * var4 * (3.0 - 2.0 * var4);
   }

   static Vec3[] shot(double var0, double var2, boolean var4, boolean var5, Vec3 var6, Vec3 var7, Vec3 var8, Vec3 var9, double var10, Vec3 var12, int[] var13) {
      // var0: seconds since the charge began; var2 (u): seconds since release; var6: chest anchor; var7: flat look; var8: side
      double u = var2;
      Vec3 var19 = var6.add(0.0, -0.57, 0.0).add(var7.scale(0.42));
      Vec3 head = var6.add(0.0, 0.36, 0.0);
      Vec3 var14;
      Vec3 var15;
      byte var16;
      if (!var4) {
         if (var0 < 1.8) {
            var16 = 10;
            double var20 = var0 / 1.8;
            var14 = var19.add(var7.scale(1.05 - 0.1 * var20)).add(var8.scale(0.08)).add(0.0, 0.3, 0.0);
            var15 = var19.add(0.0, -0.02, 0.0);
         } else if (var0 < 3.2) {
            var16 = 12;
            var14 = var6.add(var7.scale(2.5)).add(0.0, -0.4, 0.0);
            var15 = var6.add(0.0, -0.45, 0.0);
         } else if (var0 < 6.2) {
            var16 = 11;
            var14 = var6.add(var7.scale(16.0)).add(var8.scale(8.0)).add(0.0, -0.7, 0.0);
            var15 = var6.add(0.0, 1.6, 0.0);
         } else {
            var16 = 13;
            Vec3 var30 = var7.scale(55.0).add(var8.scale(-22.0)).normalize();
            double var21 = 59.0;
            if (var9 != null) {
               Vec3 var23 = var6.subtract(var9);
               double var24 = var23.dot(var30);
               double var26 = var23.lengthSqr() - (var10 - 8.0) * (var10 - 8.0);
               double var28 = var24 * var24 - var26;
               if (var28 > 0.0) {
                  var21 = Math.max(12.0, Math.min(var21, -var24 + Math.sqrt(var28)));
               }
            }

            var14 = var6.add(var30.scale(var21)).add(0.0, 2.0, 0.0);
            var15 = var6.add(0.0, 5.0, 0.0);
         }
      } else if (!var5 || var9 == null || var12 == null) {
         var16 = 5;
         double var38 = ss(BreakTimeline.PINCH, BreakTimeline.PINCH + 1.2, u);
         var14 = var6.add(var7.scale(2.4)).add(0.0, -0.35 + 0.4 * var38, 0.0);
         var15 = var6.add(0.0, -0.42 + 8.0 * var38 * var38, 0.0);
      } else if (u < 4.5) {
         // 15:03.0 - 15:07.5: front, held still, through the hand sequence and the column leaving the hands
         var16 = 5;
         var14 = var6.add(var7.scale(2.4)).add(0.0, -0.35, 0.0);
         var15 = var6.add(0.0, -0.42, 0.0);
      } else if (u < 5.0) {
         // 15:07.5: looking straight up into the lit treetops
         var16 = 7;
         var14 = var6.add(var7.scale(1.6)).add(var8.scale(0.9)).add(0.0, -0.5, 0.0);
         var15 = var6.add(0.0, 14.0, 0.0).add(var7.scale(1.5));
      } else if (u < 5.5) {
         // 15:08.0: the far treeline, the needle appears above it
         var16 = 8;
         var14 = var6.add(var7.scale(Math.max(30.0, var10 * 0.8))).add(var8.scale(6.0)).add(0.0, -0.4, 0.0);
         var15 = var6.add(0.0, 3.0, 0.0);
      } else if (u < 7.0) {
         // 15:08.5 - 15:10.0: riding beside the needle's tip as it climbs through the oily glass
         var16 = 6;
         Vec3 var22 = var6.add(0.0, -0.57, 0.0).lerp(var12, BreakTimeline.needle(u));
         Vec3 var41 = new Vec3(var6.x - var9.x, 0.0, var6.z - var9.z);
         var41 = var41.lengthSqr() < 0.25 ? var7 : var41.normalize();
         var14 = var22.add(var41.scale(4.5)).add(new Vec3(-var41.z, 0.0, var41.x).scale(1.5)).add(0.0, -2.5, 0.0);
         var15 = var22.add(0.0, 1.5, 0.0);
      } else if (u < 9.0) {
         // 15:10.0 - 15:12.0: far outside, the whole dome; the bands start at the top while the needle is half way up
         var16 = 1;
         double p = ss(7.0, 9.0, u);
         Vec3 var32 = new Vec3(var6.x - var9.x, 0.0, var6.z - var9.z);
         var32 = var32.lengthSqr() < 0.25 ? var7.scale(-1.0) : var32.normalize();
         double var39 = Math.cos(0.5);
         double var43 = Math.sin(0.5);
         Vec3 var25 = new Vec3(var32.x * var39 - var32.z * var43, 0.0, var32.x * var43 + var32.z * var39);
         // far away and low on a long lens, so the dome sits whole in the frame and the bands read as stripes (15:10)
         double dist = Math.min(var10 * 3.5 + 14.0, var10 + 168.0);
         var14 = var9.add(var25.scale(dist)).add(0.0, var10 * 0.2 + 4.0, 0.0);
         var15 = var9.add(0.0, var10 * (0.48 + 0.03 * p), 0.0);
         // the dome spans about 80% of the picture's width in the episode, its foot hidden by the near trees
         farTan = var10 / dist / 0.78;
      } else if (u < 11.25) {
         // 15:12.0 - 15:14.25: profile close-up looking up, the banded dome soft behind
         var16 = 14;
         var14 = head.add(var8.scale(1.0)).add(var7.scale(-0.15)).add(0.0, -0.1, 0.0);
         var15 = head.add(var8.scale(-0.4)).add(var7.scale(0.12)).add(0.0, 0.3, 0.0);
      } else if (u < 13.0) {
         // 15:14.25 - 15:16.0: looking up past the caster along the needle into the descending bands
         var16 = 15;
         double q = ss(11.25, 13.0, u);
         Vec3 feet = var6.add(0.0, -1.3, 0.0);
         var14 = feet.add(var7.scale(-2.3)).add(var8.scale(0.9)).add(0.0, 0.35, 0.0);
         var15 = feet.add(var7.scale(6.0)).add(0.0, 6.0 + 2.0 * q, 0.0);
      } else if (u < BreakTimeline.SHOULDER) {
         // 15:16.0 - 15:20.0: inside, low and still - bands fill the sky, the flicker, the shatter, crystals hanging
         var16 = 2;
         Vec3 var34 = new Vec3(var9.x - var6.x, 0.0, var9.z - var6.z);
         var34 = var34.lengthSqr() < 1.0 ? var7 : var34.normalize();
         double var40 = Math.max(6.0, Math.min(18.0, var10 * 0.4));
         var14 = var6.add(var34.scale(var40)).add(0.0, -0.7, 0.0);
         var15 = var6.add(0.0, var40 * 0.3, 0.0);
      } else if (u < BreakTimeline.MOTES) {
         // 15:20.0 - 15:26.0: over the shoulder, crystals drifting down in a blurred sky
         var16 = 3;
         double var36 = (u - BreakTimeline.SHOULDER) / (BreakTimeline.MOTES - BreakTimeline.SHOULDER);
         var14 = var6.add(var7.scale(-2.2)).add(var8.scale(1.4)).add(0.0, -0.2, 0.0);
         var15 = var6.add(var7.scale(7.0)).add(var8.scale(3.0 + 1.0 * var36)).add(0.0, 4.0 - 0.5 * var36, 0.0);
      } else if (u < BreakTimeline.WIDE) {
         // 15:26.0 - 15:30.5: the caster among the trees, green motes rising beside her
         var16 = 4;
         double var37 = (u - BreakTimeline.MOTES) / (BreakTimeline.WIDE - BreakTimeline.MOTES);
         // full figure filling most of the height, from a little to the side (15:26 - 15:30)
         var14 = var6.add(var7.scale(2.3 - 0.15 * var37)).add(var8.scale(0.65)).add(0.0, -0.3, 0.0);
         var15 = var6.add(0.0, -0.4, 0.0);
      } else {
         // 15:30.5 onwards: the wide, still sky with the iridescent sheen
         var16 = 9;
         var14 = var6.add(var7.scale(3.0)).add(0.0, -0.8, 0.0);
         var15 = var6.add(var7.scale(40.0)).add(0.0, 7.0, 0.0);
      }

      var13[0] = var16;
      return new Vec3[]{var14, var15};
   }

   /** Tangent of the half-width the far shot wants to frame (set by {@link #shot}). */
   static double farTan = 0.4;

   /** Field of view for a shot: the far shots use a longer lens, as the episode does (15:08, 15:10). */
   static double fov(int shot, double base) {
      return fov(shot, base, 16.0 / 9.0);
   }

   static double fov(int shot, double base, double aspect) {
      if (shot == 1) {
         double v = Math.toDegrees(2.0 * Math.atan(farTan / Math.max(0.5, aspect)));
         return Math.max(8.0, Math.min(base, v));
      }
      return shot == 8 ? Math.min(base, 55.0) : base;
   }

   /** Head pitch for the player model: looking up at the dome in the close-ups (15:12 - 15:16). */
   static float pitch(float locked, double u, boolean released) {
      if (!released) {
         return locked;
      }
      double up = ss(8.6, 9.0, u) * (1.0 - ss(12.8, 13.2, u));
      return (float)(locked + (-28.0 - locked) * up);
   }
}
