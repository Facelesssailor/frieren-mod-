package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent.Post;
import org.joml.Matrix4f;

/**
 * Full-screen layers over the Barrier Breaker cutscene: the fade in and out, the six one-frame flashes right before the
 * shatter (ep. 21, 15:17.78 - 15:18.00: white, colour-inverted, normal, black, over-exposed, dark) and the white-out of the
 * shatter itself. The inverted / over-exposed / dark frames are done with blend modes on the finished picture.
 */
@EventBusSubscriber(
   modid = "frieren_arcana",
   value = {Dist.CLIENT}
)
public final class CinemaOverlay {
   static final int NONE = 0;
   static final int INVERT = 1;
   static final int ADD = 2;
   static final int MULTIPLY = 3;
   private static boolean blendBroken;

   private CinemaOverlay() {
   }

   @SubscribeEvent
   public static void hud(Post event) {
      if (!ArcanaCinematic.breakerActive()) {
         return;
      }
      long now = System.nanoTime();
      double sinceStart = (double)(now - ArcanaCinematic.startedNanos()) / 1.0E9;
      long frac = CinemaDirector.fractureNanos();
      double u = frac == 0L ? -1.0 : (double)(now - frac) / 1.0E9;
      GuiGraphics g = event.getGuiGraphics();
      int w = g.guiWidth();
      int h = g.guiHeight();
      // keep the letterbox bars black: the flashes only cover the picture between them
      int top = Math.max(12, h / 13);
      int bottom = h - Math.max(16, h / 13);
      int mode = blendMode(u);
      if (mode != NONE && !blendBroken) {
         blendFill(g, w, top, bottom, mode, blendAmount(u));
      }
      int[] layers = layers(sinceStart, u);
      if (layers[2] >>> 24 > 1) {
         g.fill(0, top, w, bottom, layers[2]);
      }
      if (layers[3] >>> 24 > 1) {
         g.fill(0, 0, w, h, layers[3]);
      }
   }

   private static double ss(double a, double b, double x) {
      x = Math.max(0.0, Math.min(1.0, (x - a) / (b - a)));
      return x * x * (3.0 - 2.0 * x);
   }

   /** Which blend-mode frame (if any) is on screen at {@code u} seconds after release. */
   static int blendMode(double u) {
      switch (BreakTimeline.flicker(u)) {
         case 2:
            return INVERT;
         case 5:
            return ADD;
         case 6:
            return MULTIPLY;
         default:
            return NONE;
      }
   }

   static float blendAmount(double u) {
      switch (BreakTimeline.flicker(u)) {
         case 5:
            return 0.38F;
         case 6:
            return 0.62F;
         default:
            return 1.0F;
      }
   }

   /** Plain colour layers, drawn after any blend-mode frame: {lime (unused), cyan (unused), white, black}. */
   static int[] layers(double sinceStart, double u) {
      double black = Math.max(0.0, 1.0 - sinceStart / 0.7);
      double white = 0.0;
      if (u >= 0.0) {
         int f = BreakTimeline.flicker(u);
         if (f == 1) {
            white = 1.0;
         } else if (f == 4) {
            black = 1.0;
         } else if (f == 2 && blendBroken) {
            white = 0.85;
         }
         double x = u - BreakTimeline.SHATTER_AT;
         if (x > 0.0) {
            // 15:18.03: the shards appear through a white-out that clears over about a second
            white = Math.max(white, 0.88 * ss(0.0, 0.04, x) * (0.45 + 0.55 * (1.0 - ss(0.04, 0.25, x))) * (1.0 - ss(0.35, 1.2, x)));
         }
         black = Math.max(black, (u - (BreakTimeline.END - 0.7)) / 0.7);
      }
      return new int[]{0, 0, argb(white, 16514559), argb(Math.min(1.0, black), 0)};
   }

   private static int argb(double a, int rgb) {
      return (int)Math.round(Math.max(0.0, Math.min(1.0, a)) * 255.0) << 24 | rgb;
   }

   private static void blendFill(GuiGraphics g, int w, int y0, int y1, int mode, float amount) {
      try {
         g.flush();
         RenderSystem.disableDepthTest();
         RenderSystem.enableBlend();
         if (mode == INVERT) {
            RenderSystem.blendFuncSeparate(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO, SourceFactor.ZERO, DestFactor.ONE);
         } else if (mode == ADD) {
            RenderSystem.blendFuncSeparate(SourceFactor.ONE, DestFactor.ONE, SourceFactor.ZERO, DestFactor.ONE);
         } else {
            RenderSystem.blendFuncSeparate(SourceFactor.DST_COLOR, DestFactor.ZERO, SourceFactor.ZERO, DestFactor.ONE);
         }
         float c = mode == INVERT ? 1.0F : amount;
         RenderSystem.setShader(GameRenderer::getPositionColorShader);
         Matrix4f m = g.pose().last().pose();
         BufferBuilder b = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
         b.addVertex(m, 0.0F, (float)y0, 0.0F).setColor(c, c, c, 1.0F);
         b.addVertex(m, 0.0F, (float)y1, 0.0F).setColor(c, c, c, 1.0F);
         b.addVertex(m, (float)w, (float)y1, 0.0F).setColor(c, c, c, 1.0F);
         b.addVertex(m, (float)w, (float)y0, 0.0F).setColor(c, c, c, 1.0F);
         BufferUploader.drawWithShader(b.buildOrThrow());
      } catch (Throwable t) {
         blendBroken = true;
      } finally {
         try {
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
         } catch (Throwable ignored) {
         }
      }
   }
}
