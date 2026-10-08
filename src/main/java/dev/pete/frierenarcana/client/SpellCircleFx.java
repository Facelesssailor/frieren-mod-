package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.joml.Matrix4f;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class SpellCircleFx {
    static final double CHARGE_SECONDS = 10.0;
    private static long groundFor = -1L;
    private static double groundY;
    private static final double[][] TV = new double[16][4];
    private static final int[][] TE;
    private static final int[][] TF;

    private SpellCircleFx() {
    }

    private static double ease(double var0) {
        var0 = Math.max(0.0, Math.min(1.0, var0));
        return 1.0 - Math.pow(1.0 - var0, 3.0);
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent var0) {
        if (var0.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
            if (ArcanaCinematic.breakerActive()) {
                Minecraft var1 = Minecraft.getInstance();
                LocalPlayer var2 = var1.player;
                ClientLevel var3 = var1.level;
                if (var2 != null && var3 != null) {
                    float var4 = var0.getPartialTick().getGameTimeDeltaPartialTick(false);
                    double var5 = (double)(System.nanoTime() - ArcanaCinematic.startedNanos()) / 1.0E9;
                    double var7 = ease(var5 / 3.0);
                    if (!(var7 <= 0.01)) {
                        Vec3 var9 = var0.getCamera().getPosition();
                        Vec3 var10 = var2.getPosition(var4);
                        if (groundFor != ArcanaCinematic.startedNanos()) {
                            groundFor = ArcanaCinematic.startedNanos();
                            groundY = ground(var3, var10, var2);
                        }

                        SpellCircleFx.Ctx var11 = new SpellCircleFx.Ctx();
                        var11.camX = var9.x;
                        var11.camY = var9.y;
                        var11.camZ = var9.z;
                        var11.m = var0.getPoseStack().last().pose();
                        BufferSource var12 = var1.renderBuffers().bufferSource();
                        var11.vc = var12.getBuffer(ArcanaRenderTypes.MAGIC);
                        ArcanaShaders.prepare((double)((float)var3.getGameTime() + var4) / 20.0, var9);
                        double var15 = ArcanaCinematic.release != 0L ? 1.0 : Math.min(1.0, var5 / 10.0);
                        float var17 = (float)(0.8 + 0.2 * Math.sin(var5 * 4.0));
                        double var18 = ArcanaCinematic.release == 0L ? 0.0 : (double)(System.nanoTime() - ArcanaCinematic.release) / 1.0E9;
                        var11.fade = (float)Math.max(0.0, 1.0 - var18 / 3.5);
                        if ((double)var11.fade > 0.01) {
                            PixelFx.flat(
                                new Vec3(var10.x, groundY + 0.07, var10.z),
                                new Vec3(0.0, 1.0, 0.0),
                                4.0 + 3.0 * var15,
                                4,
                                0.0,
                                0.4F,
                                1.0F,
                                0.6F,
                                0.55F * var11.fade * var17
                            );
                        }

                        var11.fade = (float)Math.max(0.0, 1.0 - var18 / 4.0);
                        extras(var11, var10, var9, var5, var15, var17);
                        var11.fade = 1.0F;
                        scene(var11, var10, var9, var5);
                        var12.endBatch(ArcanaRenderTypes.MAGIC);
                    }
                }
            }
        }
    }

    static double ground(ClientLevel var0, Vec3 var1, Entity var2) {
        BlockHitResult var3 = var0.clip(new ClipContext(var1.add(0.0, 0.5, 0.0), var1.add(0.0, -12.0, 0.0), Block.VISUAL, Fluid.NONE, var2));
        return var3.getType() == Type.MISS ? var1.y : var3.getLocation().y;
    }

    static void groundCircle(SpellCircleFx.Ctx var0, Vec3 var1, double var2, double var4, double var6, float var8) {
        var0.cx = var1.x;
        var0.cy = var1.y;
        var0.cz = var1.z;
        var0.ux = 1.0;
        var0.uy = 0.0;
        var0.uz = 0.0;
        var0.vx = 0.0;
        var0.vy = 0.0;
        var0.vz = 1.0;
        double var9 = 3.6 * var4;
        var0.glow = 0.0;
        var0.r = 0.15F;
        var0.g = 0.75F;
        var0.b = 0.45F;
        var0.a = 0.07F * var8;
        var0.disc(var9 * 1.03, 72);
        var0.r = 0.35F;
        var0.g = 1.0F;
        var0.b = 0.6F;
        var0.a = 0.05F + 0.1F * (float)var6;
        var0.disc(var9 * 0.24, 40);
        var0.glow = 4.5;
        var0.r = 0.62F;
        var0.g = 1.0F;
        var0.b = 0.78F;
        var0.a = 0.95F * var8;
        var0.ring(var9, 0.05 * var4 + 0.012);
        var0.ring(var9 * 0.95, 0.02);
        var0.a = 0.8F * var8;
        var0.dashes(var9 * 0.885, 0.04, 48, var2 * 0.25, 0.55);
        var0.r = 0.78F;
        var0.g = 1.0F;
        var0.b = 0.88F;
        var0.a = 0.88F * var8;
        var0.ring(var9 * 0.8, 0.022);
        var0.ring(var9 * 0.68, 0.022);
        var0.runes(var9 * 0.74, 32, -var2 * 0.18, 0.042 * var9, 0.016);
        var0.ticks(var9 * 0.68, var9 * 0.715, 96, var2 * 0.09, 0.009);
        var0.r = 0.5F;
        var0.g = 1.0F;
        var0.b = 0.66F;
        var0.a = 0.9F * var8;
        var0.star(var9 * 0.62, 6, 2, var2 * 0.35, 0.026);
        var0.polygon(var9 * 0.62, 6, var2 * 0.35, 0.016);
        var0.a = 0.55F * var8;
        var0.star(var9 * 0.5, 12, 5, -var2 * 0.22, 0.012);
        var0.a = 0.9F * var8;
        var0.polygon(var9 * 0.34, 3, -var2 * 0.6, 0.024);
        var0.polygon(var9 * 0.34, 3, -var2 * 0.6 + Math.PI, 0.024);
        var0.ring(var9 * 0.24, 0.03);
        var0.ring(var9 * 0.13, 0.02);

        for (int var11 = 0; var11 < 6; var11++) {
            double var12 = var2 * 0.35 + (Math.PI * 2) * (double)var11 / 6.0;
            double var14 = var9 * 0.62 * Math.cos(var12);
            double var16 = var9 * 0.62 * Math.sin(var12);
            double var18 = 0.045 * var9;
            var0.seg(var14 - var18, var16, var14, var16 + var18, 0.022);
            var0.seg(var14, var16 + var18, var14 + var18, var16, 0.022);
            var0.seg(var14 + var18, var16, var14, var16 - var18, 0.022);
            var0.seg(var14, var16 - var18, var14 - var18, var16, 0.022);
            var0.ring2(var14, var16, var18 * 0.45, 0.012);
        }

        var0.r = 0.92F;
        var0.g = 1.0F;
        var0.b = 0.96F;
        var0.a = (float)(0.2 + 0.6 * var6) * var8;
        var0.ring(var9 * (0.05 + 0.06 * var6), 0.03);
        var0.glow = 0.0;
    }

    static void frontCircle(SpellCircleFx.Ctx var0, Vec3 var1, Vec3 var2, double var3, double var5, float var7) {
        var0.cx = var1.x;
        var0.cy = var1.y;
        var0.cz = var1.z;
        var0.ux = -var2.z;
        var0.uy = 0.0;
        var0.uz = var2.x;
        var0.vx = 0.0;
        var0.vy = 1.0;
        var0.vz = 0.0;
        double var8 = 0.55 + 0.35 * var5;
        var0.glow = 3.5;
        var0.r = 0.7F;
        var0.g = 1.0F;
        var0.b = 0.84F;
        var0.a = 0.85F * var7;
        var0.ring(var8, 0.022);
        var0.ring(var8 * 0.9, 0.01);
        var0.dashes(var8 * 0.83, 0.016, 32, var3 * 0.6, 0.5);
        var0.r = 0.82F;
        var0.g = 1.0F;
        var0.b = 0.92F;
        var0.a = 0.75F * var7;
        var0.ring(var8 * 0.74, 0.01);
        var0.ring(var8 * 0.6, 0.01);
        var0.runes(var8 * 0.67, 18, -var3 * 0.4, 0.04 * var8, 0.008);
        var0.r = 0.55F;
        var0.g = 1.0F;
        var0.b = 0.7F;
        var0.a = 0.8F * var7;
        var0.star(var8 * 0.56, 6, 2, var3 * 0.7, 0.012);
        var0.polygon(var8 * 0.56, 6, -var3 * 0.5, 0.008);
        var0.polygon(var8 * 0.3, 4, var3 * 1.1, 0.01);
        var0.glow = 0.0;

        for (int var10 = 0; var10 < 6; var10++) {
            double var11 = var3 * 0.7 + (Math.PI * 2) * (double)var10 / 6.0;
            Vec3 var13 = var1.add(new Vec3(var0.ux, 0.0, var0.uz).scale(Math.cos(var11) * var8 * 0.56)).add(0.0, Math.sin(var11) * var8 * 0.56, 0.0);
            PixelFx.sprite(var13, 0.14, 1, 0.85F, 1.0F, 0.9F, var0.a * var0.fade);
        }
    }

    static void remote(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, double var7) {
        SpellCircleFx.Ctx var9 = new SpellCircleFx.Ctx();
        var9.vc = var0;
        var9.m = var1;
        var9.camX = var2.x;
        var9.camY = var2.y;
        var9.camZ = var2.z;
        float var10 = (float)(0.8 + 0.2 * Math.sin(var5 * 4.0));
        PixelFx.flat(var3.add(0.0, 0.07, 0.0), new Vec3(0.0, 1.0, 0.0), 4.0 + 3.0 * var7, 4, 0.0, 0.4F, 1.0F, 0.6F, 0.55F * var10);
        Vec3 var11 = new Vec3(var2.x - var4.x, 0.0, var2.z - var4.z);
        var11 = var11.lengthSqr() < 1.0E-4 ? new Vec3(0.0, 0.0, 1.0) : var11.normalize();
        nonEuclid(var9, var4, var11, var2, var7, var5, 0.0);
    }

    private static void faceCamera(SpellCircleFx.Ctx var0, Vec3 var1, Vec3 var2) {
        var0.cx = var1.x;
        var0.cy = var1.y;
        var0.cz = var1.z;
        double var3 = var2.x - var1.x;
        double var5 = var2.y - var1.y;
        double var7 = var2.z - var1.z;
        double var9 = Math.sqrt(var3 * var3 + var5 * var5 + var7 * var7);
        var3 /= var9;
        var5 /= var9;
        var7 /= var9;
        double var11 = -var7;
        double var13 = var3;
        double var15 = Math.sqrt(var11 * var11 + var3 * var3);
        if (var15 < 1.0E-4) {
            var11 = 1.0;
            var13 = 0.0;
            var15 = 1.0;
        }

        var11 /= var15;
        var13 /= var15;
        double var17 = var5 * var13;
        double var19 = var7 * var11 - var3 * var13;
        double var21 = -var5 * var11;
        double var23 = Math.sqrt(var17 * var17 + var19 * var19 + var21 * var21);
        var0.ux = var11;
        var0.uy = 0.0;
        var0.uz = var13;
        var0.vx = var17 / var23;
        var0.vy = var19 / var23;
        var0.vz = var21 / var23;
    }

    private static void star(SpellCircleFx.Ctx var0, double var1, double var3, double var5, double var7) {
        var0.seg(var1 - var5, var3, var1 + var5, var3, var7);
        var0.seg(var1, var3 - var5, var1, var3 + var5, var7);
        var0.seg(var1 - var5 * 0.45, var3 - var5 * 0.45, var1 + var5 * 0.45, var3 + var5 * 0.45, var7 * 0.7);
        var0.seg(var1 - var5 * 0.45, var3 + var5 * 0.45, var1 + var5 * 0.45, var3 - var5 * 0.45, var7 * 0.7);
    }

    private static void extras(SpellCircleFx.Ctx var0, Vec3 var1, Vec3 var2, double var3, double var5, float var7) {
        double var8 = var1.y + 1.05;
        var0.cx = var1.x;
        var0.cy = groundY + 0.08;
        var0.cz = var1.z;
        var0.ux = 1.0;
        var0.uy = 0.0;
        var0.uz = 0.0;
        var0.vx = 0.0;
        var0.vy = 0.0;
        var0.vz = 1.0;
        var0.r = 0.6F;
        var0.g = 1.0F;
        var0.b = 0.7F;
        var0.a = 0.55F * var7;
        var0.ring(0.9 + 0.15 * Math.sin(var3 * 3.0), 0.04);
        var0.ring(1.5, 0.02);
        faceCamera(var0, new Vec3(var1.x, groundY + 0.15, var1.z), var2);
        var0.r = 0.8F;
        var0.g = 1.0F;
        var0.b = 0.88F;
        var0.a = (float)(0.25 + 0.55 * var5) * var7;
        var0.seg(-7.0 * var5, 0.0, 7.0 * var5, 0.0, 0.025);
        var0.a *= 0.3F;
        var0.seg(-4.0 * var5, 0.0, 4.0 * var5, 0.0, 0.12);
        Vec3 var10 = CinemaDirector.lockedLook();
        Vec3 var11 = new Vec3(var10.x, 0.0, var10.z);
        var11 = var11.lengthSqr() < 0.01 ? new Vec3(0.0, 0.0, 1.0) : var11.normalize();
        Vec3 var12 = new Vec3(var1.x, var8, var1.z).add(var11.scale(0.42));
        double var13 = ArcanaCinematic.release == 0L ? 0.0 : (double)(System.nanoTime() - ArcanaCinematic.release) / 1.0E9;
        nonEuclid(var0, var12, var11, var2, var5, var3, var13);
    }

    private static double smooth(double var0, double var2, double var4) {
        var4 = Math.max(0.0, Math.min(1.0, (var4 - var0) / (var2 - var0)));
        return var4 * var4 * (3.0 - 2.0 * var4);
    }

    private static void slab(
        SpellCircleFx.Ctx var0, Vec3 var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, double var7, double var9, double var11, double var13, float var15
    ) {
        double var16 = var11 * 0.55 * var13;
        double var18 = var11 * 0.37 * var13 + 1.1;
        double var20 = var11 * 0.71 * var13 + 0.4;
        double var22 = 0.45 * Math.sin(var11 * 1.3 * var13);
        Vec3[] var24 = new Vec3[16];

        for (int var25 = 0; var25 < 16; var25++) {
            double var26 = TV[var25][0];
            double var28 = TV[var25][1];
            double var30 = TV[var25][2];
            double var32 = TV[var25][3];
            double var34 = var26 * Math.cos(var16) - var32 * Math.sin(var16);
            var32 = var26 * Math.sin(var16) + var32 * Math.cos(var16);
            double var56 = var30 * Math.cos(var20) - var32 * Math.sin(var20);
            var32 = var30 * Math.sin(var20) + var32 * Math.cos(var20);
            double var57 = var28 * Math.cos(var18 * 0.5) - var32 * Math.sin(var18 * 0.5);
            var32 = var28 * Math.sin(var18 * 0.5) + var32 * Math.cos(var18 * 0.5);
            double var36 = 2.2 / (3.0 - var32);
            var26 = var34 * var36;
            var28 = var57 * var36;
            var30 = var56 * var36;
            double var38 = var22 * var28;
            var34 = var26 * Math.cos(var38) - var30 * Math.sin(var38);
            var30 = var26 * Math.sin(var38) + var30 * Math.cos(var38);
            var24[var25] = var1.add(var2.scale(var34 * var5)).add(0.0, var28 * var7, 0.0).add(var3.scale(var30 * var9));
        }

        Vec3 var40 = var4.subtract(var1).normalize();

        for (int var43 = 0; var43 < TF.length; var43++) {
            int[] var27 = TF[var43];
            Vec3 var46 = var24[var27[1]].subtract(var24[var27[0]]).cross(var24[var27[3]].subtract(var24[var27[0]]));
            double var29 = var46.lengthSqr() < 1.0E-14 ? 0.0 : Math.abs(var46.normalize().dot(var40));
            double var31 = 0.5 + 0.5 * Math.sin(var11 * 2.6 + (double)var43 * 1.7);
            int var37 = var43 % 4;
            float var33;
            float var35;
            float var59;
            float var60;
            if (var37 == 0 || var37 == 3) {
                var33 = 0.008F;
                var59 = (float)(0.05 + 0.05 * var31);
                var35 = 0.045F;
                var60 = 0.7F;
            } else if (var37 == 1) {
                var33 = 0.02F;
                var59 = (float)(0.17 + 0.17 * var29);
                var35 = (float)(0.14 + 0.1 * var29);
                var60 = 0.66F;
            } else {
                var33 = (float)(0.3 + 0.45 * var31 * var29);
                var59 = 1.0F;
                var35 = (float)(0.8 + 0.15 * var31);
                var60 = (float)(0.05 + 0.22 * var29 * var31);
            }

            var60 *= var15 * var0.fade;

            for (int var62 = 0; var62 < 4; var62++) {
                Vec3 var39 = var24[var27[var62]];
                var0.vc
                    .addVertex(var0.m, (float)(var39.x - var0.camX), (float)(var39.y - var0.camY), (float)(var39.z - var0.camZ))
                    .setColor(var33, var59, var35, var60);
            }
        }

        double var44 = 0.004 + 0.02 * Math.max(var5, var7);

        for (int var47 = 0; var47 < TE.length; var47++) {
            int[] var48 = TE[var47];
            boolean var52 = TV[var48[0]][3] < 0.0 && TV[var48[1]][3] < 0.0;
            BreakerFx.ribbon(var0.vc, var0.m, var4, var24[var48[0]], var24[var48[1]], var44, 0.62F, 1.0F, 0.92F, (var52 ? 0.3F : 0.55F) * var15 * var0.fade);
        }
    }

    private static void ray(SpellCircleFx.Ctx var0, double var1, double var3, double var5, double var7, double var9, double var11) {
        double var13 = -var7 * var11;
        double var15 = var5 * var11;
        var0.pt(var1 + var13, var3 + var15);
        var0.pt(var1 + var5 * var9, var3 + var7 * var9);
        var0.pt(var1 + var5 * var9, var3 + var7 * var9);
        var0.pt(var1 - var13, var3 - var15);
    }

    private static void sparkle(SpellCircleFx.Ctx var0, double var1, double var3, double var5, float var7, boolean var8) {
        double var9 = var5 * 0.075;
        var0.r = 0.55F;
        var0.g = 1.0F;
        var0.b = 0.86F;
        var0.a = var7 * 0.22F;
        ray(var0, var1, var3, 0.0, 1.0, var5 * 0.6, var9 * 3.2);
        ray(var0, var1, var3, 0.0, -1.0, var5 * 0.6, var9 * 3.2);
        ray(var0, var1, var3, 1.0, 0.0, var5 * 0.45, var9 * 3.2);
        ray(var0, var1, var3, -1.0, 0.0, var5 * 0.45, var9 * 3.2);
        var0.r = 1.0F;
        var0.g = 1.0F;
        var0.b = 1.0F;
        var0.a = var7;
        ray(var0, var1, var3, 0.0, 1.0, var5, var9);
        ray(var0, var1, var3, 0.0, -1.0, var5, var9);
        ray(var0, var1, var3, 1.0, 0.0, var5 * 0.72, var9);
        ray(var0, var1, var3, -1.0, 0.0, var5 * 0.72, var9);
        if (var8) {
            double var11 = 0.7071;
            double var13 = var5 * 0.42;
            var0.a = var7 * 0.8F;
            ray(var0, var1, var3, var11, var11, var13, var9 * 0.55);
            ray(var0, var1, var3, -var11, -var11, var13, var9 * 0.55);
            ray(var0, var1, var3, var11, -var11, var13, var9 * 0.55);
            ray(var0, var1, var3, -var11, var11, var13, var9 * 0.55);
        }

        var0.r = 0.85F;
        var0.g = 1.0F;
        var0.b = 0.97F;
        var0.a = var7;
        ray(var0, var1, var3, 0.0, 1.0, var5 * 0.12, var9 * 2.2);
        ray(var0, var1, var3, 0.0, -1.0, var5 * 0.12, var9 * 2.2);
    }

    static void nonEuclid(SpellCircleFx.Ctx var0, Vec3 var1, Vec3 var2, Vec3 var3, double var4, double var6, double var8) {
        Vec3 var10 = new Vec3(-var2.z, 0.0, var2.x);
        double var11 = Math.min(1.0, var8 / 0.35);
        double var13 = smooth(0.45, 1.0, var4);
        double var15 = 0.14 * Math.pow(Math.max(0.0, Math.sin(var6 * 1.6)), 6.0) * (1.0 - var13);
        double var17 = 1.0 - var11;
        double var19 = smooth(0.0, 0.04, var4);
        double var21 = 0.29 * (1.0 - 0.82 * var13) * (1.0 - var15) * (1.0 + 0.05 * Math.sin(var6 * 2.1)) * var17 * var19;
        double var23 = 0.12 * (1.0 - 0.55 * var13) * (1.0 + var15 * 0.8) * var17 * var19;
        double var25 = 0.05 * (1.0 + 0.6 * var13) * var17 * var19;
        double var27 = 1.0 + 4.0 * var13 + 6.0 * var11;
        float var29 = (float)(1.0 - 0.45 * smooth(0.8, 1.0, var4));
        if (var21 > 0.003) {
            PixelFx.sprite(var1, 1.4 * var21 + 0.1, 4, 0.05F, 0.45F, 0.32F, 0.3F * var0.fade);
            slab(var0, var1, var10, var2, var3, var21, var23, var25, var6, var27, var29);
            var0.cx = var1.x + var2.x * var25 * 1.2;
            var0.cy = var1.y;
            var0.cz = var1.z + var2.z * var25 * 1.2;
            var0.ux = var10.x;
            var0.uy = 0.0;
            var0.uz = var10.z;
            var0.vx = 0.0;
            var0.vy = 1.0;
            var0.vz = 0.0;
            var0.glow = 3.0;

            for (int var30 = 0; var30 < 4; var30++) {
                double var31 = 0.38 + 0.11 * (double)var30;
                double var33 = Math.floor((var6 + (double)var30 * 0.29) / var31);
                double var35 = (var6 + (double)var30 * 0.29) / var31 - var33;
                if (!(var35 > 0.5)) {
                    int var37 = (int)var33 * 7 + var30;
                    double var38 = Math.sin(var35 / 0.5 * Math.PI);
                    double var40 = (hash(var37, 1) * 2.0 - 1.0) * var21 * 0.9;
                    double var42 = (hash(var37, 2) * 2.0 - 1.0) * var23 * 0.8;
                    double var44 = (0.06 + 0.12 * hash(var37, 3)) * (0.5 + 0.5 * var21 / 0.29);
                    var0.r = 0.7F;
                    var0.g = 1.0F;
                    var0.b = 0.97F;
                    var0.a = (float)(0.85 * var38) * var29;
                    var0.seg(var40 - var44 * 0.77, var42 - var44 * 0.64, var40 + var44 * 0.77, var42 + var44 * 0.64, 0.006);
                    var0.seg(var40 - var44 * 0.77, var42 + var44 * 0.64, var40 + var44 * 0.77, var42 - var44 * 0.64, 0.006);
                    var0.seg(var40, var42 - var44 * 0.5, var40, var42 + var44 * 0.5, 0.004);
                    var0.seg(var40 - var44 * 0.4, var42, var40 + var44 * 0.4, var42, 0.004);
                }
            }

            var0.glow = 0.0;
        }

        double var64 = (0.035 + 0.23 * smooth(0.22, 0.85, var4)) * (1.0 + 0.03 * Math.sin(var6 * 5.0)) * Math.pow(var17, 1.5);
        faceCamera(var0, var1, var3);
        if (var4 > 0.18 && var64 > 0.003) {
            float var32 = (float)smooth(0.18, 0.3, var4);
            PixelFx.sprite(var1, var64 * 3.6, 4, 0.45F, 1.0F, 0.72F, 0.32F * var32 * var0.fade);
            PixelFx.sprite(var1, var64 * 2.2, 4, 0.85F, 1.0F, 0.93F, 0.55F * var32 * var0.fade);
            var0.r = 0.6F;
            var0.g = 1.0F;
            var0.b = 0.86F;
            var0.a = 0.26F * var32;
            var0.disc(var64, 28);
            var0.r = 0.78F;
            var0.g = 1.0F;
            var0.b = 0.92F;
            var0.a = 0.28F * var32;
            var0.disc(var64 * 0.8, 28);
            var0.r = 0.93F;
            var0.g = 1.0F;
            var0.b = 0.97F;
            var0.a = 0.4F * var32;
            var0.disc(var64 * 0.55, 24);
        }

        if (var11 >= 1.0 && var8 < 0.6) {
            PixelFx.sprite(var1, 1.2 * (1.0 - var8 / 0.6), 3, 0.9F, 1.0F, 0.94F, var0.fade);
        }

        double var65 = 0.0;
        double var34 = 0.0;
        double var36 = var6 % 1.7 / 1.7;
        if (var4 > 0.3 && var36 < 0.12 && var8 <= 0.0) {
            var65 = Math.sin(var36 / 0.12 * Math.PI) * 0.6;
            var34 = 0.9 + 0.8 * var4;
        }

        if (var8 > 0.0 && var8 < 0.6) {
            var65 = 1.0 - var8 / 0.6;
            var34 = 3.2 * (0.4 + 0.6 * var65);
        }

        if (var65 > 0.01) {
            var0.glow = 3.0;
            var0.r = 0.68F;
            var0.g = 1.0F;
            var0.b = 0.95F;
            var0.a = (float)(0.9 * var65);
            var0.seg(-var34, 0.0, var34, 0.0, 0.012);
            var0.a = (float)(0.5 * var65);
            var0.seg(0.0, -var34 * 0.35, 0.0, var34 * 0.35, 0.008);
            var0.glow = 0.0;
        }

        int var66 = 5 + (int)(17.0 * Math.min(1.0, var4));
        float var39 = (float)(1.0 - 0.7 * var11);

        for (int var67 = 0; var67 < var66; var67++) {
            double var41 = 0.5 + 0.4 * hash(var67, 11);
            double var43 = hash(var67, 12) * var41;
            double var45 = Math.floor((var6 + var43) / var41);
            double var47 = (var6 + var43) / var41 - var45;
            if (!(var47 > 0.6)) {
                int var49 = (int)var45 * 31 + var67;
                double var50 = Math.pow(Math.sin(var47 / 0.6 * Math.PI), 0.6);
                double var52 = hash(var49, 13) * Math.PI * 2.0;
                double var54 = 0.08 + (0.45 + 0.55 * var4) * Math.pow(hash(var49, 14), 0.8);
                double var56 = Math.cos(var52) * var54;
                double var58 = Math.sin(var52) * var54 * 0.75;
                double var60 = hash(var49, 15);
                double var62 = (0.05 + 0.22 * var60 * var60) * var50;
                sparkle(var0, var56, var58, var62, (float)(0.95 * var50) * var39, hash(var49, 16) < 0.3);
            }
        }
    }

    static void crystal(SpellCircleFx.Ctx var0, Vec3 var1, Vec3 var2, double var3, double var5) {
        byte var7 = 6;
        double var8 = var5 * 1.1;
        Vec3 var10 = var1.add(0.0, var3 * 1.45, 0.0);
        Vec3 var11 = var1.add(0.0, -var3 * 1.25, 0.0);
        Vec3[] var12 = new Vec3[var7];

        for (int var13 = 0; var13 < var7; var13++) {
            double var14 = var8 + (Math.PI * 2) * (double)var13 / (double)var7;
            var12[var13] = var1.add(Math.cos(var14) * var3, (var13 % 2 == 0 ? 0.08 : -0.08) * var3, Math.sin(var14) * var3);
        }

        Vec3 var30 = var2.subtract(var1).normalize();

        for (int var31 = 0; var31 < var7; var31++) {
            Vec3 var15 = var12[var31];
            Vec3 var16 = var12[(var31 + 1) % var7];

            for (int var17 = 0; var17 < 2; var17++) {
                Vec3 var18 = var17 == 0 ? var10 : var11;
                Vec3 var19 = var16.subtract(var15).cross(var18.subtract(var15)).normalize();
                double var20 = 0.45 + 0.55 * Math.abs(var19.dot(var30));
                float var22 = (float)(0.35 + 0.55 * var20);
                float var23 = 1.0F;
                float var24 = (float)(0.55 + 0.4 * var20);
                float var25 = (float)(0.55 + 0.35 * var20);

                for (Vec3 var29 : new Vec3[]{var18, var15, var16, var16}) {
                    var0.vc
                        .addVertex(var0.m, (float)(var29.x - var0.camX), (float)(var29.y - var0.camY), (float)(var29.z - var0.camZ))
                        .setColor(var22, var23, var24, var25 * var0.fade);
                }
            }
        }

        PixelFx.sprite(var1, var3 * 5.0, 4, 0.4F, 1.0F, 0.62F, 0.55F * var0.fade);
        PixelFx.sprite(var1, var3 * 1.6, 4, 0.9F, 1.0F, 0.95F, 0.9F * var0.fade);
    }

    private static double hash(int var0, int var1) {
        double var2 = Math.sin((double)var0 * 12.9898 + (double)var1 * 78.233) * 43758.5453;
        return var2 - Math.floor(var2);
    }

    private static void mote(SpellCircleFx.Ctx var0, Vec3 var1, Vec3 var2, double var3, float var5) {
        PixelFx.sprite(var1, var3 * 4.5, 4, var0.r, var0.g, var0.b, var5 * 0.8F);
        PixelFx.sprite(var1, var3 * 1.6, 58, Math.min(1.0F, var0.r + 0.3F), 1.0F, Math.min(1.0F, var0.b + 0.3F), var5);
    }

    private static void scene(SpellCircleFx.Ctx var0, Vec3 var1, Vec3 var2, double var3) {
        long var5 = System.nanoTime();
        ArcanaClient.Fracture var7 = null;

        for (ArcanaClient.Fracture var9 : ArcanaClient.fractures()) {
            if (ArcanaCinematic.release != 0L
                && var9.startNanos() >= ArcanaCinematic.release - 400000000L
                && (var7 == null || var9.startNanos() > var7.startNanos())) {
                var7 = var9;
            }
        }

        double var29 = var7 == null ? -1.0 : (double)(var5 - var7.startNanos()) / 1.0E9 - 2.6;
        if (ArcanaCinematic.release == 0L) {
            double var10000 = -1.0;
        } else {
            double var38 = (double)(var5 - ArcanaCinematic.release) / 1.0E9;
        }

        if (var7 != null && var29 > 3.4) {
            Vec3 var12 = var7.field().center();
            double var13 = (double)var7.field().radius();
            double var15 = Math.min(1.0, (var29 - 3.1 - 0.3) / 1.2);
            double var17 = Math.max(0.0, Math.min(1.0, (12.899999999999999 - var29) / 2.5));

            for (int var19 = 0; var19 < 90; var19++) {
                double var20 = hash(var19, 1) * Math.PI * 2.0;
                double var22 = var13 * (0.15 + 0.85 * Math.sqrt(hash(var19, 2)));
                double var24 = var12.y + var13 * 0.9 * hash(var19, 3) + (var29 - 3.1 - 0.3) * (0.18 + 0.4 * hash(var19, 4));
                Vec3 var26 = new Vec3(var12.x + Math.cos(var20 + var29 * 0.05) * var22 * 0.8, var24, var12.z + Math.sin(var20 + var29 * 0.05) * var22 * 0.8);
                double var27 = 0.5 + 0.5 * Math.sin(var3 * 3.0 + (double)var19);
                var0.r = 0.45F;
                var0.g = 1.0F;
                var0.b = 0.6F;
                mote(var0, var26, var2, 0.05 + 0.08 * hash(var19, 5), (float)(var15 * var17 * (0.4 + 0.6 * var27)));
            }
        }

        if (var7 != null && var29 > 9.8) {
            double var30 = Math.min(1.0, (var29 - 9.8) / 1.5);

            for (int var14 = 0; var14 < 28; var14++) {
                double var34 = hash(var14, 6) * Math.PI * 2.0;
                double var35 = 0.4 + 2.4 * hash(var14, 7);
                double var36 = var1.y + (hash(var14, 8) * 3.2 + (var29 - 9.8) * 0.3) % 3.2;
                Vec3 var21 = new Vec3(var1.x + Math.cos(var34) * var35, var36, var1.z + Math.sin(var34) * var35);
                var0.r = 0.55F;
                var0.g = 1.0F;
                var0.b = 0.7F;
                mote(var0, var21, var2, 0.015 + 0.02 * hash(var14, 9), (float)(var30 * (0.4 + 0.6 * (0.5 + 0.5 * Math.sin(var3 * 2.5 + (double)var14)))));
            }
        }

        if (var7 != null && !(var29 < 3.1)) {
            float var31 = (float)Math.min(1.0, (var29 - 3.1) / 1.5);
            var0.r = 0.75F;
            var0.g = 0.9F;
            var0.b = 1.0F;

            for (int var32 = 0; var32 < 240; var32++) {
                double var33 = var2.x + (hash(var32, 10) - 0.5) * 30.0;
                double var16 = var2.z + (hash(var32, 11) - 0.5) * 30.0;
                double var18 = var2.y + 12.0 - (var3 * (16.0 + 6.0 * hash(var32, 12)) + hash(var32, 13) * 24.0) % 24.0;
                Vec3 var37 = new Vec3(var33, var18, var16);
                faceCamera(var0, var37, var2);
                var0.a = 0.3F * var31;
                var0.seg(0.0, 0.0, 0.02, -0.8, 0.014);
            }
        }
    }

    static {
        for (int var0 = 0; var0 < 16; var0++) {
            for (int var1 = 0; var1 < 4; var1++) {
                TV[var0][var1] = (var0 >> var1 & 1) == 0 ? -1.0 : 1.0;
            }
        }

        ArrayList var5 = new ArrayList();
        ArrayList var6 = new ArrayList();

        for (int var2 = 0; var2 < 16; var2++) {
            for (int var3 = 0; var3 < 4; var3++) {
                int var4 = var2 ^ 1 << var3;
                if (var2 < var4) {
                    var5.add(new int[]{var2, var4});
                }
            }
        }

        for (int var7 = 0; var7 < 4; var7++) {
            for (int var8 = var7 + 1; var8 < 4; var8++) {
                for (int var9 = 0; var9 < 16; var9++) {
                    if ((var9 >> var7 & 1) == 0 && (var9 >> var8 & 1) == 0) {
                        var6.add(new int[]{var9, var9 | 1 << var7, var9 | 1 << var7 | 1 << var8, var9 | 1 << var8});
                    }
                }
            }
        }

        TE = var5.toArray(new int[0][]);
        TF = var6.toArray(new int[0][]);
    }

    static final class Ctx {
        VertexConsumer vc;
        Matrix4f m;
        double cx;
        double cy;
        double cz;
        double ux;
        double uy;
        double uz;
        double vx;
        double vy;
        double vz;
        double camX;
        double camY;
        double camZ;
        float r;
        float g;
        float b;
        float a;
        float fade = 1.0F;
        double glow = 0.0;

        void pt(double var1, double var3) {
            double var5 = this.cx + this.ux * var1 + this.vx * var3 - this.camX;
            double var7 = this.cy + this.uy * var1 + this.vy * var3 - this.camY;
            double var9 = this.cz + this.uz * var1 + this.vz * var3 - this.camZ;
            this.vc.addVertex(this.m, (float)var5, (float)var7, (float)var9).setColor(this.r, this.g, this.b, this.a * this.fade);
        }

        void seg(double var1, double var3, double var5, double var7, double var9) {
            double var11 = var5 - var1;
            double var13 = var7 - var3;
            double var15 = Math.sqrt(var11 * var11 + var13 * var13);
            if (!(var15 < 1.0E-6)) {
                if (this.glow > 0.0) {
                    float var17 = this.a;
                    this.a *= 0.13F;
                    double var18 = -var13 / var15 * var9 * this.glow * 0.5;
                    double var20 = var11 / var15 * var9 * this.glow * 0.5;
                    this.pt(var1 - var18, var3 - var20);
                    this.pt(var1 + var18, var3 + var20);
                    this.pt(var5 + var18, var7 + var20);
                    this.pt(var5 - var18, var7 - var20);
                    this.a = var17;
                }

                double var22 = -var13 / var15 * var9 * 0.5;
                double var19 = var11 / var15 * var9 * 0.5;
                this.pt(var1 - var22, var3 - var19);
                this.pt(var1 + var22, var3 + var19);
                this.pt(var5 + var22, var7 + var19);
                this.pt(var5 - var22, var7 - var19);
            }
        }

        void arc(double var1, double var3, double var5, double var7, int var9) {
            for (int var10 = 0; var10 < var9; var10++) {
                double var11 = var3 + (var5 - var3) * (double)var10 / (double)var9;
                double var13 = var3 + (var5 - var3) * (double)(var10 + 1) / (double)var9;
                this.seg(var1 * Math.cos(var11), var1 * Math.sin(var11), var1 * Math.cos(var13), var1 * Math.sin(var13), var7);
            }
        }

        void ring(double var1, double var3) {
            this.arc(var1, 0.0, Math.PI * 2, var3, 72);
        }

        void ring2(double var1, double var3, double var5, double var7) {
            for (int var9 = 0; var9 < 20; var9++) {
                double var10 = (Math.PI * 2) * (double)var9 / 20.0;
                double var12 = (Math.PI * 2) * (double)(var9 + 1) / 20.0;
                this.seg(var1 + var5 * Math.cos(var10), var3 + var5 * Math.sin(var10), var1 + var5 * Math.cos(var12), var3 + var5 * Math.sin(var12), var7);
            }
        }

        void dashes(double var1, double var3, int var5, double var6, double var8) {
            for (int var10 = 0; var10 < var5; var10++) {
                double var11 = var6 + (Math.PI * 2) * (double)var10 / (double)var5;
                this.arc(var1, var11, var11 + (Math.PI * 2) / (double)var5 * var8, var3, 4);
            }
        }

        void polygon(double var1, int var3, double var4, double var6) {
            for (int var8 = 0; var8 < var3; var8++) {
                double var9 = var4 + (Math.PI * 2) * (double)var8 / (double)var3;
                double var11 = var4 + (Math.PI * 2) * (double)(var8 + 1) / (double)var3;
                this.seg(var1 * Math.cos(var9), var1 * Math.sin(var9), var1 * Math.cos(var11), var1 * Math.sin(var11), var6);
            }
        }

        void star(double var1, int var3, int var4, double var5, double var7) {
            for (int var9 = 0; var9 < var3; var9++) {
                double var10 = var5 + (Math.PI * 2) * (double)var9 / (double)var3;
                double var12 = var5 + (Math.PI * 2) * (double)((var9 + var4) % var3) / (double)var3;
                this.seg(var1 * Math.cos(var10), var1 * Math.sin(var10), var1 * Math.cos(var12), var1 * Math.sin(var12), var7);
            }
        }

        void ticks(double var1, double var3, int var5, double var6, double var8) {
            for (int var10 = 0; var10 < var5; var10++) {
                double var11 = var6 + (Math.PI * 2) * (double)var10 / (double)var5;
                double var13 = var10 % 3 == 0 ? var3 : var1 + (var3 - var1) * 0.55;
                this.seg(var1 * Math.cos(var11), var1 * Math.sin(var11), var13 * Math.cos(var11), var13 * Math.sin(var11), var8);
            }
        }

        void runes(double var1, int var3, double var4, double var6, double var8) {
            for (int var10 = 0; var10 < var3; var10++) {
                double var11 = var4 + (Math.PI * 2) * (double)var10 / (double)var3;
                double var13 = var1 * Math.cos(var11);
                double var15 = var1 * Math.sin(var11);
                double var17 = -Math.sin(var11);
                double var19 = Math.cos(var11);
                double var21 = Math.cos(var11);
                double var23 = Math.sin(var11);
                int var25 = var10 % 4;
                if (var25 == 0) {
                    this.seg(var13 - var21 * var6, var15 - var23 * var6, var13 + var21 * var6, var15 + var23 * var6, var8);
                    this.seg(var13 - var17 * var6 * 0.6, var15 - var19 * var6 * 0.6, var13 + var17 * var6 * 0.6, var15 + var19 * var6 * 0.6, var8);
                } else if (var25 == 1) {
                    this.seg(var13 - var17 * var6, var15 - var19 * var6, var13 + var21 * var6, var15 + var23 * var6, var8);
                    this.seg(var13 + var21 * var6, var15 + var23 * var6, var13 + var17 * var6, var15 + var19 * var6, var8);
                } else if (var25 == 2) {
                    this.seg(var13 - var21 * var6, var15 - var23 * var6, var13 + var17 * var6, var15 + var19 * var6, var8);
                    this.seg(var13 + var17 * var6, var15 + var19 * var6, var13 + var21 * var6, var15 + var23 * var6, var8);
                    this.seg(var13 - var17 * var6, var15 - var19 * var6, var13 + var17 * var6, var15 + var19 * var6, var8);
                } else {
                    this.seg(var13 - var21 * var6, var15 - var23 * var6, var13 + var21 * var6, var15 + var23 * var6, var8);
                    this.seg(var13 - var17 * var6, var15 - var19 * var6, var13 + var17 * var6 * 0.3, var15 + var19 * var6 * 0.3, var8);
                }
            }
        }

        void disc(double var1, int var3) {
            for (int var4 = 0; var4 < var3; var4++) {
                double var5 = (Math.PI * 2) * (double)var4 / (double)var3;
                double var7 = (Math.PI * 2) * (double)(var4 + 1) / (double)var3;
                this.pt(0.0, 0.0);
                this.pt(var1 * Math.cos(var5), var1 * Math.sin(var5));
                this.pt(var1 * Math.cos(var7), var1 * Math.sin(var7));
                this.pt(var1 * Math.cos(var7), var1 * Math.sin(var7));
            }
        }
    }
}
