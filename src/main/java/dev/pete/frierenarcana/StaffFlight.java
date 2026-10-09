package dev.pete.frierenarcana;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Holding any Iron's staff lets the player fly: double-tap jump as in creative, seated on the staff, paid for with mana
 * by the existing flight drain. The Flight spell still works as before; this only removes the need to cast it.
 *
 * <p>Runs before {@link ArcanaEvents#tick} each tick, so putting the staff away ends staff flight quietly instead of
 * with the "needs a staff" error. If flight ends while the staff is still held (mana ran out, or the Flight spell was
 * used to turn it off) it is not switched back on until the staff is put away and taken out again, or mana is back
 * above a third.
 */
@EventBusSubscriber(modid = "frieren_arcana")
public final class StaffFlight {
   private static final String AUTO = "staffFlight";
   private static final String WAIT = "staffFlightWait";

   private StaffFlight() {
   }

   @SubscribeEvent
   public static void tick(PlayerTickEvent.Pre event) {
      if (!(event.getEntity() instanceof ServerPlayer p)) {
         return;
      }
      try {
         update(p);
      } catch (Throwable ignored) {
      }
   }

   private static void update(ServerPlayer p) {
      if (p.isSpectator() || !p.isAlive()) {
         return;
      }
      CompoundTag f = ArcanaEvents.flags(p);
      boolean held = ArcanaModes.hasStaff(p) && !p.isCreative();
      boolean flight = f.getBoolean("flight");
      boolean auto = f.getBoolean(AUTO);
      if (!held) {
         f.putBoolean(WAIT, false);
         if (auto) {
            f.putBoolean(AUTO, false);
            if (flight) {
               f.putBoolean("flight", false);
               ArcanaEvents.updateFlight(p);
            }
         }
         return;
      }
      if (flight) {
         return;
      }
      float mana = MagicData.getPlayerMagicData(p).getMana();
      if (auto) {
         // it was on and something switched it off while the staff stayed in hand
         f.putBoolean(AUTO, false);
         f.putBoolean(WAIT, true);
      }
      if (f.getBoolean(WAIT)) {
         double max = p.getAttributeValue(AttributeRegistry.MAX_MANA);
         if (max <= 0.0 || mana < max / 3.0) {
            return;
         }
         f.putBoolean(WAIT, false);
      }
      double drain = ArcanaConfig.FLIGHT_DRAIN.get();
      if (mana < drain * 2.0) {
         return;
      }
      f.putBoolean("flight", true);
      f.putBoolean(AUTO, true);
      ArcanaEvents.updateFlight(p);
   }
}
