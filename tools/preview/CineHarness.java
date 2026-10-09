package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Offline preview of the Barrier Breaker cutscene. Runs the mod's own client drawing code (the same classes that ship in
 * the jar) against a fake clock and a fake barrier, captures every vertex they emit, and writes one binary frame per
 * request for the WebGL renderer (prev/render.html), which draws them with the mod's own GLSL shaders.
 *
 * <p>stdin: "f idx u out" lines (u = seconds since release, negative = still charging); stdout: "ok idx".
 */
public final class CineHarness {
   static final class Capture implements VertexConsumer {
      float[] pos = new float[3 * 65536];
      byte[] col = new byte[4 * 65536];
      int n;

      void grow() {
         if ((n + 1) * 3 > pos.length) {
            pos = java.util.Arrays.copyOf(pos, pos.length * 2);
            col = java.util.Arrays.copyOf(col, col.length * 2);
         }
      }

      public VertexConsumer addVertex(float x, float y, float z) {
         grow();
         pos[n * 3] = x;
         pos[n * 3 + 1] = y;
         pos[n * 3 + 2] = z;
         col[n * 4] = (byte)255;
         col[n * 4 + 1] = (byte)255;
         col[n * 4 + 2] = (byte)255;
         col[n * 4 + 3] = (byte)255;
         n++;
         return this;
      }

      public VertexConsumer setColor(int r, int g, int b, int a) {
         int i = (n - 1) * 4;
         col[i] = (byte)r;
         col[i + 1] = (byte)g;
         col[i + 2] = (byte)b;
         col[i + 3] = (byte)a;
         return this;
      }

      public VertexConsumer setUv(float u, float v) {
         return this;
      }

      public VertexConsumer setUv1(int u, int v) {
         return this;
      }

      public VertexConsumer setUv2(int u, int v) {
         return this;
      }

      public VertexConsumer setNormal(float x, float y, float z) {
         return this;
      }
   }

   static Field field(Class<?> c, String name) throws Exception {
      Field f = c.getDeclaredField(name);
      f.setAccessible(true);
      return f;
   }

