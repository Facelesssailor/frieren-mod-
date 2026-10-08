package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class ChargeFx {
    private ChargeFx() {
    }

    private static boolean local(Vec3 var0) {
        LocalPlayer var1 = Minecraft.getInstance().player;
        return var1 != null && ArcanaCinematic.breakerActive() && var0.distanceToSqr(var1.position().add(0.0, 1.0, 0.0)) < 9.0;
    }

    public static void sigil(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, float var8, float var9, float var10, float var11
    ) {
        if (!local(var2)) {
            Vec3 var12 = ArcanaClient.camera();
            Minecraft var13 = Minecraft.getInstance();
            if (var12 != null && var13.level != null) {
                double var14 = Math.max(0.0, Math.min(1.0, (var4 - 0.7) / 0.6));
                Vec3 var16 = var2.subtract(var3.scale(0.8));
                Vec3 var17 = new Vec3(var16.x, SpellCircleFx.ground(var13.level, var16.add(0.0, -1.0, 0.0), var13.player), var16.z);
                Vec3 var18 = var16.add(0.0, 0.05, 0.0).add(new Vec3(var3.x, 0.0, var3.z).scale(0.35));
                SpellCircleFx.remote(var0, var1, var12, var17, var18, var6, var14);
            }
        }
    }

    public static void crystal(
        VertexConsumer var0, Matrix4f var1, Vec3 var2, Vec3 var3, double var4, double var6, float var8, float var9, float var10, float var11
    ) {
        Vec3 var12 = ArcanaClient.camera();
        if (var12 != null && !local(var2)) {
            double var13 = 0.5 + 0.5 * Math.sin((double)System.nanoTime() / 1.0E9 * 5.0 + var2.x * 3.1 + var2.z * 2.3);
            BreakerFx.sparkle(var0, var1, var12, var2, 0.09 + 0.07 * var13, 0.85F, 1.0F, 0.9F, (float)(0.4 + 0.5 * var13) * var11);
        }
    }
}
