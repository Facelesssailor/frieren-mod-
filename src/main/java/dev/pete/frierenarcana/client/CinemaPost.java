package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import dev.pete.frierenarcana.FrierenArcana;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

/**
 * Full-screen pass over the finished world image during the Barrier Breaker cutscene (the mod's own small post-process
 * stage, nothing from vanilla's post chains): a soft glow around bright light, and a depth-aware blur of the background
 * for the close-ups and the over-the-shoulder shot (ep. 21, 15:12 - 15:16 and 15:20 - 15:26).
 *
 * <p>The world image is copied, the pass draws into a separate target while reading the copy and the world's depth
 * texture (so nothing is read and written at once), and the result is copied back. Any failure switches it off for the
 * rest of the session; the cutscene then simply plays without it.
 */
public final class CinemaPost {
   private static ShaderInstance shader;
   private static TextureTarget scene;
   private static TextureTarget out;
   private static boolean broken;

   private CinemaPost() {
   }

   @EventBusSubscriber(
      modid = "frieren_arcana",
      value = {Dist.CLIENT},
      bus = EventBusSubscriber.Bus.MOD
   )
   public static final class Registration {
      @SubscribeEvent
      public static void shaders(RegisterShadersEvent event) {
         try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), FrierenArcana.id("arcana_post"), DefaultVertexFormat.POSITION), s -> shader = s);
         } catch (IOException | RuntimeException e) {
            broken = true;
         }
      }
   }

   @EventBusSubscriber(
      modid = "frieren_arcana",
      value = {Dist.CLIENT}
   )
   public static final class Hook {
      @SubscribeEvent
      public static void stage(RenderLevelStageEvent event) {
         if (event.getStage() != Stage.AFTER_LEVEL) {
            return;
         }
         if (!ArcanaCinematic.breakerActive()) {
            release();
            return;
         }
         if (broken || shader == null) {
            return;
         }
         float[] p = params();
         if (p[0] <= 0.001F && p[1] <= 0.25F) {
            return;
         }
         try {
            apply(p[0], p[1], p[2]);
         } catch (Throwable t) {
            broken = true;
            release();
         } finally {
            try {
               Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
               RenderSystem.enableDepthTest();
               RenderSystem.depthMask(true);
               RenderSystem.defaultBlendFunc();
            } catch (Throwable ignored) {
            }
         }
      }
   }

   private static double ss(double a, double b, double x) {
      x = Math.max(0.0, Math.min(1.0, (x - a) / (b - a)));
      return x * x * (3.0 - 2.0 * x);
   }

   /** {bloom strength, blur radius in pixels, focus distance in blocks} for this moment of the cutscene. */
   static float[] params() {
      Minecraft mc = Minecraft.getInstance();
      long now = System.nanoTime();
      double u = ArcanaCinematic.release == 0L ? -1.0 : (double)(now - ArcanaCinematic.release) / 1.0E9;
      Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
      Vec3 eye = mc.player == null ? cam : mc.player.getEyePosition();
      return params(u, CinemaDirector.currentShot(), mc.getMainRenderTarget().height, cam.distanceTo(eye));
   }

   /** The same, from plain numbers (also used by the offline preview renderer). */
   static float[] params(double u, int shot, int screenHeight, double subjectDistance) {
      double bloom;
      if (u < 0.0) {
         bloom = 0.45;
      } else {
         // brighter around the hand sequence and the column, and again over the flicker and the shatter
         bloom = 0.55 + 0.35 * (1.0 - ss(3.2, 5.0, u))
            + 0.5 * ss(BreakTimeline.FLICKER - 0.5, BreakTimeline.SHATTER_AT, u) * (1.0 - ss(BreakTimeline.SHATTER_AT + 0.4, BreakTimeline.SHATTER_AT + 2.0, u));
      }
      float blur = 0.0F;
      float focus = 2.0F;
      if (shot == 14 || shot == 15 || shot == 3) {
         blur = (float)(screenHeight * (shot == 3 ? 0.02 : (shot == 14 ? 0.016 : 0.011)));
         focus = (float)Math.max(0.6, subjectDistance);
      }
      return new float[]{(float)bloom, blur, focus};
   }

   private static TextureTarget fit(TextureTarget t, int w, int h) {
      if (t == null) {
         t = new TextureTarget(w, h, false, Minecraft.ON_OSX);
         t.setFilterMode(9729);
      } else if (t.width != w || t.height != h) {
         t.resize(w, h, Minecraft.ON_OSX);
         t.setFilterMode(9729);
      }
      return t;
   }

   private static void apply(float bloom, float blur, float focus) {
      Minecraft mc = Minecraft.getInstance();
      RenderTarget main = mc.getMainRenderTarget();
      int w = main.width;
      int h = main.height;
      scene = fit(scene, w, h);
      out = fit(out, w, h);
      // copy the finished world image
      GlStateManager._glBindFramebuffer(36008, main.frameBufferId);
      GlStateManager._glBindFramebuffer(36009, scene.frameBufferId);
      GlStateManager._glBlitFrameBuffer(0, 0, w, h, 0, 0, w, h, 16384, 9728);
      // draw the pass into our own target, reading the copy and the world's depth
      out.bindWrite(true);
      RenderSystem.disableDepthTest();
      RenderSystem.depthMask(false);
      RenderSystem.disableBlend();
      shader.setSampler("SceneSampler", scene.getColorTextureId());
      shader.setSampler("DepthSampler", main.getDepthTextureId());
      shader.safeGetUniform("ScreenSize").set((float)w, (float)h);
      shader.safeGetUniform("Bloom").set(bloom);
      shader.safeGetUniform("Blur").set(blur);
      shader.safeGetUniform("Focus").set(focus);
      shader.safeGetUniform("Planes").set(0.05F, Math.max(16.0F, mc.gameRenderer.getDepthFar()));
      RenderSystem.setShader(() -> shader);
      BufferBuilder b = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION);
      b.addVertex(-1.0F, -1.0F, 0.0F);
      b.addVertex(1.0F, -1.0F, 0.0F);
      b.addVertex(1.0F, 1.0F, 0.0F);
      b.addVertex(-1.0F, 1.0F, 0.0F);
      BufferUploader.drawWithShader(b.buildOrThrow());
      // and put the result back
      GlStateManager._glBindFramebuffer(36008, out.frameBufferId);
      GlStateManager._glBindFramebuffer(36009, main.frameBufferId);
      GlStateManager._glBlitFrameBuffer(0, 0, w, h, 0, 0, w, h, 16384, 9728);
   }

   static void release() {
      if (scene != null) {
         scene.destroyBuffers();
         scene = null;
      }
      if (out != null) {
         out.destroyBuffers();
         out = null;
      }
   }
}
