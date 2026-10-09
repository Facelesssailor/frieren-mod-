package dev.pete.frierenarcana.client;

import dev.pete.frierenarcana.FrierenArcana;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;

/**
 * The cutscene's sound, cut from ep. 21 itself and played in step with the pictures. The release part is split at the
 * episode's own cuts (15:08, 15:16, 15:18.03 shatter, 15:20) so that each section starts exactly on its beat even if the
 * game hitched in between. Played to the caster only, without position, like a soundtrack.
 */
@EventBusSubscriber(
   modid = "frieren_arcana",
   value = {Dist.CLIENT}
)
public final class BreakerAudio {
   private static final String[] CUES = new String[]{"release", "rise", "flicker", "shatter", "after"};
   private static final double[] AT = new double[]{0.0, 5.0, 13.0, BreakTimeline.SHATTER_AT, BreakTimeline.SHOULDER};
   private static final float VOLUME = 0.9F;
   private static final List<SoundInstance> PLAYING = new ArrayList<>();
   private static long session = -1L;
   private static long releaseSeen;
   private static int next;
   private static SoundInstance charge;
   private static boolean broken;
   private static int available = -1;

   private BreakerAudio() {
   }

   static ResourceLocation id(String cue) {
      return FrierenArcana.id("breaker." + cue);
   }

   /** True when the episode audio is installed and playing, so the plain glass-break fallback can stay quiet. */
   static boolean active() {
      return !broken && available == 1 && session != -1L;
   }

   @SubscribeEvent
   public static void frame(RenderFrameEvent.Pre event) {
      if (broken) {
         return;
      }
      try {
         update();
      } catch (Throwable t) {
         broken = true;
         stopAll();
      }
   }

   private static void update() {
      Minecraft mc = Minecraft.getInstance();
      if (!ArcanaCinematic.breakerActive()) {
         if (session != -1L) {
            stopAll();
            session = -1L;
         }
         return;
      }
      if (available < 0) {
         available = mc.getSoundManager().getSoundEvent(id("release")) != null ? 1 : 0;
      }
      if (available == 0) {
         return;
      }
      long now = System.nanoTime();
      if (session != ArcanaCinematic.started) {
         stopAll();
         session = ArcanaCinematic.started;
         next = 0;
         releaseSeen = 0L;
         charge = play("charge");
      }
      if (ArcanaCinematic.release != 0L && releaseSeen == 0L) {
         releaseSeen = ArcanaCinematic.release;
         if (charge != null) {
            mc.getSoundManager().stop(charge);
            charge = null;
         }
      }
      if (releaseSeen == 0L) {
         return;
      }
      long fracture = CinemaDirector.fractureNanos();
      while (next < CUES.length) {
         // the hand sequence follows the release; everything on the dome follows the fracture clock like the pictures do
         long origin = next == 0 || fracture == 0L ? releaseSeen : fracture;
         double u = (double)(now - origin) / 1.0E9;
         if (u < AT[next]) {
            break;
         }
         boolean stale = next + 1 < AT.length && u > AT[next + 1];
         if (!stale) {
            play(CUES[next]);
         }
         next++;
      }
   }

   private static SoundInstance play(String cue) {
      SimpleSoundInstance s = new SimpleSoundInstance(id(cue), SoundSource.MASTER, VOLUME, 1.0F, RandomSource.create(), false, 0, Attenuation.NONE, 0.0, 0.0, 0.0, true);
      Minecraft.getInstance().getSoundManager().play(s);
      PLAYING.add(s);
      return s;
   }

   private static void stopAll() {
      try {
         Minecraft mc = Minecraft.getInstance();
         for (SoundInstance s : PLAYING) {
            mc.getSoundManager().stop(s);
         }
      } catch (Throwable ignored) {
      }
      PLAYING.clear();
      charge = null;
   }
}
