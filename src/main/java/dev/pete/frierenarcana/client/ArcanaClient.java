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

    private static ArcanaClient.VisualField readField(CompoundTag var0) {
        return new ArcanaClient.VisualField(var0.getUUID("id"), ArcanaNetwork.vector(var0, "center"), var0.getInt("radius"), var0.getBoolean("defensive"));
    }

    public static void receive(CompoundTag var0) {
        if (!CastCircles.receive(var0)) {
            String var1 = var0.getString("kind");
            switch (var1) {
                case "fields":
                    FIELDS.clear();

                    for (Tag var9 : var0.getList("fields", 10)) {
                        FIELDS.add(readField((CompoundTag)var9));
                    }
                    break;
                case "sight":
                    sight = var0.getBoolean("active");
                    MANA.clear();

                    for (Tag var4 : var0.getList("players", 10)) {
                        CompoundTag var5 = (CompoundTag)var4;
                        MANA.put(var5.getUUID("player"), new ArcanaClient.Mana(var5.getFloat("mana"), var5.getFloat("capacity")));
                    }
                    break;
                case "beam":
                    BEAMS.add(
                        new ArcanaClient.Beam(ArcanaNetwork.vector(var0, "start"), ArcanaNetwork.vector(var0, "end"), var0.getInt("style"), System.nanoTime())
                    );
                    break;
                case "shatter":
                    FRACTURES.add(new ArcanaClient.Fracture(readField(var0.getCompound("field")), ArcanaNetwork.vector(var0, "impact"), System.nanoTime()));
                    break;
                case "flight":
                    UUID var6 = var0.getUUID("player");
                    if (var0.getBoolean("active")) {
                        FLIGHTS.put(var6, System.nanoTime());
                    } else {
                        FLIGHTS.remove(var6);
                    }
                    break;
                case "effect":
                    EFFECTS.add(new ArcanaClient.Effect(ArcanaNetwork.vector(var0, "center"), var0.getInt("style"), var0.getInt("strength"), System.nanoTime()));
                    break;
                case "charge":
                    UUID var3 = var0.getUUID("player");
                    ArcanaCinematic.charge(var3, var0.getBoolean("active"), var0.getBoolean("breaker"));
                    if (var0.getBoolean("active")) {
                        CHARGES.put(var3, System.nanoTime());
                        if (var0.getBoolean("breaker")) {
                            BREAKER_CHARGES.add(var3);
                        } else {
                            BREAKER_CHARGES.remove(var3);
                        }
                    } else {
                        CHARGES.remove(var3);
                        BREAKER_CHARGES.remove(var3);
                    }
            }
        }
    }

    public static boolean rainBlocked(Vec3 var0) {
        return FIELDS.stream().anyMatch(var1 -> !var1.defensive && var1.center.distanceToSqr(var0) < (double)(var1.radius * var1.radius));
    }

    public static int rainHeight(int var0, int var1, int var2) {
        int var3 = var2;

        for (ArcanaClient.VisualField var5 : FIELDS) {
            if (!var5.defensive) {
                double var6 = (double)var0 + 0.5 - var5.center.x;
                double var8 = (double)var1 + 0.5 - var5.center.z;
                double var10 = var6 * var6 + var8 * var8;
                if (var10 < (double)(var5.radius * var5.radius)) {
                    var3 = Math.max(var3, (int)Math.ceil(var5.center.y + Math.sqrt((double)(var5.radius * var5.radius) - var10)));
                }
            }
        }

        return var3;
    }

    public static double boundaryHit(Vec3 var0, Vec3 var1, boolean var2) {
        Vec3 var3 = var1.subtract(var0);
        double var4 = Double.POSITIVE_INFINITY;

        for (ArcanaClient.VisualField var7 : FIELDS) {
            if (!var7.defensive || var2 && !(var7.center.distanceToSqr(var0) < (double)(var7.radius * var7.radius))) {
                Vec3 var8 = var0.subtract(var7.center);
                var4 = Math.min(var4, BarrierGeometry.firstHit(var8.x, var8.y, var8.z, var3.x, var3.y, var3.z, (double)var7.radius));
            }
        }

        return var4;
    }

    public static Vec3 movement(Entity var0, Vec3 var1) {
        return BarrierSlide.client(var0, var1);
    }

    @SubscribeEvent
    public static void logout(LoggingOut var0) {
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
    public static void render(RenderLevelStageEvent var0) {
        if (var0.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
            Minecraft var1 = Minecraft.getInstance();
            if (var1.level != null) {
                Vec3 var2 = var0.getCamera().getPosition();
                drawCamera = var2;
                PoseStack var3 = var0.getPoseStack();
                var3.pushPose();
                Matrix4f var4 = var3.last().pose();
                BufferSource var5 = var1.renderBuffers().bufferSource();
                VertexConsumer var6 = var5.getBuffer(ArcanaRenderTypes.MAGIC);
                double var7 = (double)((float)var1.level.getGameTime() + var0.getPartialTick().getGameTimeDeltaPartialTick(false)) / 20.0;
                ArcanaShaders.prepare(var7, var2);
                if (!FRACTURES.isEmpty()) {
                    ArcanaRefraction.capture(var7);
                } else {
                    ArcanaRefraction.release();
                }

                for (ArcanaClient.VisualField var10 : FIELDS) {
                    if (var10.center.distanceToSqr(var2) < Math.pow((double)(var10.radius + 180), 2.0)) {
                        sphere(var6, var4, var10, var7, 0.0);
                    }
                }

                long var22 = System.nanoTime();
                FRACTURES.removeIf(var2x -> (double)(var22 - var2x.startNanos) / 1.0E9 > 15.3);

                for (ArcanaClient.Fracture var12 : FRACTURES) {
                    fracture(var6, var4, var12, var7, (double)(var22 - var12.startNanos) / 1.0E9);
                }

                BEAMS.removeIf(var2x -> (double)(var22 - var2x.startNanos()) / 1.0E9 > SpellFx.beamLife(var2x.style()));

                for (ArcanaClient.Beam var29 : BEAMS) {
                    beam(var6, var4, var29, (double)(var22 - var29.startNanos) / 1.0E9);
                }

                if (sight) {
                    for (Player var30 : var1.level.players()) {
                        ArcanaClient.Mana var13 = MANA.get(var30.getUUID());
                        if (var13 != null) {
                            Vec3 var14 = ShipSpace.world(var1.level, var30.getPosition(var0.getPartialTick().getGameTimeDeltaPartialTick(false)));
                            double var15 = Math.max(0.15, Math.min(7.0, 0.4 + Math.sqrt((double)Math.max(0.0F, var13.current) / 100.0) * 1.7));

                            for (int var17 = 0; var17 < 4; var17++) {
                                spiral(var6, var4, var14, var15, 0.45 + (double)var17 * 0.1, var7 + (double)var17 * Math.PI / 2.0, 0.2F);
                            }
                        }
                    }
                }

                CHARGES.entrySet().removeIf(var2x -> (double)(var22 - var2x.getValue()) / 1.0E9 > 12.0);

                for (Player var31 : var1.level.players()) {
                    if (CHARGES.containsKey(var31.getUUID())) {
                        Vec3 var35 = ShipSpace.world(var1.level, var31.getPosition(var0.getPartialTick().getGameTimeDeltaPartialTick(false)))
                            .add(0.0, 1.0, 0.0);
                        boolean var38 = BREAKER_CHARGES.contains(var31.getUUID());
                        Vec3 var39 = var31.getLookAngle();
                        double var16 = Math.min(1.0, (double)(var22 - CHARGES.get(var31.getUUID())) / 1.0E9 / (double)(var38 ? 5 : 8));
                        ChargeFx.sigil(
                            var6,
                            var4,
                            var35.add(var39.scale(0.8)),
                            var39,
                            0.7 + var16 * 0.6,
                            var7,
                            var38 ? 0.05F : 0.72F,
                            var38 ? 0.8F : 0.91F,
                            var38 ? 0.38F : 1.0F,
                            0.8F
                        );

                        for (int var18 = 0; var18 < 12; var18++) {
                            double var19 = (double)var18 * 2.39996 + var7;
                            Vec3 var21 = var35.add(Math.cos(var19) * (1.0 - var16 * 0.5), Math.sin(var19 * 0.7) * 0.85, Math.sin(var19) * (1.0 - var16 * 0.5));
                            ChargeFx.crystal(
                                var6, var4, var21, new Vec3(0.0, 1.0, 0.0), 0.025, 0.09, var38 ? 0.1F : 0.8F, var38 ? 0.85F : 0.96F, var38 ? 0.4F : 1.0F, 0.6F
                            );
                        }
                    }
                }

                FLIGHTS.entrySet().removeIf(var2x -> (double)(var22 - var2x.getValue()) / 1.0E9 > 1.5);
                EFFECTS.removeIf(var2x -> (double)(var22 - var2x.startNanos()) / 1.0E9 > SpellFx.effectLife(var2x.style()));

                for (ArcanaClient.Effect var32 : EFFECTS) {
                    effect(var6, var4, var32, (double)(var22 - var32.startNanos) / 1.0E9, var7);
                }

                var5.endBatch(ArcanaRenderTypes.MAGIC);
                if (!FRACTURES.isEmpty()) {
                    VertexConsumer var27 = var5.getBuffer(ArcanaRenderTypes.REFRACTION);

                    for (ArcanaClient.Fracture var36 : FRACTURES) {
                        prisms(var27, var4, var36, (double)(var22 - var36.startNanos) / 1.0E9);
                    }

                    var5.endBatch(ArcanaRenderTypes.REFRACTION);
                }

                var3.popPose();

                for (Player var34 : var1.level.players()) {
                    if (FLIGHTS.containsKey(var34.getUUID())) {
                        Vec3 var37 = ShipSpace.world(var1.level, var34.getPosition(var0.getPartialTick().getGameTimeDeltaPartialTick(false))).subtract(var2);
                        var3.pushPose();
                        var3.translate(var37.x, var37.y - 0.22 + Math.sin(var7 * 3.0) * 0.025, var37.z);
                        var3.mulPose(Axis.YP.rotationDegrees(-var34.getYRot()));
                        var3.mulPose(Axis.XP.rotationDegrees(90.0F));
                        var1.getItemRenderer()
                            .renderStatic(
                                FrierenArcana.STAFF.get().getDefaultInstance(),
                                ItemDisplayContext.NONE,
                                LevelRenderer.getLightColor(var1.level, var34.blockPosition()),
                                OverlayTexture.NO_OVERLAY,
                                var3,
                                var5,
                                var1.level,
                                var34.getId()
                            );
                        var3.popPose();
                    }
                }

                var5.endBatch();
            }
        }
    }

    private static void sphere(VertexConsumer var0, Matrix4f var1, ArcanaClient.VisualField var2, double var3, double var5) {
        if (!var2.defensive()) {
            BarrierLook.sphere(var0, var1, var2, var3, var5);
        } else if (var2.defensive) {
            defensiveShell(var0, var1, var2, var3, var5);
        } else {
            byte var7 = 64;
            byte var8 = 32;
            float var9 = (float)Math.max(0.0, 1.0 - var5 / 1.5);

            for (int var10 = 0; var10 < var8; var10++) {
                for (int var11 = 0; var11 < var7; var11++) {
                    Vec3[] var12 = new Vec3[]{
                        shell(var11, var10, var7, var8),
                        shell(var11 + 1, var10, var7, var8),
                        shell(var11 + 1, var10 + 1, var7, var8),
                        shell(var11, var10 + 1, var7, var8)
                    };
                    Vec3 var13 = var12[0].add(var12[2]).scale(0.5).normalize();
                    double var14 = var5 * var5 * (0.9 + 0.6 * Math.sin((double)var11 * 3.7 + (double)var10 * 2.2));
                    Vec3 var16 = var2.center.add(var13.scale(var14)).add(0.0, -var5 * var5 * 0.8, 0.0);

                    for (Vec3 var20 : var12) {
                        Vec3 var21 = var16.add(var20.scale((double)var2.radius));
                        double var22 = Math.pow(1.0 - Math.abs(var20.dot(drawCamera.subtract(var21).normalize())), 2.5);
                        double var24 = 0.5 + 0.5 * Math.sin(var20.y * 32.0 - var3 * 1.2 + Math.sin(var20.x * 9.0 + var20.z * 11.0) * 0.7);
                        float var26 = (float)(0.1 + 0.4 * var22 + 0.035 * var24) * var9;
                        vertex(var0, var1, var21, 0.3F + (float)var24 * 0.12F, 0.9F, CinemaDirector.bandTag(var20.y), var26);
                    }
                }
            }
        }
    }

    private static Vec3 shell(int var0, int var1, int var2, int var3) {
        double var4 = (double)var0 * Math.PI * 2.0 / (double)var2;
        double var6 = (double)var1 * Math.PI / (double)var3;
        return new Vec3(Math.sin(var6) * Math.cos(var4), Math.cos(var6), Math.sin(var6) * Math.sin(var4));
    }

    private static void defensiveShell(VertexConsumer var0, Matrix4f var1, ArcanaClient.VisualField var2, double var3, double var5) {
        int var7 = 0;

        for (HexSphere.Panel var9 : HexSphere.PANELS) {
            Vec3 var10 = var9.normal();
            Vec3 var11 = var2.center.add(var10.scale(var5 * var5 * (1.0 + (double)(var7++ % 7) * 0.17)));
            Vec3 var12 = var11.add(var10.scale((double)var2.radius));
            float var13 = (float)(0.17 * Math.max(0.0, 1.0 - var5 / 2.0));

            for (int var14 = 0; var14 < var9.corners().size(); var14++) {
                Vec3 var15 = var11.add(var9.corners().get(var14).scale((double)var2.radius));
                Vec3 var16 = var11.add(var9.corners().get((var14 + 1) % var9.corners().size()).scale((double)var2.radius));
                triangle(var0, var1, var12, var15, var16, 0.48F, 0.83F, 1.0F, var13);
                quad(var0, var1, var15, var16, var16.lerp(var12, 0.035), var15.lerp(var12, 0.035), 0.72F, 0.95F, 1.0F, var13 * 5.0F);
            }
        }
    }

    private static void beam(VertexConsumer var0, Matrix4f var1, ArcanaClient.Beam var2, double var3) {
        if (!SpellFx.beam(var0, var1, var2.style(), var2.start(), var2.end(), var3)) {
            if (var2.style() == 3) {
                BreakerFx.beam(var0, var1, var2.start(), var2.end(), var3);
            } else {
                Vec3 var5 = var2.end.subtract(var2.start).normalize();
                if (!(var5.lengthSqr() < 0.1)) {
                    float var6 = var2.style == 3 ? 1.4F : (var2.style == 2 ? 1.6F : 0.5F);
                    float var7 = (float)Math.pow(Math.max(0.0, 1.0 - var3 / (double)var6), 0.65);
                    if (var2.style == 15) {
                        Vec3 var25 = var2.end.subtract(var2.start);
                        Vec3 var28 = basis(var5);
                        Vec3 var29 = var5.cross(var28);
                        Vec3 var30 = var2.start;

                        for (int var31 = 1; var31 <= 32; var31++) {
                            Vec3 var33 = var2.start
                                .add(var25.scale((double)var31 / 32.0))
                                .add(var28.scale(var31 == 32 ? 0.0 : Math.sin((double)var31 * 7.3 + var3 * 20.0) * 0.24))
                                .add(var29.scale(var31 == 32 ? 0.0 : Math.cos((double)var31 * 4.1) * 0.12));
                            tube(var0, var1, var30, var33, 0.018, 0.92F, 0.98F, 1.0F, var7);
                            tube(var0, var1, var30, var33, 0.07, 0.38F, 0.62F, 1.0F, var7 * 0.18F);
                            var30 = var33;
                        }
                    } else if (var2.style == 18) {
                        for (int var24 = 0; var24 < 7; var24++) {
                            double var27 = Math.min(1.0, Math.max(0.0, var3 * 3.0 - (double)var24 * 0.07));
                            crystal(var0, var1, var2.start.lerp(var2.end, var27), var5, 0.11, 0.22, 0.53F, 0.42F, 0.3F, var7);
                        }
                    } else if (var2.style == 4) {
                        for (int var23 = 0; var23 < 5; var23++) {
                            double var26 = Math.min(1.0, Math.max(0.0, var3 * 4.0 - (double)var23 * 0.08));
                            crystal(var0, var1, var2.start.lerp(var2.end, var26), var5, 0.13, 0.45, 0.63F, 0.88F, 1.0F, var7);
                        }
                    } else if (var2.style == 10) {
                        Vec3 var22 = basis(var5);
                        double var9 = 0.6 + var3 * 3.0;
                        quad(
                            var0,
                            var1,
                            var2.start.add(var22.scale(var9)),
                            var2.end.add(var22.scale(var9 * 0.3)),
                            var2.end.subtract(var22.scale(var9 * 0.3)),
                            var2.start.subtract(var22.scale(var9)),
                            0.85F,
                            1.0F,
                            0.67F,
                            var7 * 0.16F
                        );
                        tube(var0, var1, var2.start.subtract(var22.scale(var9)), var2.end.add(var22.scale(var9 * 0.3)), 0.018, 1.0F, 1.0F, 0.88F, var7);
                        tube(var0, var1, var2.start.add(var22.scale(var9)), var2.end.subtract(var22.scale(var9 * 0.3)), 0.018, 1.0F, 1.0F, 0.88F, var7);
                    } else {
                        double var8 = var2.style == 2
                            ? 1.35
                            : (var2.style == 5 ? 0.5 : (var2.style == 3 ? 0.16 : (var2.style == 16 ? 0.5 : (var2.style == 10 ? 0.22 : 0.065))));
                        float var10 = var2.style == 3 ? 0.012F : (var2.style == 4 ? 0.52F : (var2.style == 10 ? 1.0F : 0.68F));
                        float var11 = var2.style == 3 ? 0.085F : (var2.style == 10 ? 0.92F : (var2.style != 16 && var2.style != 19 ? 0.86F : 0.86F));
                        float var12 = var2.style == 3 ? 0.04F : (var2.style == 10 ? 0.24F : (var2.style != 16 && var2.style != 19 ? 1.0F : 0.48F));
                        taperedBeam(var0, var1, var2.start, var2.end, var8 * 2.9, var3, var10, var11, var12, var7 * 0.08F);
                        taperedBeam(var0, var1, var2.start, var2.end, var8 * 1.8, var3, var10, var11, var12, var7 * 0.18F);
                        taperedBeam(var0, var1, var2.start, var2.end, var8, var3, var10, var11, var12, var7 * 0.95F);
                        if (var2.style != 3) {
                            taperedBeam(var0, var1, var2.start, var2.end, var8 * 0.38, var3, 1.0F, 1.0F, 1.0F, var7);
                        } else {
                            taperedBeam(var0, var1, var2.start, var2.end, var8 * 1.1, var3, 0.05F, 0.55F, 0.23F, var7 * 0.4F);
                            Vec3 var13 = basis(var5);
                            Vec3 var14 = var5.cross(var13);

                            for (int var15 = 0; var15 < 10; var15++) {
                                double var16 = (double)var15 * 2.39996 + var3 * 5.0;
                                double var18 = ((double)var15 * 0.17 + var3 * 0.8) % 1.0;
                                Vec3 var20 = var2.start.lerp(var2.end, var18);
                                Vec3 var21 = var20.add(var13.scale(Math.cos(var16) * var8 * 2.0)).add(var14.scale(Math.sin(var16) * var8 * 2.0));
                                triangle(
                                    var0,
                                    var1,
                                    var21,
                                    var21.add(var13.scale(0.25)),
                                    var21.add(var5.scale(0.5)).add(var14.scale(0.12)),
                                    0.06F,
                                    0.75F,
                                    0.31F,
                                    var7 * 0.55F
                                );
                            }
                        }

                        sigil(var0, var1, var2.start, var5, var2.style == 2 ? 1.65 : 0.4, var3 * 2.0, var10, var11, var12, var7 * 0.85F);
                        if (var2.style == 2) {
                            orientedRing(var0, var1, var2.end, var5, 0.2 + var3 * 6.0, 0.025, var10, var11, var12, var7 * 0.5F);
                        }

                        for (int var32 = 0; var32 < 12; var32++) {
                            double var34 = (double)var32 * 2.39996;
                            Vec3 var35 = basis(var5);
                            Vec3 var17 = var5.cross(var35);
                            Vec3 var36 = var35.scale(Math.cos(var34)).add(var17.scale(Math.sin(var34)));
                            Vec3 var19 = var2.end.add(var36.scale(var3 * (var2.style == 2 ? 4.0 : 1.5))).add(var5.scale(-var3 * 0.5));
                            crystal(var0, var1, var19, var36, 0.018, 0.1, var10, var11, var12, var7 * 0.75F);
                        }
                    }
                }
            }
        }
    }

    private static Vec3 basis(Vec3 var0) {
        return var0.cross(Math.abs(var0.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0)).normalize();
    }

    private static void taperedBeam(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, float var8, float var9, float var10, float var11
    ) {
        Vec3 var12 = var3.subtract(var2).normalize();
        Vec3 var13 = basis(var12);
        Vec3 var14 = var12.cross(var13);

        for (int var15 = 0; var15 < 8; var15++) {
            for (int var16 = 0; var16 < 24; var16++) {
                double var17 = (double)var15 / 8.0;
                double var19 = (double)(var15 + 1) / 8.0;
                double var21 = (double)var16 * Math.PI / 12.0;
                double var23 = (double)(var16 + 1) * Math.PI / 12.0;
                double var25 = var4 * (0.62 + 0.38 * Math.sin(Math.PI * (0.1 + 0.8 * var17))) * (1.0 + 0.04 * Math.sin(var17 * 30.0 - var6 * 15.0));
                double var27 = var4 * (0.62 + 0.38 * Math.sin(Math.PI * (0.1 + 0.8 * var19))) * (1.0 + 0.04 * Math.sin(var19 * 30.0 - var6 * 15.0));
                Vec3 var29 = var13.scale(Math.cos(var21)).add(var14.scale(Math.sin(var21)));
                Vec3 var30 = var13.scale(Math.cos(var23)).add(var14.scale(Math.sin(var23)));
                Vec3 var31 = var2.lerp(var3, var17);
                Vec3 var32 = var2.lerp(var3, var19);
                quad(
                    var0,
                    var1,
                    var31.add(var29.scale(var25)),
                    var32.add(var29.scale(var27)),
                    var32.add(var30.scale(var27)),
                    var31.add(var30.scale(var25)),
                    var8,
                    var9,
                    var10,
                    var11
                );
            }
        }
    }

    private static void orientedRing(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, float var8, float var9, float var10, float var11
    ) {
        Vec3 var12 = basis(var3);
        Vec3 var13 = var3.cross(var12);

        for (int var14 = 0; var14 < 96; var14++) {
            double var15 = (double)var14 * Math.PI / 48.0;
            double var17 = (double)(var14 + 1) * Math.PI / 48.0;
            Vec3 var19 = var12.scale(Math.cos(var15)).add(var13.scale(Math.sin(var15)));
            Vec3 var20 = var12.scale(Math.cos(var17)).add(var13.scale(Math.sin(var17)));
            quad(
                var0,
                var1,
                var2.add(var19.scale(var4)),
                var2.add(var20.scale(var4)),
                var2.add(var20.scale(var4 + var6)),
                var2.add(var19.scale(var4 + var6)),
                var8,
                var9,
                var10,
                var11
            );
        }
    }

    private static void sigil(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, float var8, float var9, float var10, float var11
    ) {
        orientedRing(var0, var1, var2, var3, var4, 0.014, var8, var9, var10, var11);
        Vec3 var12 = basis(var3);
        Vec3 var13 = var3.cross(var12);

        for (int var14 = 0; var14 < 12; var14++) {
            double var15 = (double)var14 * Math.PI / 6.0 + var6 * 0.2;
            Vec3 var17 = var12.scale(Math.cos(var15)).add(var13.scale(Math.sin(var15)));
            Vec3 var18 = var12.scale(-Math.sin(var15)).add(var13.scale(Math.cos(var15)));
            Vec3 var19 = var2.add(var17.scale(var4 * 0.71));
            tube(var0, var1, var19.add(var18.scale(var4 * 0.04)), var19.add(var17.scale(var4 * 0.1)), 0.009, var8, var9, var10, var11);
            tube(var0, var1, var19.add(var17.scale(var4 * 0.1)), var19.subtract(var18.scale(var4 * 0.04)), 0.009, var8, var9, var10, var11);
            tube(var0, var1, var19.subtract(var18.scale(var4 * 0.04)), var19.add(var17.scale(var4 * 0.02)), 0.006, var8, var9, var10, var11);
        }

        for (int var20 = 0; var20 < 6; var20++) {
            double var21 = (double)var20 * Math.PI / 3.0 + var6 * 0.12;
            double var22 = (double)(var20 + 2) * Math.PI / 3.0 + var6 * 0.12;
            tube(
                var0,
                var1,
                var2.add(var12.scale(Math.cos(var21) * var4 * 0.45)).add(var13.scale(Math.sin(var21) * var4 * 0.45)),
                var2.add(var12.scale(Math.cos(var22) * var4 * 0.45)).add(var13.scale(Math.sin(var22) * var4 * 0.45)),
                0.008,
                var8,
                var9,
                var10,
                var11 * 0.65F
            );
        }
    }

    private static void crystal(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, float var8, float var9, float var10, float var11
    ) {
        var3 = var3.normalize();
        Vec3 var12 = basis(var3);
        Vec3 var13 = var3.cross(var12);
        Vec3 var14 = var2.add(var3.scale(var6));
        Vec3 var15 = var2.subtract(var3.scale(var6 * 0.65));

        for (int var16 = 0; var16 < 6; var16++) {
            double var17 = (double)var16 * Math.PI / 3.0;
            double var19 = (double)(var16 + 1) * Math.PI / 3.0;
            Vec3 var21 = var2.add(var12.scale(Math.cos(var17) * var4)).add(var13.scale(Math.sin(var17) * var4));
            Vec3 var22 = var2.add(var12.scale(Math.cos(var19) * var4)).add(var13.scale(Math.sin(var19) * var4));
            float var23 = 0.66F + (float)(var16 % 3) * 0.15F;
            triangle(var0, var1, var14, var21, var22, var8 * var23, var9 * var23, var10 * var23, var11);
            triangle(var0, var1, var15, var22, var21, var8 * var23, var9 * var23, var10 * var23, var11 * 0.9F);
        }
    }

    private static void tube(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, float var6, float var7, float var8, float var9) {
        Vec3 var10 = var3.subtract(var2).normalize();
        Vec3 var11 = var10.cross(Math.abs(var10.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0)).normalize();
        Vec3 var12 = var10.cross(var11);

        for (int var13 = 0; var13 < 20; var13++) {
            double var14 = (double)var13 * Math.PI / 10.0;
            double var16 = (double)(var13 + 1) * Math.PI / 10.0;
            Vec3 var18 = var11.scale(Math.cos(var14) * var4).add(var12.scale(Math.sin(var14) * var4));
            Vec3 var19 = var11.scale(Math.cos(var16) * var4).add(var12.scale(Math.sin(var16) * var4));
            quad(var0, var1, var2.add(var18), var3.add(var18), var3.add(var19), var2.add(var19), var6, var7, var8, var9);
        }
    }

    private static void fracture(VertexConsumer var0, Matrix4f var1, ArcanaClient.Fracture var2, double var3, double var5) {
        BreakerFx.fracture(var0, var1, var2, var3, var5);
    }

    private static void prisms(VertexConsumer var0, Matrix4f var1, ArcanaClient.Fracture var2, double var3) {
        BreakerFx.prisms(var0, var1, var2, var3);
    }

    private static void effect(VertexConsumer var0, Matrix4f var1, ArcanaClient.Effect var2, double var3, double var5) {
        if (!SpellFx.effect(var0, var1, var2.style(), var2.center(), var2.strength(), var3, var5)) {
            Vec3 var7 = var2.center;
            int var8 = var2.strength;
            if (var2.style == 20) {
                float var9 = (float)Math.max(0.0, 0.35 * (1.0 - var3 / 0.65));

                for (int var10 = 0; var10 < 4; var10++) {
                    double var11 = (double)var10 * 2.39996 + var5;
                    crystal(
                        var0,
                        var1,
                        var7.add(Math.cos(var11) * 0.24, var3 * 0.2, Math.sin(var11) * 0.24),
                        new Vec3(0.0, 1.0, 0.0),
                        0.009,
                        0.03,
                        0.65F,
                        0.85F,
                        1.0F,
                        var9
                    );
                }
            } else if (var2.style == 11) {
                float var21 = (float)Math.min(1.0, Math.min(var3 * 3.0, (15.0 - var3) * 2.0));

                for (int var29 = 0; var29 < 24 * var8; var29++) {
                    double var38 = (double)var29 * 2.39996;
                    double var13 = Math.sqrt((double)(var29 + 1) / (24.0 * (double)var8)) * (double)(2 + var8);
                    Vec3 var15 = var7.add(Math.cos(var38) * var13, 0.02, Math.sin(var38) * var13);
                    Vec3 var16 = var15.add(0.0, 0.25 + (double)(var29 % 4) * 0.07, 0.0);
                    tube(var0, var1, var15, var16, 0.012, 0.1F, 0.6F, 0.2F, var21);

                    for (int var17 = 0; var17 < 5; var17++) {
                        double var18 = (double)var17 * Math.PI * 2.0 / 5.0;
                        Vec3 var20 = var16.add(Math.cos(var18) * 0.09, 0.015, Math.sin(var18) * 0.09);
                        crystal(
                            var0, var1, var20, new Vec3(Math.cos(var18), 0.2, Math.sin(var18)), 0.035, 0.07, var29 % 2 == 0 ? 1.0F : 0.68F, 0.55F, 1.0F, var21
                        );
                    }
                }
            } else if (var2.style != 14 && var2.style != 21) {
                if (var2.style == 13) {
                    float var23 = (float)Math.max(0.0, 1.0 - var3 / 1.3);

                    for (int var31 = 0; var31 < 12 + var8 * 3; var31++) {
                        double var40 = (double)var31 * 2.39996;
                        double var45 = Math.sqrt((double)(var31 + 1) / (12.0 + (double)(var8 * 3))) * (double)(1 + var8);
                        Vec3 var49 = var7.add(Math.cos(var40) * var45, -0.4, Math.sin(var40) * var45);
                        tube(
                            var0,
                            var1,
                            var49,
                            var49.add(Math.sin(var5 * 6.0 + (double)var31) * 0.15, 1.0 + var3 * 2.0, Math.cos(var5 * 6.0 + (double)var31) * 0.15),
                            0.1,
                            1.0F,
                            0.22F,
                            0.02F,
                            var23
                        );
                    }
                } else if (var2.style == 17) {
                    float var24 = (float)Math.max(0.0, 1.0 - var3 / 1.3);

                    for (int var32 = 0; var32 < 12 + var8 * 3; var32++) {
                        double var41 = var5 * 3.0 + (double)var32 * 2.39996;
                        Vec3 var46 = var7.add(Math.cos(var41) * (1.0 + var3), 0.2 + (double)(var32 % 5) * 0.25, Math.sin(var41) * (1.0 + var3));
                        crystal(var0, var1, var46, new Vec3(-Math.sin(var41), 0.3, Math.cos(var41)), 0.045, 0.25, 0.83F, 0.9F, 1.0F, var24);
                    }
                } else if (var2.style == 15) {
                    float var25 = (float)Math.max(0.0, 1.0 - var3 / 1.3);

                    for (int var33 = 0; var33 < 8; var33++) {
                        double var42 = (double)var33 * Math.PI / 4.0;
                        Vec3 var47 = var7.add(Math.cos(var42) * (0.5 + var3 * 3.0), Math.sin((double)var33 * 2.1) * 0.5, Math.sin(var42) * (0.5 + var3 * 3.0));
                        tube(var0, var1, var7, var47, 0.025, 0.6F, 0.8F, 1.0F, var25);
                    }
                } else if (var2.style == 6) {
                    float var26 = (float)Math.max(0.0, 1.0 - var3 / 1.3);
                    double var34 = Math.sin(Math.min(1.0, var3) * Math.PI) * 2.0;

                    for (int var12 = 0; var12 < 8 + var8; var12++) {
                        double var48 = (double)var12 * Math.PI * 2.0 / (double)(8 + var8);
                        Vec3 var50 = var7.add(Math.cos(var48) * (1.0 + (double)var8 * 0.5), 0.0, Math.sin(var48) * (1.0 + (double)var8 * 0.5));
                        crystal(
                            var0,
                            var1,
                            var50.add(0.0, var34 * 0.5, 0.0),
                            new Vec3(0.12 * Math.sin((double)var12), 1.0, 0.12 * Math.cos((double)var12)),
                            0.26,
                            0.2 + var34 * (1.0 + (double)(var12 % 3) * 0.25),
                            0.52F,
                            0.4F,
                            0.29F,
                            var26
                        );
                    }
                } else if (var2.style == 8 || var2.style == 9) {
                    float var28 = (float)Math.max(0.0, 1.0 - var3 / (double)(var2.style == 9 ? 10 : 6));

                    for (int var36 = 0; var36 < 1; var36++) {
                        sigil(
                            var0,
                            var1,
                            var7.add(0.0, 0.7, 0.0),
                            new Vec3(0.0, 1.0, 0.0),
                            0.62,
                            var5 * (double)(var36 % 2 == 0 ? 1 : -1),
                            var2.style == 9 ? 1.0F : 0.75F,
                            var2.style == 9 ? 0.73F : 0.45F,
                            var2.style == 9 ? 0.1F : 1.0F,
                            var28
                        );
                    }

                    for (int var37 = 0; var37 < 16; var37++) {
                        double var44 = (double)var37 * 2.39996 + var5;
                        crystal(
                            var0,
                            var1,
                            var7.add(Math.cos(var44) * 0.68, (double)(var37 % 5) * 0.35, Math.sin(var44) * 0.68),
                            new Vec3(0.0, 1.0, 0.0),
                            0.025,
                            0.07,
                            var2.style == 9 ? 1.0F : 0.75F,
                            var2.style == 9 ? 0.73F : 0.45F,
                            var2.style == 9 ? 0.1F : 1.0F,
                            var28
                        );
                    }
                } else {
                    float var27 = (float)Math.max(0.0, 1.0 - var3 / 1.3);

                    for (int var35 = 0; var35 < 18; var35++) {
                        double var43 = (double)var35 * 2.39996 + var5;
                        crystal(
                            var0,
                            var1,
                            var7.add(Math.cos(var43) * (0.4 + var3), var3 * (0.4 + (double)(var35 % 4) * 0.25), Math.sin(var43) * (0.4 + var3)),
                            new Vec3(0.0, 1.0, 0.0),
                            0.018,
                            0.06,
                            0.8F,
                            0.94F,
                            1.0F,
                            var27
                        );
                    }
                }
            } else {
                float var22 = (float)Math.max(0.0, 1.0 - var3 / 4.0);
                boolean var30 = var2.style == 21;

                for (int var39 = 0; var39 < 6; var39++) {
                    spiralColor(
                        var0,
                        var1,
                        var7,
                        3.0 + (double)var8 * 0.35,
                        1.0 + (double)var8 * 0.18,
                        var3 * 5.0 + (double)var39 * Math.PI / 3.0,
                        var22 * 0.65F,
                        var30 ? 1.0F : 0.65F,
                        var30 ? 0.28F : 0.84F,
                        var30 ? 0.03F : 1.0F
                    );
                }
            }
        }
    }

    private static void spiral(VertexConsumer var0, Matrix4f var1, Vec3 var2, double var3, double var5, double var7, float var9) {
        if (!SpellFx.aura(var0, var1, var2, var3, var5, var7, var9)) {
            for (int var10 = 0; var10 < 48; var10++) {
                double var11 = (double)var10 / 48.0;
                double var13 = (double)(var10 + 1) / 48.0;
                double var15 = var7 + var11 * Math.PI * 4.0;
                double var17 = var7 + var13 * Math.PI * 4.0;
                Vec3 var19 = var2.add(Math.cos(var15) * var5, var3 * var11, Math.sin(var15) * var5);
                Vec3 var20 = var2.add(Math.cos(var17) * var5, var3 * var13, Math.sin(var17) * var5);
                float var21 = (float)Math.sin(Math.PI * var11);
                tube(var0, var1, var19, var20, 0.013, 0.64F, 0.86F, 1.0F, var9 * var21);
                tube(var0, var1, var19, var20, 0.06, 0.5F, 0.76F, 1.0F, var9 * var21 * 0.12F);
            }
        }
    }

    private static void spiralColor(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, double var3, double var5, double var7, float var9, float var10, float var11, float var12
    ) {
        for (int var13 = 0; var13 < 48; var13++) {
            double var14 = (double)var13 / 48.0;
            double var16 = (double)(var13 + 1) / 48.0;
            double var18 = var7 + var14 * Math.PI * 4.0;
            double var20 = var7 + var16 * Math.PI * 4.0;
            Vec3 var22 = var2.add(Math.cos(var18) * var5 * (0.2 + 0.8 * var14), var3 * var14, Math.sin(var18) * var5 * (0.2 + 0.8 * var14));
            Vec3 var23 = var2.add(Math.cos(var20) * var5 * (0.2 + 0.8 * var16), var3 * var16, Math.sin(var20) * var5 * (0.2 + 0.8 * var16));
            float var24 = (float)Math.sin(Math.PI * var14);
            quad(var0, var1, var22, var23, var23.add(0.0, 0.22, 0.0), var22.add(0.0, 0.22, 0.0), var10, var11, var12, var9 * var24 * 0.32F);
            tube(var0, var1, var22, var23, 0.012, var10, var11, var12, var9 * var24 * 0.65F);
        }
    }

    private static void cube(VertexConsumer var0, Matrix4f var1, Vec3 var2, double var3, float var5, float var6, float var7, float var8) {
        Vec3 var9 = var2.add(-var3, -var3, -var3);
        Vec3 var10 = var2.add(var3, -var3, -var3);
        Vec3 var11 = var2.add(var3, -var3, var3);
        Vec3 var12 = var2.add(-var3, -var3, var3);
        Vec3 var13 = var9.add(0.0, var3 * 2.0, 0.0);
        Vec3 var14 = var10.add(0.0, var3 * 2.0, 0.0);
        Vec3 var15 = var11.add(0.0, var3 * 2.0, 0.0);
        Vec3 var16 = var12.add(0.0, var3 * 2.0, 0.0);
        quad(var0, var1, var9, var10, var14, var13, var5, var6, var7, var8);
        quad(var0, var1, var10, var11, var15, var14, var5, var6, var7, var8);
        quad(var0, var1, var11, var12, var16, var15, var5, var6, var7, var8);
        quad(var0, var1, var12, var9, var13, var16, var5, var6, var7, var8);
        quad(var0, var1, var13, var14, var15, var16, var5, var6, var7, var8);
        quad(var0, var1, var9, var12, var11, var10, var5, var6, var7, var8);
    }

    private static void ring(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, double var3, double var5, double var7, float var9, float var10, float var11, float var12
    ) {
        for (int var13 = 0; var13 < 64; var13++) {
            double var14 = (double)var13 * Math.PI / 32.0 + var5;
            double var16 = (double)(var13 + 1) * Math.PI / 32.0 + var5;
            Vec3 var18 = var2.add(Math.cos(var14) * var3, 0.0, Math.sin(var14) * var3);
            Vec3 var19 = var2.add(Math.cos(var16) * var3, 0.0, Math.sin(var16) * var3);
            Vec3 var20 = var2.add(Math.cos(var14) * (var3 + var7), 0.0, Math.sin(var14) * (var3 + var7));
            Vec3 var21 = var2.add(Math.cos(var16) * (var3 + var7), 0.0, Math.sin(var16) * (var3 + var7));
            quad(var0, var1, var18, var19, var21, var20, var9, var10, var11, var12);
        }
    }

    private static void triangle(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, float var5, float var6, float var7, float var8) {
        quad(var0, var1, var2, var3, var4, var4, var5, var6, var7, var8);
    }

    private static void quad(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, float var6, float var7, float var8, float var9) {
        vertex(var0, var1, var2, var6, var7, var8, var9);
        vertex(var0, var1, var3, var6, var7, var8, var9);
        vertex(var0, var1, var4, var6, var7, var8, var9);
        vertex(var0, var1, var5, var6, var7, var8, var9);
    }

    private static void vertex(VertexConsumer var0, Matrix4f var1, Vec3 var2, float var3, float var4, float var5, float var6) {
        Vec3 var7 = var2.subtract(drawCamera);
        var0.addVertex(var1, (float)var7.x, (float)var7.y, (float)var7.z).setColor(var3, var4, var5, var6);
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

    public static Vec3 camera() {
        return drawCamera;
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
