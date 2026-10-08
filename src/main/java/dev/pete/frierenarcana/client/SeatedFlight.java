package dev.pete.frierenarcana.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.pete.frierenarcana.ArcanaModes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent.Post;

@EventBusSubscriber(
    modid = "frieren_arcana",
    value = {Dist.CLIENT}
)
public final class SeatedFlight {
    private static final float DROP = 5.0F;

    private SeatedFlight() {
    }

    public static ItemStack staff(LivingEntity var0) {
        ItemStack var1 = var0.getMainHandItem();
        if (var1 != null && !var1.isEmpty() && ArcanaModes.isStaff(var1)) {
            return var1;
        } else {
            ItemStack var2 = var0.getOffhandItem();
            return var2 != null && !var2.isEmpty() && ArcanaModes.isStaff(var2) ? var2 : null;
        }
    }

    public static boolean isSeated(LivingEntity var0) {
        if (!(var0 instanceof Player) || var0.isPassenger() || var0.isSleeping() || var0.isFallFlying() || var0.isInWater()) {
            return false;
        } else {
            return !var0.onGround() && ArcanaClient.isFlying(var0.getUUID()) ? staff(var0) != null : false;
        }
    }

    public static void pose(HumanoidModel<?> var0, LivingEntity var1, float var2) {
        if (isSeated(var1)) {
            float var3 = Mth.sin(var2 * 0.09F) * 0.07F;
            var0.head.y += 5.0F;
            var0.body.y += 5.0F;
            var0.rightArm.y += 5.0F;
            var0.leftArm.y += 5.0F;
            var0.rightLeg.y += 5.0F;
            var0.leftLeg.y += 5.0F;
            var0.body.xRot = -0.06F;
            var0.rightLeg.xRot = -1.12F + var3;
            var0.leftLeg.xRot = -0.92F - var3;
            var0.rightLeg.yRot = 0.1F;
            var0.leftLeg.yRot = -0.06F;
            var0.rightLeg.zRot = 0.04F;
            var0.leftLeg.zRot = -0.02F;
            var0.rightArm.xRot = -1.0F;
            var0.rightArm.yRot = -0.12F;
            var0.rightArm.zRot = 0.08F;
            var0.leftArm.xRot = -0.62F;
            var0.leftArm.yRot = 0.22F;
            var0.leftArm.zRot = -0.05F;
        }
    }

    @SubscribeEvent
    public static void render(Post<?, ?> var0) {
        LivingEntity var1 = var0.getEntity();
        if (isSeated(var1)) {
            ItemStack var2 = staff(var1);
            PoseStack var3 = var0.getPoseStack();
            float var4 = Mth.rotLerp(var0.getPartialTick(), var1.yBodyRotO, var1.yBodyRot);
            var3.pushPose();
            var3.mulPose(Axis.YP.rotationDegrees(180.0F - var4));
            var3.translate(0.0, 0.34, 0.12);
            var3.mulPose(Axis.XP.rotationDegrees(90.0F));
            float var5 = 1.0F;
            var3.scale(var5, var5, var5);
            Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(var2, ItemDisplayContext.NONE, var0.getPackedLight(), 0, var3, var0.getMultiBufferSource(), Minecraft.getInstance().level, 0);
            var3.popPose();
        }
    }
}
