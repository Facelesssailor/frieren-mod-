package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.platform.InputConstants.Type;
import dev.pete.frierenarcana.ArcanaNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class ArcanaKeys {
    private static final String CATEGORY = "key.categories.frieren_arcana";
    public static final KeyMapping CYCLE = new KeyMapping(
        "key.frieren_arcana.cycle", KeyConflictContext.IN_GAME, Type.KEYSYM, 78, "key.categories.frieren_arcana"
    );
    public static final KeyMapping CAST = new KeyMapping(
        "key.frieren_arcana.cast", KeyConflictContext.IN_GAME, Type.KEYSYM, 89, "key.categories.frieren_arcana"
    );
    public static final KeyMapping STOP = new KeyMapping(
        "key.frieren_arcana.stop", KeyConflictContext.IN_GAME, Type.KEYSYM, 66, "key.categories.frieren_arcana"
    );
    public static final KeyMapping SKIP = new KeyMapping(
        "key.frieren_arcana.skip_cinematic", KeyConflictContext.IN_GAME, Type.KEYSYM, 75, "key.categories.frieren_arcana"
    );
    public static final KeyMapping THROW = new KeyMapping(
        "key.frieren_arcana.throw_lift", KeyConflictContext.IN_GAME, Type.KEYSYM, 85, "key.categories.frieren_arcana"
    );
    private static int mode;

    @SubscribeEvent
    public static void tick(Post var0) {
        Minecraft var1 = Minecraft.getInstance();
        if (var1.player != null && var1.screen == null) {
            while (THROW.consumeClick()) {
                PacketDistributor.sendToServer(new ArcanaNetwork.ModeRequest(4));
            }

            while (CYCLE.consumeClick()) {
                mode = (mode + 1) % 3;
                PacketDistributor.sendToServer(new ArcanaNetwork.ModeRequest(3));
                var1.player
                    .displayClientMessage(Component.translatable("message.frieren_arcana.mode", Component.translatable("mode.frieren_arcana." + mode)), true);
            }

            while (CAST.consumeClick()) {
                PacketDistributor.sendToServer(new ArcanaNetwork.ModeRequest(mode));
            }

            while (STOP.consumeClick()) {
                PacketDistributor.sendToServer(new ArcanaNetwork.ModeRequest(3));
            }
        }
    }

    @EventBusSubscriber(
        modid = "frieren_arcana",
        value = {Dist.CLIENT},
        bus = Bus.MOD
    )
    public static final class Registration {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent e) {
            e.register(ArcanaKeys.CYCLE);
            e.register(ArcanaKeys.CAST);
            e.register(ArcanaKeys.STOP);
            e.register(ArcanaKeys.SKIP);
            e.register(ArcanaKeys.THROW);
        }
    }
}
