package dev.pete.frierenarcana.mixin;

import dev.pete.frierenarcana.BarrierData;
import dev.pete.frierenarcana.BarrierHooks;
import dev.pete.frierenarcana.ShipSpace;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({ServerPlayer.class})
public abstract class ServerPlayerBarrierMixin {
    @Inject(
        method = {"changeDimension"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void arcana$dimension(DimensionTransition transition, CallbackInfoReturnable<Entity> ci) {
        if (BarrierHooks.transitionBlocked((ServerPlayer)this, transition)) {
            ci.setReturnValue(null);
        }
    }

    @Inject(
        method = {"teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void arcana$teleport(
        ServerLevel destination, double x, double y, double z, Set<RelativeMovement> relative, float yaw, float pitch, CallbackInfoReturnable<Boolean> ci
    ) {
        ServerPlayer p = (ServerPlayer)this;
        Vec3 target = ShipSpace.world(destination, new Vec3(x, y, z));
        if (destination == p.serverLevel()) {
            if (BarrierData.get(destination).crosses(ShipSpace.world(p), target, false)) {
                ci.setReturnValue(false);
            }
        } else if (BarrierData.get(p.serverLevel()).containsConfining(ShipSpace.world(p)) || BarrierData.get(destination).containsConfining(target)) {
            ci.setReturnValue(false);
        }
    }
}
