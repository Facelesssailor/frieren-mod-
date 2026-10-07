package dev.pete.frierenarcana.mixin;

import dev.pete.frierenarcana.BarrierHooks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Entity.class})
public abstract class EntityBarrierMixin {
    @ModifyVariable(
        method = {"move"},
        at = @At("HEAD"),
        argsOnly = true
    )
    private Vec3 arcana$movement(Vec3 movement) {
        return BarrierHooks.movement((Entity)this, movement);
    }

    @Inject(
        method = {"setPosRaw"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void arcana$position(double x, double y, double z, CallbackInfo ci) {
        if (BarrierHooks.teleportBlocked((Entity)this, x, y, z)) {
            ci.cancel();
        }
    }

    @Inject(
        method = {"changeDimension"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void arcana$dimension(DimensionTransition transition, CallbackInfoReturnable<Entity> ci) {
        if (BarrierHooks.transitionBlocked((Entity)this, transition)) {
            ci.setReturnValue(null);
        }
    }
}
