package dev.facelesssailor.siebenkreis.client;

import dev.facelesssailor.siebenkreis.SiebenkreisMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = SiebenkreisMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SiebenkreisClient {
    private SiebenkreisClient() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(SiebenkreisMod.CIRCLE.get(), SiebenkreisRenderer::new);
    }
}
