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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
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
    private static ArmorStand camera;
    private static Entity originalCamera;
    private static CameraType originalType;
    private static long started;
    private static long release;
    private static boolean breaker;
    private static Vec3 previous = Vec3.ZERO;

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
    public static void tick(Post event) {
        Minecraft mc = Minecraft.getInstance();

        while (ArcanaKeys.SKIP.consumeClick()) {
            skip();
        }

        if (active()) {
            if (mc.player != null && mc.level == camera.level() && mc.player.isAlive() && mc.screen == null) {
                double age = (double)(System.nanoTime() - started) / 1.0E9;
                double after = release == 0L ? 0.0 : (double)(System.nanoTime() - release) / 1.0E9;
                if (!(age > 14.0) && !(after > 3.6)) {
                    Vec3 focus = mc.player.position().add(0.0, 1.3, 0.0);
                    Vec3 look = mc.player.getLookAngle().normalize();
                    Vec3 horizontal = new Vec3(look.x, 0.0, look.z).normalize();
                    if (horizontal.lengthSqr() < 0.1) {
                        horizontal = new Vec3(0.0, 0.0, 1.0);
                    }

                    Vec3 side = new Vec3(-horizontal.z, 0.0, horizontal.x);
                    double progress = Math.min(1.0, age / (double)(breaker ? 5 : 8));
                    double orbit = Math.sin(progress * Math.PI * 0.65) * 0.8;
                    Vec3 desired = focus.subtract(horizontal.scale(release == 0L ? 3.6 - progress * 1.5 : 6.0))
                        .add(side.scale(1.7 + orbit))
                        .add(0.0, release == 0L ? -0.25 + progress * 0.75 : 0.7, 0.0);
                    BlockHitResult hit = mc.level.clip(new ClipContext(focus, desired, Block.VISUAL, Fluid.NONE, mc.player));
                    if (hit.getType() != Type.MISS) {
                        desired = hit.getLocation().lerp(focus, 0.12);
                    }

                    Vec3 position = previous.lerp(desired, 0.24);
                    previous = position;
                    camera.xo = camera.getX();
                    camera.yo = camera.getY();
                    camera.zo = camera.getZ();
                    camera.yRotO = camera.getYRot();
                    camera.xRotO = camera.getXRot();
                    camera.yHeadRotO = camera.getYHeadRot();
                    camera.setPos(position.x, position.y - (double)camera.getEyeHeight(), position.z);
                    Vec3 target = focus.add(look.scale(release == 0L ? 0.2 : 3.0));
                    Vec3 delta = target.subtract(position);
                    camera.setYRot((float)(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0));
                    camera.setXRot((float)(-Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance()))));
                    camera.setYHeadRot(camera.getYRot());
                    camera.setYBodyRot(camera.getYRot());
                } else {
                    restore();
                }
            } else {
                restore();
            }
        }
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
