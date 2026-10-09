package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * One shot of the Zoltraak barrage, drawn the way Fern's volleys look in the anime: a thin white streak with a pale blue
 * glow that leaves its launch point at an angle, bends smoothly onto the target, and is gone a moment after it lands -
 * a fast-moving head with a tapering trail rather than a chain of separate beam pieces.
 */
final class FernBarrageFx {
   static final int STYLE = 21;
   static final double LIFE = 0.62;
   private static final int N = 28;

   private FernBarrageFx() {
   }

   private static double hash(double a, double b, double c, int salt) {
      double v = Math.sin(a * 12.9898 + b * 78.233 + c * 37.719 + salt * 4.1414) * 43758.5453;
      return v - Math.floor(v);
   }

   private static double clamp(double x) {
      return x < 0.0 ? 0.0 : (x > 1.0 ? 1.0 : x);
   }

   private static Vec3 bezier(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double t) {
      double s = 1.0 - t;
      return p0.scale(s * s * s).add(p1.scale(3.0 * s * s * t)).add(p2.scale(3.0 * s * t * t)).add(p3.scale(t * t * t));
   }

   /** Bow the shot away from the caster's line of sight, on the side it was launched from (0 if the caster is not found). */
   private static double outward(Vec3 start, Vec3 side) {
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level == null) {
            return 0.0;
         }
         Player best = null;
         double bestD = 3.5 * 3.5;
         for (Player p : mc.level.players()) {
            double d = p.getEyePosition().distanceToSqr(start);
            if (d < bestD) {
               bestD = d;
               best = p;
            }
         }
         if (best == null) {
            return 0.0;
         }
         return start.subtract(best.getEyePosition()).dot(side) >= 0.0 ? 1.0 : -1.0;
      } catch (Throwable t) {
         return 0.0;
      }
   }

   /** Fern's small launch circle: two rings and six spokes, facing along the shot. */
   private static void circle(VertexConsumer vc, Matrix4f m, Vec3 cam, Vec3 c, Vec3 dir, double r, float a) {
      Vec3 u = new Vec3(-dir.z, 0.0, dir.x);
      u = u.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : u.normalize();
      Vec3 v = dir.cross(u).normalize();
      int n = 24;
      for (int ring = 0; ring < 2; ring++) {
         double rr = r * (ring == 0 ? 1.0 : 0.72);
         for (int i = 0; i < n; i++) {
            double t0 = Math.PI * 2.0 * i / n;
            double t1 = Math.PI * 2.0 * (i + 1) / n;
            Vec3 p0 = c.add(u.scale(Math.cos(t0) * rr)).add(v.scale(Math.sin(t0) * rr));
            Vec3 p1 = c.add(u.scale(Math.cos(t1) * rr)).add(v.scale(Math.sin(t1) * rr));
            BreakerFx.ribbon(vc, m, cam, p0, p1, ring == 0 ? 0.03 : 0.018, 0.86F, 0.92F, 1.0F, 0.9F * a);
         }
      }
      for (int i = 0; i < 6; i++) {
         double t = Math.PI * 2.0 * i / 6.0;
         Vec3 p0 = c.add(u.scale(Math.cos(t) * r * 0.72)).add(v.scale(Math.sin(t) * r * 0.72));
         Vec3 p1 = c.add(u.scale(Math.cos(t + 2.0944) * r * 0.72)).add(v.scale(Math.sin(t + 2.0944) * r * 0.72));
         BreakerFx.ribbon(vc, m, cam, p0, p1, 0.014, 0.8F, 0.9F, 1.0F, 0.7F * a);
      }
   }

   static void draw(VertexConsumer vc, Matrix4f m, Vec3 start, Vec3 end, double age) {
      Vec3 cam = ArcanaClient.camera();
      if (cam == null || age > LIFE) {
         return;
      }
      Vec3 dir = end.subtract(start);
      double len = dir.length();
      if (len < 0.05) {
         return;
      }
      Vec3 f = dir.scale(1.0 / len);
      Vec3 side = new Vec3(-f.z, 0.0, f.x);
      side = side.lengthSqr() < 1.0E-4 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
      Vec3 up = side.cross(f).normalize();
      if (up.y < 0.0) {
         up = up.scale(-1.0);
      }
      double qx = Math.floor(start.x * 8.0);
      double qy = Math.floor(start.y * 8.0);
      double qz = Math.floor(start.z * 8.0);
      double sign = outward(start, side);
      if (sign == 0.0) {
         sign = hash(qx, qy, qz, 1) < 0.5 ? -1.0 : 1.0;
      }
      double reach = Math.min(1.0, len / 8.0);
      double lead = Math.min(3.0, len * 0.3);
      Vec3 p1 = start.add(f.scale(lead)).add(side.scale(sign * (0.9 + 0.7 * hash(qx, qy, qz, 2)) * reach)).add(up.scale((-0.2 + 0.8 * hash(qx, qy, qz, 3)) * reach));
      Vec3 p2 = end.subtract(f.scale(lead)).add(side.scale(sign * 0.35 * hash(qx, qy, qz, 4) * reach)).add(up.scale(0.25 * (hash(qx, qy, qz, 5) - 0.5) * reach));
      Vec3[] pts = new Vec3[N + 1];
      for (int i = 0; i <= N; i++) {
         pts[i] = bezier(start, p1, p2, end, (double)i / N);
      }
      // the head races along the curve, the trail follows and is pulled into the target
      double travel = 0.16 + 0.006 * len;
      double head = clamp(age / travel);
      double tail = clamp((age - 0.09) / (travel + 0.14));
      double far = Math.max(1.0, cam.distanceTo(bezier(start, p1, p2, end, head)) / 12.0);
      if (head > tail + 1.0E-3) {
         double span = head - tail;
         for (int i = 0; i < N; i++) {
            double t0 = (double)i / N;
            double t1 = (double)(i + 1) / N;
            if (t1 <= tail || t0 >= head) {
               continue;
            }
            Vec3 a = t0 < tail ? bezier(start, p1, p2, end, tail) : pts[i];
            Vec3 b = t1 > head ? bezier(start, p1, p2, end, head) : pts[i + 1];
            // brightest and widest just behind the head, thinning toward the tail
            double k = clamp(((Math.min(t1, head) + Math.max(t0, tail)) * 0.5 - tail) / span);
            float al = (float)(Math.pow(k, 0.7));
            BreakerFx.ribbon(vc, m, cam, a, b, (0.42 * (0.4 + 0.6 * k)) * far, 0.55F, 0.72F, 1.0F, 0.12F * al);
            BreakerFx.ribbon(vc, m, cam, a, b, (0.17 * (0.5 + 0.5 * k)) * far, 0.78F, 0.87F, 1.0F, 0.38F * al);
            BreakerFx.ribbon(vc, m, cam, a, b, (0.065 * (0.35 + 0.65 * k)) * far, 1.0F, 1.0F, 1.0F, 0.97F * al);
         }
         Vec3 h = bezier(start, p1, p2, end, head);
         if (head < 1.0) {
            PixelFx.sprite(h, 0.32 * far, PixelFx.GLINT, 1.0F, 1.0F, 1.0F, 0.9F);
            PixelFx.sprite(h, 0.6 * far, PixelFx.GLOW, 0.7F, 0.82F, 1.0F, 0.45F);
         }
      }
      // a small circle where the shot leaves, opening and fading in a blink, and a white pop where it lands
      float launch = (float)(clamp(age / 0.03) * (1.0 - clamp((age - 0.08) / 0.16)));
      if (launch > 0.0F) {
         Vec3 t0 = bezier(start, p1, p2, end, 0.02).subtract(start).normalize();
         circle(vc, m, cam, start, t0, 0.3 * (0.7 + 0.3 * clamp(age / 0.08)), launch);
         PixelFx.sprite(start, 0.6, PixelFx.GLOW, 0.78F, 0.88F, 1.0F, 0.5F * launch);
      }
      double hit = age - travel;
      if (hit >= 0.0) {
         float pop = (float)(1.0 - clamp(hit / (LIFE - travel)));
         PixelFx.sprite(end, 0.5 + 0.6 * clamp(hit / 0.12), PixelFx.BURST + Math.min(3, (int)(hit * 22.0)), 0.95F, 0.97F, 1.0F, pop);
         PixelFx.sprite(end, 0.9, PixelFx.GLOW, 0.75F, 0.85F, 1.0F, 0.5F * pop);
      }
   }
}