   public static void main(String[] args) throws Exception {
      double radius = Double.parseDouble(System.getProperty("radius", "48"));
      double charge = Double.parseDouble(System.getProperty("charge", "7.0"));
      int screenH = Integer.parseInt(System.getProperty("height", "720"));
      Vec3 center = new Vec3(0.0, 64.0, 0.0);
      Vec3 feet = new Vec3(9.0, 64.0, -14.0);
      Vec3 look = new Vec3(0.35, 0.0, 1.0).normalize();
      Vec3 eye = feet.add(0.0, 1.62, 0.0);
      double apexY = center.y + Math.sqrt(radius * radius - (eye.x - center.x) * (eye.x - center.x) - (eye.z - center.z) * (eye.z - center.z));
      Vec3 impact = new Vec3(eye.x, apexY, eye.z);

      Class<?> vfC = Class.forName("dev.pete.frierenarcana.client.ArcanaClient$VisualField");
      Object visual = vfC.getConstructor(UUID.class, Vec3.class, int.class, boolean.class).newInstance(UUID.randomUUID(), center, (int)radius, false);
      Class<?> frC = Class.forName("dev.pete.frierenarcana.client.ArcanaClient$Fracture");
      Constructor<?> frNew = frC.getDeclaredConstructor(vfC, Vec3.class, long.class);
      frNew.setAccessible(true);
      @SuppressWarnings("unchecked")
      List<Object> fractures = (List<Object>)field(ArcanaClient.class, "FRACTURES").get(null);
      Field drawCamera = field(ArcanaClient.class, "drawCamera");
      field(CinemaDirector.class, "lockLook").set(null, look);
      Field pq = field(PixelFx.class, "q");
      Field pcount = field(PixelFx.class, "count");
      Method extras = SpellCircleFx.class.getDeclaredMethod("extras", SpellCircleFx.Ctx.class, Vec3.class, Vec3.class, double.class, double.class, float.class);
      extras.setAccessible(true);
      Method scene = SpellCircleFx.class.getDeclaredMethod("scene", SpellCircleFx.Ctx.class, Vec3.class, Vec3.class, double.class);
      scene.setAccessible(true);
      Method fern = SpellFx.class.getDeclaredMethod("fern", VertexConsumer.class, Matrix4f.class, Vec3.class, Vec3.class, Vec3.class, Vec3.class, double.class, double.class);
      fern.setAccessible(true);
      Constructor<SpellCircleFx.Ctx> ctxNew = SpellCircleFx.Ctx.class.getDeclaredConstructor();
      ctxNew.setAccessible(true);

      Vec3 anchor = feet.add(0.0, 1.3, 0.0);
      boolean barrage = System.getProperty("mode", "barrier").startsWith("barrage");
      boolean oldBarrage = System.getProperty("mode", "barrier").equals("barrage-old");
      Vec3[] targets = new Vec3[]{feet.add(look.scale(14.0)).add(new Vec3(-look.z, 0, look.x).scale(-3.0)), feet.add(look.scale(18.0)).add(new Vec3(-look.z, 0, look.x).scale(2.5)), feet.add(look.scale(11.0)).add(new Vec3(-look.z, 0, look.x).scale(4.0))};
      Vec3 flat = new Vec3(look.x, 0.0, look.z).normalize();
      Vec3 side = new Vec3(-flat.z, 0.0, flat.x);
      Matrix4f id = new Matrix4f();
      Vec3 camPrev = null;
      int lastShot = -1;
      double lastU = 0.0;
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      String line;
      while ((line = in.readLine()) != null) {
         String[] p = line.trim().split("\\s+");
         if (p.length < 4 || !p[0].equals("f")) {
            continue;
         }
         int idx = Integer.parseInt(p[1]);
         double u = Double.parseDouble(p[2]);
         String out = p[3];
         long now = System.nanoTime();
         boolean released = u >= 0.0;
         double t = released ? charge + u : charge + u;
         ArcanaCinematic.started = now - (long)(t * 1.0E9);
         ArcanaCinematic.release = released ? now - (long)(u * 1.0E9) : 0L;
         fractures.clear();
         Object frac = null;
         if (released) {
            frac = frNew.newInstance(visual, impact, now - (long)(u * 1.0E9));
            fractures.add(frac);
         }
         // camera, eased between frames the way CinemaFrame.place does
         int[] so = new int[1];
         Vec3[] et = CinemaDirector.shot(t, released ? u : 0.0, released, frac != null, anchor, flat, side, center, radius, impact, so);
         int shot = so[0];
         Vec3 cam = et[0];
         if (camPrev != null && shot == lastShot) {
            double dt = Math.min(0.1, Math.max(0.0, u - lastU));
            double k = 1.0 - Math.pow(1.0 - CinemaDirector.ease(shot), dt * 20.0);
            cam = camPrev.lerp(et[0], k);
         }
         camPrev = cam;
         lastShot = shot;
         lastU = u;
         drawCamera.set(null, cam);
         double gameSec = 1000.0 + t;

         Capture magic = new Capture();
         Capture refr = new Capture();
         pcount.setInt(null, 0);
         if (frac != null && !barrage) {
            BreakerFx.fracture(magic, id, (ArcanaClient.Fracture)frac, gameSec, u);
            BreakerFx.prisms(refr, id, (ArcanaClient.Fracture)frac, u);
            BreakerFx.beam(magic, id, eye.add(flat.scale(0.42)), impact, u);
         }
         SpellCircleFx.Ctx ctx = ctxNew.newInstance();
         ctx.vc = magic;
         ctx.m = id;
         ctx.camX = cam.x;
         ctx.camY = cam.y;
         ctx.camZ = cam.z;
         ctx.fade = 1.0F;
         double chargeLevel = released ? 1.0 : Math.min(1.0, t / 10.0);
         float flick = (float)(0.8 + 0.2 * Math.sin(t * 4.0));
         if (!barrage) {
            extras.invoke(null, ctx, feet, cam, t, chargeLevel, flick);
            ctx.fade = 1.0F;
            scene.invoke(null, ctx, feet, cam, t);
         }
         int nSprites = pcount.getInt(null);
         double[] q = (double[])pq.get(null);
         pcount.setInt(null, 0);

         if (barrage) {
            // Zoltraak barrage: one volley every 3 ticks at the nearest targets, as ArcanaModes.tick / BarrageFern.volley do
            frac = null;
            fractures.clear();
            magic.n = 0;
            refr.n = 0;
            pcount.setInt(null, 0);
            cam = feet.add(flat.scale(-3.2)).add(side.scale(-1.6)).add(0.0, 2.3, 0.0);
            Vec3 aim = feet.add(flat.scale(12.0)).add(0.0, 1.2, 0.0);
            et = new Vec3[]{cam, aim};
            shot = 99;
            drawCamera.set(null, cam);
            double tt = u;
            for (int vol = (int)Math.floor(Math.max(0.0, tt - 0.6) / 0.15); vol * 0.15 <= tt; vol++) {
               double age = tt - vol * 0.15;
               if (age < 0.0 || age > 0.6) {
                  continue;
               }
               java.util.Random r = new java.util.Random(vol * 7919L);
               Vec3 tg = targets[vol % targets.length].add(0.0, 1.0, 0.0);
               Vec3 look2 = tg.subtract(eye).normalize();
               Vec3 s6 = new Vec3(-look2.z, 0.0, look2.x).normalize();
               Vec3 up7 = s6.cross(look2).normalize();
               if (up7.y < 0.0) {
                  up7 = up7.scale(-1.0);
               }
               double sgn = r.nextDouble() < 0.5 ? 1.0 : -1.0;
               Vec3 launch = eye.add(look2.scale(0.8 + r.nextDouble())).add(s6.scale(sgn * (0.7 + r.nextDouble() * 1.25))).add(up7.scale(-0.3 + r.nextDouble() * 1.2));
               if (oldBarrage) {
                  Vec3 d11 = tg.subtract(launch);
                  double l12 = d11.length();
                  Vec3 d14 = d11.scale(1.0 / l12);
                  Vec3 c15 = launch.add(look2.scale(Math.min(3.0, l12 * 0.3))).add(s6.scale(sgn * 0.5));
                  Vec3 c16 = tg.subtract(d14.scale(Math.min(3.0, l12 * 0.3))).add(up7.scale(0.4 * (r.nextDouble() - 0.5)));
                  Vec3 prevP = launch;
                  for (int k = 1; k <= 9; k++) {
                     double a = k / 9.0, b = 1.0 - a;
                     Vec3 pnt = launch.scale(b * b * b).add(c15.scale(3 * b * b * a)).add(c16.scale(3 * b * a * a)).add(tg.scale(a * a * a));
                     double wob = Math.sin(a * Math.PI * 3.0 + vol) * 0.14 * Math.sin(a * Math.PI);
                     pnt = pnt.add(s6.scale(wob)).add(0.0, wob * 0.6, 0.0);
                     if (age <= SpellFx.beamLife(k == 1 ? 6 : 1)) {
                        if (k == 1) {
                           SpellFx.beam(magic, id, 6, prevP, pnt, age);
                        } else {
                           Vec3 dd = pnt.subtract(prevP).normalize();
                           fern.invoke(null, magic, id, cam, prevP, pnt, dd, age, SpellFx.beamLife(1));
                        }
                     }
                     prevP = pnt;
                  }
               } else if (age <= SpellFx.beamLife(21)) {
                  SpellFx.beam(magic, id, 21, launch, tg, age);
               }
            }
            nSprites = pcount.getInt(null);
            q = (double[])pq.get(null);
            pcount.setInt(null, 0);
         }
         // rain columns around the camera, using the mod's (patched) rain roof
         float[] rain = new float[4 * 900];
         int nRain = 0;
         int cx = (int)Math.floor(cam.x);
         int cz = (int)Math.floor(cam.z);
         for (int dx = barrage ? 13 : -12; dx <= 12; dx++) {
            for (int dz = -12; dz <= 12; dz++) {
               if (dx * dx + dz * dz > 144) {
                  continue;
               }
               int x = cx + dx;
               int z = cz + dz;
               int floor = ArcanaClient.rainHeight(x, z, 64);
               double y0 = Math.max(floor, cam.y - 10.0);
               double y1 = cam.y + 10.0;
               if (y1 > y0 && nRain < 900) {
                  rain[nRain * 4] = (float)(x + 0.5 - cam.x);
                  rain[nRain * 4 + 1] = (float)(z + 0.5 - cam.z);
                  rain[nRain * 4 + 2] = (float)(y0 - cam.y);
                  rain[nRain * 4 + 3] = (float)(y1 - cam.y);
                  nRain++;
               }
            }
         }
         float[] post = CinemaPost.params(released ? u : -1.0, shot, screenH, cam.distanceTo(eye));
         int[] layers = CinemaOverlay.layers(t, released ? u : -1.0);
         int blend = released ? CinemaOverlay.blendMode(u) : 0;
         float blendAmount = released ? CinemaOverlay.blendAmount(u) : 1.0F;
         float headPitch = CinemaDirector.pitch(0.0F, u, released);
         float yaw = (float)Math.toDegrees(Math.atan2(-flat.x, flat.z));

         int bytes = 256 + (magic.n + refr.n) * 16 + nSprites * 4 * 24 + nRain * 16 + 64;
         ByteBuffer bb = ByteBuffer.allocate(bytes).order(ByteOrder.LITTLE_ENDIAN);
         bb.putInt(0x41524341).putInt(idx).putFloat((float)u).putInt(shot);
         bb.putFloat((float)cam.x).putFloat((float)cam.y).putFloat((float)cam.z);
         bb.putFloat((float)et[1].x).putFloat((float)et[1].y).putFloat((float)et[1].z);
         bb.putFloat(post[0]).putFloat(post[1]).putFloat(post[2]);
         for (int l : layers) {
            bb.putInt(l);
         }
         bb.putInt(blend).putFloat(blendAmount);
         bb.putFloat((float)feet.x).putFloat((float)feet.y).putFloat((float)feet.z).putFloat(yaw).putFloat(headPitch);
         bb.putFloat((float)gameSec).putFloat((float)t);
         bb.putInt(magic.n).putInt(refr.n).putInt(nSprites).putInt(nRain);
         bb.putFloat((float)center.x).putFloat((float)center.y).putFloat((float)center.z).putFloat((float)radius);
         bb.putFloat((float)CinemaDirector.fov(shot, 70.0));
         bb.putInt(barrage ? targets.length : 0);
         while (bb.position() < 256) {
            bb.put((byte)0);
         }
         for (Capture c : new Capture[]{magic, refr}) {
            for (int i = 0; i < c.n; i++) {
               bb.putFloat(c.pos[i * 3]).putFloat(c.pos[i * 3 + 1]).putFloat(c.pos[i * 3 + 2]);
               bb.put(c.col[i * 4]).put(c.col[i * 4 + 1]).put(c.col[i * 4 + 2]).put(c.col[i * 4 + 3]);
            }
         }
         for (int i = 0; i < nSprites; i++) {
            int o = i * 20;
            int frame = (int)q[o + 12];
            float u0 = (frame % 16) / 16.0F + 8.0E-4F;
            float v0 = (frame / 16) / 16.0F + 8.0E-4F;
            float u1 = (frame % 16 + 1) / 16.0F - 8.0E-4F;
            float v1 = (frame / 16 + 1) / 16.0F - 8.0E-4F;
            float[][] uv = new float[][]{{u0, v0}, {u1, v0}, {u1, v1}, {u0, v1}};
            for (int k = 0; k < 4; k++) {
               bb.putFloat((float)(q[o + k * 3] - cam.x)).putFloat((float)(q[o + k * 3 + 1] - cam.y)).putFloat((float)(q[o + k * 3 + 2] - cam.z));
               bb.putFloat(uv[k][0]).putFloat(uv[k][1]);
               bb.put((byte)(int)(q[o + 13] * 255.0)).put((byte)(int)(q[o + 14] * 255.0)).put((byte)(int)(q[o + 15] * 255.0)).put((byte)(int)(q[o + 16] * 255.0));
            }
         }
         for (int i = 0; i < nRain * 4; i++) {
            bb.putFloat(rain[i]);
         }
         if (barrage) {
            for (Vec3 tg : targets) {
               bb.putFloat((float)(tg.x - cam.x)).putFloat((float)(tg.y - cam.y)).putFloat((float)(tg.z - cam.z));
            }
         }
         try (DataOutputStream os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out), 1 << 20))) {
            os.write(bb.array(), 0, bb.position());
         } catch (IOException e) {
            System.out.println("err " + idx + " " + e);
            continue;
         }
         System.out.println("ok " + idx + " " + shot + " " + magic.n + " " + refr.n + " " + nSprites);
         System.out.flush();
      }
   }
}
