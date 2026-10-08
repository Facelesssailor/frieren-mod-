package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.phys.Vec3;
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
                    double var7 = ease(var5 / 1.6);
                    if (!(var7 <= 0.01)) {
                        Vec3 var9 = var2.getViewVector(var4);
                        Vec3 var10 = var2.getEyePosition(var4);
                        double var11 = 3.1 + 0.4 * var7;
                        SpellCircleFx.Ctx var13 = new SpellCircleFx.Ctx();
                        var13.cx = var10.x + var9.x * var11;
                        var13.cy = var10.y - 0.15 + var9.y * var11;
                        var13.cz = var10.z + var9.z * var11;
                        double var14 = -var9.z;
                        double var16 = var9.x;
                        double var18 = Math.sqrt(var14 * var14 + var16 * var16);
                        if (var18 < 1.0E-4) {
                            var14 = 1.0;
                            var16 = 0.0;
                            var18 = 1.0;
                        }

                        var14 /= var18;
                        var16 /= var18;
                        var13.ux = var14;
                        var13.uy = 0.0;
                        var13.uz = var16;
                        var13.vx = var16 * var9.y - 0.0 * var9.z;
                        double var20 = var9.y * var16 - var9.z * 0.0;
                        double var22 = var9.z * var14 - var9.x * var16;
                        double var24 = var9.x * 0.0 - var9.y * var14;
                        double var26 = Math.sqrt(var20 * var20 + var22 * var22 + var24 * var24);
                        var13.vx = var20 / var26;
                        var13.vy = var22 / var26;
                        var13.vz = var24 / var26;
                        if (var13.vy < 0.0) {
                            var13.vx = -var13.vx;
                            var13.vy = -var13.vy;
                            var13.vz = -var13.vz;
                        }

                        Vec3 var28 = var0.getCamera().getPosition();
                        var13.camX = var28.x;
                        var13.camY = var28.y;
                        var13.camZ = var28.z;
                        var13.m = var0.getPoseStack().last().pose();
                        BufferSource var29 = var1.renderBuffers().bufferSource();
                        var13.vc = var29.getBuffer(ArcanaRenderTypes.MAGIC);
                        ArcanaShaders.prepare((double)((float)var3.getGameTime() + var4) / 20.0, var28);
                        double var30 = var5;
                        float var32 = (float)(0.78 + 0.22 * Math.sin(var5 * 5.0));
                        double var33 = 2.5 * var7;
                        var13.r = 0.15F;
                        var13.g = 0.75F;
                        var13.b = 0.45F;
                        var13.a = 0.1F * var32;
                        var13.disc(var33 * 0.98, 48);
                        var13.r = 0.55F;
                        var13.g = 1.0F;
                        var13.b = 0.72F;
                        var13.a = 0.92F * var32;
                        var13.ring(var33, 0.06 * var7 + 0.01);
                        var13.ring(var33 * 0.93, 0.025);
                        var13.a = 0.8F * var32;
                        var13.dashes(var33 * 0.86, 0.05, 36, var5 * 0.35, 0.55);
                        var13.r = 0.7F;
                        var13.g = 1.0F;
                        var13.b = 0.85F;
                        var13.a = 0.85F * var32;
                        var13.ring(var33 * 0.74, 0.03);
                        var13.ring(var33 * 0.62, 0.03);
                        var13.runes(var33 * 0.68, 24, -var5 * 0.28, 0.065 * var33, 0.02);
                        var13.ticks(var33 * 0.62, var33 * 0.71, 72, var5 * 0.12, 0.012);
                        var13.r = 0.45F;
                        var13.g = 1.0F;
                        var13.b = 0.6F;
                        var13.a = 0.9F * var32;
                        var13.star(var33 * 0.56, 6, 2, var5 * 0.5, 0.035);
                        var13.polygon(var33 * 0.56, 6, var5 * 0.5, 0.02);
                        var13.polygon(var33 * 0.32, 3, -var5 * 0.8, 0.03);
                        var13.polygon(var33 * 0.32, 3, -var5 * 0.8 + Math.PI, 0.03);
                        var13.ring(var33 * 0.22, 0.04);
                        var13.ring(var33 * 0.12, 0.025);

                        for (int var35 = 0; var35 < 6; var35++) {
                            double var36 = var30 * 0.5 + (Math.PI * 2) * (double)var35 / 6.0;
                            double var38 = var33 * 0.56 * Math.cos(var36);
                            double var40 = var33 * 0.56 * Math.sin(var36);
                            double var42 = 0.05 * var33;
                            var13.seg(var38 - var42, var40, var38, var40 + var42, 0.03);
                            var13.seg(var38, var40 + var42, var38 + var42, var40, 0.03);
                            var13.seg(var38 + var42, var40, var38, var40 - var42, 0.03);
                            var13.seg(var38, var40 - var42, var38 - var42, var40, 0.03);
                        }

                        double var46 = Math.min(1.0, var30 / 4.0);
                        var13.r = 0.9F;
                        var13.g = 1.0F;
                        var13.b = 0.95F;
                        var13.a = (float)(0.15 + 0.5 * var46) * var32;
                        var13.disc(var33 * (0.05 + 0.1 * var46), 24);
                        extras(var13, var2.getPosition(var4), var28, var30, var46, var32);
                        scene(var13, var2.getPosition(var4), var28, var30);
                        var29.endBatch(ArcanaRenderTypes.MAGIC);
                    }
                }
            }
        }
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
        double var10 = 120.0;
        double var12 = 0.03 + 0.05 * var5;
        var0.r = 0.75F;
        var0.g = 1.0F;
        var0.b = 0.85F;
        var0.a = (float)(0.55 + 0.4 * var5);

        for (int var14 = 0; var14 < 2; var14++) {
            var0.cx = var1.x;
            var0.cy = var8;
            var0.cz = var1.z;
            if (var14 == 0) {
                var0.ux = 1.0;
                var0.uy = 0.0;
                var0.uz = 0.0;
            } else {
                var0.ux = 0.0;
                var0.uy = 0.0;
                var0.uz = 1.0;
            }

            var0.vx = 0.0;
            var0.vy = 1.0;
            var0.vz = 0.0;
            var0.seg(0.0, 0.0, 0.0, var10, var12);
            var0.a *= 0.35F;
            var0.seg(0.0, 0.0, 0.0, var10, var12 * 3.2);
            var0.a /= 0.35F;
        }

        var0.cx = var1.x;
        var0.cy = var1.y + 0.05;
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
        Vec3 var28 = new Vec3(var1.x, var8, var1.z);
        faceCamera(var0, var28, var2);
        double var15 = 0.07 + 0.09 * var5;
        var0.r = 0.35F;
        var0.g = 1.0F;
        var0.b = 0.55F;
        var0.a = 0.3F;
        var0.disc(var15 * 2.2, 20);
        var0.r = 0.8F;
        var0.g = 1.0F;
        var0.b = 0.9F;
        var0.a = 0.95F;
        var0.disc(var15, 20);
        var0.r = 1.0F;
        var0.g = 1.0F;
        var0.b = 1.0F;

        for (int var17 = 0; var17 < 10; var17++) {
            double var18 = (double)var17 * 12.9898;
            double var20 = (var3 * 0.9 + (double)var17 * 0.37) % 1.0;
            double var22 = var18 * 7.13 % (Math.PI * 2);
            double var24 = 0.25 + 1.3 * (var18 * 3.7 % 1.0);
            double var26 = Math.sin(var20 * Math.PI);
            var0.a = (float)(0.9 * var26);
            star(
                var0,
                Math.cos(var22) * var24,
                Math.sin(var22) * var24 * 0.8,
                (0.04 + 0.12 * ((double)(var17 % 3) / 2.0)) * var26 * (0.6 + var5),
                0.012 + 0.012 * var26
            );
        }

        star(var0, 0.0, 0.0, 0.35 * (0.5 + var5) * (0.8 + 0.2 * Math.sin(var3 * 9.0)), 0.025);
    }

    private static double hash(int var0, int var1) {
        double var2 = Math.sin((double)var0 * 12.9898 + (double)var1 * 78.233) * 43758.5453;
        return var2 - Math.floor(var2);
    }

    private static void mote(SpellCircleFx.Ctx var0, Vec3 var1, Vec3 var2, double var3, float var5) {
        faceCamera(var0, var1, var2);
        var0.a = var5 * 0.35F;
        var0.disc(var3 * 2.4, 8);
        var0.a = var5;
        var0.disc(var3, 8);
    }

    private static void scene(SpellCircleFx.Ctx var0, Vec3 var1, Vec3 var2, double var3) {
        long var5 = System.nanoTime();
        ArcanaClient.Fracture var7 = null;

        for (ArcanaClient.Fracture var9 : ArcanaClient.fractures()) {
            if (var7 == null || var9.startNanos() > var7.startNanos()) {
                var7 = var9;
            }
        }

        double var32 = var7 == null ? -1.0 : (double)(var5 - var7.startNanos()) / 1.0E9;
        double var10 = ArcanaCinematic.release == 0L ? -1.0 : (double)(var5 - ArcanaCinematic.release) / 1.0E9;
        Vec3 var12 = null;
        double var13 = 0.0;
        if (var7 != null) {
            var12 = var7.field().center();
            var13 = (double)var7.field().radius();
        } else if (var10 >= 0.0) {
            double var15 = Double.POSITIVE_INFINITY;

            for (ArcanaClient.VisualField var18 : ArcanaClient.fields()) {
                double var19 = var18.center().distanceToSqr(var1);
                if (var19 < var15) {
                    var15 = var19;
                    var12 = var18.center();
                    var13 = (double)var18.radius();
                }
            }
        }

        if (var12 != null && var10 >= 0.0) {
            double var33 = var32 < 1.2 ? 1.0 : Math.max(0.0, 1.0 - (var32 - 1.2) / 1.2);
            double var38 = Math.min(1.0, var10 / 0.6);
            if (var33 > 0.01) {
                double var43 = var12.y + var13;
                var0.r = 0.85F;
                var0.g = 1.0F;
                var0.b = 0.9F;

                for (int var21 = 0; var21 < 2; var21++) {
                    var0.cx = var12.x;
                    var0.cy = var43 - var13 * 0.12;
                    var0.cz = var12.z;
                    if (var21 == 0) {
                        var0.ux = 1.0;
                        var0.uy = 0.0;
                        var0.uz = 0.0;
                    } else {
                        var0.ux = 0.0;
                        var0.uy = 0.0;
                        var0.uz = 1.0;
                    }

                    var0.vx = 0.0;
                    var0.vy = 1.0;
                    var0.vz = 0.0;
                    var0.a = (float)(0.95 * var33);
                    var0.seg(0.0, 0.0, 0.0, 90.0 * var38, 0.07);
                    var0.a = (float)(0.3 * var33);
                    var0.seg(0.0, 0.0, 0.0, 90.0 * var38, 0.32);
                }

                Vec3 var46 = new Vec3(var12.x, var43, var12.z);
                faceCamera(var0, var46, var2);
                var0.r = 0.5F;
                var0.g = 1.0F;
                var0.b = 0.65F;
                var0.a = (float)(0.35 * var33);
                var0.disc(1.3 * var38, 20);
                var0.r = 1.0F;
                var0.g = 1.0F;
                var0.b = 1.0F;
                var0.a = (float)(0.9 * var33);
                var0.disc(0.35 * var38, 16);
                star(var0, 0.0, 0.0, 1.6 * var38 * (0.85 + 0.15 * Math.sin(var3 * 14.0)), 0.05);
            }
        }

        if (var7 != null && var32 > 0.4) {
            Vec3 var34 = var7.field().center();
            double var16 = (double)var7.field().radius();
            double var40 = Math.min(1.0, (var32 - 0.4) / 1.0);
            double var20 = Math.max(0.0, Math.min(1.0, (5.2 - var32) / 1.6));

            for (int var22 = 0; var22 < 90; var22++) {
                double var23 = hash(var22, 1) * Math.PI * 2.0;
                double var25 = var16 * (0.15 + 0.85 * Math.sqrt(hash(var22, 2)));
                double var27 = var34.y + var16 * 0.9 * hash(var22, 3) + (var32 - 0.4) * (0.25 + 0.5 * hash(var22, 4));
                Vec3 var29 = new Vec3(var34.x + Math.cos(var23 + var32 * 0.05) * var25 * 0.8, var27, var34.z + Math.sin(var23 + var32 * 0.05) * var25 * 0.8);
                double var30 = 0.5 + 0.5 * Math.sin(var3 * 3.0 + (double)var22);
                var0.r = 0.45F;
                var0.g = 1.0F;
                var0.b = 0.6F;
                mote(var0, var29, var2, 0.05 + 0.08 * hash(var22, 5), (float)(var40 * var20 * (0.4 + 0.6 * var30)));
            }
        }

        if (var7 != null && var32 > 2.0) {
            double var35 = Math.min(1.0, (var32 - 2.0) / 1.2);

            for (int var39 = 0; var39 < 28; var39++) {
                double var41 = hash(var39, 6) * Math.PI * 2.0;
                double var44 = 0.4 + 2.4 * hash(var39, 7);
                double var47 = var1.y + (hash(var39, 8) * 3.2 + (var32 - 2.0) * 0.3) % 3.2;
                Vec3 var24 = new Vec3(var1.x + Math.cos(var41) * var44, var47, var1.z + Math.sin(var41) * var44);
                var0.r = 0.55F;
                var0.g = 1.0F;
                var0.b = 0.7F;
                mote(var0, var24, var2, 0.015 + 0.02 * hash(var39, 9), (float)(var35 * (0.4 + 0.6 * (0.5 + 0.5 * Math.sin(var3 * 2.5 + (double)var39)))));
            }
        }

        var0.r = 0.75F;
        var0.g = 0.9F;
        var0.b = 1.0F;

        for (int var36 = 0; var36 < 140; var36++) {
            double var37 = var2.x + (hash(var36, 10) - 0.5) * 30.0;
            double var42 = var2.z + (hash(var36, 11) - 0.5) * 30.0;
            double var45 = var2.y + 12.0 - (var3 * (16.0 + 6.0 * hash(var36, 12)) + hash(var36, 13) * 24.0) % 24.0;
            Vec3 var48 = new Vec3(var37, var45, var42);
            faceCamera(var0, var48, var2);
            var0.a = 0.22F;
            var0.seg(0.0, 0.0, 0.02, -0.7, 0.012);
        }
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

        void pt(double var1, double var3) {
            double var5 = this.cx + this.ux * var1 + this.vx * var3 - this.camX;
            double var7 = this.cy + this.uy * var1 + this.vy * var3 - this.camY;
            double var9 = this.cz + this.uz * var1 + this.vz * var3 - this.camZ;
            this.vc.addVertex(this.m, (float)var5, (float)var7, (float)var9).setColor(this.r, this.g, this.b, this.a);
        }

        void seg(double var1, double var3, double var5, double var7, double var9) {
            double var11 = var5 - var1;
            double var13 = var7 - var3;
            double var15 = Math.sqrt(var11 * var11 + var13 * var13);
            if (!(var15 < 1.0E-6)) {
                double var17 = -var13 / var15 * var9 * 0.5;
                double var19 = var11 / var15 * var9 * 0.5;
                this.pt(var1 - var17, var3 - var19);
                this.pt(var1 + var17, var3 + var19);
                this.pt(var5 + var17, var7 + var19);
                this.pt(var5 - var17, var7 - var19);
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
