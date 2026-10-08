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
                        var29.endBatch(ArcanaRenderTypes.MAGIC);
                    }
                }
            }
        }
    }

    private static final class Ctx {
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
