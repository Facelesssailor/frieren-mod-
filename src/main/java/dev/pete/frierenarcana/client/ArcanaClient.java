package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.pete.frierenarcana.ArcanaNetwork;
import dev.pete.frierenarcana.BarrierGeometry;
import dev.pete.frierenarcana.BarrierSlide;
import dev.pete.frierenarcana.FrierenArcana;
import dev.pete.frierenarcana.ShipSpace;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.joml.Matrix4f;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class ArcanaClient {
    private static final List<ArcanaClient.VisualField> FIELDS = new ArrayList<>();
    private static final Map<UUID, ArcanaClient.Mana> MANA = new HashMap<>();
    private static final Map<UUID, Long> CHARGES = new HashMap<>();
    private static final Set<UUID> BREAKER_CHARGES = new HashSet<>();
    private static final List<ArcanaClient.Beam> BEAMS = new ArrayList<>();
    private static final Map<UUID, Long> FLIGHTS = new HashMap<>();
    private static final List<ArcanaClient.Effect> EFFECTS = new ArrayList<>();
    private static final List<ArcanaClient.Fracture> FRACTURES = new ArrayList<>();
    private static boolean sight;
    private static Vec3 drawCamera = Vec3.ZERO;

    private static ArcanaClient.VisualField readField(CompoundTag tag) {
        return new ArcanaClient.VisualField(tag.getUUID("id"), ArcanaNetwork.vector(tag, "center"), tag.getInt("radius"), tag.getBoolean("defensive"));
    }

    public static void receive(CompoundTag tag) {
        if (!CastCircles.receive(tag)) {
            String var1 = tag.getString("kind");
            switch (var1) {
                case "fields":
                    FIELDS.clear();

                    for (Tag raw : tag.getList("fields", 10)) {
                        FIELDS.add(readField((CompoundTag)raw));
                    }
                    break;
                case "sight":
                    sight = tag.getBoolean("active");
                    MANA.clear();

                    for (Tag raw : tag.getList("players", 10)) {
                        CompoundTag t = (CompoundTag)raw;
                        MANA.put(t.getUUID("player"), new ArcanaClient.Mana(t.getFloat("mana"), t.getFloat("capacity")));
                    }
                    break;
                case "beam":
                    BEAMS.add(
                        new ArcanaClient.Beam(ArcanaNetwork.vector(tag, "start"), ArcanaNetwork.vector(tag, "end"), tag.getInt("style"), System.nanoTime())
                    );
                    break;
                case "shatter":
                    FRACTURES.add(new ArcanaClient.Fracture(readField(tag.getCompound("field")), ArcanaNetwork.vector(tag, "impact"), System.nanoTime()));
                    break;
                case "flight":
                    UUID playerx = tag.getUUID("player");
                    if (tag.getBoolean("active")) {
                        FLIGHTS.put(playerx, System.nanoTime());
                    } else {
                        FLIGHTS.remove(playerx);
                    }
                    break;
                case "effect":
                    EFFECTS.add(new ArcanaClient.Effect(ArcanaNetwork.vector(tag, "center"), tag.getInt("style"), tag.getInt("strength"), System.nanoTime()));
                    break;
                case "charge":
                    UUID player = tag.getUUID("player");
                    ArcanaCinematic.charge(player, tag.getBoolean("active"), tag.getBoolean("breaker"));
                    if (tag.getBoolean("active")) {
                        CHARGES.put(player, System.nanoTime());
                        if (tag.getBoolean("breaker")) {
                            BREAKER_CHARGES.add(player);
                        } else {
                            BREAKER_CHARGES.remove(player);
                        }
                    } else {
                        CHARGES.remove(player);
                        BREAKER_CHARGES.remove(player);
                    }
            }
        }
    }

    public static boolean rainBlocked(Vec3 position) {
        return FIELDS.stream().anyMatch(f -> !f.defensive && f.center.distanceToSqr(position) < (double)(f.radius * f.radius));
    }

    public static int rainHeight(int x, int z, int terrainHeight) {
        int result = terrainHeight;

        for (ArcanaClient.VisualField f : FIELDS) {
            if (!f.defensive) {
                double dx = (double)x + 0.5 - f.center.x;
                double dz = (double)z + 0.5 - f.center.z;
                double horizontal = dx * dx + dz * dz;
                if (horizontal < (double)(f.radius * f.radius)) {
                    result = Math.max(result, (int)Math.ceil(f.center.y + Math.sqrt((double)(f.radius * f.radius) - horizontal)));
                }
            }
        }

        return result;
    }

    public static double boundaryHit(Vec3 start, Vec3 end, boolean attack) {
        Vec3 d = end.subtract(start);
        double nearest = Double.POSITIVE_INFINITY;

        for (ArcanaClient.VisualField f : FIELDS) {
            if (!f.defensive || attack && !(f.center.distanceToSqr(start) < (double)(f.radius * f.radius))) {
                Vec3 r = start.subtract(f.center);
                nearest = Math.min(nearest, BarrierGeometry.firstHit(r.x, r.y, r.z, d.x, d.y, d.z, (double)f.radius));
            }
        }

        return nearest;
    }

    public static Vec3 movement(Entity var0, Vec3 var1) {
        return BarrierSlide.client(var0, var1);
    }

    @SubscribeEvent
    public static void logout(LoggingOut event) {
        FIELDS.clear();
        MANA.clear();
        CHARGES.clear();
        BREAKER_CHARGES.clear();
        BEAMS.clear();
        FRACTURES.clear();
        FLIGHTS.clear();
        EFFECTS.clear();
        sight = false;
        ArcanaCinematic.restore();
        ArcanaRefraction.release();
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                Vec3 camera = event.getCamera().getPosition();
                drawCamera = camera;
                PoseStack pose = event.getPoseStack();
                pose.pushPose();
                Matrix4f matrix = pose.last().pose();
                BufferSource buffers = mc.renderBuffers().bufferSource();
                VertexConsumer fill = buffers.getBuffer(ArcanaRenderTypes.MAGIC);
                double time = (double)((float)mc.level.getGameTime() + event.getPartialTick().getGameTimeDeltaPartialTick(false)) / 20.0;
                ArcanaShaders.prepare(time, camera);
                if (!FRACTURES.isEmpty()) {
                    ArcanaRefraction.capture(time);
                } else {
                    ArcanaRefraction.release();
                }

                for (ArcanaClient.VisualField f : FIELDS) {
                    if (f.center.distanceToSqr(camera) < Math.pow((double)(f.radius + 180), 2.0)) {
                        sphere(fill, matrix, f, time, 0.0);
                    }
                }

                long now = System.nanoTime();
                FRACTURES.removeIf(fx -> (double)(now - fx.startNanos) / 1.0E9 > 3.2);

                for (ArcanaClient.Fracture fx : FRACTURES) {
                    fracture(fill, matrix, fx, time, (double)(now - fx.startNanos) / 1.0E9);
                }

                BEAMS.removeIf(b -> (double)(now - b.startNanos) / 1.0E9 > (b.style == 3 ? 1.2 : (b.style == 2 ? 1.4 : 0.35)));

                for (ArcanaClient.Beam b : BEAMS) {
                    beam(fill, matrix, b, (double)(now - b.startNanos) / 1.0E9);
                }

                if (sight) {
                    for (Player p : mc.level.players()) {
                        ArcanaClient.Mana mana = MANA.get(p.getUUID());
                        if (mana != null) {
                            Vec3 center = ShipSpace.world(mc.level, p.getPosition(event.getPartialTick().getGameTimeDeltaPartialTick(false)));
                            double height = Math.max(0.15, Math.min(7.0, 0.4 + Math.sqrt((double)Math.max(0.0F, mana.current) / 100.0) * 1.7));

                            for (int j = 0; j < 4; j++) {
                                spiral(fill, matrix, center, height, 0.45 + (double)j * 0.1, time + (double)j * Math.PI / 2.0, 0.2F);
                            }
                        }
                    }
                }

                CHARGES.entrySet().removeIf(e -> (double)(now - e.getValue()) / 1.0E9 > 12.0);

                for (Player px : mc.level.players()) {
                    if (CHARGES.containsKey(px.getUUID())) {
                        Vec3 center = ShipSpace.world(mc.level, px.getPosition(event.getPartialTick().getGameTimeDeltaPartialTick(false))).add(0.0, 1.0, 0.0);
                        boolean breaker = BREAKER_CHARGES.contains(px.getUUID());
                        Vec3 direction = px.getLookAngle();
                        double charge = Math.min(1.0, (double)(now - CHARGES.get(px.getUUID())) / 1.0E9 / (double)(breaker ? 5 : 8));
                        sigil(
                            fill,
                            matrix,
                            center.add(direction.scale(0.8)),
                            direction,
                            0.7 + charge * 0.6,
                            time,
                            breaker ? 0.05F : 0.72F,
                            breaker ? 0.8F : 0.91F,
                            breaker ? 0.38F : 1.0F,
                            0.8F
                        );

                        for (int j = 0; j < 12; j++) {
                            double a = (double)j * 2.39996 + time;
                            Vec3 mote = center.add(Math.cos(a) * (1.0 - charge * 0.5), Math.sin(a * 0.7) * 0.85, Math.sin(a) * (1.0 - charge * 0.5));
                            crystal(
                                fill,
                                matrix,
                                mote,
                                new Vec3(0.0, 1.0, 0.0),
                                0.025,
                                0.09,
                                breaker ? 0.1F : 0.8F,
                                breaker ? 0.85F : 0.96F,
                                breaker ? 0.4F : 1.0F,
                                0.6F
                            );
                        }
                    }
                }

                FLIGHTS.entrySet().removeIf(e -> (double)(now - e.getValue()) / 1.0E9 > 1.5);
                EFFECTS.removeIf(
                    e -> (double)(now - e.startNanos) / 1.0E9
                            > (e.style == 11 ? 15.0 : (e.style == 9 ? 10.0 : (e.style == 8 ? 6.0 : (e.style != 14 && e.style != 21 ? 1.3 : 4.0))))
                );

                for (ArcanaClient.Effect e : EFFECTS) {
                    effect(fill, matrix, e, (double)(now - e.startNanos) / 1.0E9, time);
                }

                buffers.endBatch(ArcanaRenderTypes.MAGIC);
                if (!FRACTURES.isEmpty()) {
                    VertexConsumer glass = buffers.getBuffer(ArcanaRenderTypes.REFRACTION);

                    for (ArcanaClient.Fracture fx : FRACTURES) {
                        prisms(glass, matrix, fx, (double)(now - fx.startNanos) / 1.0E9);
                    }

                    buffers.endBatch(ArcanaRenderTypes.REFRACTION);
                }

                pose.popPose();

                for (Player pxx : mc.level.players()) {
                    if (FLIGHTS.containsKey(pxx.getUUID())) {
                        Vec3 c = ShipSpace.world(mc.level, pxx.getPosition(event.getPartialTick().getGameTimeDeltaPartialTick(false))).subtract(camera);
                        pose.pushPose();
                        pose.translate(c.x, c.y - 0.22 + Math.sin(time * 3.0) * 0.025, c.z);
                        pose.mulPose(Axis.YP.rotationDegrees(-pxx.getYRot()));
                        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                        mc.getItemRenderer()
                            .renderStatic(
                                FrierenArcana.STAFF.get().getDefaultInstance(),
                                ItemDisplayContext.NONE,
                                LevelRenderer.getLightColor(mc.level, pxx.blockPosition()),
                                OverlayTexture.NO_OVERLAY,
                                pose,
                                buffers,
                                mc.level,
                                pxx.getId()
                            );
                        pose.popPose();
                    }
                }

                buffers.endBatch();
            }
        }
    }

    private static void sphere(VertexConsumer out, Matrix4f m, ArcanaClient.VisualField field, double time, double fracture) {
        if (field.defensive) {
            defensiveShell(out, m, field, time, fracture);
        } else {
            int longitude = 64;
            int latitude = 32;
            float fade = (float)Math.max(0.0, 1.0 - fracture / 1.5);

            for (int j = 0; j < latitude; j++) {
                for (int i = 0; i < longitude; i++) {
                    Vec3[] normals = new Vec3[]{
                        shell(i, j, longitude, latitude),
                        shell(i + 1, j, longitude, latitude),
                        shell(i + 1, j + 1, longitude, latitude),
                        shell(i, j + 1, longitude, latitude)
                    };
                    Vec3 mid = normals[0].add(normals[2]).scale(0.5).normalize();
                    double drift = fracture * fracture * (0.9 + 0.6 * Math.sin((double)i * 3.7 + (double)j * 2.2));
                    Vec3 center = field.center.add(mid.scale(drift)).add(0.0, -fracture * fracture * 0.8, 0.0);

                    for (Vec3 n : normals) {
                        Vec3 point = center.add(n.scale((double)field.radius));
                        double fresnel = Math.pow(1.0 - Math.abs(n.dot(drawCamera.subtract(point).normalize())), 2.5);
                        double shimmer = 0.5 + 0.5 * Math.sin(n.y * 32.0 - time * 1.2 + Math.sin(n.x * 9.0 + n.z * 11.0) * 0.7);
                        float alpha = (float)(0.1 + 0.4 * fresnel + 0.035 * shimmer) * fade;
                        vertex(out, m, point, 0.3F + (float)shimmer * 0.12F, 0.9F, CinemaDirector.bandTag(), alpha);
                    }
                }
            }
        }
    }

    private static Vec3 shell(int i, int j, int longitude, int latitude) {
        double a = (double)i * Math.PI * 2.0 / (double)longitude;
        double t = (double)j * Math.PI / (double)latitude;
        return new Vec3(Math.sin(t) * Math.cos(a), Math.cos(t), Math.sin(t) * Math.sin(a));
    }

    private static void defensiveShell(VertexConsumer out, Matrix4f m, ArcanaClient.VisualField field, double time, double fracture) {
        int index = 0;

        for (HexSphere.Panel panel : HexSphere.PANELS) {
            Vec3 n = panel.normal();
            Vec3 center = field.center.add(n.scale(fracture * fracture * (1.0 + (double)(index++ % 7) * 0.17)));
            Vec3 mid = center.add(n.scale((double)field.radius));
            float alpha = (float)(0.11 * Math.max(0.0, 1.0 - fracture / 2.0));

            for (int i = 0; i < panel.corners().size(); i++) {
                Vec3 a = center.add(panel.corners().get(i).scale((double)field.radius));
                Vec3 b = center.add(panel.corners().get((i + 1) % panel.corners().size()).scale((double)field.radius));
                triangle(out, m, mid, a, b, 0.48F, 0.83F, 1.0F, alpha);
                quad(out, m, a, b, b.lerp(mid, 0.035), a.lerp(mid, 0.035), 0.72F, 0.95F, 1.0F, alpha * 5.0F);
            }
        }
    }

    private static void beam(VertexConsumer out, Matrix4f m, ArcanaClient.Beam beam, double age) {
        Vec3 d = beam.end.subtract(beam.start).normalize();
        if (!(d.lengthSqr() < 0.1)) {
            float duration = beam.style == 3 ? 1.4F : (beam.style == 2 ? 1.6F : 0.5F);
            float alpha = (float)Math.pow(Math.max(0.0, 1.0 - age / (double)duration), 0.65);
            if (beam.style == 15) {
                Vec3 delta = beam.end.subtract(beam.start);
                Vec3 u = basis(d);
                Vec3 v = d.cross(u);
                Vec3 previous = beam.start;

                for (int j = 1; j <= 32; j++) {
                    Vec3 next = beam.start
                        .add(delta.scale((double)j / 32.0))
                        .add(u.scale(j == 32 ? 0.0 : Math.sin((double)j * 7.3 + age * 20.0) * 0.24))
                        .add(v.scale(j == 32 ? 0.0 : Math.cos((double)j * 4.1) * 0.12));
                    tube(out, m, previous, next, 0.018, 0.92F, 0.98F, 1.0F, alpha);
                    tube(out, m, previous, next, 0.07, 0.38F, 0.62F, 1.0F, alpha * 0.18F);
                    previous = next;
                }
            } else if (beam.style == 18) {
                for (int j = 0; j < 7; j++) {
                    double t = Math.min(1.0, Math.max(0.0, age * 3.0 - (double)j * 0.07));
                    crystal(out, m, beam.start.lerp(beam.end, t), d, 0.11, 0.22, 0.53F, 0.42F, 0.3F, alpha);
                }
            } else if (beam.style == 4) {
                for (int j = 0; j < 5; j++) {
                    double t = Math.min(1.0, Math.max(0.0, age * 4.0 - (double)j * 0.08));
                    crystal(out, m, beam.start.lerp(beam.end, t), d, 0.13, 0.45, 0.63F, 0.88F, 1.0F, alpha);
                }
            } else if (beam.style == 10) {
                Vec3 u = basis(d);
                double span = 0.6 + age * 3.0;
                quad(
                    out,
                    m,
                    beam.start.add(u.scale(span)),
                    beam.end.add(u.scale(span * 0.3)),
                    beam.end.subtract(u.scale(span * 0.3)),
                    beam.start.subtract(u.scale(span)),
                    0.85F,
                    1.0F,
                    0.67F,
                    alpha * 0.16F
                );
                tube(out, m, beam.start.subtract(u.scale(span)), beam.end.add(u.scale(span * 0.3)), 0.018, 1.0F, 1.0F, 0.88F, alpha);
                tube(out, m, beam.start.add(u.scale(span)), beam.end.subtract(u.scale(span * 0.3)), 0.018, 1.0F, 1.0F, 0.88F, alpha);
            } else {
                double base = beam.style == 2
                    ? 1.35
                    : (beam.style == 5 ? 0.5 : (beam.style == 3 ? 0.16 : (beam.style == 16 ? 0.5 : (beam.style == 10 ? 0.22 : 0.065))));
                float r = beam.style == 3 ? 0.012F : (beam.style == 4 ? 0.52F : (beam.style == 10 ? 1.0F : 0.68F));
                float g = beam.style == 3 ? 0.085F : (beam.style == 10 ? 0.92F : (beam.style != 16 && beam.style != 19 ? 0.86F : 0.86F));
                float b = beam.style == 3 ? 0.04F : (beam.style == 10 ? 0.24F : (beam.style != 16 && beam.style != 19 ? 1.0F : 0.48F));
                taperedBeam(out, m, beam.start, beam.end, base * 2.9, age, r, g, b, alpha * 0.08F);
                taperedBeam(out, m, beam.start, beam.end, base * 1.8, age, r, g, b, alpha * 0.18F);
                taperedBeam(out, m, beam.start, beam.end, base, age, r, g, b, alpha * 0.95F);
                if (beam.style != 3) {
                    taperedBeam(out, m, beam.start, beam.end, base * 0.38, age, 1.0F, 1.0F, 1.0F, alpha);
                } else {
                    taperedBeam(out, m, beam.start, beam.end, base * 1.1, age, 0.05F, 0.55F, 0.23F, alpha * 0.4F);
                    Vec3 u = basis(d);
                    Vec3 v = d.cross(u);

                    for (int j = 0; j < 10; j++) {
                        double a = (double)j * 2.39996 + age * 5.0;
                        double t = ((double)j * 0.17 + age * 0.8) % 1.0;
                        Vec3 center = beam.start.lerp(beam.end, t);
                        Vec3 p = center.add(u.scale(Math.cos(a) * base * 2.0)).add(v.scale(Math.sin(a) * base * 2.0));
                        triangle(out, m, p, p.add(u.scale(0.25)), p.add(d.scale(0.5)).add(v.scale(0.12)), 0.06F, 0.75F, 0.31F, alpha * 0.55F);
                    }
                }

                sigil(out, m, beam.start, d, beam.style == 2 ? 1.65 : 0.4, age * 2.0, r, g, b, alpha * 0.85F);
                if (beam.style == 2) {
                    orientedRing(out, m, beam.end, d, 0.2 + age * 6.0, 0.025, r, g, b, alpha * 0.5F);
                }

                for (int j = 0; j < 12; j++) {
                    double a = (double)j * 2.39996;
                    Vec3 u = basis(d);
                    Vec3 v = d.cross(u);
                    Vec3 rad = u.scale(Math.cos(a)).add(v.scale(Math.sin(a)));
                    Vec3 p = beam.end.add(rad.scale(age * (beam.style == 2 ? 4.0 : 1.5))).add(d.scale(-age * 0.5));
                    crystal(out, m, p, rad, 0.018, 0.1, r, g, b, alpha * 0.75F);
                }
            }
        }
    }

    private static Vec3 basis(Vec3 d) {
        return d.cross(Math.abs(d.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0)).normalize();
    }

    private static void taperedBeam(VertexConsumer out, Matrix4f m, Vec3 start, Vec3 end, double radius, double age, float r, float g, float b, float alpha) {
        Vec3 d = end.subtract(start).normalize();
        Vec3 u = basis(d);
        Vec3 v = d.cross(u);

        for (int j = 0; j < 8; j++) {
            for (int i = 0; i < 24; i++) {
                double t = (double)j / 8.0;
                double tt = (double)(j + 1) / 8.0;
                double a = (double)i * Math.PI / 12.0;
                double aa = (double)(i + 1) * Math.PI / 12.0;
                double rr = radius * (0.62 + 0.38 * Math.sin(Math.PI * (0.1 + 0.8 * t))) * (1.0 + 0.04 * Math.sin(t * 30.0 - age * 15.0));
                double rrr = radius * (0.62 + 0.38 * Math.sin(Math.PI * (0.1 + 0.8 * tt))) * (1.0 + 0.04 * Math.sin(tt * 30.0 - age * 15.0));
                Vec3 x = u.scale(Math.cos(a)).add(v.scale(Math.sin(a)));
                Vec3 y = u.scale(Math.cos(aa)).add(v.scale(Math.sin(aa)));
                Vec3 p = start.lerp(end, t);
                Vec3 q = start.lerp(end, tt);
                quad(out, m, p.add(x.scale(rr)), q.add(x.scale(rrr)), q.add(y.scale(rrr)), p.add(y.scale(rr)), r, g, b, alpha);
            }
        }
    }

    private static void orientedRing(
        VertexConsumer out, Matrix4f m, Vec3 center, Vec3 normal, double radius, double width, float r, float g, float b, float alpha
    ) {
        Vec3 u = basis(normal);
        Vec3 v = normal.cross(u);

        for (int i = 0; i < 96; i++) {
            double a = (double)i * Math.PI / 48.0;
            double c = (double)(i + 1) * Math.PI / 48.0;
            Vec3 p = u.scale(Math.cos(a)).add(v.scale(Math.sin(a)));
            Vec3 q = u.scale(Math.cos(c)).add(v.scale(Math.sin(c)));
            quad(
                out,
                m,
                center.add(p.scale(radius)),
                center.add(q.scale(radius)),
                center.add(q.scale(radius + width)),
                center.add(p.scale(radius + width)),
                r,
                g,
                b,
                alpha
            );
        }
    }

    private static void sigil(VertexConsumer out, Matrix4f m, Vec3 center, Vec3 normal, double radius, double time, float r, float g, float b, float alpha) {
        orientedRing(out, m, center, normal, radius, 0.014, r, g, b, alpha);
        Vec3 u = basis(normal);
        Vec3 v = normal.cross(u);

        for (int i = 0; i < 12; i++) {
            double a = (double)i * Math.PI / 6.0 + time * 0.2;
            Vec3 radial = u.scale(Math.cos(a)).add(v.scale(Math.sin(a)));
            Vec3 tangent = u.scale(-Math.sin(a)).add(v.scale(Math.cos(a)));
            Vec3 p = center.add(radial.scale(radius * 0.71));
            tube(out, m, p.add(tangent.scale(radius * 0.04)), p.add(radial.scale(radius * 0.1)), 0.009, r, g, b, alpha);
            tube(out, m, p.add(radial.scale(radius * 0.1)), p.subtract(tangent.scale(radius * 0.04)), 0.009, r, g, b, alpha);
            tube(out, m, p.subtract(tangent.scale(radius * 0.04)), p.add(radial.scale(radius * 0.02)), 0.006, r, g, b, alpha);
        }

        for (int i = 0; i < 6; i++) {
            double a = (double)i * Math.PI / 3.0 + time * 0.12;
            double aa = (double)(i + 2) * Math.PI / 3.0 + time * 0.12;
            tube(
                out,
                m,
                center.add(u.scale(Math.cos(a) * radius * 0.45)).add(v.scale(Math.sin(a) * radius * 0.45)),
                center.add(u.scale(Math.cos(aa) * radius * 0.45)).add(v.scale(Math.sin(aa) * radius * 0.45)),
                0.008,
                r,
                g,
                b,
                alpha * 0.65F
            );
        }
    }

    private static void crystal(VertexConsumer out, Matrix4f m, Vec3 center, Vec3 d, double width, double length, float r, float g, float b, float alpha) {
        d = d.normalize();
        Vec3 u = basis(d);
        Vec3 v = d.cross(u);
        Vec3 top = center.add(d.scale(length));
        Vec3 bottom = center.subtract(d.scale(length * 0.65));

        for (int i = 0; i < 6; i++) {
            double a = (double)i * Math.PI / 3.0;
            double aa = (double)(i + 1) * Math.PI / 3.0;
            Vec3 p = center.add(u.scale(Math.cos(a) * width)).add(v.scale(Math.sin(a) * width));
            Vec3 q = center.add(u.scale(Math.cos(aa) * width)).add(v.scale(Math.sin(aa) * width));
            float shade = 0.66F + (float)(i % 3) * 0.15F;
            triangle(out, m, top, p, q, r * shade, g * shade, b * shade, alpha);
            triangle(out, m, bottom, q, p, r * shade, g * shade, b * shade, alpha * 0.9F);
        }
    }

    private static void tube(VertexConsumer out, Matrix4f matrix, Vec3 start, Vec3 end, double radius, float r, float g, float b, float alpha) {
        Vec3 d = end.subtract(start).normalize();
        Vec3 u = d.cross(Math.abs(d.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0)).normalize();
        Vec3 v = d.cross(u);

        for (int i = 0; i < 20; i++) {
            double a = (double)i * Math.PI / 10.0;
            double c = (double)(i + 1) * Math.PI / 10.0;
            Vec3 aa = u.scale(Math.cos(a) * radius).add(v.scale(Math.sin(a) * radius));
            Vec3 cc = u.scale(Math.cos(c) * radius).add(v.scale(Math.sin(c) * radius));
            quad(out, matrix, start.add(aa), end.add(aa), end.add(cc), start.add(cc), r, g, b, alpha);
        }
    }

    private static void fracture(VertexConsumer out, Matrix4f m, ArcanaClient.Fracture f, double time, double age) {
        sphere(out, m, f.field, time, Math.max(0.001, age - 0.9));
        Vec3 n = f.impact.subtract(f.field.center).normalize();
        Vec3 u = n.cross(Math.abs(n.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0)).normalize();
        Vec3 v = n.cross(u);
        double propagation = Math.min(Math.PI, age * 7.0);
        double radius = (double)f.field.radius + 0.08;
        float alpha = (float)Math.max(0.0, 1.0 - age / 3.2);

        for (int ray = 0; ray < 15; ray++) {
            double angle = (double)ray * Math.PI * 2.0 / 15.0;
            Vec3 previous = f.impact;

            for (int j = 1; j <= 18; j++) {
                double t = propagation * (double)j / 18.0;
                double wobble = Math.sin((double)j * 2.7 + (double)ray) * 0.1;
                Vec3 lateral = u.scale(Math.cos(angle + wobble)).add(v.scale(Math.sin(angle + wobble)));
                Vec3 point = f.field.center.add(n.scale(Math.cos(t) * radius)).add(lateral.scale(Math.sin(t) * radius));
                tube(out, m, previous, point, 0.045, 0.015F, 0.09F, 0.04F, alpha);
                Vec3 offset = u.scale(0.04).add(v.scale(0.02));
                tube(out, m, previous.add(offset), point.add(offset), 0.018, 0.45F, 1.0F, 0.78F, alpha * 0.85F);
                tube(out, m, previous.subtract(offset), point.subtract(offset), 0.018, 0.76F, 0.48F, 1.0F, alpha * 0.65F);
                previous = point;
            }
        }
    }

    private static void prisms(VertexConsumer out, Matrix4f m, ArcanaClient.Fracture f, double age) {
        Vec3 n = f.impact.subtract(f.field.center).normalize();
        Vec3 u = basis(n);
        Vec3 v = n.cross(u);
        double propagation = Math.min(Math.PI, age * 7.0);
        double width = Math.min(3.0, (double)f.field.radius * 0.045);
        double drift = Math.max(0.0, age - 0.9);
        float alpha = (float)Math.max(0.0, 0.45 * (1.0 - age / 3.2));

        for (int i = 0; i < 64; i++) {
            double a = (double)i * 2.39996;
            double t = propagation * Math.sqrt(((double)i + 0.5) / 64.0);
            Vec3 normal = n.scale(Math.cos(t)).add(u.scale(Math.cos(a) * Math.sin(t))).add(v.scale(Math.sin(a) * Math.sin(t)));
            Vec3 side = basis(normal);
            Vec3 up = normal.cross(side);
            Vec3 center = f.field.center.add(normal.scale((double)f.field.radius + 0.12 + drift * drift * 2.0));
            Vec3 aa = center.add(side.scale(-width)).add(up.scale(-width * 0.4));
            Vec3 bb = center.add(side.scale(width * 0.7)).add(up.scale(-width * 0.25));
            Vec3 cc = center.add(up.scale(width * 0.9));
            triangle(out, m, aa, bb, cc, 0.5F + (float)Math.sin(a) * 0.45F, 0.5F + (float)Math.cos(a) * 0.45F, 0.5F, alpha);
        }
    }

    private static void effect(VertexConsumer out, Matrix4f m, ArcanaClient.Effect e, double age, double time) {
        Vec3 c = e.center;
        int level = e.strength;
        if (e.style == 20) {
            float alpha = (float)Math.max(0.0, 0.35 * (1.0 - age / 0.65));

            for (int j = 0; j < 4; j++) {
                double a = (double)j * 2.39996 + time;
                crystal(out, m, c.add(Math.cos(a) * 0.24, age * 0.2, Math.sin(a) * 0.24), new Vec3(0.0, 1.0, 0.0), 0.009, 0.03, 0.65F, 0.85F, 1.0F, alpha);
            }
        } else if (e.style == 11) {
            float alpha = (float)Math.min(1.0, Math.min(age * 3.0, (15.0 - age) * 2.0));

            for (int i = 0; i < 24 * level; i++) {
                double angle = (double)i * 2.39996;
                double rad = Math.sqrt((double)(i + 1) / (24.0 * (double)level)) * (double)(2 + level);
                Vec3 root = c.add(Math.cos(angle) * rad, 0.02, Math.sin(angle) * rad);
                Vec3 flower = root.add(0.0, 0.25 + (double)(i % 4) * 0.07, 0.0);
                tube(out, m, root, flower, 0.012, 0.1F, 0.6F, 0.2F, alpha);

                for (int petal = 0; petal < 5; petal++) {
                    double a = (double)petal * Math.PI * 2.0 / 5.0;
                    Vec3 q = flower.add(Math.cos(a) * 0.09, 0.015, Math.sin(a) * 0.09);
                    crystal(out, m, q, new Vec3(Math.cos(a), 0.2, Math.sin(a)), 0.035, 0.07, i % 2 == 0 ? 1.0F : 0.68F, 0.55F, 1.0F, alpha);
                }
            }
        } else if (e.style != 14 && e.style != 21) {
            if (e.style == 13) {
                float alpha = (float)Math.max(0.0, 1.0 - age / 1.3);

                for (int j = 0; j < 12 + level * 3; j++) {
                    double a = (double)j * 2.39996;
                    double r = Math.sqrt((double)(j + 1) / (12.0 + (double)(level * 3))) * (double)(1 + level);
                    Vec3 root = c.add(Math.cos(a) * r, -0.4, Math.sin(a) * r);
                    tube(
                        out,
                        m,
                        root,
                        root.add(Math.sin(time * 6.0 + (double)j) * 0.15, 1.0 + age * 2.0, Math.cos(time * 6.0 + (double)j) * 0.15),
                        0.1,
                        1.0F,
                        0.22F,
                        0.02F,
                        alpha
                    );
                }
            } else if (e.style == 17) {
                float alpha = (float)Math.max(0.0, 1.0 - age / 1.3);

                for (int j = 0; j < 12 + level * 3; j++) {
                    double a = time * 3.0 + (double)j * 2.39996;
                    Vec3 blade = c.add(Math.cos(a) * (1.0 + age), 0.2 + (double)(j % 5) * 0.25, Math.sin(a) * (1.0 + age));
                    crystal(out, m, blade, new Vec3(-Math.sin(a), 0.3, Math.cos(a)), 0.045, 0.25, 0.83F, 0.9F, 1.0F, alpha);
                }
            } else if (e.style == 15) {
                float alpha = (float)Math.max(0.0, 1.0 - age / 1.3);

                for (int j = 0; j < 8; j++) {
                    double a = (double)j * Math.PI / 4.0;
                    Vec3 end = c.add(Math.cos(a) * (0.5 + age * 3.0), Math.sin((double)j * 2.1) * 0.5, Math.sin(a) * (0.5 + age * 3.0));
                    tube(out, m, c, end, 0.025, 0.6F, 0.8F, 1.0F, alpha);
                }
            } else if (e.style == 6) {
                float alpha = (float)Math.max(0.0, 1.0 - age / 1.3);
                double rise = Math.sin(Math.min(1.0, age) * Math.PI) * 2.0;

                for (int i = 0; i < 8 + level; i++) {
                    double a = (double)i * Math.PI * 2.0 / (double)(8 + level);
                    Vec3 root = c.add(Math.cos(a) * (1.0 + (double)level * 0.5), 0.0, Math.sin(a) * (1.0 + (double)level * 0.5));
                    crystal(
                        out,
                        m,
                        root.add(0.0, rise * 0.5, 0.0),
                        new Vec3(0.12 * Math.sin((double)i), 1.0, 0.12 * Math.cos((double)i)),
                        0.26,
                        0.2 + rise * (1.0 + (double)(i % 3) * 0.25),
                        0.52F,
                        0.4F,
                        0.29F,
                        alpha
                    );
                }
            } else if (e.style == 8 || e.style == 9) {
                float alpha = (float)Math.max(0.0, 1.0 - age / (double)(e.style == 9 ? 10 : 6));

                for (int i = 0; i < 1; i++) {
                    sigil(
                        out,
                        m,
                        c.add(0.0, 0.7, 0.0),
                        new Vec3(0.0, 1.0, 0.0),
                        0.62,
                        time * (double)(i % 2 == 0 ? 1 : -1),
                        e.style == 9 ? 1.0F : 0.75F,
                        e.style == 9 ? 0.73F : 0.45F,
                        e.style == 9 ? 0.1F : 1.0F,
                        alpha
                    );
                }

                for (int j = 0; j < 16; j++) {
                    double a = (double)j * 2.39996 + time;
                    crystal(
                        out,
                        m,
                        c.add(Math.cos(a) * 0.68, (double)(j % 5) * 0.35, Math.sin(a) * 0.68),
                        new Vec3(0.0, 1.0, 0.0),
                        0.025,
                        0.07,
                        e.style == 9 ? 1.0F : 0.75F,
                        e.style == 9 ? 0.73F : 0.45F,
                        e.style == 9 ? 0.1F : 1.0F,
                        alpha
                    );
                }
            } else {
                float alpha = (float)Math.max(0.0, 1.0 - age / 1.3);

                for (int j = 0; j < 18; j++) {
                    double a = (double)j * 2.39996 + time;
                    crystal(
                        out,
                        m,
                        c.add(Math.cos(a) * (0.4 + age), age * (0.4 + (double)(j % 4) * 0.25), Math.sin(a) * (0.4 + age)),
                        new Vec3(0.0, 1.0, 0.0),
                        0.018,
                        0.06,
                        0.8F,
                        0.94F,
                        1.0F,
                        alpha
                    );
                }
            }
        } else {
            float alpha = (float)Math.max(0.0, 1.0 - age / 4.0);
            boolean fire = e.style == 21;

            for (int j = 0; j < 6; j++) {
                spiralColor(
                    out,
                    m,
                    c,
                    3.0 + (double)level * 0.35,
                    1.0 + (double)level * 0.18,
                    age * 5.0 + (double)j * Math.PI / 3.0,
                    alpha * 0.65F,
                    fire ? 1.0F : 0.65F,
                    fire ? 0.28F : 0.84F,
                    fire ? 0.03F : 1.0F
                );
            }
        }
    }

    private static void spiral(VertexConsumer out, Matrix4f matrix, Vec3 center, double height, double radius, double time, float alpha) {
        for (int i = 0; i < 48; i++) {
            double t = (double)i / 48.0;
            double t2 = (double)(i + 1) / 48.0;
            double a = time + t * Math.PI * 4.0;
            double c = time + t2 * Math.PI * 4.0;
            Vec3 p = center.add(Math.cos(a) * radius, height * t, Math.sin(a) * radius);
            Vec3 q = center.add(Math.cos(c) * radius, height * t2, Math.sin(c) * radius);
            float fade = (float)Math.sin(Math.PI * t);
            tube(out, matrix, p, q, 0.013, 0.64F, 0.86F, 1.0F, alpha * fade);
            tube(out, matrix, p, q, 0.06, 0.5F, 0.76F, 1.0F, alpha * fade * 0.12F);
        }
    }

    private static void spiralColor(
        VertexConsumer out, Matrix4f m, Vec3 center, double height, double radius, double time, float alpha, float r, float g, float b
    ) {
        for (int i = 0; i < 48; i++) {
            double t = (double)i / 48.0;
            double t2 = (double)(i + 1) / 48.0;
            double a = time + t * Math.PI * 4.0;
            double c = time + t2 * Math.PI * 4.0;
            Vec3 p = center.add(Math.cos(a) * radius * (0.2 + 0.8 * t), height * t, Math.sin(a) * radius * (0.2 + 0.8 * t));
            Vec3 q = center.add(Math.cos(c) * radius * (0.2 + 0.8 * t2), height * t2, Math.sin(c) * radius * (0.2 + 0.8 * t2));
            float fade = (float)Math.sin(Math.PI * t);
            quad(out, m, p, q, q.add(0.0, 0.22, 0.0), p.add(0.0, 0.22, 0.0), r, g, b, alpha * fade * 0.32F);
            tube(out, m, p, q, 0.012, r, g, b, alpha * fade * 0.65F);
        }
    }

    private static void cube(VertexConsumer out, Matrix4f m, Vec3 c, double h, float r, float g, float b, float alpha) {
        Vec3 a = c.add(-h, -h, -h);
        Vec3 d = c.add(h, -h, -h);
        Vec3 e = c.add(h, -h, h);
        Vec3 f = c.add(-h, -h, h);
        Vec3 aa = a.add(0.0, h * 2.0, 0.0);
        Vec3 dd = d.add(0.0, h * 2.0, 0.0);
        Vec3 ee = e.add(0.0, h * 2.0, 0.0);
        Vec3 ff = f.add(0.0, h * 2.0, 0.0);
        quad(out, m, a, d, dd, aa, r, g, b, alpha);
        quad(out, m, d, e, ee, dd, r, g, b, alpha);
        quad(out, m, e, f, ff, ee, r, g, b, alpha);
        quad(out, m, f, a, aa, ff, r, g, b, alpha);
        quad(out, m, aa, dd, ee, ff, r, g, b, alpha);
        quad(out, m, a, f, e, d, r, g, b, alpha);
    }

    private static void ring(VertexConsumer out, Matrix4f matrix, Vec3 center, double radius, double time, double width, float r, float g, float b, float alpha) {
        for (int i = 0; i < 64; i++) {
            double a = (double)i * Math.PI / 32.0 + time;
            double c = (double)(i + 1) * Math.PI / 32.0 + time;
            Vec3 p = center.add(Math.cos(a) * radius, 0.0, Math.sin(a) * radius);
            Vec3 q = center.add(Math.cos(c) * radius, 0.0, Math.sin(c) * radius);
            Vec3 pa = center.add(Math.cos(a) * (radius + width), 0.0, Math.sin(a) * (radius + width));
            Vec3 qa = center.add(Math.cos(c) * (radius + width), 0.0, Math.sin(c) * (radius + width));
            quad(out, matrix, p, q, qa, pa, r, g, b, alpha);
        }
    }

    private static void triangle(VertexConsumer out, Matrix4f m, Vec3 a, Vec3 b, Vec3 c, float r, float g, float blue, float alpha) {
        quad(out, m, a, b, c, c, r, g, blue, alpha);
    }

    private static void quad(VertexConsumer out, Matrix4f m, Vec3 a, Vec3 b, Vec3 c, Vec3 d, float r, float g, float blue, float alpha) {
        vertex(out, m, a, r, g, blue, alpha);
        vertex(out, m, b, r, g, blue, alpha);
        vertex(out, m, c, r, g, blue, alpha);
        vertex(out, m, d, r, g, blue, alpha);
    }

    private static void vertex(VertexConsumer out, Matrix4f matrix, Vec3 p, float r, float g, float b, float alpha) {
        Vec3 relative = p.subtract(drawCamera);
        out.addVertex(matrix, (float)relative.x, (float)relative.y, (float)relative.z).setColor(r, g, b, alpha);
    }

    public static boolean isFlying(UUID var0) {
        Long var1 = FLIGHTS.get(var0);
        return var1 != null && System.nanoTime() - var1 < 1500000000L;
    }

    public static List fields() {
        return FIELDS;
    }

    public static List fractures() {
        return FRACTURES;
    }

    private static record Beam(Vec3 start, Vec3 end, int style, long startNanos) {
    }

    private static record Effect(Vec3 center, int style, int strength, long startNanos) {
    }

    private static record Fracture(ArcanaClient.VisualField field, Vec3 impact, long startNanos) {
    }

    public static record Mana(float current, float capacity) {
    }

    @EventBusSubscriber(
        modid = "frieren_arcana",
        value = {Dist.CLIENT},
        bus = Bus.MOD
    )
    public static final class Registration {
        @SubscribeEvent
        public static void gui(RegisterGuiLayersEvent event) {
            event.registerAboveAll(
                FrierenArcana.id("mana_sight"),
                (graphics, partial) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (ArcanaClient.sight && mc.player != null) {
                        graphics.drawString(mc.font, Component.translatable("hud.frieren_arcana.mana_sight"), 8, 8, 13626111);
                        if (mc.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof Player p && ArcanaClient.MANA.containsKey(p.getUUID())) {
                            ArcanaClient.Mana mana = ArcanaClient.MANA.get(p.getUUID());
                            graphics.drawCenteredString(
                                mc.font,
                                p.getName().getString() + "  " + Math.round(mana.current) + " / " + Math.round(mana.capacity) + " mana",
                                graphics.guiWidth() / 2,
                                graphics.guiHeight() / 2 + 18,
                                15398655
                            );
                        }
                    }
                }
            );
        }
    }

    public static record VisualField(UUID id, Vec3 center, int radius, boolean defensive) {
    }
}
