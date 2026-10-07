package dev.pete.frierenarcana.mixin;

import dev.pete.frierenarcana.BarrierHooks;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerGamePacketListenerImpl.class})
public abstract class ConnectionBarrierMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(
        method = {"teleport(DDDFFLjava/util/Set;)V"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void arcana$connectionTeleport(double x, double y, double z, float yaw, float pitch, Set<RelativeMovement> relative, CallbackInfo ci) {
        if (BarrierHooks.teleportBlocked(this.player, x, y, z)) {
            ci.cancel();
        }
    }
}
