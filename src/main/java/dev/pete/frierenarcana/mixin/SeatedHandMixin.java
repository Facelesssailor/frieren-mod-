package dev.pete.frierenarcana.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.pete.frierenarcana.ArcanaModes;
import dev.pete.frierenarcana.client.SeatedFlight;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandLayer.class})
public abstract class SeatedHandMixin {
    @Inject(
        method = {"renderArmWithItem"},
        at = {@At("HEAD")},
        cancellable = true,
        require = 0
    )
    private void frierenArcana$hideHeldStaff(
        LivingEntity var1, ItemStack var2, ItemDisplayContext var3, HumanoidArm var4, PoseStack var5, MultiBufferSource var6, int var7, CallbackInfo var8
    ) {
        if (SeatedFlight.isSeated(var1) && ArcanaModes.isStaff(var2)) {
            var8.cancel();
        }
    }
}
