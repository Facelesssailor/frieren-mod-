package dev.pete.frierenarcana.mixin;

import dev.pete.frierenarcana.client.SeatedFlight;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HumanoidModel.class})
public abstract class SeatedPoseMixin {
    @Inject(
        method = {"setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V"},
        at = {@At("TAIL")},
        require = 0
    )
    private void frierenArcana$seat(LivingEntity var1, float var2, float var3, float var4, float var5, float var6, CallbackInfo var7) {
        SeatedFlight.pose((HumanoidModel<?>)this, var1, var4);
    }
}
