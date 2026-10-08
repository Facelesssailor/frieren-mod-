package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class BreakerFx {
    public static final double LIFE = 13.5;
    public static final double BEAM_LIFE = 3.4;
    static final double CRACK_END = 3.0;
    static final double SHATTER = 3.1;

    private BreakerFx() {
    }

    static double hash(int var0, int var1) {
        double var2 = Math.sin((double)var0 * 12.9898 + (double)var1 * 78.233) * 43758.5453;
        return var2 - Math.floor(var2);
    }

    private static double sstep(double var0, double var2, double var4) {
        double var6 = Math.max(0.0, Math.min(1.0, (var4 - var0) / (var2 - var0)));
        return var6 * var6 * (3.0 - 2.0 * var6);
    }

    private static void v(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, double var3, double var5, double var7, float var9, float var10, float var11, float var12
    ) {
        var0.addVertex(var1, (float)(var3 - var2.x), (float)(var5 - var2.y), (float)(var7 - var2.z)).setColor(var9, var10, var11, var12);
    }

    static void ribbon(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, Vec3 var4, double var5, float var7, float var8, float var9, float var10) {
        if (!(var10 <= 0.003F)) {
            double var11 = var4.x - var3.x;
            double var13 = var4.y - var3.y;
            double var15 = var4.z - var3.z;
            double var17 = (var3.x + var4.x) * 0.5 - var2.x;
            double var19 = (var3.y + var4.y) * 0.5 - var2.y;
            double var21 = (var3.z + var4.z) * 0.5 - var2.z;
            double var23 = var13 * var21 - var15 * var19;
            double var25 = var15 * var17 - var11 * var21;
            double var27 = var11 * var19 - var13 * var17;
            double var29 = Math.sqrt(var23 * var23 + var25 * var25 + var27 * var27);
            if (!(var29 < 1.0E-9)) {
                double var31 = var5 * 0.5 / var29;
                var23 *= var31;
                var25 *= var31;
                var27 *= var31;
                v(var0, var1, var2, var3.x - var23, var3.y - var25, var3.z - var27, var7, var8, var9, var10);
                v(var0, var1, var2, var3.x + var23, var3.y + var25, var3.z + var27, var7, var8, var9, var10);
                v(var0, var1, var2, var4.x + var23, var4.y + var25, var4.z + var27, var7, var8, var9, var10);
                v(var0, var1, var2, var4.x - var23, var4.y - var25, var4.z - var27, var7, var8, var9, var10);
            }
        }
    }

    private static Vec3[] basis(Vec3 var0, Vec3 var1) {
        double var2 = var0.x - var1.x;
        double var4 = var0.y - var1.y;
        double var6 = var0.z - var1.z;
        double var8 = Math.sqrt(var2 * var2 + var4 * var4 + var6 * var6);
        if (var8 < 1.0E-6) {
            var2 = 0.0;
            var4 = 0.0;
            var6 = 1.0;
            var8 = 1.0;
        }

        var2 /= var8;
        var4 /= var8;
        var6 /= var8;
        double var10 = -var6;
        double var12 = var2;
        double var14 = Math.sqrt(var10 * var10 + var2 * var2);
        if (var14 < 1.0E-4) {
            var10 = 1.0;
            var12 = 0.0;
            var14 = 1.0;
        }

        var10 /= var14;
        var12 /= var14;
        Vec3 var16 = new Vec3(var10, 0.0, var12);
        Vec3 var17 = new Vec3(var4 * var12, var6 * var10 - var2 * var12, -var4 * var10);
        return new Vec3[]{var16, var17.normalize()};
    }

    static void disc(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, float var6, float var7, float var8, float var9) {
        if (!(var9 <= 0.003F) && !(var4 <= 0.0)) {
            PixelFx.sprite(var3, var4 * 2.3, 4, var6, var7, var8, Math.min(1.0F, var9 * 1.4F));
        }
    }

    static void sparkle(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, float var6, float var7, float var8, float var9) {
        if (!(var9 <= 0.003F) && !(var4 <= 0.0)) {
            int var10 = var4 < 0.25 ? 0 : (var4 < 0.6 ? 1 : (var4 < 1.2 ? 2 : 3));
            PixelFx.sprite(var3, var4 * 2.1, var10, var6, var7, var8, var9);
        }
    }

    public static void beam(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4) {
        Vec3 var6 = ArcanaClient.camera();
        if (var6 != null && !(var4 > 3.4)) {
            double var7 = sstep(0.0, 0.22, var4);
            float var9 = (float)(var4 < 2.4 ? 1.0 : Math.max(0.0, 1.0 - (var4 - 2.4) / 1.0));
            float var10 = (float)(0.88 + 0.12 * Math.sin(var4 * 37.0));
            Vec3 var11 = var2.lerp(var3, var7);
            ribbon(var0, var1, var6, var2, var11, 1.15, 0.3F, 0.9F, 0.55F, 0.07F * var9);
            ribbon(var0, var1, var6, var2, var11, 0.42, 0.45F, 1.0F, 0.65F, 0.26F * var9 * var10);
            ribbon(var0, var1, var6, var2, var11, 0.13, 0.8F, 1.0F, 0.88F, 0.75F * var9);
            ribbon(var0, var1, var6, var2, var11, 0.045, 1.0F, 1.0F, 1.0F, 0.98F * var9);
            Vec3 var12 = var3.subtract(var2);
            double var13 = var12.length();

            for (int var15 = 0; var15 < 26; var15++) {
                double var16 = (hash(var15, 1) + var4 * (0.9 + 0.6 * hash(var15, 2))) % 1.0;
                if (!(var16 > var7)) {
                    Vec3 var18 = var2.add(var12.scale(var16))
                        .add(
                            Math.sin((double)var15 * 2.3 + var4 * 6.0) * 0.18,
                            Math.cos((double)var15 * 1.7 + var4 * 5.0) * 0.18,
                            Math.sin((double)var15 * 3.1 - var4 * 4.0) * 0.18
                        );
                    disc(var0, var1, var6, var18, 0.06 + 0.05 * hash(var15, 3), 0.7F, 1.0F, 0.82F, 0.7F * var9);
                }
            }

            float var19 = (float)(0.85 + 0.15 * Math.sin(var4 * 22.0));
            sparkle(var0, var1, var6, var2, 1.4 * (double)var19, 0.9F, 1.0F, 0.94F, 0.9F * var9);
            disc(var0, var1, var6, var2, 0.7, 0.4F, 1.0F, 0.6F, 0.35F * var9);
            if (var7 >= 0.999 && var13 > 0.5) {
                double var20 = var4 - 0.22;
                sparkle(var0, var1, var6, var3, (2.2 + 0.6 * Math.sin(var4 * 18.0)) * (double)var9, 1.0F, 1.0F, 1.0F, 0.95F * var9);
                disc(var0, var1, var6, var3, 1.6 + var20 * 1.5, 0.45F, 1.0F, 0.65F, 0.45F * var9);
            }
        }
    }

    public static void fracture(VertexConsumer var0, Matrix4f var1, ArcanaClient.Fracture var2, double var3, double var5) {
        Vec3 var7 = ArcanaClient.camera();
        if (var7 != null) {
            Vec3 var8 = var2.field().center();
            double var9 = (double)var2.field().radius();
            Vec3 var11 = var2.impact();
            if (var5 < 3.4) {
                float var12 = (float)(1.0 + 1.3 * sstep(1.8, 3.1, var5));
                float var13 = (float)(1.0 - sstep(3.1, 3.4, var5));
                BarrierLook.shell(var0, var1, var8, var9, var7, var12 * var13, 1.0F);
                cracks(var0, var1, var7, var8, var9, var11, var5, var13);
            }

            double var22 = var5 - 3.0;
            if (var22 > 0.0 && var22 < 0.8) {
                float var14 = (float)(Math.sin(Math.min(1.0, var22 / 0.8) * Math.PI) * 0.9);
                disc(var0, var1, var7, var11, var9 * 0.35 + var22 * var9 * 0.6, 0.85F, 1.0F, 0.92F, var14 * 0.5F);
                sparkle(var0, var1, var7, var11, var9 * 0.25, 1.0F, 1.0F, 1.0F, var14);
            }

            BreakerFx.Shard var23 = new BreakerFx.Shard();
            int var15 = shardCount(var9);

            for (int var16 = 0; var16 < var15; var16++) {
                if (shard(var16, var15, var8, var9, var11, var5, var23)) {
                    double var17 = hash(var16, 9) * 6.2831 + var5 * 0.5;
                    float var19 = (float)(0.55 + 0.45 * Math.cos(var17));
                    float var20 = (float)(0.55 + 0.45 * Math.cos(var17 - 2.1));
                    float var21 = (float)(0.55 + 0.45 * Math.cos(var17 + 2.1));
                    quad(var0, var1, var7, var23, var19, var20, var21, var23.alpha * 0.16F);
                    if (var16 % 9 == 0 && var23.facet > 0.85) {
                        sparkle(var0, var1, var7, var23.p0, var23.size * 0.8, 1.0F, 1.0F, 1.0F, var23.alpha * (float)((var23.facet - 0.85) / 0.15));
                    }
                }
            }
        }
    }

    private static void cracks(VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, Vec3 var6, double var7, float var9) {
        Vec3 var10 = var6.subtract(var3).normalize();
        Vec3 var11 = var10.cross(Math.abs(var10.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0)).normalize();
        Vec3 var12 = var10.cross(var11);
        double var13 = Math.PI * Math.pow(Math.min(1.0, var7 / 3.0), 0.8);
        double var15 = var4 + 0.06;
        double var17 = 0.05 + var4 * 0.0025;
        float var19 = (float)(0.75 + 0.25 * Math.sin(var7 * 25.0)) * var9;
        byte var20 = 22;

        for (int var21 = 0; var21 < var20; var21++) {
            double var22 = (double)var21 * Math.PI * 2.0 / (double)var20 + hash(var21, 1) * 0.2;
            Vec3 var24 = var6;
            byte var25 = 26;

            for (int var26 = 1; var26 <= var25; var26++) {
                double var27 = Math.PI * (double)var26 / (double)var25;
                if (var27 > var13) {
                    break;
                }

                double var29 = (hash(var21 * 31 + var26, 2) - 0.5) * 0.35;
                Vec3 var31 = var11.scale(Math.cos(var22 + var29)).add(var12.scale(Math.sin(var22 + var29)));
                Vec3 var32 = var3.add(var10.scale(Math.cos(var27) * var15)).add(var31.scale(Math.sin(var27) * var15));
                ribbon(var0, var1, var2, var24, var32, var17 * 4.0, 0.45F, 1.0F, 0.65F, 0.18F * var19);
                ribbon(var0, var1, var2, var24, var32, var17, 0.92F, 1.0F, 0.95F, 0.95F * var19);
                if (var26 % 5 == 2) {
                    double var33 = var22 + var29 + (hash(var21, var26) > 0.5 ? 0.35 : -0.35);
                    Vec3 var35 = var11.scale(Math.cos(var33)).add(var12.scale(Math.sin(var33)));
                    double var36 = Math.min(var13, var27 + Math.PI / (double)var25 * 2.5);
                    Vec3 var38 = var3.add(var10.scale(Math.cos(var36) * var15)).add(var35.scale(Math.sin(var36) * var15));
                    ribbon(var0, var1, var2, var32, var38, var17 * 0.6, 0.78F, 0.62F, 1.0F, 0.7F * var19);
                }

                var24 = var32;
            }
        }

        if (var13 < Math.PI) {
            Vec3 var39 = null;

            for (int var40 = 0; var40 <= 72; var40++) {
                double var23 = (Math.PI * 2) * (double)var40 / 72.0;
                Vec3 var41 = var11.scale(Math.cos(var23)).add(var12.scale(Math.sin(var23)));
                Vec3 var42 = var3.add(var10.scale(Math.cos(var13) * var15)).add(var41.scale(Math.sin(var13) * var15));
                if (var39 != null) {
                    ribbon(var0, var1, var2, var39, var42, var17 * 2.5, 0.7F, 1.0F, 0.8F, 0.5F * var9);
                }

                var39 = var42;
            }
        }
    }

    public static void prisms(VertexConsumer var0, Matrix4f var1, ArcanaClient.Fracture var2, double var3) {
        Vec3 var5 = ArcanaClient.camera();
        if (var5 != null) {
            Vec3 var6 = var2.field().center();
            double var7 = (double)var2.field().radius();
            BreakerFx.Shard var9 = new BreakerFx.Shard();
            int var10 = shardCount(var7);

            for (int var11 = 0; var11 < var10; var11++) {
                if (shard(var11, var10, var6, var7, var2.impact(), var3, var9)) {
                    quad(var0, var1, var5, var9, (float)hash(var11, 5), (float)hash(var11, 6), (float)var9.facet, var9.alpha * 0.62F);
                }
            }
        }
    }

    static int shardCount(double var0) {
        return (int)Math.max(260.0, Math.min(1100.0, var0 * var0 * 0.7));
    }

    static boolean shard(int var0, int var1, Vec3 var2, double var3, Vec3 var5, double var6, BreakerFx.Shard var8) {
        double var9 = 1.0 - 1.15 * ((double)var0 + 0.5) / (double)var1;
        if (var9 < -0.15) {
            return false;
        } else {
            double var11 = Math.sqrt(Math.max(0.0, 1.0 - var9 * var9));
            double var13 = (double)var0 * 2.399963229728653;
            double var15 = Math.cos(var13) * var11;
            double var19 = Math.sin(var13) * var11;
            Vec3 var21 = new Vec3(var15, var9, var19);
            Vec3 var22 = var5.subtract(var2).normalize();
            double var23 = Math.acos(Math.max(-1.0, Math.min(1.0, var21.dot(var22))));
            double var25 = 3.0 + 0.35 * var23 / Math.PI;
            double var27 = var6 - var25;
            if (var27 < 0.0) {
                return false;
            } else {
                double var29 = 7.699999999999999 + 3.2 * hash(var0, 4);
                float var31 = (float)(Math.min(1.0, var27 / 0.15) * Math.max(0.0, Math.min(1.0, (var29 - var6) / 1.4)));
                if (var31 <= 0.004F) {
                    return false;
                } else {
                    double var32 = 0.25 + 0.9 * hash(var0, 1);
                    double var34 = 0.8 + 0.9 * hash(var0, 2);
                    double var36 = Math.sin(var27 * (0.8 + hash(var0, 7)) + (double)var0) * 0.35 * var27 / (1.0 + var27);
                    Vec3 var38 = var21.cross(Math.abs(var9) < 0.95 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0)).normalize();
                    Vec3 var39 = var21.cross(var38);
                    Vec3 var40 = var2.add(var21.scale(var3 + var32 * var27)).add(var38.scale(var36)).add(0.0, -0.5 * var34 * var27 * var27 * 0.6, 0.0);
                    double var41 = (hash(var0, 3) - 0.5) * 3.0 * var27;
                    double var43 = (hash(var0, 8) - 0.5) * 2.2 * var27;
                    Vec3 var45 = var38.scale(Math.cos(var41)).add(var39.scale(Math.sin(var41)));
                    Vec3 var46 = var39.scale(Math.cos(var41)).subtract(var38.scale(Math.sin(var41)));
                    var46 = var46.scale(Math.cos(var43)).add(var21.scale(Math.sin(var43)));
                    double var47 = Math.max(0.45, Math.min(3.6, var3 * 0.075)) * (0.45 + 0.9 * hash(var0, 10));
                    boolean var49 = hash(var0, 11) > 0.45;
                    double var50 = hash(var0, 12);
                    double var52 = hash(var0, 13);
                    double var54 = hash(var0, 14);
                    var8.p0 = var40.add(var45.scale(var47 * (0.7 + 0.5 * var50)));
                    var8.p1 = var40.add(var45.scale(-var47 * 0.5 * (0.6 + var52))).add(var46.scale(var47 * (0.6 + 0.4 * var54)));
                    var8.p2 = var40.add(var45.scale(-var47 * 0.4 * (0.6 + var54))).add(var46.scale(-var47 * (0.5 + 0.5 * var50)));
                    var8.p3 = var49 ? var8.p2 : var40.add(var45.scale(var47 * 0.25)).add(var46.scale(-var47 * (0.8 + 0.3 * var52)));
                    Vec3 var56 = ArcanaClient.camera();
                    Vec3 var57 = var45.cross(var46).normalize();
                    double var58 = var56 == null ? 0.5 : Math.abs(var57.dot(var56.subtract(var40).normalize()));
                    var8.facet = Math.pow(var58, 3.0);
                    var8.alpha = var31;
                    var8.size = var47;
                    return true;
                }
            }
        }
    }

    private static void quad(VertexConsumer var0, Matrix4f var1, Vec3 var2, BreakerFx.Shard var3, float var4, float var5, float var6, float var7) {
        if (!(var7 <= 0.003F)) {
            v(var0, var1, var2, var3.p0.x, var3.p0.y, var3.p0.z, var4, var5, var6, var7);
            v(var0, var1, var2, var3.p1.x, var3.p1.y, var3.p1.z, var4, var5, var6, var7);
            v(var0, var1, var2, var3.p2.x, var3.p2.y, var3.p2.z, var4, var5, var6, var7);
            v(var0, var1, var2, var3.p3.x, var3.p3.y, var3.p3.z, var4, var5, var6, var7);
        }
    }

    static final class Shard {
        Vec3 p0;
        Vec3 p1;
        Vec3 p2;
        Vec3 p3;
        float alpha;
        double facet;
        double size;
    }
}
