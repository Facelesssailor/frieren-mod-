package dev.pete.frierenarcana.client.veil;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Veil point lights for the mod's glows. Only loaded with Veil. */
final class VeilLights {
    private static final Map<String, LightRenderHandle<PointLightData>> LIVE = new HashMap<>();

    private VeilLights() {
    }

    static void sync(Map<String, VeilCompat.Glow> wanted) {
        for (Iterator<Map.Entry<String, LightRenderHandle<PointLightData>>> it = LIVE.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, LightRenderHandle<PointLightData>> e = it.next();
            if (!wanted.containsKey(e.getKey()) || !e.getValue().isValid()) {
                if (e.getValue().isValid()) {
                    e.getValue().free();
                }
                it.remove();
            }
        }
        for (Map.Entry<String, VeilCompat.Glow> e : wanted.entrySet()) {
            VeilCompat.Glow g = e.getValue();
            LightRenderHandle<PointLightData> h = LIVE.get(e.getKey());
            if (h == null) {
                h = VeilRenderSystem.renderer().getLightRenderer().addLight(new PointLightData());
                LIVE.put(e.getKey(), h);
            }
            h.getLightData().setPosition(g.x(), g.y(), g.z()).setRadius(g.radius()).setColor(g.r(), g.g(), g.b()).setBrightness(g.brightness());
            h.markDirty();
        }
    }

    static void clear() {
        for (LightRenderHandle<PointLightData> h : LIVE.values()) {
            if (h.isValid()) {
                h.free();
            }
        }
        LIVE.clear();
    }
}
