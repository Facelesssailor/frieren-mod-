package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class BarrierLook {
    private static final int SEG = 112;
    private static final int RINGS = 56;
    private static final double[] SX = new double[6441];
    private static final double[] SY = new double[SX.length];
    private static final double[] SZ = new double[SX.length];

    private BarrierLook() {
    }

    public static void sphere(VertexConsumer var0, Matrix4f var1, ArcanaClient.VisualField var2, double var3, double var5) {
        Vec3 var7 = ArcanaClient.camera();
        if (var7 != null) {
            float var8 = (float)Math.max(0.0, 1.0 - var5 / 1.5);
            if (!(var8 <= 0.001F)) {
                shell(var0, var1, var2.center(), (double)var2.radius(), var7, var8, -1.0F);
            }
        }
    }

    static void shell(VertexConsumer var0, Matrix4f var1, Vec3 var2, double var3, Vec3 var5, float var6, float var7) {
        for (int var8 = 0; var8 < 56; var8++) {
            if (!(SY[var8 * 113] < -0.55) || !(SY[(var8 + 1) * 113] < -0.55)) {
                for (int var9 = 0; var9 < 112; var9++) {
                    int var10 = var8 * 113 + var9;
                    int var11 = var10 + 1;
                    int var12 = (var8 + 1) * 113 + var9;
                    int var13 = var12 + 1;
                    vtx(var0, var1, var2, var3, var5, var10, var6, var7);
                    vtx(var0, var1, var2, var3, var5, var11, var6, var7);
                    vtx(var0, var1, var2, var3, var5, var13, var6, var7);
                    vtx(var0, var1, var2, var3, var5, var12, var6, var7);
                }
            }
        }
    }

    private static void vtx(VertexConsumer var0, Matrix4f var1, Vec3 var2, double var3, Vec3 var5, int var6, float var7, float var8) {
        double var9 = SX[var6];
        double var11 = SY[var6];
        double var13 = SZ[var6];
        double var15 = var2.x + var9 * var3;
        double var17 = var2.y + var11 * var3;
        double var19 = var2.z + var13 * var3;
        double var21 = var5.x - var15;
        double var23 = var5.y - var17;
        double var25 = var5.z - var19;
        double var27 = Math.sqrt(var21 * var21 + var23 * var23 + var25 * var25);
        double var29 = var27 < 1.0E-6 ? 1.0 : Math.abs(var9 * var21 + var11 * var23 + var13 * var25) / var27;
        double var31 = Math.pow(1.0 - var29, 2.2);
        float var33 = var8 >= 0.0F ? 0.6F + 0.04F * Math.min(1.0F, var8) : CinemaDirector.bandTag(var11);
        float var34 = (var33 - 0.6F) / 0.04F;
        float var35 = (float)((0.06 + 0.32 * var31) * (1.0 + 0.6 * (double)var34)) * var7;
        var0.addVertex(var1, (float)(var15 - var5.x), (float)(var17 - var5.y), (float)(var19 - var5.z))
            .setColor((float)var31, 0.9F, var33, Math.min(1.0F, var35));
    }

    static {
        for (int var0 = 0; var0 <= 56; var0++) {
            for (int var1 = 0; var1 <= 112; var1++) {
                double var2 = (double)var1 * Math.PI * 2.0 / 112.0;
                double var4 = (double)var0 * Math.PI / 56.0;
                int var6 = var0 * 113 + var1;
                SX[var6] = Math.sin(var4) * Math.cos(var2);
                SY[var6] = Math.cos(var4);
                SZ[var6] = Math.sin(var4) * Math.sin(var2);
            }
        }
    }
}
