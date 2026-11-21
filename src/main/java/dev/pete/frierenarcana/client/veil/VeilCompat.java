package dev.pete.frierenarcana.client.veil;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.Map;
import net.neoforged.fml.ModList;

/**
 * The mod's optional use of Veil (FoundryMC's rendering library). With Veil installed, everything the mod draws as glow
 * (the Barrier Breaker's hand sequence, needle and column, spell circles, sigils, crystals, Zoltraak) is drawn a second
 * time into Veil's bloom buffer, which Veil blurs and lays back over the picture, and the spells give off real coloured
 * light. Without Veil this class touches nothing of it: the Veil-facing classes ({@link VeilBloom}, {@link VeilLights})
 * are only loaded once Veil is known to be present.
 *
 * <p>Bloom stays off under a shader-pack mod (Veil switches its own bloom off there) and whenever Veil's bloom pipeline is
 * not running. Any failure switches the whole integration off for the session.
 */
public final class VeilCompat {
    private static Boolean available;
    private static boolean broken;
    private static Field bloomEnabled;

    private VeilCompat() {
    }

    /** Veil is installed, no shader-pack mod is, and nothing has failed. Decided once. */
    public static boolean available() {
        if (available == null) {
            ModList mods = ModList.get();
            available = mods != null && mods.isLoaded("veil") && !mods.isLoaded("iris") && !mods.isLoaded("oculus");
            if (available) {
                try {
                    Class<?> c = Class.forName("foundry.veil.impl.client.render.pipeline.VeilBloomRenderer");
                    bloomEnabled = c.getDeclaredField("enabled");
                    bloomEnabled.setAccessible(true);
                } catch (Throwable t) {
                    bloomEnabled = null;
                }
                LogUtils.getLogger().info("Frieren Arcana: Veil found, using its bloom and lights");
            }
        }
        return available && !broken;
    }

    /** Veil's bloom pipeline is running this frame. */
    public static boolean bloomActive() {
        if (!available()) {
            return false;
        }
        try {
            return bloomEnabled == null || bloomEnabled.getBoolean(null);
        } catch (Throwable t) {
            return false;
        }
    }

    /** {@code main}, or a consumer that also copies every glow vertex into the bloom buffer. */
    public static VertexConsumer tee(VertexConsumer main) {
        if (!bloomActive()) {
            return main;
        }
        try {
            return VeilBloom.tee(main);
        } catch (Throwable t) {
            fail(t);
            return main;
        }
    }

    /** Draw what {@link #tee} collected into Veil's bloom buffer. Call right after the matching batch was drawn. */
    public static void flushBloom() {
        if (!available()) {
            return;
        }
        try {
            VeilBloom.flush();
        } catch (Throwable t) {
            fail(t);
        }
    }

    /** Make Veil's point lights match {@code wanted} (keyed so a light keeps its handle from frame to frame). */
    public static void lights(Map<String, Glow> wanted) {
        if (!available()) {
            return;
        }
        try {
            VeilLights.sync(wanted);
        } catch (Throwable t) {
            fail(t);
        }
    }

    public static void clear() {
        if (available != null && available) {
            try {
                VeilLights.clear();
                VeilBloom.discard();
            } catch (Throwable ignored) {
            }
        }
    }

    public static void fail(Throwable t) {
        if (!broken) {
            broken = true;
            LogUtils.getLogger().warn("Frieren Arcana: Veil integration failed, switching it off ({})", t.toString());
            try {
                VeilLights.clear();
            } catch (Throwable ignored) {
            }
        }
    }

    /** A point light: position, reach in blocks, colour 0-1 and brightness. */
    public record Glow(double x, double y, double z, float radius, float r, float g, float b, float brightness) {
    }
}
