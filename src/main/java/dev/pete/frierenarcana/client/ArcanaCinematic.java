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

    public static void charge(UUID caster, boolean charging, boolean piercing) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null && caster.equals(mc.player.getUUID())) {
            if (!charging) {
                if (active()) {
                    release = System.nanoTime();
                }
            } else {
                if (active()) {
                    restore();
                }

                breaker = piercing;
                started = System.nanoTime();
                release = 0L;
                originalCamera = mc.getCameraEntity();
                originalType = mc.options.getCameraType();
                Vec3 from = mc.gameRenderer.getMainCamera().getPosition();
                camera = new ArmorStand(mc.level, from.x, from.y, from.z);
                camera.setInvisible(true);
                camera.setNoGravity(true);
                previous = from;
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                mc.setCameraEntity(camera);
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
            Minecraft mc = Minecraft.getInstance();
            if (mc.getCameraEntity() == camera) {
                mc.setCameraEntity((Entity)(originalCamera == null ? mc.player : originalCamera));
            }

            if (originalType != null) {
                mc.options.setCameraType(originalType);
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
    public static void input(MovementInputUpdateEvent event) {
        if (active()) {
            Input input = event.getInput();
            input.forwardImpulse = 0.0F;
            input.leftImpulse = 0.0F;
            input.jumping = false;
            input.shiftKeyDown = false;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
        }
    }

    @SubscribeEvent
    public static void opening(Opening event) {
        if (active() && event.getNewScreen() instanceof PauseScreen) {
            skip();
            event.setCanceled(true);
        } else if (active() && event.getNewScreen() != null) {
            restore();
        }
    }

    @SubscribeEvent
    public static void hand(RenderHandEvent event) {
        if (active()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void layer(Pre event) {
        if (active()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void interaction(InteractionKeyMappingTriggered event) {
        if (active()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    @SubscribeEvent
    public static void logout(LoggingOut event) {
        restore();
    }

    @SubscribeEvent
    public static void hud(net.neoforged.neoforge.client.event.RenderGuiEvent.Post event) {
        if (active()) {
            GuiGraphics graphics = event.getGuiGraphics();
            int h = graphics.guiHeight();
            int w = graphics.guiWidth();
            graphics.fill(0, 0, w, Math.max(12, h / 13), -553186539);
            graphics.fill(0, h - Math.max(16, h / 13), w, h, -553186539);
            graphics.drawCenteredString(
                Minecraft.getInstance().font,
                Component.translatable("cinematic.frieren_arcana.skip", ArcanaKeys.SKIP.getTranslatedKeyMessage()),
                w / 2,
                h - 13,
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
