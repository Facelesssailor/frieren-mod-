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

    private static CastCircles.Style st(int var0, double var1, CircleArt.Design var3, float var4, float var5, float var6, float var7, float var8, float var9) {
        return new CastCircles.Style(var0, var1, new CircleArt.Look(var3, var4, var5, var6, var7, var8, var9));
    }

    public static CastCircles.Style style(String var0) {
        switch (var0) {
            case "zoltraak_heavy":
                return st(0, 2.6, CircleArt.Design.HEAVY, 1.0F, 0.97F, 0.92F, 0.95F, 0.78F, 0.5F);
            case "nephtear":
                return st(0, 1.15, CircleArt.Design.ICE, 0.92F, 0.98F, 1.0F, 0.45F, 0.82F, 1.0F);
            case "reamstroha":
                return st(0, 1.2, CircleArt.Design.WATER, 0.9F, 0.96F, 1.0F, 0.25F, 0.55F, 1.0F);
            case "balgrant":
                return st(1, 2.5, CircleArt.Design.EARTH, 1.0F, 0.93F, 0.8F, 0.82F, 0.58F, 0.3F);
            case "sorganeil":
                return st(0, 1.1, CircleArt.Design.BIND, 1.0F, 1.0F, 0.86F, 0.82F, 0.88F, 0.42F);
            case "vollzanbel":
                return st(1, 2.5, CircleArt.Design.FIRE, 1.0F, 0.93F, 0.8F, 1.0F, 0.45F, 0.12F);
            case "judradjim":
                return st(0, 1.45, CircleArt.Design.LIGHTNING, 0.96F, 0.92F, 1.0F, 0.58F, 0.32F, 1.0F);
            case "waldgose":
                return st(1, 2.5, CircleArt.Design.WIND, 0.93F, 1.0F, 0.96F, 0.42F, 0.95F, 0.72F);
            case "daosdorg":
                return st(1, 2.5, CircleArt.Design.FIRE, 1.0F, 0.88F, 0.75F, 1.0F, 0.28F, 0.08F);
            case "catastrovia":
                return st(2, 3.2, CircleArt.Design.ARROWS, 1.0F, 0.98F, 0.9F, 1.0F, 0.82F, 0.42F);
            case "goddess_healing":
                return st(1, 2.3, CircleArt.Design.HOLY, 1.0F, 0.98F, 0.86F, 1.0F, 0.84F, 0.45F);
            case "goddess_cleansing":
                return st(1, 2.2, CircleArt.Design.HOLY, 0.92F, 1.0F, 0.98F, 0.55F, 0.95F, 0.9F);
            case "goddess_three_spears":
                return st(0, 1.45, CircleArt.Design.HOLY, 1.0F, 0.98F, 0.86F, 1.0F, 0.84F, 0.45F);
            case "golden_transmutation":
                return st(1, 2.6, CircleArt.Design.GOLD, 1.0F, 0.95F, 0.75F, 1.0F, 0.78F, 0.25F);
            case "dragate":
                return st(0, 1.05, CircleArt.Design.EARTH, 1.0F, 0.95F, 0.86F, 0.75F, 0.62F, 0.45F);
            case "reelseiden":
                return st(0, 0.95, CircleArt.Design.CUT, 0.97F, 0.97F, 1.0F, 0.7F, 0.75F, 1.0F);
            case "jubelade":
                return st(0, 1.25, CircleArt.Design.PETAL, 1.0F, 0.95F, 0.97F, 1.0F, 0.55F, 0.72F);
            case "examination_barrier":
                return st(1, 2.7, CircleArt.Design.BARRIER, 0.9F, 1.0F, 0.94F, 0.35F, 1.0F, 0.55F);
            case "defensive_barrier":
                return st(1, 2.7, CircleArt.Design.BARRIER, 0.92F, 0.97F, 1.0F, 0.45F, 0.75F, 1.0F);
            case "flower_field":
                return st(1, 2.4, CircleArt.Design.FLOWER, 1.0F, 0.97F, 0.97F, 1.0F, 0.7F, 0.82F);
            case "jilwer":
                return st(1, 1.2, CircleArt.Design.SPEED, 0.94F, 1.0F, 0.96F, 0.6F, 1.0F, 0.8F);
            default:
                return null;
        }
    }

    public static boolean receive(CompoundTag var0) {
        if ("blackhole".equals(var0.getString("kind"))) {
            BlackHoleCinema.start(var0);
            return true;
        } else if ("barrage".equals(var0.getString("kind"))) {
            BarrageClient.state(var0.getBoolean("active"));
            return true;
        } else if (!"circle".equals(var0.getString("kind"))) {
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

                    for (CastCircles.Active var10 : ACTIVE) {
                        Player var11 = var2.getPlayerByUUID(var10.player);
                        if (var11 != null) {
                            double var12 = (double)(var3 - var10.start) / 1.0E9;
                            double var14 = (double)(var10.end - var3) / 1.0E9;
                            draw(
                                var8,
                                var10.spell,
                                var11.getEyePosition(var5),
                                var11.getViewVector(var5),
                                var11.getPosition(var5),
                                (double)var11.getEyeHeight(),
                                var12,
                                var14,
                                (int)(var10.player.getLeastSignificantBits() & 65535L)
                            );
                        }
                    }

                    var7.endBatch(ArcanaRenderTypes.MAGIC);
                }
            } else {
                ACTIVE.clear();
            }
        }
    }

    public static void draw(SpellCircleFx.Ctx var0, String var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, double var7, double var9, int var11) {
        CastCircles.Style var12 = style(var1);
        if (var12 != null) {
            double var13 = Math.min(1.0, var7 / 0.45);
            float var15 = (float)Math.max(0.0, Math.min(1.0, var9 / 0.35));
            if (!(var15 <= 0.0F)) {
                double var16 = var12.radius();
                if (var12.mode() == 0) {
                    double var18 = 0.9 + 0.35 * var16;
                    var0.cx = var2.x + var3.x * var18;
                    var0.cy = var2.y - 0.25 + var3.y * var18;
                    var0.cz = var2.z + var3.z * var18;
                    double var20 = -var3.z;
                    double var22 = var3.x;
                    double var24 = Math.sqrt(var20 * var20 + var22 * var22);
                    if (var24 < 1.0E-4) {
                        var20 = 1.0;
                        var22 = 0.0;
                        var24 = 1.0;
                    }

                    var20 /= var24;
                    var22 /= var24;
                    double var26 = var3.y * var22;
                    double var28 = var3.z * var20 - var3.x * var22;
                    double var30 = -var3.y * var20;
                    double var32 = Math.sqrt(var26 * var26 + var28 * var28 + var30 * var30);
                    var26 /= var32;
                    var28 /= var32;
                    var30 /= var32;
                    if (var28 < 0.0) {
                        var26 = -var26;
                        var28 = -var28;
                        var30 = -var30;
                    }

                    var0.ux = var20;
                    var0.uy = 0.0;
                    var0.uz = var22;
                    var0.vx = var26;
                    var0.vy = var28;
                    var0.vz = var30;
                } else {
                    var0.cx = var4.x;
                    var0.cy = var4.y + (var12.mode() == 1 ? 0.06 : var5 + 1.4);
                    var0.cz = var4.z;
                    var0.ux = 1.0;
                    var0.uy = 0.0;
                    var0.uz = 0.0;
                    var0.vx = 0.0;
                    var0.vy = 0.0;
                    var0.vz = 1.0;
                }

                var0.fade = 1.0F;
                CircleArt.draw(var0, var12.look(), var16, var7 + (double)(var11 % 97) * 0.13, var13, var15, var11 + var1.hashCode());
                Vec3 var34 = new Vec3(var0.cx, var0.cy, var0.cz);
                CircleArt.Look var19 = var12.look();
                if (var12.mode() == 0) {
                    PixelFx.sprite(var34, var16 * 2.6, 4, var19.gr(), var19.gg(), var19.gb(), 0.22F * var15 * (float)var13);
                } else {
                    PixelFx.flat(
                        var34.add(0.0, 0.01, 0.0),
                        new Vec3(0.0, 1.0, 0.0),
                        var16 * 2.6,
                        4,
                        0.0,
                        var19.gr(),
                        var19.gg(),
                        var19.gb(),
                        0.3F * var15 * (float)var13
                    );
                }
            }
        }
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

    public static record Style(int mode, double radius, CircleArt.Look look) {
    }
}
