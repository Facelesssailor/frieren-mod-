package dev.pete.frierenarcana.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class CastCircles {
    private static final List<CastCircles.Active> ACTIVE = new ArrayList<>();

    private CastCircles() {
    }

    private static CastCircles.Style style(String var0) {
        switch (var0) {
            case "zoltraak":
            case "zoltraak_barrage":
                return new CastCircles.Style(0, 1.15, 0.86F, 0.5F, 1.0F, 6, 2, 1);
            case "zoltraak_heavy":
                return new CastCircles.Style(0, 3.3, 1.0F, 0.86F, 0.52F, 8, 3, 2);
            case "nephtear":
                return new CastCircles.Style(0, 1.35, 0.6F, 0.92F, 1.0F, 6, 2, 0);
            case "reamstroha":
                return new CastCircles.Style(0, 1.3, 0.4F, 0.72F, 1.0F, 5, 2, 1);
            case "balgrant":
                return new CastCircles.Style(1, 2.5, 0.92F, 0.72F, 0.42F, 4, 1, 2);
            case "sorganeil":
                return new CastCircles.Style(1, 2.1, 0.82F, 0.92F, 0.62F, 5, 2, 1);
            case "vollzanbel":
                return new CastCircles.Style(0, 2.3, 1.0F, 0.55F, 0.25F, 8, 3, 2);
            case "judradjim":
                return new CastCircles.Style(0, 1.5, 0.92F, 0.96F, 0.42F, 6, 2, 1);
            case "waldgose":
                return new CastCircles.Style(1, 2.3, 0.62F, 1.0F, 0.82F, 6, 2, 1);
            case "daosdorg":
                return new CastCircles.Style(0, 2.1, 1.0F, 0.62F, 0.32F, 7, 3, 2);
            case "catastrovia":
                return new CastCircles.Style(2, 3.2, 0.72F, 0.52F, 1.0F, 8, 3, 2);
            case "goddess_healing":
                return new CastCircles.Style(1, 2.3, 1.0F, 0.96F, 0.72F, 6, 2, 2);
            case "goddess_cleansing":
                return new CastCircles.Style(1, 2.1, 0.72F, 1.0F, 0.92F, 6, 2, 1);
            case "goddess_three_spears":
                return new CastCircles.Style(0, 1.9, 1.0F, 0.92F, 0.52F, 3, 1, 2);
            case "golden_transmutation":
                return new CastCircles.Style(1, 2.8, 1.0F, 0.86F, 0.32F, 8, 3, 2);
            case "dragate":
                return new CastCircles.Style(0, 1.25, 0.82F, 0.76F, 0.66F, 4, 1, 1);
            case "reelseiden":
                return new CastCircles.Style(0, 1.15, 0.82F, 1.0F, 1.0F, 5, 2, 0);
            case "examination_barrier":
            case "defensive_barrier":
                return new CastCircles.Style(1, 2.7, 0.62F, 1.0F, 0.8F, 6, 2, 2);
            case "jubelade":
                return new CastCircles.Style(0, 1.5, 1.0F, 0.72F, 0.82F, 6, 2, 1);
            default:
                return null;
        }
    }

    public static boolean receive(CompoundTag var0) {
        if (!"circle".equals(var0.getString("kind"))) {
            return false;
        } else {
            String var1 = var0.getString("spell");
            if (style(var1) == null) {
                return true;
            } else {
                UUID var2 = var0.getUUID("player");
                long var3 = System.nanoTime();
                long var5 = var3 + (long)var0.getInt("ticks") * 50000000L;

                for (CastCircles.Active var8 : ACTIVE) {
                    if (var8.player.equals(var2) && var8.spell.equals(var1) && var8.end > var3) {
                        var8.end = var5;
                        return true;
                    }
                }

                ACTIVE.add(new CastCircles.Active(var2, var1, var3, var5));
                return true;
            }
        }
    }

    private static double ease(double var0) {
        var0 = Math.max(0.0, Math.min(1.0, var0));
        return 1.0 - Math.pow(1.0 - var0, 3.0);
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent var0) {
        if (var0.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS && !ACTIVE.isEmpty()) {
            Minecraft var1 = Minecraft.getInstance();
            ClientLevel var2 = var1.level;
            if (var2 != null && var1.player != null) {
                long var3 = System.nanoTime();
                ACTIVE.removeIf(var2x -> var2x.end < var3);
                if (!ACTIVE.isEmpty() && !ArcanaCinematic.active()) {
                    float var5 = var0.getPartialTick().getGameTimeDeltaPartialTick(false);
                    Vec3 var6 = var0.getCamera().getPosition();
                    BufferSource var7 = var1.renderBuffers().bufferSource();
                    SpellCircleFx.Ctx var8 = new SpellCircleFx.Ctx();
                    var8.camX = var6.x;
                    var8.camY = var6.y;
                    var8.camZ = var6.z;
                    var8.m = var0.getPoseStack().last().pose();
                    var8.vc = var7.getBuffer(ArcanaRenderTypes.MAGIC);
                    ArcanaShaders.prepare((double)((float)var2.getGameTime() + var5) / 20.0, var6);
                    boolean var9 = false;

                    for (CastCircles.Active var11 : ACTIVE) {
                        Player var12 = var2.getPlayerByUUID(var11.player);
                        CastCircles.Style var13 = style(var11.spell);
                        if (var12 != null && var13 != null) {
                            double var14 = (double)(var3 - var11.start) / 1.0E9;
                            double var16 = (double)(var11.end - var3) / 1.0E9;
                            double var18 = ease(var14 / 0.35);
                            double var20 = Math.min(1.0, var16 / 0.4);
                            if (!(var18 <= 0.02) && !(var20 <= 0.0)) {
                                double var22 = var13.radius() * var18;
                                Vec3 var24 = var12.getViewVector(var5);
                                Vec3 var25 = var12.getPosition(var5);
                                if (var13.mode() == 0) {
                                    Vec3 var26 = var12.getEyePosition(var5);
                                    double var27 = 1.5 + var13.radius() * 0.5;
                                    var8.cx = var26.x + var24.x * var27;
                                    var8.cy = var26.y - 0.2 + var24.y * var27;
                                    var8.cz = var26.z + var24.z * var27;
                                    double var29 = -var24.z;
                                    double var31 = var24.x;
                                    double var33 = Math.sqrt(var29 * var29 + var31 * var31);
                                    if (var33 < 1.0E-4) {
                                        var29 = 1.0;
                                        var31 = 0.0;
                                        var33 = 1.0;
                                    }

                                    var29 /= var33;
                                    var31 /= var33;
                                    double var35 = var24.y * var31;
                                    double var37 = var24.z * var29 - var24.x * var31;
                                    double var39 = -var24.y * var29;
                                    double var41 = Math.sqrt(var35 * var35 + var37 * var37 + var39 * var39);
                                    var35 /= var41;
                                    var37 /= var41;
                                    var39 /= var41;
                                    if (var37 < 0.0) {
                                        var35 = -var35;
                                        var37 = -var37;
                                        var39 = -var39;
                                    }

                                    var8.ux = var29;
                                    var8.uy = 0.0;
                                    var8.uz = var31;
                                    var8.vx = var35;
                                    var8.vy = var37;
                                    var8.vz = var39;
                                } else {
                                    var8.cx = var25.x;
                                    var8.cy = var25.y + (var13.mode() == 1 ? 0.06 : (double)var12.getEyeHeight() + 1.2);
                                    var8.cz = var25.z;
                                    var8.ux = 1.0;
                                    var8.uy = 0.0;
                                    var8.uz = 0.0;
                                    var8.vx = 0.0;
                                    var8.vy = 0.0;
                                    var8.vz = 1.0;
                                }

                                double var44 = var14 + (double)(var11.start % 1000L) * 0.001;
                                float var28 = (float)((0.78 + 0.22 * Math.sin(var14 * 5.0)) * var20);
                                layers(var8, var22, var44, var13, var28);
                                var9 = true;
                            }
                        }
                    }

                    var7.endBatch(ArcanaRenderTypes.MAGIC);
                }
            } else {
                ACTIVE.clear();
            }
        }
    }

    private static void layers(SpellCircleFx.Ctx var0, double var1, double var3, CastCircles.Style var5, float var6) {
        float var7 = var5.r();
        float var8 = var5.g();
        float var9 = var5.b();
        var0.r = var7 * 0.5F;
        var0.g = var8 * 0.5F;
        var0.b = var9 * 0.5F;
        var0.a = 0.1F * var6;
        var0.disc(var1 * 0.97, 40);
        var0.r = Math.min(1.0F, var7 + 0.2F);
        var0.g = Math.min(1.0F, var8 + 0.2F);
        var0.b = Math.min(1.0F, var9 + 0.2F);
        var0.a = 0.92F * var6;
        var0.ring(var1, 0.045 + 0.02 * var1);
        var0.ring(var1 * 0.93, 0.02);
        var0.r = var7;
        var0.g = var8;
        var0.b = var9;
        var0.a = 0.8F * var6;
        var0.dashes(var1 * 0.85, 0.035, 30, var3 * 0.4, 0.55);
        var0.ring(var1 * 0.72, 0.022);
        var0.runes(var1 * 0.78, 18, -var3 * 0.3, 0.05 * var1, 0.016);
        var0.ticks(var1 * 0.68, var1 * 0.72, 60, var3 * 0.12, 0.01);
        var0.a = 0.9F * var6;
        var0.star(var1 * 0.62, var5.points(), var5.skip(), var3 * 0.5, 0.028);
        var0.polygon(var1 * 0.62, var5.points(), var3 * 0.5, 0.016);
        if (var5.ring() >= 1) {
            var0.ring(var1 * 0.34, 0.03);
            var0.polygon(var1 * 0.34, Math.max(3, var5.points() / 2), -var3 * 0.8, 0.022);
        }

        if (var5.ring() >= 2) {
            var0.ring(var1 * 0.46, 0.018);
            var0.runes(var1 * 0.4, 10, var3 * 0.5, 0.035 * var1, 0.012);
        }

        var0.ring(var1 * 0.14, 0.02);
        var0.r = Math.min(1.0F, var7 + 0.3F);
        var0.g = Math.min(1.0F, var8 + 0.3F);
        var0.b = Math.min(1.0F, var9 + 0.3F);
        var0.a = 0.35F * var6;
        var0.disc(var1 * 0.08, 16);
    }

    private static final class Active {
        final UUID player;
        final String spell;
        final long start;
        long end;

        Active(UUID var1, String var2, long var3, long var5) {
            this.player = var1;
            this.spell = var2;
            this.start = var3;
            this.end = var5;
        }
    }

    private static record Style(int mode, double radius, float r, float g, float b, int points, int skip, int ring) {
    }
}
