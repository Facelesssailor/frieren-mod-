package dev.pete.frierenarcana.client;

import java.util.UUID;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.player.Input;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre;
import net.neoforged.neoforge.client.event.ScreenEvent.Opening;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class ArcanaCinematic {
    public static ArmorStand camera;
    public static Entity originalCamera;
    public static CameraType originalType;
    public static long started;
    public static long release;
    public static boolean breaker;
    public static Vec3 previous = Vec3.ZERO;

    public static boolean active() {
        return camera != null;
    }

    public static void charge(UUID var0, boolean var1, boolean var2) {
        Minecraft var3 = Minecraft.getInstance();
        if (var3.player != null && var3.level != null && var0.equals(var3.player.getUUID())) {
            if (!var1) {
                if (active()) {
                    release = System.nanoTime();
                }
            } else {
                if (active()) {
                    restore();
                }

                breaker = var2;
                started = System.nanoTime();
                release = 0L;
                originalCamera = var3.getCameraEntity();
                originalType = var3.options.getCameraType();
                Vec3 var4 = var3.gameRenderer.getMainCamera().getPosition();
                camera = new ArmorStand(var3.level, var4.x, var4.y, var4.z);
                camera.setInvisible(true);
                camera.setNoGravity(true);
                previous = var4;
                var3.options.setCameraType(CameraType.FIRST_PERSON);
                var3.setCameraEntity(camera);
            }
        }
    }

    public static void skip() {
        if (active()) {
            restore();
        }
    }

    public static void restore() {
        if (camera != null) {
            Minecraft var0 = Minecraft.getInstance();
            if (var0.getCameraEntity() == camera) {
                var0.setCameraEntity((Entity)(originalCamera == null ? var0.player : originalCamera));
            }

            if (originalType != null) {
                var0.options.setCameraType(originalType);
            }

            camera.discard();
            camera = null;
            originalCamera = null;
            originalType = null;
        }
    }

    @SubscribeEvent
    public static void tick(Post var0) {
        CinemaDirector.tick(var0);
    }

    @SubscribeEvent
    public static void input(MovementInputUpdateEvent var0) {
        if (active()) {
            Input var1 = var0.getInput();
            var1.forwardImpulse = 0.0F;
            var1.leftImpulse = 0.0F;
            var1.jumping = false;
            var1.shiftKeyDown = false;
            var1.up = false;
            var1.down = false;
            var1.left = false;
            var1.right = false;
        }
    }

    @SubscribeEvent
    public static void opening(Opening var0) {
        if (active() && var0.getNewScreen() instanceof PauseScreen) {
            skip();
            var0.setCanceled(true);
        } else if (active() && var0.getNewScreen() != null) {
            restore();
        }
    }

    @SubscribeEvent
    public static void hand(RenderHandEvent var0) {
        if (active()) {
            var0.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void layer(Pre var0) {
        if (active()) {
            var0.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void interaction(InteractionKeyMappingTriggered var0) {
        if (active()) {
            var0.setCanceled(true);
            var0.setSwingHand(false);
        }
    }

    @SubscribeEvent
    public static void logout(LoggingOut var0) {
        restore();
    }

    @SubscribeEvent
    public static void hud(net.neoforged.neoforge.client.event.RenderGuiEvent.Post var0) {
        if (active()) {
            GuiGraphics var1 = var0.getGuiGraphics();
            int var2 = var1.guiHeight();
            int var3 = var1.guiWidth();
            var1.fill(0, 0, var3, Math.max(12, var2 / 13), -553186539);
            var1.fill(0, var2 - Math.max(16, var2 / 13), var3, var2, -553186539);
            var1.drawCenteredString(
                Minecraft.getInstance().font,
                Component.translatable("cinematic.frieren_arcana.skip", ArcanaKeys.SKIP.getTranslatedKeyMessage()),
                var3 / 2,
                var2 - 13,
                14149631
            );
        }
    }

    public static boolean breakerActive() {
        return active() ? breaker : false;
    }

    public static long startedNanos() {
        return started;
    }
}
