package dev.pete.frierenarcana.client;

/**
 * Barrier Breaker timing, measured frame by frame from Frieren ep. 21 (14:49-15:42).
 * All times are seconds after the player releases the spell (anime 15:03.00 = 0).
 *
 * <pre>
 *  0.00-1.28  glass panes burst around the hands         15:03.00
 *  1.36-1.88  small cyan orb between the hands             15:04.36
 *  1.92-2.30  pink sphere with green blades               15:04.92
 *  2.28-2.68  small pink orb split by a red line           15:05.28
 *  2.80-3.12  whitish-green spear collapses at the hands   15:05.80
 *  3.20       the light leaves the hands as a column       15:06.20
 *  3.20-12.0  needle climbs to the apex at ~0.12 radius/s  (0.46 R at 15:10.0, 0.61 R at 15:11.25)
 *  6.20-10.2  rainbow bands descend from the apex to the ground while the needle is still rising
 *             (seen from the far shot: a thin cap at 15:10.0, the top half striped by 15:11.75)
 *  14.78      white / inverted / normal / black / bright / dark - one anime frame each
 *  15.03      shatter: white-out, crystals hang over the field
 *  17.0       over-the-shoulder, background blurred, crystals falling
 *  23.0       caster among the trees with green motes
 *  27.5       wide sky with the iridescent sheen; rain may start
 * </pre>
 */
public final class BreakTimeline {
   public static final double ORB = 1.36;
   public static final double SPHERE = 1.92;
   public static final double SPLIT = 2.28;
   public static final double SPEAR = 2.80;
   public static final double PINCH = 3.2;
   public static final double APEX = 12.0;
   public static final double BANDS_BEGIN = 6.2;
   public static final double BANDS_GROUND = 10.2;
   public static final double FLICKER = 14.78;
   public static final double SHATTER_AT = 15.03;
   /** w (seconds after the needle origin) at which the dome shatters; shard/crack code is written against it. */
   public static final double SHATTER = 5.0;
   /** u at which w = 0. */
   public static final double ORIGIN = SHATTER_AT - SHATTER;
   public static final double SHOULDER = 17.0;
   public static final double MOTES = 23.0;
   public static final double WIDE = 27.5;
   public static final double END = 32.5;
   /** How long a fracture stays alive on the client (seconds after it was received). */
   public static final double LIFE = END + 1.5;
   /** One anime frame (23.976 fps). */
   public static final double FRAME = 1.0 / 23.976;

   private BreakTimeline() {
   }

   static double clamp(double x) {
      return x < 0.0 ? 0.0 : (x > 1.0 ? 1.0 : x);
   }

   static double sstep(double a, double b, double x) {
      double t = clamp((x - a) / (b - a));
      return t * t * (3.0 - 2.0 * t);
   }

   /** Needle tip height as a fraction of the way from the hands to the apex. */
   public static double needle(double u) {
      if (u <= PINCH) {
         return 0.0;
      }
      return Math.pow(clamp((u - PINCH) / (APEX - PINCH)), 0.95);
   }

   /**
    * Height (unit-sphere y) of the lower edge of the rainbow bands. 1 = apex, 0 = ground ring,
    * values below 0 carry on past the ground so the lower hemisphere fills too.
    */
   public static double bandFront(double u) {
      if (u < BANDS_BEGIN) {
         return 1.05;
      }
      double t = (u - BANDS_BEGIN) / (BANDS_GROUND - BANDS_BEGIN);
      // measured: linear at ~0.27 radius/s from the apex, easing slightly as it reaches the ground
      return 1.0 - (t <= 1.0 ? t : 1.0 + (1.0 - Math.exp(-(t - 1.0) * 1.6)) * 0.6);
   }

   /** 0 before the bands, rising to 1 when the bands reach the ground. */
   public static double bandProgress(double u) {
      return clamp((u - BANDS_BEGIN) / (BANDS_GROUND - BANDS_BEGIN));
   }

   /** How strongly the struck dome shows its oily film (starts while the needle is still near the hands). */
   public static double film(double u) {
      return sstep(PINCH, PINCH + 2.3, u);
   }

   /**
    * The six one-frame flashes just before the shatter (15:17.78-15:18.00), in order:
    * 1 white, 2 colour-inverted, 3 normal with a flare at the needle base, 4 black, 5 over-exposed, 6 dark and contrasty.
    * Returns 0 outside them.
    */
   public static int flicker(double u) {
      double f = (u - FLICKER) / FRAME;
      if (f < 0.0 || u >= SHATTER_AT) {
         return 0;
      }
      return Math.min(6, 1 + (int)Math.floor(f));
   }

   /** Convert seconds since release into the w clock used by the crack and shard code. */
   public static double w(double u) {
      return u - ORIGIN;
   }
}
